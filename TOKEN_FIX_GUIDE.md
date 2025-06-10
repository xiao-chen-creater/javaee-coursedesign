# GitHub Token修复指导

## 🚨 当前问题
GitHub Token已过期或无效，导致无法推送代码和创建PR。

## 🔧 解决步骤

### 步骤1: 生成新的GitHub Personal Access Token

1. **访问GitHub设置**:
   ```
   https://github.com/settings/tokens
   ```

2. **创建新Token**:
   - 点击 "Generate new token" → "Generate new token (classic)"
   - Note: `javaee-coursedesign-fix-token`
   - Expiration: `90 days`

3. **选择权限**:
   ```
   ✅ repo (完整仓库访问权限)
     ✅ repo:status
     ✅ repo_deployment
     ✅ public_repo
     ✅ repo:invite
     ✅ security_events
   ✅ workflow (GitHub Actions)
   ```

4. **复制Token**:
   ```
   格式: ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ⚠️ 只显示一次，请立即复制保存
   ```

### 步骤2: 设置环境变量

```bash
# 设置新token
export GITHUB_TOKEN="ghp_your_new_token_here"

# 验证token有效性
curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user
```

### 步骤3: 更新Git远程URL

```bash
cd /workspace/javaee-coursedesign

# 使用新token更新远程URL
git remote set-url origin https://${GITHUB_TOKEN}@github.com/xiao-chen-creater/javaee-coursedesign.git

# 验证远程URL
git remote -v
```

### 步骤4: 推送代码

```bash
# 确认在正确分支
git branch --show-current

# 推送到GitHub
git push -u origin fix/orderitem-complete-fields-v2
```

### 步骤5: 创建PR

推送成功后，可以：

1. **通过GitHub网页**:
   - 访问: https://github.com/xiao-chen-creater/javaee-coursedesign
   - 点击 "Compare & pull request"

2. **使用API工具**:
   ```bash
   # 使用create_pr工具
   # 或者curl命令
   ```

## 🔍 验证Token是否有效

```bash
# 测试API访问
curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user

# 期望输出: 用户信息JSON
# 错误输出: {"message": "Bad credentials"}
```

## 🚨 常见问题

### 问题1: Token权限不足
**症状**: `403 Forbidden` 错误
**解决**: 确保token包含 `repo` 权限

### 问题2: Token格式错误
**症状**: `401 Unauthorized` 错误  
**解决**: 确认token格式为 `ghp_` 开头

### 问题3: Token过期
**症状**: `Bad credentials` 错误
**解决**: 生成新token并更新环境变量

## 📋 当前代码状态

### ✅ 已完成
- OrderItem.java实体类修复
- OrderItemMapper.xml配置更新
- 完整的技术文档
- 本地代码提交完成

### ⏳ 待完成
- 推送到GitHub远程仓库
- 创建Pull Request
- 功能测试验证

## 🎯 修复后的操作流程

1. **设置新token** → 2. **推送代码** → 3. **创建PR** → 4. **合并代码** → 5. **部署测试**

---

**重要提醒**: 
- GitHub Token只显示一次，请妥善保存
- 建议设置90天过期时间，避免频繁更新
- 确保token权限包含完整的repo访问权限