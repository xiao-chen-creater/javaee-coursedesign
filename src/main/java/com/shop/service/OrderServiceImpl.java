package com.shop.service;

import com.shop.entity.*;
import com.shop.mapper.OrderItemMapper;
import com.shop.mapper.OrderMapper;
import com.shop.util.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Override
    public Order findById(int id) {
        return orderMapper.findById(id);
    }

    @Override
    public List<Order> findByUserId(int userId) {
        return orderMapper.findByUserId(userId);
    }

    @Override
    public List<Order> findByUserIdAndStatus(int userId, String status) {
        return orderMapper.findByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional
    public Either<Order> createOrderFromCart(User user, List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Either.error("空订单");
        }

        // 获取购物车中选中的商品
        List<Cart> cartItems = cartService.findByUserId(user.getId());
        List<Cart> selectedItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartItems) {
            if (productIds.contains(cart.getProductId())) {
                selectedItems.add(cart);
                BigDecimal itemTotal = cart.getProduct().getPrice().multiply(new BigDecimal(cart.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }
        if (selectedItems.isEmpty()) {
            return Either.error("空订单");
        }

        // 创建订单
        Order order = Order.forCreate(user, totalAmount);
        if (orderMapper.insert(order) <= 0) {
            return Either.error("订单创建失败");
        }

        // 删除库存 建立订单明细
        List<OrderItem> orderItems = new ArrayList<>();
        for (Cart item : selectedItems) {
            Product product = productService.findById(item.getProductId());
            if (product == null) {
                return Either.error("商品 %s 不存在", item.getProduct().getName());
            }
            if (product.getStock() >= item.getQuantity()) {
                product.setStock(product.getStock() - item.getQuantity());
                product.setSales(product.getSales() + item.getQuantity());
                if (!productService.updateProduct(product)) {
                    // 更新失败
                    return Either.error("商品 %s 更新失败", product.getName());
                }
            } else {
                // 库存不足
                return Either.error("商品 %s 库存不足", product.getName());
            }

            // 订单明细
            orderItems.add(OrderItem.forCreate(order, product, item.getQuantity(), item.getProduct().getPrice()));
        }

        if (orderItemMapper.batchInsert(orderItems) < orderItems.size()) {
            // 未全部插入
            return Either.error("订单明细更新失败");
        }

        // 从购物车中删除已结算的商品
        for (Cart item : selectedItems) {
            cartService.removeFromCart(user.getId(), item.getProductId());
        }

        return Either.of(order);
    }

    @Override
    public Either<Order> createOrderFromProduct(User user, int productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            return Either.error("商品 %d 不存在", productId);
        }

        if (product.getStock() < 1) {
            return Either.error("商品 %s 已售罄", product.getName());
        }

        // 创建订单
        Order order = Order.forCreate(user, product.getPrice());
        if (orderMapper.insert(order) <= 0) {
            return Either.error("订单创建失败");
        }

        // 删除库存 建立订单明细
        product.setStock(product.getStock() - 1);
        product.setSales(product.getSales() + 1);
        if (!productService.updateProduct(product)) {
            // 删除失败
            return Either.error("商品 %s 更新失败", product.getName());
        }
        OrderItem item = OrderItem.forCreate(order, product, 1, product.getPrice());
        if (orderItemMapper.batchInsert(List.of(item)) <= 0) {
            return Either.error("订单明细更新失败");
        }
        return Either.of(order);
    }

    @Override
    public boolean updateStatus(int orderId, String status) {
        return orderMapper.updateStatus(orderId, status) > 0;
    }
}