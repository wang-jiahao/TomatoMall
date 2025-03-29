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
}