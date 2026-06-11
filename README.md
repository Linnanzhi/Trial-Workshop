# 试炼坊 (Trial Workshop) - 智能题库管理系统

## 项目简介
试炼坊是一个基于 Spring Boot + Vue 3 的智能题库管理系统，支持题目的导入、管理、组卷和考试功能，集成了 AI 智能解析能力。

## 技术栈

### 后端
- Java 8
- Spring Boot 2.7.x
- Spring Security + JWT
- MyBatis-Plus
- MySQL 8.0

### 前端
- Vue 3 + TypeScript
- Arco Design Vue
- Pinia
- Vite

## 快速开始

### 环境要求
- JDK 8+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 配置信息
- 数据库: localhost:3306/trial_workshop
- 后端端口: 8088
- 前端端口: 3000
- Context Path: /api

### 启动步骤

1. **创建数据库**
```bash
# 运行数据库初始化脚本
setup-database.bat
```

2. **启动后端**
```bash
cd trial-server
mvn spring-boot:run
```

3. **启动前端**
```bash
cd trial-web
npm install
npm run dev
```

4. **访问应用**
```
http://localhost:3000
```

## 文档导航

### 📚 快速入门
- [快速启动指南](docs/QUICK_START_GUIDE.md) - 项目启动和基本使用
- [数据库设置](docs/DATABASE_SETUP.md) - 数据库配置和初始化
- [SQL 脚本](CREATE_AI_TABLES.sql) - AI 相关表结构

### 🔐 验证码功能
- [验证码实现总结](docs/CAPTCHA_IMPLEMENTATION_SUMMARY.md) - 功能实现详情
- [验证码快速测试](docs/CAPTCHA_QUICK_TEST.md) - 测试步骤和场景
- [验证码错误修复](docs/CAPTCHA_FIXES.md) - 常见问题和解决方案

### 🤖 AI 解析功能
- [AI 配置使用](docs/AI_CONFIG_USAGE.md) - AI 服务配置指南
- [未解析文本功能](docs/UNPARSED_TEXT_FEATURE.md) - 快速解析模式说明
- [未解析文本测试](docs/UNPARSED_TEXT_TEST.md) - 测试指南

### 📋 项目总结
- [功能实现总结](docs/IMPLEMENTATION_SUMMARY.md) - 完整功能清单和技术细节

## 核心功能

### 1. 用户认证
- ✅ 用户注册/登录
- ✅ JWT 令牌认证
- ✅ 图形验证码防护
- ✅ Session 管理

### 2. 题库管理
- ✅ 题目增删改查
- ✅ 分类和标签管理
- ✅ 批量操作（分类、标签、删除）
- ✅ 题目搜索和筛选

### 3. 智能导入
- ✅ 文件上传（Word/PDF/TXT）
- ✅ 文本粘贴解析
- ✅ 快速解析（正则表达式）
- ✅ AI 智能解析（OpenAI/通义千问）
- ✅ 未解析文本标红提示

### 4. 考试功能
- ✅ 智能组卷
- ✅ 在线考试
- ✅ 自动评分
- ✅ 错题本

### 5. 数据统计
- ✅ 题库统计
- ✅ 考试记录
- ✅ AI 使用统计
- ✅ 成本追踪

## 项目结构

```
trial-workshop/
├── trial-server/              # 后端服务
│   ├── src/main/java/
│   │   └── com/trial/server/
│   │       ├── common/        # 工具类
│   │       ├── config/        # 配置类
│   │       ├── controller/    # 控制器
│   │       ├── dto/           # 数据传输对象
│   │       ├── entity/        # 实体类
│   │       ├── mapper/        # MyBatis Mapper
│   │       └── service/       # 业务逻辑
│   └── src/main/resources/
│       └── application.yml    # 配置文件
├── trial-web/                 # 前端应用
│   ├── src/
│   │   ├── api/              # API 接口
│   │   ├── components/       # 组件
│   │   ├── router/           # 路由
│   │   ├── store/            # 状态管理
│   │   └── views/            # 页面
│   └── vite.config.ts        # Vite 配置
├── docs/                      # 文档目录
└── README.md                  # 项目说明
```

## 开发指南

### 后端开发
```bash
cd trial-server
mvn clean install
mvn spring-boot:run
```

### 前端开发
```bash
cd trial-web
npm install
npm run dev
```

### 构建部署
```bash
# 后端打包
cd trial-server
mvn clean package

# 前端打包
cd trial-web
npm run build
```

## API 文档
启动后端后访问: http://localhost:8088/api/doc.html

## 常见问题

### 1. 验证码不显示
- 检查后端是否启动
- 检查 `/captcha` 接口是否在白名单中
- 查看浏览器控制台错误

### 2. AI 解析失败
- 检查 AI 配置是否正确
- 确认 API Key 有效
- 查看剩余额度

### 3. 数据库连接失败
- 确认 MySQL 服务已启动
- 检查数据库配置
- 验证用户名密码

