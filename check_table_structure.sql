-- 检查数据库表结构
USE javaee_shop;

-- 查看order_items表结构
DESCRIBE order_items;

-- 查看products表结构  
DESCRIBE products;

-- 查看orders表结构
DESCRIBE orders;

-- 检查order_items表中是否有数据
SELECT COUNT(*) as order_items_count FROM order_items;

-- 检查products表中是否有数据
SELECT COUNT(*) as products_count FROM products;

-- 如果有数据，查看order_items表的示例数据
SELECT * FROM order_items LIMIT 5;

-- 查看products表的示例数据
SELECT id, name, price, stock FROM products LIMIT 5;