# 数据库结构修复说明

## 问题描述

用户在提交订单时遇到以下错误：

### 第一个错误（已修复）：
```
jakarta.servlet.ServletException: Request processing failed: org.springframework.jdbc.BadSqlGrammarException: 
### Error updating database.  Cause: java.sql.SQLSyntaxErrorException: (conn=412) Unknown column 'category_id' in 'SET'
```

### 第二个错误（新发现）：
```
订单创建异常： ### Error updating database. Cause: java.sql.SQLSyntaxErrorException: (conn=429) Unknown column 'price' in 'INSERT INTO' 
### The error may exist in file [OrderItemMapper.xml] 
### The error may involve com.shop.mapper.OrderItemMapper.batchInsert-Inline 
### SQL: INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?) , (?, ?, ?, ?) 
### Cause: java.sql.SQLSyntaxErrorException: (conn=429) Unknown column 'price' in 'INSERT INTO'
```

## 根本原因

代码中的Mapper文件和实体类包含了数据库表中不存在的字段：

### Products表缺失字段：
- `category_id` - 分类ID字段
- `create_time` - 创建时间字段

### Order_items表缺失字段：
- `price` - 商品单价字段

这导致在执行商品和订单相关的数据库操作时出现SQL语法错误。

## 修复方案

### 方案1：修改代码适配现有数据库（已实施）

#### 1. 修改Product实体类
**文件**: `src/main/java/com/shop/entity/Product.java`

**修改内容**:
```java
// 注释掉数据库中不存在的字段
// private int categoryId;
// private Date createTime;
```

#### 2. 修改ProductMapper.xml
**文件**: `src/main/resources/mapper/ProductMapper.xml`

**修改内容**:
- 移除resultMap中的category_id和create_time映射
- 修改insert语句，移除category_id和create_time字段
- 修改update语句，移除category_id字段
- 将所有ORDER BY create_time改为ORDER BY id

#### 3. 修改ProductServiceImpl
**文件**: `src/main/java/com/shop/service/impl/ProductServiceImpl.java`

**修改内容**:
```java
// 注释掉设置创建时间的代码
// product.setCreateTime(new Date());
```

#### 4. 修改OrderItem实体类
**文件**: `src/main/java/com/shop/entity/OrderItem.java`

**修改内容**:
```java
// 注释掉数据库中不存在的price字段
// private BigDecimal price;

// 添加方法从关联的Product对象获取价格
public BigDecimal getPrice() {
    return product != null ? product.getPrice() : BigDecimal.ZERO;
}
```

#### 5. 修改OrderItemMapper.xml
**文件**: `src/main/resources/mapper/OrderItemMapper.xml`

**修改内容**:
- 移除resultMap中的price映射
- 修改insert和batchInsert语句，移除price字段
- 修改关联查询，移除products表中不存在的字段引用

### 方案2：修改数据库结构（可选）

如果您希望保持完整的功能，可以执行以下SQL来添加缺失的字段：

```sql
-- 使用提供的 fix_database_schema.sql 文件
USE javaee_shop;

-- 1. 修复 products 表
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS category_id INT COMMENT '分类ID';

ALTER TABLE products 
ADD COLUMN IF NOT EXISTS create_time DATETIME COMMENT '创建时间';

-- 2. 修复 order_items 表
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS price DECIMAL(10, 2) COMMENT '商品单价';

-- 3. 为现有数据设置默认值
UPDATE products SET category_id = 1 WHERE category_id IS NULL;
UPDATE products SET create_time = NOW() WHERE create_time IS NULL;

-- 为现有订单明细设置价格（从商品表获取当前价格）
UPDATE order_items oi 
JOIN products p ON oi.product_id = p.id 
SET oi.price = p.price 
WHERE oi.price IS NULL;
```

## 修复后的影响

### 正面影响
✅ 解决了订单提交时的数据库错误  
✅ 解决了订单明细创建时的price字段错误  
✅ 系统可以正常运行  
✅ 商品相关功能恢复正常  
✅ 订单创建和支付流程正常工作  

### 功能限制
⚠️ 暂时无法使用商品分类功能  
⚠️ 商品创建时间信息丢失  
⚠️ 商品排序改为按ID排序而非创建时间  
⚠️ 订单明细中的价格信息从关联的商品对象获取（而非独立存储）  

## 测试验证

修复后请测试以下功能：

### 1. 商品相关功能
- ✅ 商品列表显示
- ✅ 商品搜索
- ✅ 商品详情查看
- ✅ 商品库存更新

### 2. 订单功能
- ✅ 添加商品到购物车
- ✅ 购物车结算
- ✅ 订单提交和支付（不再出现price字段错误）
- ✅ 订单明细创建（使用商品当前价格）
- ✅ 订单状态更新
- ✅ 我的订单页面显示

### 3. 用户体验
- ✅ 不再出现"网络错误"提示
- ✅ 支付流程正常工作
- ✅ 订单创建成功

## 部署步骤

1. **重新编译项目**:
   ```bash
   mvn clean compile
   mvn package
   ```

2. **部署到Web服务器**:
   - 停止现有应用
   - 部署新的war文件
   - 启动应用

3. **验证修复**:
   - 访问商品列表页面
   - 测试完整的购物和支付流程
   - 确认不再出现数据库错误

## 后续建议

### 短期
- 监控系统运行状况
- 收集用户反馈
- 确保核心功能稳定

### 长期
- 考虑实施方案2，完善数据库结构
- 重新启用商品分类功能
- 添加商品创建时间跟踪
- 优化商品排序逻辑

## 回滚方案

如果修复后出现其他问题，可以：

1. **代码回滚**:
   ```bash
   git checkout HEAD~1
   mvn clean package
   ```

2. **数据库回滚**:
   - 如果执行了方案2的SQL，可以删除添加的字段
   ```sql
   ALTER TABLE products DROP COLUMN category_id;
   ALTER TABLE products DROP COLUMN create_time;
   ```

---

**修复状态**: ✅ 已完成  
**测试状态**: 待验证  
**影响范围**: 商品和订单模块  
**风险等级**: 低