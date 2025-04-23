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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

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
    public void handleAlipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // 明确设置响应类型为支付宝要求的纯文本格式
        response.setContentType("text/plain;charset=" + charset);

        // 1. 转换请求参数（兼容支付宝的application/x-www-form-urlencoded格式）
        Map<String, String> params = convertRequestParams(request);

        log.info("[支付宝回调] 收到通知，参数: {}", params);
//        try {
//            // 2. 签名验证（必须最先执行）
//            if (!verifyAlipaySignature(params)) {
//                log.warn("[支付宝回调] 签名验证失败，疑似非法请求，参数: {}", params);
//                response.getWriter().print("fail");
//                return;
//            }
//
//            // 3. 仅处理交易成功的通知
//            String tradeStatus = params.get("trade_status");
//            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
        processPaymentSuccess(params, response);
//            }else {
//                log.info("[支付宝回调] 忽略非成功交易状态: {}", params.get("trade_status"));
//                response.getWriter().print("success");
//            }
//        } catch (AlipayApiException e) {
//            log.error("[支付宝回调] 签名验证异常", e);
//            response.getWriter().print("fail");
//        } catch (Exception e) {
//            log.error("[支付宝回调] 处理过程中发生未预期异常", e);
//            response.getWriter().print("fail");
//        }
    }

    private Map<String, String> convertRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                try {
                    // 关键：对参数值进行 URL 解码
                    String encodedValue = values[0].replace(" ", "+");
                    String decodedValue = URLDecoder.decode(encodedValue, charset);
                    params.put(key, decodedValue);
                } catch (UnsupportedEncodingException e) {
                    log.error("参数解码失败: {}", key, e);
                    params.put(key, values[0]); // 保留原始值
                }
            }
        });
        return params;
    }

    private boolean verifyAlipaySignature(Map<String, String> params) throws AlipayApiException {
        return AlipaySignature.rsaCheckV1(
                params,
                alipayPublicKey,
                charset,
                signType
        );
    }

    private void processPaymentSuccess(Map<String, String> params, HttpServletResponse response) throws IOException {

//        String orderIdStr = params.get("out_trade_no");
//        String alipayTradeNo = params.get("trade_no");
//        String amount = params.get("total_amount");
//        String receivedAppId = params.get("app_id");
//        String receivedSellerId = params.get("seller_id");
//
//        // 1. 基础参数校验
//        if (orderIdStr == null || orderIdStr.isEmpty()) {
//            log.error("[支付宝回调] 订单号参数缺失");
//            response.getWriter().print("fail");
//            return;
//        }
//
//        // 2. 校验 app_id
//        if (!appId.equals(receivedAppId)) {
//            log.error("[支付宝回调] app_id 不匹配，预期: {}，实际: {}", appId, receivedAppId);
//            response.getWriter().print("fail");
//            return;
//        }
//
//        // 3. 校验 seller_id（替换为你的 seller_id）
//        String expectedSellerId = "2088721065321335";
//        if (!expectedSellerId.equals(receivedSellerId)) {
//            log.error("[支付宝回调] seller_id 不匹配，预期: {}，实际: {}", expectedSellerId, receivedSellerId);
//            response.getWriter().print("fail");
//            return;
//        }

//        try {
//            Long orderId = Long.parseLong(orderIdStr);
//            Order order = orderService.getOrderById(orderId);
//
//            // 4. 订单存在性校验
//            if (order == null) {
//                log.error("[支付宝回调] 订单不存在，订单ID: {}", orderId);
//                response.getWriter().print("fail");
//                return;
//            }
//
//            // 5. 幂等性检查（必须放在事务外）
//            if ("PAID".equals(order.getStatus())) {
//                log.info("[支付宝回调] 订单已支付，直接返回成功，订单ID: {}", orderId);
//                response.getWriter().print("success");
//                return;
//            }
//
//            // 6. 金额校验（防止金额篡改）
//            if (new BigDecimal(amount).compareTo(order.getTotalAmount()) != 0) {
//                log.error("[支付宝回调] 金额不一致，订单金额: {}，回调金额: {}", order.getTotalAmount(), amount);
//                response.getWriter().print("fail");
//                return;
//            }
//
//            // 7. 处理核心业务逻辑（包含库存扣减）
//            orderService.handlePaymentSuccess(orderId, alipayTradeNo, amount);
//            log.info("[支付宝回调] 订单处理完成，订单ID: {}", orderId);
        response.getWriter().print("success");
//        } catch (NumberFormatException e) {
//            log.error("[支付宝回调] 订单号格式错误: {}", orderIdStr, e);
//            response.getWriter().print("fail");
//        } catch (Exception e) {
//            log.error("[支付宝回调] 订单处理异常，订单ID: {}", orderIdStr, e);
//            response.getWriter().print("fail");
//        }
    }
    @GetMapping("/returnUrl")
    public String returnUrl() {
        return "支付成功了";
    }
}