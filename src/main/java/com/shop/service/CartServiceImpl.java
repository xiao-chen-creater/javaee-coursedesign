package com.shop.service;

import com.shop.entity.Cart;
import com.shop.entity.Product;
import com.shop.mapper.CartMapper;
import com.shop.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Cart> findByUserId(int userId) {
        return cartMapper.findByUserId(userId);
    }

    @Override
    public String addToCart(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            return "参数错误";
        }

        Product product = productMapper.findById(productId);
        if (product == null || product.getStock() < quantity) {
            return "库存不足";
        }

        Cart existingCart = cartMapper.findByUserIdAndProductId(userId, productId);
        if (existingCart != null) {
            // 更新数量
            int newQuantity = existingCart.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                return "库存不足";
            }
            if (cartMapper.updateQuantity(userId, productId, newQuantity) > 0) {
                return null;
            }
        } else {
            // 新增购物车项
            Cart cart = Cart.forCreate(userId, productId, quantity);
            if (cartMapper.insert(cart) > 0) {
                return null;
            }
        }

        return "添加失败";
    }

    @Override
    public String updateQuantity(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            return "参数错误";
        }

        // 检查库存
        Product product = productMapper.findById(productId);
        if (product == null || product.getStock() < quantity) {
            return "库存不足";
        }

        return cartMapper.updateQuantity(userId, productId, quantity) > 0 ? null : "更新失败";
    }

    @Override
    public boolean removeFromCart(int userId, int productId) {
        return cartMapper.deleteByUserIdAndProductId(userId, productId) > 0;
    }

}