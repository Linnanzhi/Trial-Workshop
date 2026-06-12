# 🧪 验证码快速测试

验证码功能的测试步骤和场景

## 📋 目录

- [测试环境检查](#测试环境检查)
- [API 测试](#api-测试)
- [功能测试](#功能-测试)
- [压力测试](#压力测试)
- [故障排查](#故障排查)

---

## 测试环境检查

### 前置条件

```bash
✓ 后端已启动（http://localhost:8088/api）
✓ 前端已启动（http://localhost:5173）
✓ MySQL 数据库连接正常
✓ 浏览器打开开发者工具（F12）
```

### 验证系统状态

```bash
# 1. 检查后端服务
curl http://localhost:8088/api/captcha

# 2. 检查前端应用
curl http://localhost:5173

# 3. 检查数据库
mysql -u root -p -e "USE trial_workshop; SELECT COUNT(*) FROM t_user;"
```

---

## API 测试

### 使用 Knife4j 文档（推荐）

1. **打开 API 文档**
   - 访问：`http://localhost:8088/api/doc.html`

2. **找到验证码接口**
   - 左侧菜单 → "验证码" 模块
   - 找到 "获取验证码" API

3. **执行测试**
   - 点击 "Try it out"
   - 点击 "Execute"

4. **查看响应**
   - 应该看到 200 状态码
   - Response 中包含 Base64 图片

### 使用 curl 测试

```bash
# 1. 基础请求
curl -v http://localhost:8088/api/captcha

# 2. 完整请求（带 Cookie）
curl -v -c cookies.txt http://localhost:8088/api/captcha

# 3. 查看 Session
curl -v -b cookies.txt http://localhost:8088/api/captcha
```

**成功响应示例：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHgAAAAoCAIAAABHBj/6AAAACXBIWXMAAA..."
  }
}
```

### 使用 Postman 测试

1. **新建请求**
   - Method: `GET`
   - URL: `http://localhost:8088/api/captcha`

2. **配置**
   - Headers: 默认即可
   - 勾选 "Cookies" 以保存 Session

3. **发送请求**
   - 点击 Send
   - 查看 Response

4. **多次请求**
   - 每次获取应该得到不同的验证码
   - 前端 Cookies 会自动管理 Session

---

## 功能测试

### 测试场景 1：获取单个验证码

**操作：**
1. 打开浏览器开发者工具（F12）
2. 访问登录页面：`http://localhost:5173/login`
3. 点击"获取验证码"按钮

**预期结果：**
```
✓ 图片正常显示
✓ 图片包含 4 位随机字符
✓ 每次点击获得不同的验证码
✓ 控制台无错误
```

**检查 Network 标签：**
- Request: `GET /api/captcha`
- Status: `200 OK`
- Response: 包含 Base64 图片

### 测试场景 2：连续刷新验证码

**操作：**
1. 在登录页面连续点击"刷新"按钮 5 次

**预期结果：**
```
✓ 每次都能成功获取新的验证码
✓ 响应时间 < 100ms
✓ 内存占用不会持续增长
✓ 控制台无错误
```

### 测试场景 3：验证码输入验证

**操作：**
1. 获取验证码，记录图片中的字符（如 "A3x2"）
2. 输入用户名：admin
3. 输入密码：admin123
4. 输入验证码：a3x2（小写）
5. 点击登录

**预期结果：**
```
✓ 登录成功
✓ 跳转到首页
✓ 验证码被清除（无法重复使用）
```

**不区分大小写验证：**
- 输入 "A3X2" ✓ 成功
- 输入 "a3x2" ✓ 成功
- 输入 "a3X2" ✓ 成功

### 测试场景 4：错误的验证码

**操作：**
1. 获取验证码
2. 输入错误的验证码（如输入 "0000"）
3. 点击登录

**预期结果：**
```
✓ 登录失败
✓ 显示错误提示："验证码错误"
✓ 验证码自动刷新
✓ 用户可重新输入
```

### 测试场景 5：验证码过期

**操作：**
1. 获取验证码
2. 等待 5 分钟
3. 尝试登录

**预期结果：**
```
✓ 登录失败
✓ 显示提示："验证码已过期"
✓ 用户需重新获取验证码
```

---

## 压力测试

### 测试场景 1：高频请求

**脚本：**
```bash
#!/bin/bash
# 每秒获取 10 次验证码，共 100 次
for i in {1..100}; do
  curl -s http://localhost:8088/api/captcha > /dev/null
  if [ $((i % 10)) -eq 0 ]; then
    echo "已完成 $i 次请求"
  fi
done
echo "测试完成"
```

**运行：**
```bash
chmod +x stress_test.sh
./stress_test.sh
```

**预期结果：**
```
✓ 所有请求都成功返回 200
✓ 无内存泄漏
✓ 平均响应时间 < 100ms
✓ 服务器不崩溃
```

### 测试场景 2：并发请求

**使用 Apache Bench：**
```bash
ab -n 1000 -c 50 http://localhost:8088/api/captcha
```

**输出示例：**
```
Benchmarking localhost (be patient)...
Completed 100 requests
Completed 200 requests
...
Finished 1000 requests

Server Software:        
Server Hostname:        localhost
Server Port:            8088

Document Path:          /api/captcha
Document Length:        5243 bytes

Concurrency Level:      50
Time taken for tests:   8.234 seconds
Complete requests:      1000
Failed requests:        0
Requests per second:    121.45 [#/sec]
Mean time per request:  410.23 [ms]
```

**评判标准：**
- ✓ Failed requests = 0
- ✓ Requests per second > 100
- ✓ Mean time per request < 500ms

---

## 故障排查

### ❌ 获取验证码返回 404

**症状：**
```json
{
  "status": 404,
  "message": "Not Found"
}
```

**原因分析：**
- 后端服务未启动
- 接口路径错误
- 上下文路径配置错误

**解决方案：**
1. 检查后端是否运行
   ```bash
   ps aux | grep java
   ```

2. 验证接口路径
   ```bash
   curl http://localhost:8088/api/captcha
   ```

3. 检查配置文件
   ```yaml
   server:
     servlet:
       context-path: /api  # 确保正确
   ```

### ❌ 获取验证码返回 500

**症状：**
```json
{
  "code": 500,
  "msg": "Internal Server Error",
  "error": "..."
}
```

**常见原因及解决方案：**

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| NullPointerException | Graphics2D 为 null | 检查 BufferedImage 初始化 |
| IOException | 图片写入失败 | 检查磁盘空间 |
| OutOfMemoryError | 内存不足 | 增加堆内存 `-Xmx512m` |

### ❌ 验证码图片显示不出来

**症状：**
- 前端看不到图片
- 控制台报错

**原因分析：**
- Base64 编码错误
- 图片 MIME 类型错误
- CORS 跨域问题

**调试步骤：**
```javascript
// 在前端控制台执行
const img = document.querySelector('img');
console.log('图片 src:', img.src.substring(0, 100)); // 查看前 100 字符
```

**检查 Base64 格式：**
```
✓ 正确: data:image/png;base64,iVBORw0KGgo...
✗ 错误: data:image/jpeg;base64,...（应该是 png）
✗ 错误: base64,iVBORw0KGgo...（缺少 data: 前缀）
```

### ❌ 验证码验证总是失败

**症状：**
- 输入正确的验证码仍显示"验证码错误"
- 登录失败

**原因分析：**
- Session 丢失或未正确保存
- 验证码大小写处理错误
- 验证码已过期

**排查方法：**

1. **检查 Session 是否保存**
   ```java
   // 在 CaptchaController 中添加日志
   String captcha = code.toLowerCase();
   System.out.println("存储验证码: " + captcha);
   session.setAttribute("captcha", captcha);
   ```

2. **检查验证逻辑**
   ```java
   // 在 AuthController 中添加日志
   String stored = (String) session.getAttribute("captcha");
   String input = req.getCaptcha().toLowerCase();
   System.out.println("存储: " + stored + ", 输入: " + input);
   System.out.println("相等: " + input.equals(stored));
   ```

3. **使用 Postman 测试**
   - 勾选 "Cookies" 记住 Session
   - 先 GET `/api/captcha`
   - 再 POST `/api/login` 在同一 Session 中

---

## 测试清单

完成以下检查确保验证码功能正常：

- [ ] API 返回 200 状态码
- [ ] 响应包含 Base64 图片
- [ ] 前端能正常显示验证码图片
- [ ] 点击刷新能获取新的验证码
- [ ] 输入正确验证码能成功登录
- [ ] 输入错误验证码会失败
- [ ] 验证码不区分大小写
- [ ] 响应时间 < 100ms
- [ ] 无内存泄漏
- [ ] 高并发下仍能正常工作

---

## 常用测试命令

```bash
# 获取验证码并保存响应
curl http://localhost:8088/api/captcha -o response.json

# 提取 Base64 图片并保存
curl http://localhost:8088/api/captcha | jq '.data.image' | \
  sed 's/"data:image\/png;base64,//g' | \
  base64 -d > captcha.png

# 打开图片查看
open captcha.png  # macOS
eog captcha.png   # Linux

# 性能测试
wrk -t4 -c100 -d30s http://localhost:8088/api/captcha

# 响应时间统计
ab -t 10 -g results.tsv http://localhost:8088/api/captcha
```

---

**✅ 开始测试吧！** 

有问题参考 [错误修复指南](CAPTCHA_FIXES.md)
