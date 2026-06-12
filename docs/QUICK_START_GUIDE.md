# 🚀 快速启动指南

试炼坊（Trial Workshop）- 考试系统平台

## 📋 目录

- [项目简介](#项目简介)
- [系统要求](#系统要求)
- [快速启动](#快速启动)
- [验证安装](#验证安装)
- [常见问题](#常见问题)

---

## 项目简介

**试炼坊** 是一个功能完整的在线考试系统，提供以下核心功能：

✅ **用户认证系统** - 注册/登录/JWT令牌管理  
✅ **题库管理** - 创建分类、标签、题目的完整题库  
✅ **在线考试** - 支持多种题型（单选、多选、判断、填空、简答）  
✅ **错题本** - 自动记录错题，使用艾宾浩斯复习曲线  
✅ **AI 智能解析** - 支持 OpenAI/通义千问/DeepSeek 文本解析  
✅ **数据分析** - 个人学习数据统计和可视化  
✅ **验证码** - 图形验证码防护  

---

## 系统要求

### 后端环境
- **Java 11+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Spring Boot 2.7.18**

### 前端环境
- **Node.js 16+**
- **npm 8+** 或 **yarn 1.22+**

### 可选
- **Git** - 版本控制
- **Postman** - API 测试

---

## 快速启动

### 1️⃣ 克隆项目

```bash
git clone https://github.com/Linnanzhi/Trial-Workshop.git
cd Trial-Workshop
```

### 2️⃣ 数据库配置

#### 方式一：使用初始化脚本（推荐）

```bash
# 连接 MySQL
mysql -u root -p

# 执行初始化脚本
source trial-server/src/main/resources/db/init.sql
```

#### 方式二：手动创建

```sql
CREATE DATABASE IF NOT EXISTS trial_workshop 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
  
USE trial_workshop;
-- 然后执行 init.sql 中的 SQL 语句
```

**初始管理员账户：**
- 用户名: `admin`
- 密码: `admin123`
- ⚠️ 首次启动后建议修改密码

详细配置步骤见 [数据库设置](DATABASE_SETUP.md)

### 3️⃣ 后端启动

#### 修改数据库连接

编辑 `trial-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/trial_workshop
    username: root
    password: 你的MySQL密码  # 修改为实际密码
```

#### 启动应用

```bash
cd trial-server

# 使用 Maven
mvn clean install
mvn spring-boot:run

# 或使用 IDE（推荐）
# 在 IDEA 中打开 TrialServerApplication.java 并点击运行按钮
```

**启动成功标志：**
```
2026-02-25 10:30:45.123  INFO 12345 --- [main] c.t.s.TrialServerApplication : 
Started TrialServerApplication in 8.523 seconds (JVM running for 9.156)
```

**后端访问地址：**
- API 服务: `http://localhost:8088/api`
- API 文档: `http://localhost:8088/api/doc.html`（Knife4j）

### 4️⃣ 前端启动

```bash
cd trial-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**前端访问地址：**
- 应用首页: `http://localhost:5173`

---

## 验证安装

### 1. 验证后端

**使用 Knife4j 文档：**
1. 打开浏览器访问 `http://localhost:8088/api/doc.html`
2. 点击"获取验证码" API，选择"Try it out"
3. 看到返回的图形验证码即表示成功

**或使用 curl：**
```bash
curl http://localhost:8088/api/captcha
```

应该返回类似的 JSON：
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "image": "data:image/png;base64,iVBORw0KGgo..."
  }
}
```

### 2. 验证前端

打开浏览器访问 `http://localhost:5173`，应该看到：
- ✅ 登录页面正常显示
- ✅ 能点击"获取验证码"获取图形验证码
- ✅ 浏览器控制台没有报错

### 3. 验证数据库

```bash
mysql -u root -p -e "USE trial_workshop; SELECT COUNT(*) as user_count FROM t_user;"
```

应该返回：
```
user_count
1
```

---

## 项目结构

```
Trial-Workshop/
├── trial-server/                    # 后端服务（Spring Boot）
│   ├── src/main/java/com/trial/server/
│   │   ├── controller/              # API 控制器
│   │   ├── service/                 # 业务逻辑层
│   │   ├── entity/                  # 数据库实体
│   │   ├── mapper/                  # MyBatis 映射
│   │   ├── config/                  # 配置类
│   │   └── common/                  # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml          # 配置文件
│   │   ├── db/init.sql              # 数据库初始化脚本
│   │   └── mapper/                  # MyBatis XML
│   └── pom.xml                      # Maven 依赖配置
│
├── trial-web/                       # 前端应用（Vue 3）
│   ├── src/
│   │   ├── views/                   # 页面组件
│   │   ├── components/              # 可复用组件
│   │   ├── stores/                  # Pinia 状态管理
│   │   ├── api/                     # API 调用模块
│   │   └── App.vue                  # 根组件
│   ├── package.json                 # NPM 依赖
│   └── vite.config.ts               # Vite 配置
│
├── docs/                            # 文档
│   ├── QUICK_START_GUIDE.md         # 快速启动
│   ├── DATABASE_SETUP.md            # 数据库配置
│   ├── CAPTCHA_IMPLEMENTATION_SUMMARY.md
│   ├── AI_CONFIG_USAGE.md
│   └── ...
│
└── README.md                        # 项目说明
```

---

## 常见问题

### ❌ 后端无法启动

**问题 1：MySQL 连接失败**
```
Connection refused: connect
```

**解决方案：**
- 确保 MySQL 服务已启动
- 检查 `application.yml` 中的数据库 URL、用户名、密码
- 确保数据库 `trial_workshop` 已创建

**问题 2：端口 8088 已被占用**
```
Address already in use: bind
```

**解决方案：**
- 方式 1：修改 `application.yml` 的 `server.port`
- 方式 2：杀死占用该端口的进程

### ❌ 前端无法启动

**问题 1：依赖安装失败**
```
npm ERR! code ERESOLVE
```

**解决方案：**
```bash
npm install --legacy-peer-deps
# 或清除缓存后重新安装
npm cache clean --force
npm install
```

**问题 2：访问 `localhost:5173` 显示空白**

**解决方案：**
- 检查浏览器控制台是否有错误信息
- 确保后端 API 服务正常运行
- 尝试硬刷新页面（Ctrl+Shift+R）

### ❌ 获取验证码失败

**问题：返回 500 错误**

**解决方案：**
- 检查后端日志中的错误信息
- 确保 Java 环境变量配置正确
- 参考 [验证码快速测试](CAPTCHA_QUICK_TEST.md)

---

## 下一步

✨ **根据你的需求选择相应文档继续阅读：**

1. 📊 **详细配置** → 阅读 [数据库设置](DATABASE_SETUP.md)
2. 🔐 **验证码功能** → 阅读 [验证码实现总结](CAPTCHA_IMPLEMENTATION_SUMMARY.md)
3. 🤖 **AI 解析配置** → 阅读 [AI 配置使用](AI_CONFIG_USAGE.md)
4. 🔍 **完整功能列表** → 阅读 [功能实现总结](IMPLEMENTATION_SUMMARY.md)

---

## 获取帮助

- 📖 **API 文档**：`http://localhost:8088/api/doc.html`（Knife4j）
- 🐛 **报告问题**：[GitHub Issues](https://github.com/Linnanzhi/Trial-Workshop/issues)
- 💬 **讨论功能**：[GitHub Discussions](https://github.com/Linnanzhi/Trial-Workshop/discussions)

---

**祝你使用愉快！🎉**
