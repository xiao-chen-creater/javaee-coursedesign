-- 修复数据库表结构 - 添加缺失的列
USE javaee_shop;

-- 1. 修复 products 表
-- 检查并添加 category_id 列到 products 表
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS category_id INT COMMENT '分类ID';

-- 检查并添加 create_time 列到 products 表（如果也缺失的话）
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS create_time DATETIME COMMENT '创建时间';

-- 2. 修复 order_items 表
-- 检查并添加 price 列到 order_items 表
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS price DECIMAL(10, 2) COMMENT '商品单价';

-- 检查并添加 product_name 列到 order_items 表
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS product_name VARCHAR(255) COMMENT '商品名称';

-- 3. 为现有数据设置默认值
-- 设置products表默认值
UPDATE products SET category_id = 1 WHERE category_id IS NULL;
UPDATE products SET create_time = NOW() WHERE create_time IS NULL;

-- 为现有订单明细设置价格和商品名称（从商品表获取当前信息）
UPDATE order_items oi 
JOIN products p ON oi.product_id = p.id 
SET oi.price = p.price, oi.product_name = p.name 
WHERE oi.price IS NULL OR oi.product_name IS NULL;

-- 4. 显示表结构确认修改
SELECT 'Products表结构修复完成' as status;
DESCRIBE products;

SELECT 'Order_items表结构修复完成' as status;
DESCRIBE order_items;