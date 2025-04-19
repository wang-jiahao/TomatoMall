package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.po.AliPay;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.util.JwtUtils;
import com.example.tomatomall.vo.CheckoutRequest;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.private-key}")
    private String privateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.server-url}")
    private String serverUrl;

    @Value("${alipay.charset}")
    private String charset;

    @Value("${alipay.sign-type}")
    private String signType;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    private static final String FORMAT = "JSON"; // 固定值

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockpileRepository stockpileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    // === 提交订单（库存冻结） ===
    @Override
    @Transactional
    public Order createOrder(String username, List<Long> cartItemIds, String shippingAddress, String paymentMethod) {
        try{
            // 1. 获取用户信息
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            log.info("用户查询成功: {}", user.getUsername());

            // 2. 获取购物车项并校验
            List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
            if (cartItems.isEmpty()) {
                throw new RuntimeException("购物车项不存在");
            }
            log.info("购物车项查询成功，数量: {}", cartItems.size());

            // 3. 校验库存并冻结
            cartItems.forEach(item -> {
                Stockpile stock = productService.getStockpile(item.getProduct().getId());
                int available = stock.getAmount() - stock.getFrozen();
                if (available < item.getQuantity()) {
                    throw new RuntimeException("商品 [" + item.getProduct().getTitle() + "] 库存不足");
                }
                stock.setFrozen(stock.getFrozen() + item.getQuantity()); // 冻结库存
                stockpileRepository.save(stock);
            });

            // 4. 创建订单对象
            Order order = new Order();
            order.setUser(user);
            order.setShippingAddress(shippingAddress);
            order.setPaymentMethod(paymentMethod);

            // 5. 计算总金额并设置订单项
            BigDecimal totalAmount = calculateTotalAmount(cartItems);
            order.setTotalAmount(totalAmount);
            order.setItems(createOrderItems(cartItems, order)); // 关联订单项

            // 6. 保存订单
            Order savedOrder = orderRepository.save(order);

            // 7. 清空已结算的购物车项（可选）
            cartItemRepository.deleteAll(cartItems);

            return savedOrder;
        }catch (Exception e) {
            log.error("创建订单失败: ", e); // 关键：记录异常
            throw e;
        }

    }

    // === 生成支付宝支付表单 ===
    @Override
    public String generatePaymentForm(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        try {
            AlipayClient alipayClient = new DefaultAlipayClient(
                serverUrl, appId, privateKey, FORMAT, charset, alipayPublicKey, signType
            );
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);

            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", order.getOrderId());
            bizContent.put("total_amount", order.getTotalAmount());
            bizContent.put("subject", "TomatoMall订单支付");
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            request.setBizContent(bizContent.toJSONString());

            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付表单生成失败", e);
        }
    }

    // === 处理支付成功回调 ===
    @Override
    @Transactional
    public void handlePaymentSuccess(Long orderId, String alipayTradeNo, String amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 幂等性检查
        if ("PAID".equals(order.getStatus())) {
            return;
        }

        // 扣减库存并更新订单
        order.getItems().forEach(item -> {
            Stockpile stock = stockpileRepository.findByProduct_Id(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("库存记录不存在"));
            stock.setFrozen(stock.getFrozen() - item.getQuantity());
            stock.setAmount(stock.getAmount() - item.getQuantity());
            stockpileRepository.save(stock);
        });

        order.setStatus("PAID");
        order.setAlipayTradeNo(alipayTradeNo);
        orderRepository.save(order);
    }

    // === 定时任务：取消超时未支付订单 ===
    @Override
    @Transactional
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void cancelExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findByStatusAndExpireTimeBefore("PENDING", new Date());
        expiredOrders.forEach(order -> {
            // 1. 释放冻结库存
            order.getItems().forEach(item -> {
                Stockpile stock = stockpileRepository.findByProduct_Id(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("库存记录不存在"));
                stock.setFrozen(stock.getFrozen() - item.getQuantity());
                stockpileRepository.save(stock);
            });

            // 2. 更新订单状态为过期
            order.setStatus("EXPIRED");
            orderRepository.save(order);
            log.info("订单 {} 因超时未支付已取消", order.getOrderId());
        });
    }

    // === 私有工具方法 ===

    // 计算订单总金额
    private BigDecimal calculateTotalAmount(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> BigDecimal.valueOf(item.getProduct().getPrice() * item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 创建订单项（快照商品信息）
    private List<OrderItem> createOrderItems(List<CartItem> cartItems, Order order) {
        return cartItems.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductTitle(cartItem.getProduct().getTitle());
            orderItem.setProductPrice(BigDecimal.valueOf(cartItem.getProduct().getPrice()));
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProductId(cartItem.getProduct().getId()); // 设置productId
            return orderItem;
        }).collect(Collectors.toList());
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }
}