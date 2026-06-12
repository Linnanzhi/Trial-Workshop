# 📊 数据库设置指南

详细的数据库配置和初始化说明

## 📋 目录

- [数据库要求](#数据库要求)
- [安装 MySQL](#安装-mysql)
- [数据库初始化](#数据库初始化)
- [配置应用连接](#配置应用连接)
- [验证数据库](#验证数据库)
- [常见问题](#常见问题)

---

## 数据库要求

- **MySQL 8.0+** 或 **MariaDB 10.5+**
- **字符集：utf8mb4**（支持 emoji 和多语言）
- **排序规则：utf8mb4_unicode_ci**
- **最低 1 GB 可用空间**

---

## 安装 MySQL

### Windows

1. **下载 MySQL**
   - 访问 https://dev.mysql.com/downloads/mysql/
   - 选择 "Windows (x86, 64-bit), ZIP Archive" 版本
   - 或使用 MySQL 5.7 版本（更稳定）

2. **安装步骤**
   ```bash
   # 解压到目录，如 C:\mysql-8.0
   cd C:\mysql-8.0\bin
   
   # 初始化数据
   mysqld --initialize-insecure --user=mysql
   
   # 安装 Windows 服务
   mysqld --install
   
   # 启动服务
   net start MySQL80
   ```

3. **验证安装**
   ```bash
   mysql -u root
   # 如果进入 mysql> 提示符，说明安装成功
   ```

### macOS

```bash
# 使用 Homebrew 安装
brew install mysql

# 启动服务
brew services start mysql

# 验证安装
mysql -u root
```

### Linux (Ubuntu/Debian)

```bash
# 安装 MySQL
sudo apt-get update
sudo apt-get install mysql-server

# 启动服务
sudo systemctl start mysql

# 验证安装
mysql -u root -p
```

### Docker（推荐，最简单）

```bash
# 拉取 MySQL 镜像
docker pull mysql:8.0

# 运行容器
docker run --name trial_mysql \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -p 3306:3306 \
  -d mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci

# 验证容器运行
docker ps
```

---

## 数据库初始化

### 方式一：使用 SQL 脚本初始化（推荐）

#### 步骤 1：找到初始化脚本

```
trial-server/src/main/resources/db/init.sql
```

#### 步骤 2：连接 MySQL

```bash
mysql -u root -p
# 输入密码
```

#### 步骤 3：执行初始化脚本

```sql
source /path/to/trial-server/src/main/resources/db/init.sql;

-- 或如果在 Windows 上：
source C:\path\to\trial-server\src\main\resources\db\init.sql;
```

#### 步骤 4：验证初始化

```sql
USE trial_workshop;
SHOW TABLES;
-- 应该显示所有表：
-- t_user, t_category, t_tag, t_question, t_exam, t_error_book, t_ai_config 等
```

### 方式二：使用 MySQL Workbench（GUI 方式）

1. 打开 MySQL Workbench
2. 连接到 MySQL 服务器
3. 打开 SQL 编辑器 (File → Open SQL Script)
4. 选择 `init.sql` 文件
5. 点击闪电图标执行脚本

### 方式三：命令行一行执行

```bash
mysql -u root -p trial_workshop < trial-server/src/main/resources/db/init.sql
```

---

## 数据库架构

### 1. 用户管理表

#### t_user（用户表）
```sql
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,         -- BCrypt 加密
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',        -- USER 或 ADMIN
    status TINYINT DEFAULT 1,               -- 0=禁用, 1=正常
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 2. 题库管理表

#### t_category（分类表）
```sql
CREATE TABLE t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,              -- 支持分类树
    user_id BIGINT NOT NULL,                 -- 用户隔离
    sort INT DEFAULT 0                       -- 排序序号
);
```

#### t_question（题目表）
```sql
CREATE TABLE t_question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type TINYINT NOT NULL,                   -- 1=单选, 2=多选, 3=判断, 4=填空, 5=简答
    stem TEXT NOT NULL,                      -- 题干
    options JSON,                            -- 选项数组：["A","B","C","D"]
    answer TEXT NOT NULL,                    -- 正确答案
    analysis TEXT,                           -- 解析
    difficulty TINYINT DEFAULT 3,            -- 1-5 星难度
    category_id BIGINT,
    user_id BIGINT NOT NULL,
    source VARCHAR(20) DEFAULT 'MANUAL'     -- MANUAL/AI_IMPORT/FILE_IMPORT
);
```

#### t_tag（标签表）
```sql
CREATE TABLE t_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20) DEFAULT '#1890ff',     -- HEX 颜色
    user_id BIGINT NOT NULL
);
```

### 3. 考试管理表

#### t_exam（试卷表）
```sql
CREATE TABLE t_exam (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    total_score INT DEFAULT 100,
    pass_score INT DEFAULT 60,
    duration INT DEFAULT 60,                 -- 考试时长（分钟）
    shuffle_question TINYINT DEFAULT 0,      -- 打乱题序
    shuffle_option TINYINT DEFAULT 0,        -- 打乱选项
    auto_collect_error TINYINT DEFAULT 1,    -- 错题自动收录
    user_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1                 -- 0=草稿, 1=已发布
);
```

#### t_exam_record（考试记录表）
```sql
CREATE TABLE t_exam_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    exam_id BIGINT NOT NULL,
    score INT,
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    status TINYINT DEFAULT 0,                -- 0=考试中, 1=已交卷, 2=已批改
    answers JSON,                            -- 答题详情
    start_time DATETIME,
    end_time DATETIME,
    duration INT DEFAULT 0                   -- 实际用时（秒）
);
```

### 4. 错题本表

#### t_error_book（错题本）
```sql
CREATE TABLE t_error_book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    error_count INT DEFAULT 1,               -- 错误次数
    last_error_time DATETIME,
    next_review_time DATETIME,               -- 艾宾浩斯复习时间
    review_stage INT DEFAULT 0,              -- 复习阶段
    mastered TINYINT DEFAULT 0               -- 是否已掌握
);
```

### 5. AI 配置表

#### t_ai_config（AI 配置表）
```sql
CREATE TABLE t_ai_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,           -- openai/qianwen/deepseek
    api_key VARCHAR(500) NOT NULL,           -- AES-128 加密存储
    model VARCHAR(100),                      -- gpt-4o-mini / qwen-max
    base_url VARCHAR(255),                   -- 自定义 API 地址
    max_tokens INT DEFAULT 4000,
    temperature DECIMAL(3,2) DEFAULT 0.3,    -- 0.0-1.0
    enabled TINYINT DEFAULT 1
);
```

#### t_ai_parse_log（解析日志表）
```sql
CREATE TABLE t_ai_parse_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100),
    input_text TEXT,                         -- 截取前 1000 字符
    questions_count INT,                     -- 解析出的题目数
    tokens_used INT,                         -- 消耗的 token
    cost DECIMAL(10,6),                      -- 成本（美元）
    parse_time INT,                          -- 耗时（毫秒）
    success TINYINT,                         -- 0=失败, 1=成功
    error_message TEXT
);
```

---

## 配置应用连接

### 编辑 application.yml

打开 `trial-server/src/main/resources/application.yml`：

```yaml
server:
  port: 8088
  servlet:
    context-path: /api

spring:
  application:
    name: trial-server
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/trial_workshop?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的 MySQL 密码
  
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

### 连接字符串详解

```
jdbc:mysql://localhost:3306/trial_workshop?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

| 参数 | 说明 |
|------|------|
| `localhost:3306` | MySQL 服务器地址和端口 |
| `trial_workshop` | 数据库名称 |
| `useUnicode=true` | 使用 Unicode 编码 |
| `characterEncoding=utf-8` | 字符编码（必须与数据库一致） |
| `useSSL=false` | 不使用 SSL 连接 |
| `serverTimezone=Asia/Shanghai` | 时区设置 |
| `allowPublicKeyRetrieval=true` | 允许公钥检索 |

---

## 验证数据库

### 1. 检查数据库和表

```bash
mysql -u root -p

# 列出所有数据库
SHOW DATABASES;

# 选择数据库
USE trial_workshop;

# 显示所有表
SHOW TABLES;

# 查看表结构
DESC t_user;
```

### 2. 验证初始数据

```sql
-- 查看初始管理员账户
SELECT id, username, nickname, role FROM t_user;
```

应该返回：
```
+----+----------+--------+-------+
| id | username | nickname | role |
+----+----------+--------+-------+
|  1 | admin    | 管理员   | ADMIN |
+----+----------+--------+-------+
```

### 3. 测试应用连接

启动后端，查看日志：

```
Starting TrialServerApplication...
Successfully acquired a new connection...
```

如果看到这些消息，说明数据库连接成功。

---

## 常见问题

### ❌ "Unknown database 'trial_workshop'"

**原因：** 数据库不存在

**解决方案：**
```sql
CREATE DATABASE IF NOT EXISTS trial_workshop 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

### ❌ "Character set 'utf8' is not supported"

**原因：** MySQL 8.0 不支持 `utf8`，需要用 `utf8mb4`

**解决方案：**
```sql
ALTER DATABASE trial_workshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### ❌ "Access denied for user 'root'@'localhost'"

**原因：** 密码错误或用户不存在

**解决方案：**
```bash
# 重置 root 密码
mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

### ❌ "The server time zone value 'UTC' is unrecognized"

**原因：** MySQL 时区配置问题

**解决方案：**
- 在 JDBC URL 中添加 `&serverTimezone=Asia/Shanghai`
- 或在 MySQL 配置中设置全局时区

### ❌ "Table 'trial_workshop.t_user' doesn't exist"

**原因：** 初始化脚本未执行

**解决方案：**
重新执行初始化脚本：
```bash
mysql -u root -p trial_workshop < init.sql
```

---

## 备份和恢复

### 备份数据库

```bash
mysqldump -u root -p trial_workshop > backup.sql
```

### 恢复数据库

```bash
mysql -u root -p trial_workshop < backup.sql
```

---

## 性能优化建议

1. **创建索引** - 已在 init.sql 中创建
2. **定期备份** - 建议每周备份一次
3. **清理日志** - 定期清理过期的解析日志
4. **监控连接** - 监控数据库连接数

---

**✅ 数据库配置完成！现在你可以启动应用了。**

参考 [快速启动指南](QUICK_START_GUIDE.md) 继续。
