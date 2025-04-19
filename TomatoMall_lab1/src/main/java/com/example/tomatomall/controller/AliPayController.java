package com.example.tomatomall.controller;

import lombok.extern.slf4j.Slf4j;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.service.OrderService;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.tomatomall.po.AliPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
public class AliPayController {

    @Autowired
    private OrderService orderService;

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
    private static final String FORMAT = "JSON";
    //vvvyyv9548@sandbox.com    支付邮箱
    @GetMapping("/pay") //subject=xxx&traceNo=xxx&totalAmount=xxx
    public void pay(AliPay aliPay, HttpServletResponse httpResponse) throws Exception {
        // 1. 创建Client，通用SDK提供的Client，负责调用支付宝的API
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId,
                privateKey, FORMAT, charset, alipayPublicKey, signType);
        // 2. 创建 Request并设置Request参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 发送请求的 Request类
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", aliPay.getTraceNo());  // 我们自己生成的订单编号
        bizContent.put("total_amount", aliPay.getTotalAmount()); // 订单的总金额
        bizContent.put("subject", aliPay.getSubject());   // 支付的名称
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");  // 固定配置
        request.setBizContent(bizContent.toString());
        // 执行请求，拿到响应的结果，返回给浏览器
        String form = "";
        try {
            form = alipayClient.pageExecute(request).getBody(); // 调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + charset);
        httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }
    @PostMapping("/notify")
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        try {
            // 1. 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                params, alipayPublicKey, charset, signType
            );
            if (!signVerified) {
                log.error("[支付宝回调] 签名验证失败，参数: {}", params);
                response.getWriter().print("fail");
                return;
            }
            log.info("[支付宝回调] 签名验证成功，交易状态: {}", params.get("trade_status"));

            // 2. 处理支付成功回调
            if ("TRADE_SUCCESS".equals(params.get("trade_status"))) {
                String orderIdStr = params.get("out_trade_no");
                String alipayTradeNo = params.get("trade_no");
                String amount = params.get("total_amount");

                // 3. 校验订单ID格式
                if (orderIdStr == null || orderIdStr.isEmpty()) {
                    log.error("[支付宝回调] 订单ID为空，参数: {}", params);
                    response.getWriter().print("fail");
                    return;
                }

                long orderId;
                try {
                    orderId = Long.parseLong(orderIdStr);
                } catch (NumberFormatException e) {
                    log.error("[支付宝回调] 订单ID格式错误: {}", orderIdStr, e);
                    response.getWriter().print("fail");
                    return;
                }

                // 4. 查询订单
                Order order = orderService.getOrderById(orderId);
                if (order == null) {
                    log.error("[支付宝回调] 订单不存在，订单ID: {}", orderId);
                    response.getWriter().print("fail");
                    return;
                }

                // 5. 幂等性检查
                if ("PAID".equals(order.getStatus())) {
                    log.info("[支付宝回调] 订单已支付，跳过处理，订单ID: {}", orderId);
                    response.getWriter().print("success");
                    return;
                }

                // 6. 处理订单
                try {
                    orderService.handlePaymentSuccess(orderId, alipayTradeNo, amount);
                    log.info("[支付宝回调] 订单处理成功，订单ID: {}", orderId);
                } catch (Exception e) {
                    log.error("[支付宝回调] 订单处理失败，订单ID: {}", orderId, e);
                    response.getWriter().print("fail");
                    return;
                }
            }
            response.getWriter().print("success");
        } catch (Exception e) {
            log.error("[支付宝回调] 服务器内部错误", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/returnUrl")
    public String returnUrl() {
        return "支付成功了";
    }
}