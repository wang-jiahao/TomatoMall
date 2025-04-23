package com.example.tomatomall.po;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 订单总金额（精确到小数点后两位）
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    // 支付方式（如ALIPAY/WECHAT_PAY）
    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    // 订单状态（PENDING/PAID/CANCELLED/EXPIRED）
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    // 订单创建时间（自动生成）
    @Column(name = "create_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime = new Date();

    // 订单过期时间（创建时间+30分钟）
    @Column(name = "expire_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireTime;

    // 支付宝交易号（支付成功后更新）
    @Column(name = "alipay_trade_no", length = 64)
    private String alipayTradeNo;

    // 收货地址（JSON格式存储，包含姓名、电话、地址等）
    @Column(name = "shipping_address", columnDefinition = "TEXT", nullable = false)
    private String shippingAddress;

    // 订单项（一对多关联）
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    // === 新增构造方法：自动设置过期时间 ===
    @PrePersist
    public void setExpireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createTime);
        calendar.add(Calendar.MINUTE, 30); // 30分钟后过期
        expireTime = calendar.getTime();
    }

    // === 辅助方法：判断订单是否已过期 ===
    @Transient
    public boolean isExpired() {
        return new Date().after(expireTime);
    }


}