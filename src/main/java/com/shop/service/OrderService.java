package com.shop.service;

import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.util.Either;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 根据用户ID查询订单列表
     */
    List<Order> findByUserId(int userId);

    /**
     * 根据用户ID和订单状态查询订单列表
     */
    List<Order> findByUserIdAndStatus(int userId, String status);

    /**
     * 根据ID查询订单详情
     */
    Order findById(int id);
    
    /**
     * 创建订单（从购物车结算）
     */
    Either<Order> createOrderFromCart(User user, List<Integer> productIds);

    /**
     * 创建订单（直接结算）
     */
    Either<Order> createOrderFromProduct(User user, int productId);

    /**
     * 更新订单状态
     */
    boolean updateStatus(int orderId, String status);

}