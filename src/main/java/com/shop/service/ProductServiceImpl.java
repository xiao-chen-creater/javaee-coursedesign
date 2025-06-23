package com.shop.service;

import com.shop.entity.Product;
import com.shop.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductMapper productMapper;

    @Override
    public Product findById(int id) {
        return productMapper.findById(id);
    }

    @Override
    public List<Product> searchProducts(String keyword, String sort) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findByPage(1, 100, sort); // 返回前100个商品
        }
        return productMapper.search(keyword.trim(), sort);
    }
    
    @Override
    public List<Product> findHotProducts(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        return productMapper.findHotProducts(limit);
    }
    
    @Override
    public List<Product> findLatestProducts(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        return productMapper.findLatestProducts(limit);
    }

    @Override
    public List<Product> findByPage(int page, int size, String sort) {
        if (page < 1) {
            page = 1;
        }
        if (size <= 0) {
            size = 10;
        }
        int offset = (page - 1) * size;
        return productMapper.findByPage(offset, size, sort);
    }
    
    @Override
    public int getTotalCount() {
        return productMapper.countAll();
    }

    @Override
    public boolean updateProduct(Product product) {
        if (product == null) {
            return false;
        }
        return productMapper.update(product) > 0;
    }

    @Override
    public List<String> getSuggestions(String keyword, int limit) {
        return productMapper.findByKeyword(keyword.trim(), limit);
    }
}