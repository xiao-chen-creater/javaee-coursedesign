# OrderItem表product_name字段缺失修复

## 🚨 问题描述

用户在提交订单时遇到以下错误：

```
订单提交失败：订单创建异常： 
### Error updating database. Cause: java.sql.SQLException: (conn=478) Field 'product_name' doesn't have a default value 
### The error may exist in file [OrderItemMapper.xml] 
### The error may involve com.shop.mapper.OrderItemMapper.batchInsert-Inline 
### SQL: INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?) , (?, ?, ?) 
### Cause: java.sql.SQLException: (conn=478) Field 'product_name' doesn't have a default value
```

## 🔍 问题分析

### 根本原因
1. **数据库表结构**: `order_items`表中存在`product_name`字段
2. **字段约束**: 该字段没有设置默认值（NOT NULL without DEFAULT）
3. **代码不匹配**: SQL语句没有为`product_name`字段提供值

### 错误的SQL语句
```sql
INSERT INTO order_items (order_id, product_id, quantity) 
VALUES (?, ?, ?) , (?, ?, ?)
```

### 数据库期望的结构
```sql
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    product_name VARCHAR(255) NOT NULL  -- 这个字段没有默认值
);
```

## 🛠️ 修复方案

### 1. 修改OrderItem.java实体类

**添加productName字段**:
```java
@Data
public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private String productName;  // 新增字段
    
    private Product product;
    
    public static OrderItem forCreate(int orderId, int productId, int quantity, BigDecimal price, Product product) {
        OrderItem orderItem = new OrderItem();
        orderItem.orderId = orderId;
        orderItem.productId = productId;
        orderItem.quantity = quantity;
        orderItem.productName = product != null ? product.getName() : ""; // 设置商品名称
        orderItem.product = product;
        return orderItem;
    }
}
```

### 2. 修改OrderItemMapper.xml

**更新resultMap**:
```xml
<resultMap id="OrderItemResultMap" type="OrderItem">
    <id property="id" column="id"/>
    <result property="orderId" column="order_id"/>
    <result property="productId" column="product_id"/>
    <result property="quantity" column="quantity"/>
    <result property="productName" column="product_name"/>  <!-- 新增映射 -->
    
    <association property="product" javaType="Product">
        <!-- 商品关联信息 -->
    </association>
</resultMap>
```

**更新insert语句**:
```xml
<insert id="insert" parameterType="OrderItem" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO order_items (order_id, product_id, quantity, product_name)
    VALUES (#{orderId}, #{productId}, #{quantity}, #{productName})
</insert>
```

**更新batchInsert语句**:
```xml
<insert id="batchInsert" parameterType="list">
    INSERT INTO order_items (order_id, product_id, quantity, product_name)
    VALUES
    <foreach collection="list" item="item" separator=",">
        (#{item.orderId}, #{item.productId}, #{item.quantity}, #{item.productName})
    </foreach>
</insert>
```

## 📊 修复前后对比

### 修复前
```sql
-- 错误的SQL（缺少product_name字段）
INSERT INTO order_items (order_id, product_id, quantity) 
VALUES (1, 2, 3);

-- 结果：SQLException: Field 'product_name' doesn't have a default value
```

### 修复后
```sql
-- 正确的SQL（包含product_name字段）
INSERT INTO order_items (order_id, product_id, quantity, product_name) 
VALUES (1, 2, 3, '商品名称');

-- 结果：成功插入
```

## 🧪 验证方法

### 1. 编译验证
```bash
mvn compile
# 应该编译成功，无错误
```

### 2. 数据库结构检查
```sql
-- 使用提供的检查脚本
mysql -u username -p database_name < check_table_structure.sql
```

### 3. 功能测试
1. 添加商品到购物车
2. 进入结算页面
3. 点击"提交订单"
4. 验证订单创建成功
5. 检查"我的订单"页面

## 🔄 为什么会出现这个问题？

### 可能的原因
1. **数据库迁移**: 数据库表结构在某个时点添加了`product_name`字段
2. **代码同步**: 代码没有及时更新以适配新的表结构
3. **环境差异**: 开发环境和生产环境的表结构不一致

### 预防措施
1. **版本控制**: 数据库结构变更应该有对应的代码变更
2. **自动化测试**: 集成测试应该覆盖数据库操作
3. **环境一致性**: 确保所有环境的数据库结构一致

## 📈 影响评估

### 正面影响
- ✅ 解决订单提交失败问题
- ✅ 订单明细包含商品名称信息
- ✅ 提升用户体验

### 注意事项
- 🟡 需要确保所有环境的数据库都有`product_name`字段
- 🟡 历史数据可能需要数据迁移
- 🟡 需要测试所有相关的订单功能

## 🚀 部署建议

### 部署前检查
```sql
-- 检查表结构
DESCRIBE order_items;

-- 确认product_name字段存在
SHOW COLUMNS FROM order_items LIKE 'product_name';
```

### 部署步骤
1. **备份数据库**
2. **部署代码更新**
3. **验证订单创建功能**
4. **检查错误日志**

### 回滚方案
如果出现问题，可以：
1. 回滚到之前的代码版本
2. 或者为`product_name`字段添加默认值：
```sql
ALTER TABLE order_items MODIFY COLUMN product_name VARCHAR(255) DEFAULT '';
```

## 📝 相关文档

- `COMPLETE_ORDER_FIX.md` - 完整订单修复总结
- `ORDER_ITEMS_FIX.md` - OrderItem表修复详情
- `check_table_structure.sql` - 数据库结构检查脚本

---

**修复状态**: ✅ 已完成  
**测试状态**: 编译通过，待功能验证  
**风险等级**: 低（向后兼容）  
**部署优先级**: 高（解决关键业务功能）