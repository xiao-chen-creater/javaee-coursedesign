package com.shop.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体类
 */
@Data
public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private String productName;  // 添加商品名称字段
    // 注释掉数据库中不存在的price字段
    // private BigDecimal price;

    private Product product;

    public static OrderItem forCreate(int orderId, int productId, int quantity, BigDecimal price, Product product) {
        OrderItem orderItem = new OrderItem();
        orderItem.orderId = orderId;
        orderItem.productId = productId;
        orderItem.quantity = quantity;
        orderItem.productName = product != null ? product.getName() : ""; // 设置商品名称
        // orderItem.price = price; // 数据库中暂时没有price字段
        orderItem.product = product;
        return orderItem;
    }
    
    // 添加一个方法来获取商品价格（从关联的Product对象中获取）
    public BigDecimal getPrice() {
        return product != null ? product.getPrice() : BigDecimal.ZERO;
    }
    
    // 添加一个方法来计算总价
    public BigDecimal getTotalPrice() {
        return getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}