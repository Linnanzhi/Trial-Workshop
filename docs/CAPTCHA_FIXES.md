# 🔧 验证码错误修复

验证码常见问题的解决方案

## 📋 目录

- [常见错误](#常见错误)
- [错误排查](#错误排查)
- [常见问题](#常见问题)

---

## 常见错误

### 错误 1：验证码图片为空或损坏

**症状：**
```
前端看到破损的图片或无法显示
浏览器控制台报错：Failed to load image
```

**原因：**
- BufferedImage 创建失败
- ImageIO 写入异常
- Base64 编码格式错误

**解决方案：**

```java
// 检查 CaptchaUtil.generateCaptcha() 的 try-catch
try {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    String base64Image = "data:image/png;base64," + 
        Base64.getEncoder().encodeToString(baos.toByteArray());
    return new String[]{code.toString(), base64Image};
} catch (IOException e) {
    // 添加详细日志
    log.error("验证码生成失败", e);
    throw new RuntimeException("生成验证码失败: " + e.getMessage());
}
```

**检查清单：**
- [ ] ImageIO 依赖已添加
- [ ] 磁盘空间充足
- [ ] /tmp 目录有写入权限

---

### 错误 2：验证码总是验证失败

**症状：**
```
输入正确的验证码仍显示"验证码错误"
登录界面无法通过验证码验证
```

**原因：**
- Session 未正确保存或丢失
- 大小写处理不当
- 验证逻辑错误

**解决方案：**

```java
// ❌ 错误做法
String captcha = code;  // 不转小写
session.setAttribute("captcha", captcha);

// ✓ 正确做法
String captcha = code.toLowerCase();
session.setAttribute("captcha", captcha);

// 登录验证
String stored = (String) session.getAttribute("captcha");
String input = req.getCaptcha().toLowerCase();  // 必须转小写

if (stored == null) {
    return Result.error(400, "验证码已过期，请重新获取");
}

if (!input.equals(stored)) {
    return Result.error(400, "验证码错误");
}

// 验证成功后立即清除
session.removeAttribute("captcha");
```

**检查清单：**
- [ ] 验证前检查 stored 不为 null
- [ ] 两端都转小写再对比
- [ ] 使用 equals() 而非 ==
- [ ] 验证成功后清除 Session

---

### 错误 3：Session 丢失

**症状：**
```
获取验证码成功，登录时显示"验证码已过期"
刷新页面后验证码丢失
```

**原因：**
- 浏览器未启用 Cookie
- Session 超时配置过短
- 跨域请求丢失 Session

**解决方案：**

**配置 Session 超时时间：**
```yaml
server:
  servlet:
    session:
      timeout: 30m  # 30 分钟
```

**确保浏览器启用 Cookie：**
```javascript
// 前端检查
if (!navigator.cookieEnabled) {
    alert("请启用浏览器 Cookie");
}
```

**处理跨域请求：**
```java
// 前端 axios 配置
axios.defaults.withCredentials = true;

// 后端 CORS 配置
@CrossOrigin(origins = "http://localhost:5173", 
             allowCredentials = "true")
@RestController
public class CaptchaController {
    // ...
}
```

**检查清单：**
- [ ] 浏览器已启用 Cookie
- [ ] Session 超时时间合理（>= 5 分钟）
- [ ] 跨域请求已配置 withCredentials

---

### 错误 4：验证码安全问题

**症状：**
```
验证码被暴力猜测
同一验证码可多次使用
验证码永不过期
```

**原因：**
- 无失效机制
- 无频率限制
- 无锁定机制

**解决方案：**

**添加验证码失效时间：**
```java
public class CaptchaVO {
    private String code;
    private long createdTime;
    
    public boolean isExpired() {
        // 5 分钟过期
        return System.currentTimeMillis() - createdTime > 5 * 60 * 1000;
    }
}

// 在 CaptchaController 中
@GetMapping
public Result<Map<String, String>> getCaptcha(HttpSession session) {
    String[] captcha = CaptchaUtil.generateCaptcha();
    CaptchaVO vo = new CaptchaVO();
    vo.setCode(captcha[0].toLowerCase());
    vo.setCreatedTime(System.currentTimeMillis());
    
    session.setAttribute("captcha", vo);
    
    Map<String, String> result = new HashMap<>();
    result.put("image", captcha[1]);
    return Result.success(result);
}

// 在验证时检查过期
CaptchaVO stored = (CaptchaVO) session.getAttribute("captcha");
if (stored == null || stored.isExpired()) {
    return Result.error(400, "验证码已过期");
}
```

**添加错误次数限制：**
```java
public class LoginUtil {
    private static Map<String, Integer> errorCount = new ConcurrentHashMap<>();
    private static Map<String, Long> lockTime = new ConcurrentHashMap<>();
    
    public static boolean isLocked(String username) {
        Long time = lockTime.get(username);
        if (time != null && System.currentTimeMillis() - time < 5 * 60 * 1000) {
            return true;
        }
        return false;
    }
    
    public static void recordError(String username) {
        Integer count = errorCount.getOrDefault(username, 0) + 1;
        errorCount.put(username, count);
        
        // 错误 3 次后锁定 5 分钟
        if (count >= 3) {
            lockTime.put(username, System.currentTimeMillis());
            errorCount.put(username, 0);
        }
    }
}

// 在 login 中使用
if (LoginUtil.isLocked(username)) {
    return Result.error(429, "登录失败次数过多，请 5 分钟后再试");
}

if (!captchaCorrect) {
    LoginUtil.recordError(username);
    return Result.error(400, "验证码错误");
}

LoginUtil.clearError(username);
```

**检查清单：**
- [ ] 验证码 5 分钟过期
- [ ] 一次性使用（验证后立即删除）
- [ ] 错误次数限制
- [ ] 账户锁定机制

---

## 错误排查

### 排查步骤 1：检查后端日志

```bash
# 查看 Spring Boot 日志
tail -f logs/spring.log

# 搜索错误
grep -i error logs/spring.log

# 查看特定时间的日志
tail -f logs/spring.log | grep "captcha"
```

### 排查步骤 2：使用 Postman 调试

1. **单独测试获取验证码**
   ```
   GET http://localhost:8088/api/captcha
   ```

2. **检查 Response Headers**
   - 确保有 `Set-Cookie` header
   - 确保包含 `JSESSIONID`

3. **保存 Cookie**
   - 在 Postman Cookies 中查看
   - 应该有一个 `JSESSIONID` cookie

4. **用同一 Cookie 测试登录**
   - 使用之前保存的 cookie
   - POST 登录请求

### 排查步骤 3：检查浏览器开发者工具

**Network 标签：**
1. 获取验证码请求
   - URL: `http://localhost:8088/api/captcha`
   - Status: 200
   - Response Headers: 包含 `Set-Cookie`

2. 登录请求
   - URL: `http://localhost:8088/api/login`
   - 请求 Headers 包含 Cookie

**Application 标签：**
1. Cookies 中查看 `JSESSIONID`
2. 确保域名正确（localhost）
3. 确保路径正确（/api）

**Console 标签：**
1. 查看 JavaScript 错误
2. 查看网络错误

### 排查步骤 4：调整日志级别

在 `application.yml` 中添加：

```yaml
logging:
  level:
    com.trial.server: DEBUG
    org.springframework: INFO
    org.hibernate: INFO
```

然后在 Controller 中添加日志：

```java
@GetMapping
public Result<Map<String, String>> getCaptcha(HttpSession session) {
    log.debug("获取验证码请求，Session ID: {}", session.getId());
    
    String[] captcha = CaptchaUtil.generateCaptcha();
    String code = captcha[0].toLowerCase();
    
    session.setAttribute("captcha", code);
    log.debug("验证码已存储: {}", code);
    
    Map<String, String> result = new HashMap<>();
    result.put("image", captcha[1]);
    return Result.success(result);
}
```

---

## 常见问题

### Q1: 为什么每次获取的验证码都一样？

**A:** 这不应该发生。检查随机数生成器：

```java
// ❌ 错误：Random 未初始化
Random random;
code.append(random.nextInt(...));

// ✓ 正确：在方法内创建 Random
Random random = new Random();
for (int i = 0; i < CODE_LENGTH; i++) {
    code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
}
```

### Q2: 验证码图片很模糊或无法识别？

**A:** 调整干扰参数：

```java
// 减少干扰线
for (int i = 0; i < 3; i++) {  // 原来是 5
    // ...
}

// 减少噪点
for (int i = 0; i < 30; i++) {  // 原来是 50
    // ...
}

// 增加字体大小
g.setFont(new Font("Arial", Font.BOLD, 32));  // 原来是 28
```

### Q3: 验证码生成速度太慢？

**A:** 检查是否有以下问题：

```java
// ❌ 错误：每次创建新 Font 对象
Font font = new Font("Arial", Font.BOLD, 28);
g.setFont(font);

// ✓ 正确：复用 Font 对象或提取为常量
private static final Font CAPTCHA_FONT = new Font("Arial", Font.BOLD, 28);
g.setFont(CAPTCHA_FONT);
```

### Q4: 部署到生产环境后验证码不工作？

**A:** 检查以下配置：

```yaml
# 生产环境配置
server:
  servlet:
    session:
      # Cookie 配置
      cookie:
        secure: true        # HTTPS only
        http-only: true     # 防止 XSS
        same-site: lax      # CSRF 防护
      timeout: 30m          # Session 超时
```

---

**✅ 验证码问题解决！** 

如果还有问题，参考完整的 [实现总结](IMPLEMENTATION_SUMMARY.md)
