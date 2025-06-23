package com.shop.service;

import com.shop.mapper.UserMapper;
import com.shop.entity.User;
import com.shop.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        String encryptedPassword = StringUtils.md5(password);
        return userMapper.findByUsernameAndPassword(username, encryptedPassword);
    }
    
    @Override
    public boolean register(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return false;
        }
        
        // 检查用户名是否已存在
        if (isUsernameExists(user.getUsername())) {
            return false;
        }
        
        // 检查邮箱是否已存在
        if (user.getEmail() != null && isEmailExists(user.getEmail())) {
            return false;
        }
        
        // 密码加密
        user.setPassword(StringUtils.md5(user.getPassword()));
        user.setCreateTime(new Date());
        
        return userMapper.insert(user) > 0;
    }
    
    @Override
    public User findById(int id) {
        return userMapper.findById(id);
    }

    @Override
    public User findByName(String name) {
        return userMapper.findByUsername(name);
    }

    @Override
    public boolean updateUser(User user) {
        return userMapper.update(user) > 0;
    }
    
    @Override
    public boolean isUsernameExists(String username) {
        if (username == null) {
            return false;
        }
        return userMapper.countByUsername(username) > 0;
    }
    
    @Override
    public boolean isEmailExists(String email) {
        if (email == null) {
            return false;
        }
        return userMapper.countByEmail(email) > 0;
    }
}