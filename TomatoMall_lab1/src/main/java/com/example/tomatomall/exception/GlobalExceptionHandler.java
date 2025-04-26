package com.example.tomatomall.exception;

import com.example.tomatomall.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
//    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 捕获请求体缺失或格式错误
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
//        logger.error("请求体解析失败: ", e);
        return ResponseEntity.badRequest()
                .body(Response.buildFailure("请求体格式错误或缺失", "400"));
    }

    // 捕获其他异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleException(Exception e) {
//        logger.error("服务器内部错误: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.buildFailure("服务器内部错误", "500"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<?>> handleBusinessException(RuntimeException e) {
        // 根据异常消息返回不同状态码（如商品不存在返回400）
        if (e.getMessage().contains("商品不存在")) {
            return ResponseEntity.badRequest()
                    .body(Response.buildFailure(e.getMessage(), "400"));
        }
        // 库存记录不存在 → 400
        else if (e.getMessage().contains("库存记录不存在")) {
            return ResponseEntity.badRequest()
                    .body(Response.buildFailure(e.getMessage(), "400"));
        }
        else if (e.getMessage().startsWith("商品库存不足")) {
            return ResponseEntity.badRequest()
                    .body(Response.buildFailure(e.getMessage(), "400"));
        }
        else if (e.getMessage().startsWith("购物车商品不存在")) {
            return ResponseEntity.badRequest()
                    .body(Response.buildFailure(e.getMessage(), "400"));
        }
        else if (e.getMessage().startsWith("广告不存在")) {
            return ResponseEntity.badRequest()
                    .body(Response.buildFailure(e.getMessage(), "400"));
        }
        // 其他未处理的异常返回500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.buildFailure("服务器内部错误", "500"));
    }
}