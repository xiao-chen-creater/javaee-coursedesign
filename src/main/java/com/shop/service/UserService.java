package com.shop.service;

import com.shop.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户登录
     */
    User login(String username, String password);
    
    /**
     * 用户注册
     */
    boolean register(User user);

    /**
     * 根据ID查询用户
     */
    User findById(int id);

    /**
     * 根据ID查询用户
     */
    User findByName(String name);

    /**
     * 更新用户信息
     */
    boolean updateUser(User user);
    
    /**
     * 检查用户名是否存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean isEmailExists(String email);
}