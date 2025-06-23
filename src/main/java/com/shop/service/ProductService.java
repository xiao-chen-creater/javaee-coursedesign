package com.shop.service;

import com.shop.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 根据ID查询商品
     */
    Product findById(int id);

    /**
     * 搜索商品
     */
    List<Product> searchProducts(String keyword, String sort);
    
    /**
     * 查询热销商品
     */
    List<Product> findHotProducts(int limit);
    
    /**
     * 查询最新商品
     */
    List<Product> findLatestProducts(int limit);

    /**
     * 分页查询商品
     */
    List<Product> findByPage(int page, int size, String sort);
    
    /**
     * 统计商品总数
     */
    int getTotalCount();

    /**
     * 更新商品
     */
    boolean updateProduct(Product product);

    /**
     * 自动补全
     */
    List<String> getSuggestions(String keyword, int limit);
}