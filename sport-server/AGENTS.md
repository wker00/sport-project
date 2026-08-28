# AGENTS.md

## 项目概况

- **Spring Boot 3.2.0** + **Java 21** 单模块后端，体育器材商城 API 服务
- 包前缀 `com.sportzone`，启动类 `SportzoneServerUserApplication`（含 `@EnableScheduling`）
- 核心依赖：MyBatis-Plus 3.5.5、MySQL、Redis、auth0/java-jwt 4.4.0（**仅此 JWT 库在用**，pom 中的 jjwt 已引入但未实际使用，`JwtUtils.java` 中有冗余 import，建议清理）、SpringDoc 2.3.0、BCrypt、Hutool 5.8.25、阿里云 OSS 3.18.4、Lombok、spring-boot-starter-aop
- 前端项目：`vue-project-admin/`（管理端，端口 9091）和 `vue-project-user/`（用户端，端口 9090）
- Swagger **已禁用**（`application.yml` 中 `springdoc.api-docs.enabled: false` 和 `springdoc.swagger-ui.enabled: false`）
- CORS 全开：`addAllowedOriginPattern("*")` + `allowCredentials(true)`

## 环境要求

- MySQL 3306，数据库 `sport_db`，用户 `root`/`123456`
- Redis 6379，database 0
- 建表脚本：`sql/sport_db.sql`（14 张表），种子数据：`sql/seed_data.sql`

## 开发命令

```bash
# 本地运行（需要 MySQL + Redis 运行中）
./mvnw spring-boot:run

# 打包
./mvnw package -DskipTests

# 运行单测（需要 MySQL + Redis）
./mvnw test -Dtest=类名
```

**已知构建问题**：本地 JDK 版本可能高于 pom.xml 中的 `java.version`(21)。若编译失败，需在 `pom.xml` 中更新 `maven-compiler-plugin` 的 Lombok 注解处理器版本为本地可用的最新版（如 1.18.46）。

## 认证机制

请求头 `Authorization: Bearer <token>`，两套独立的拦截器：

| 拦截器 | 路径 | 放行白名单 | Redis key 校验 |
|---|---|---|---|
| `UserLoginInterceptor` | `/api/**`（排除 `/api/admin/**`） | `/api/user/register`, `/api/user/login`, `/api/user/product/**`, `/api/user/points/gifts`, `/api/user/coupon/list` | `user_token{id}` |
| `AdminLoginInterceptor` | `/api/admin/**` | `/api/admin/login` | `admin_token{id}` |

- 拦截器自动补全缺失的 `Bearer ` 前缀；退出登录删除 Redis 中的 token
- **单设备登录**：拦截器比较请求 token 与 Redis 存储的 token 是否一致，不一致则返回 401 "账号已在其他设备登录"
- 通过 `ThreadLocalUtil.getUserId()` 获取当前用户/管理员 ID（两套拦截器共用）
- 管理端 API 仅校验登录状态（JWT + Redis），不设细粒度权限控制

## 通用响应

`Result<T>`：`code=200` 成功 / `code=500` 失败
全局异常（`common/GlobalExceptionHandler`）统一处理 `RuntimeException` + 参数校验异常，controller 层无需 try-catch

## MyBatis-Plus 惯例

- 全部注解映射（虽然 `application.yml` 中配置了 `mapper-locations`，但项目中不存在任何 Mapper XML 文件，全靠注解映射）
- 下划线→驼峰自动映射，主键自增
- 逻辑删除：列名 `is_deleted`，删除值 1，正常值 0（全局配置）
- 自动填充：`createTime` / `updateTime`（由 `MybatisPlusConfig implements MetaObjectHandler` 实现）
- 已配置 `PaginationInnerInterceptor(DbType.MYSQL)`
- 分页查询返回 `common/dto/PageResult<T>`
- User 实体含 `totalConsumption`（`DECIMAL(12,2)`，累计消费金额缓存）：确认收货时自动累加，退款通过时自动扣减，会员升级时同步刷新

## 模块边界

```
com.sportzone/
├── common/          # 全局：GlobalExceptionHandler、PageResult、定时任务
│   ├── schedule/    # OrderScheduleTask（每10分钟自动确认收货）、StatisticsScheduleTask（每日00:05统计）
│   ├── dto/         # PageResult<T>
│   └── enums/       # UserLevelEnum（会员等级折扣）
├── config/          # CorsConfig、MybatisPlusConfig、RedisConfig、SwaggerConfig、WebMvcConfig
├── interceptors/    # UserLoginInterceptor、AdminLoginInterceptor
├── utils/           # JwtUtils、PasswordEncoder、Result、ThreadLocalUtil
├── user/            # 用户端（只读商品浏览 + 用户自身操作）
│   ├── controller/  # 7 个：User, Product(只读), Order, Cart, Address, Coupon, Points
│   ├── entity/      # 12 个实体（User 含 totalConsumption 累计消费、Product、Order、Cart 等）
│   ├── mapper/      # 12 个 Mapper
│   ├── service/     # 9 个 Service + 9 个 impl
│   ├── dto/         # 15 个 DTO
│   └── vo/          # 17 个 VO
└── admin/           # 管理端（完整 CRUD）
    ├── controller/  # 9 个：AdminAuth, AdminCategory, AdminCoupon, AdminPointsGift, AdminProduct, AdminOrder, AdminUser, AdminDashboard, AdminReview
    ├── entity/      # Admin、StatisticsDaily
    ├── mapper/      # AdminMapper、StatisticsDailyMapper
    ├── service/impl/# AdminServiceImpl（含全部管理业务，单文件）
    ├── dto/         # 13 个 DTO
    └── vo/          # AdminVO、AdminReviewVO、DashboardVO
```

**关键路由边界**：
- `/api/user/product/**` — 用户端，**仅支持 GET 只读**（CRUD 已迁移到 `/api/admin/product/**`）
- `/api/user/**`（非 `/api/admin/**`） — 需用户登录（`UserLoginInterceptor` 校验 JWT + Redis，白名单除外）
- `/api/admin/**` — 需管理员登录（`AdminLoginInterceptor` 校验 JWT + Redis）

## 管理端 API 速查

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/admin/login` | 管理员登录 |
| `POST` | `/api/admin/logout` | 退出登录 |
| `POST` | `/api/admin/register` | 创建管理员（仅超级管理员） |
| `GET` | `/api/admin/info` | 当前管理员信息 |
| `PUT` | `/api/admin/info` | 更新管理员信息 |
| `PUT` | `/api/admin/{id}/info` | 超级管理员更新其他管理员信息 |
| `PUT` | `/api/admin/password` | 修改密码（清除 Redis token，需重新登录） |
| `GET` | `/api/admin/list` | 管理员列表 |
| `DELETE` | `/api/admin/{id}` | 删除管理员（仅超级管理员） |
| `PUT` | `/api/admin/{id}/enable` | 启用管理员（仅超级管理员） |
| `PUT` | `/api/admin/{id}/disable` | 禁用管理员，清除 Redis token（仅超级管理员） |
| `PUT` | `/api/admin/{id}/password` | 重置管理员密码（仅超级管理员） |
| `POST` | `/api/admin/avatar` | 上传头像（自动删除旧头像） |
| `GET` | `/api/admin/category/list` | 分类列表 |
| `GET` | `/api/admin/category/{id}` | 分类详情 |
| `POST` | `/api/admin/category` | 创建分类 |
| `PUT` | `/api/admin/category/{id}` | 更新分类 |
| `DELETE` | `/api/admin/category/{id}` | 删除分类 |
| `GET` | `/api/admin/points/gifts/list` | 积分商品列表 |
| `GET` | `/api/admin/points/gifts/{id}` | 积分商品详情 |
| `POST` | `/api/admin/points/gifts` | 创建积分商品（JSON / multipart） |
| `PUT` | `/api/admin/points/gifts/{id}` | 更新积分商品（JSON / multipart） |
| `DELETE` | `/api/admin/points/gifts/{id}` | 删除积分商品 |
| `GET` | `/api/admin/points/gifts/orders` | 兑换记录列表 |
| `POST` | `/api/admin/product` | 创建商品（JSON / multipart） |
| `PUT` | `/api/admin/product/{id}` | 更新商品（JSON / multipart） |
| `DELETE` | `/api/admin/product/{id}` | 删除商品 |
| `GET` | `/api/admin/product/{id}` | 商品详情 |
| `GET` | `/api/admin/product/list` | 商品列表（分页） |
| `GET` | `/api/admin/product/category/{categoryId}` | 按分类获取商品 |
| `GET` | `/api/admin/product/search` | 搜索商品 |
| `GET` | `/api/admin/order/list` | 订单列表（含 `items` 和 `user`） |
| `GET` | `/api/admin/order/{id}` | 订单详情（含 `items` 和 `user`） |
| `PUT` | `/api/admin/order/{id}/deliver` | 确认送达（运输中→待收货） |
| `PUT` | `/api/admin/order/ship` | 发货 |
| `PUT` | `/api/admin/order/refund` | 处理退款 |
| `GET` | `/api/admin/user/list` | 用户列表 |
| `GET` | `/api/admin/user/{id}` | 用户详情 |
| `PUT` | `/api/admin/user/{id}/password` | 重置用户密码（仅超级管理员） |
| `GET` | `/api/admin/coupon/list` | 优惠券模板列表 |
| `GET` | `/api/admin/coupon/{id}` | 优惠券模板详情 |
| `POST` | `/api/admin/coupon` | 创建优惠券 |
| `PUT` | `/api/admin/coupon/{id}` | 更新优惠券 |
| `DELETE` | `/api/admin/coupon/{id}` | 删除优惠券 |
| `GET` | `/api/admin/coupon/{id}/records` | 优惠券发放记录 |
| `GET` | `/api/admin/dashboard` | 仪表盘数据 |
| `GET` | `/api/admin/statistics/daily` | 每日统计数据 |
| `GET` | `/api/admin/review/list` | 评价列表 |
| `GET` | `/api/admin/review/{id}` | 评价详情 |
| `PUT` | `/api/admin/review/{id}/reply` | 回复评价 |
| `DELETE` | `/api/admin/review/{id}` | 删除评价 |

## 定时任务

| 类 | 调度 | 功能 |
|---|---|---|
| `OrderScheduleTask` | `fixedRate = 600000`（10 分钟） | 发货超 2 小时自动确认收货（状态 2→3） |
| `StatisticsScheduleTask` | `cron = 0 5 0 * * ?`（每日 00:05） | 汇总昨日新用户、活跃用户、订单量、金额至 `statistics_daily` 表 |

## OSS 文件上传

- `OssService.uploadAvatar(file, userId)` — 上传，返回 URL
- `OssService.deleteFile(fileUrl)` — 从 URL 解析 key 后删除 OSS 文件
- 上传新头像时自动删除旧头像（用户端和管理端均已实现）
- 凭证在 `application.yml`，部署需替换。建议使用环境变量注入（`${VAR:default}`）
- 限制：10MB

## 测试

- 仅 1 个测试文件 `SportzoneServerUserApplicationTests`（Spring 上下文加载测试），无业务逻辑单测

## 交互要求

1. 你在处理所有问题时，**全程思考过程必须使用中文**（包括需求分析、逻辑拆解、方案选择、步骤推导等所有内部推理环节）;
2. 最终输出的所有回答内容（包括文字解释、代码注释、步骤说明等）必须全部使用中文，仅代码语法本身的英文关键词除外。
