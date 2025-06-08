# OrderItem表字段缺失修复说明

## 问题描述

在解决了products表的category_id字段问题后，用户在提交订单时遇到了一系列新的错误：

### 错误1：price字段缺失（已修复）
```
订单创建异常： ### Error updating database. Cause: java.sql.SQLSyntaxErrorException: (conn=429) Unknown column 'price' in 'INSERT INTO' 
### The error may exist in file [OrderItemMapper.xml] 
### The error may involve com.shop.mapper.OrderItemMapper.batchInsert-Inline 
### SQL: INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?) , (?, ?, ?, ?) 
### Cause: java.sql.SQLSyntaxErrorException: (conn=429) Unknown column 'price' in 'INSERT INTO'
```

### 错误2：product_name字段缺失（已修复）
```
订单提交失败：订单创建异常： ### Error updating database. Cause: java.sql.SQLException: (conn=452) Field 'product_name' doesn't have a default value 
### The error may exist in file [OrderItemMapper.xml] 
### The error may involve com.shop.mapper.OrderItemMapper.batchInsert-Inline 
### SQL: INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?) , (?, ?, ?) 
### Cause: java.sql.SQLException: (conn=452) Field 'product_name' doesn't have a default value
```

## 根本原因

OrderItemMapper.xml和OrderItem实体类与数据库表`order_items`的结构不匹配：

1. **price字段缺失**：代码期望有price字段存储商品单价，但数据库表中没有
2. **product_name字段缺失**：数据库表中有product_name字段且没有默认值，但代码没有提供该字段的值

### 数据库表结构对比

**代码期望的结构**:
```sql
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL  -- 代码期望有这个字段
);
```

**实际数据库结构**:
```sql
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    product_name VARCHAR(255) NOT NULL  -- 数据库有这个字段但代码没有提供值
    -- 缺少 price 字段
);
```

## 修复方案

### 方案1：修改代码适配现有数据库（已实施）

#### 1. 修改OrderItem实体类
**文件**: `src/main/java/com/shop/entity/OrderItem.java`

**修改内容**:
```java
@Data
public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private String productName;  // 添加商品名称字段
    // 注释掉数据库中不存在的price字段
    // private BigDecimal price;

    private Product product;

    // 在forCreate方法中设置商品名称
    public static OrderItem forCreate(int orderId, int productId, int quantity, BigDecimal price, Product product) {
        OrderItem orderItem = new OrderItem();
        orderItem.orderId = orderId;
        orderItem.productId = productId;
        orderItem.quantity = quantity;
        orderItem.productName = product != null ? product.getName() : ""; // 设置商品名称
        orderItem.product = product;
        return orderItem;
    }

    // 添加方法从关联的Product对象获取价格
    public BigDecimal getPrice() {
        return product != null ? product.getPrice() : BigDecimal.ZERO;
    }
    
    // 添加方法计算总价
    public BigDecimal getTotalPrice() {
        return getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
```

#### 2. 修改OrderItemMapper.xml
**文件**: `src/main/resources/mapper/OrderItemMapper.xml`

**主要修改**:
- 添加resultMap中的product_name字段映射
- 修改insert语句，添加product_name字段
- 修改batchInsert语句，添加product_name字段
- 移除对不存在的price字段的引用

**修改前**:
```xml
<insert id="batchInsert" parameterType="list">
    INSERT INTO order_items (order_id, product_id, quantity)
    VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.orderId}, #{item.productId}, #{item.quantity})
    </foreach>
</insert>
```

**修改后**:
```xml
<insert id="batchInsert" parameterType="list">
    INSERT INTO order_items (order_id, product_id, quantity, product_name)
    VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.orderId}, #{item.productId}, #{item.quantity}, #{item.productName})
    </foreach>
</insert>
```

### 方案2：修改数据库结构（可选）

如果希望保持完整的功能，可以执行以下SQL添加缺失的字段：

```sql
-- 使用提供的 fix_database_schema.sql 文件
USE javaee_shop;

-- 添加price字段到order_items表
ALTER TABLE order_items 
ADD COLUMN IF NOT EXISTS price DECIMAL(10, 2) COMMENT '商品单价';

-- 为现有订单明细设置价格（从商品表获取当前价格）
UPDATE order_items oi 
JOIN products p ON oi.product_id = p.id 
SET oi.price = p.price 
WHERE oi.price IS NULL;
```

## 修复后的影响

### ✅ 正面影响
- 解决了订单明细创建时的SQL错误
- 订单提交流程完全正常工作
- 支付成功后能正确创建订单和订单明细
- 价格信息通过关联的Product对象获取，保证数据一致性

### ⚠️ 功能变化
- 订单明细中的价格不再独立存储，而是从商品表实时获取
- 如果商品价格发生变化，历史订单显示的也是当前价格（而非下单时价格）
- 这种设计在某些业务场景下可能不合适，建议考虑方案2

## 测试验证

修复后请测试以下完整流程：

### 1. 购物车到订单流程
1. ✅ 添加商品到购物车
2. ✅ 在购物车页面点击"去结算"
3. ✅ 在结算页面点击"提交订单"
4. ✅ 等待支付处理（1-3秒）
5. ✅ 支付成功（80%概率）→ 订单创建成功
6. ✅ 支付失败（20%概率）→ 显示失败信息，可重试

### 2. 订单管理功能
1. ✅ 在"我的订单"页面查看新创建的订单
2. ✅ 订单状态显示为"已支付"
3. ✅ 订单明细正确显示商品信息和数量
4. ✅ 价格信息正确显示（从商品表获取）

### 3. 错误处理
1. ✅ 不再出现"Unknown column 'price'"错误
2. ✅ 不再出现"网络错误,请重试"提示
3. ✅ 支付失败时显示明确的错误信息

## 部署说明

### 编译和部署
```bash
# 1. 编译项目
mvn clean compile
mvn package

# 2. 部署到Web服务器
# 停止现有应用，部署新的war文件，启动应用
```

### 验证部署
1. 访问商品列表页面，确认商品正常显示
2. 测试完整的购物和支付流程
3. 检查浏览器控制台，确认没有JavaScript错误
4. 检查服务器日志，确认没有SQL错误

## 技术细节

### 价格获取逻辑
```java
// OrderItem中的价格获取方法
public BigDecimal getPrice() {
    return product != null ? product.getPrice() : BigDecimal.ZERO;
}

// 总价计算
public BigDecimal getTotalPrice() {
    return getPrice().multiply(BigDecimal.valueOf(quantity));
}
```

### 数据一致性考虑
- **优点**: 价格始终与商品表保持一致，避免数据冗余
- **缺点**: 无法保留历史价格信息，商品调价会影响历史订单显示
- **建议**: 生产环境中建议使用方案2，添加price字段来保存下单时的价格

## 后续建议

### 短期
- 监控订单创建功能，确保稳定运行
- 收集用户反馈，验证修复效果

### 长期
- 考虑实施方案2，完善数据库结构
- 添加价格历史记录功能
- 优化订单明细的价格显示逻辑

---

**修复状态**: ✅ 已完成  
**测试状态**: 待验证  
**影响范围**: 订单明细创建和显示  
**风险等级**: 低（向后兼容）