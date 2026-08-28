# SportZone - 体育器材购物商城

一个体育器材在线购物平台，包含用户端、管理后台和后端API服务。

## 项目结构

```
sport-project/
├── sport-admin/          # 管理后台前端
├── sport-user/           # 用户端前端
└── sport-server/         # 后端API服务
```

## 技术栈

### 后端 (sport-server)
- **框架**: Spring Boot 3.2.0
- **语言**: Java 21
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL
- **缓存**: Redis
- **认证**: JWT (auth0/java-jwt 4.4.0)
- **文件存储**: 阿里云OSS
- **API文档**: SpringDoc (Swagger)
- **工具**: Hutool 5.8.25, Lombok

### 前端 (sport-admin / sport-user)
- **框架**: Vue 3.5.32
- **构建工具**: Vite 8.0.8
- **UI组件库**: Element Plus 2.14.0
- **状态管理**: Pinia 3.0.4
- **路由**: Vue Router 5.0.4
- **HTTP客户端**: Axios
- **样式**: SCSS

## 功能特性

### 用户端
- 商品浏览与搜索
- 购物车管理
- 订单下单与支付
- 收货地址管理
- 优惠券领取与使用
- 积分系统（积分商品兑换）
- 用户中心（个人信息、订单管理）

### 管理后台
- 管理员登录与权限管理
- 商品管理（增删改查）
- 分类管理
- 订单管理（发货、退款处理）
- 用户管理
- 优惠券管理
- 积分商品管理
- 评价管理
- 数据仪表盘（统计图表）

## 快速开始

### 环境要求
- Node.js: ^20.19.0 或 >=22.12.0
- Java: 21
- MySQL: 3306
- Redis: 6379

### 数据库初始化
```bash
# 导入数据库结构
mysql -u root -p < sport-server/sql/sport_db.sql

# 导入种子数据
mysql -u root -p sport_db < sport-server/sql/seed_data.sql
```

### 启动后端服务
```bash
cd sport-server

# 使用Maven Wrapper
./mvnw spring-boot:run

# 或打包后运行
./mvnw package -DskipTests
java -jar target/sportzone-server-0.0.1-SNAPSHOT.jar
```

### 启动用户端前端
```bash
cd sport-user
npm install
npm run dev
# 访问 http://localhost:9090
```

### 启动管理后台前端
```bash
cd sport-admin
npm install
npm run dev
# 访问 http://localhost:9091
```

## 默认账户

### 管理员账户
- 用户名: admin
- 密码: admin123

### 测试用户账户
- 用户名: testuser
- 密码: 123456

## 开发说明

### 后端API基础URL
- 用户端API: `http://localhost:8080/api/user/`
- 管理端API: `http://localhost:8080/api/admin/`

### 认证方式
请求头携带Token: `Authorization: Bearer <token>`

### 项目特点
- 单设备登录控制（同一账号只能在一个设备登录）
- 定时任务（自动确认收货、每日数据统计）
- 文件上传使用阿里云OSS
- 完善的异常处理和参数校验

## 部署说明

### 后端配置
修改 `sport-server/src/main/resources/application.yml` 中的以下配置：
- 数据库连接信息
- Redis连接信息
- 阿里云OSS配置

### 前端代理配置
前端Vite开发服务器已配置代理，生产部署需配置Nginx等反向代理。

## 许可证

本项目仅供学习交流使用。

---

如有问题，请提交Issue或联系项目维护者。
