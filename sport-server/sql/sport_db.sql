-- ============================================
-- SportZone 体育商城 建表SQL完整版
-- 版本：V1.0
-- 日期：2026-05-18
-- 说明：共11张核心业务表
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------
-- 【表1】sys_user（用户表）
-- 说明：存储用户基本信息，包含会员等级和积分余额
-- ------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号（唯一）',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `user_level` TINYINT DEFAULT 1 COMMENT '会员等级（1-5：1普通/2银卡/3金卡/4钻石/5黑金）',
  `points_balance` BIGINT DEFAULT 0 COMMENT '积分余额',
  `total_consumption` DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计消费金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记（0正常/1已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------
-- 【表2】sys_user_address（用户地址表）
-- 说明：存储用户的收货地址信息，支持多地址管理
-- ------------------------------
DROP TABLE IF EXISTS `sys_user_address`;
CREATE TABLE `sys_user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联sys_user）',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址（0否/1是）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- ------------------------------
-- 【表3】sys_category（商品分类表）
-- 说明：存储商品分类信息，按运动类型分类
-- ------------------------------
DROP TABLE IF EXISTS `sys_category`;
CREATE TABLE `sys_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称（如：篮球、羽毛球）',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '分类图片URL',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '分类图标',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号（数值越小越靠前）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ------------------------------
-- 【表4】sys_product（商品表）
-- 说明：存储商品基本信息，包含价格、库存、图文详情
-- ------------------------------
DROP TABLE IF EXISTS `sys_product`;
CREATE TABLE `sys_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` BIGINT NOT NULL COMMENT '分类ID（关联sys_category）',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `subtitle` VARCHAR(255) DEFAULT NULL COMMENT '商品副标题/简述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价（划线价）',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `sales_count` INT DEFAULT 0 COMMENT '销量',
  `rating_score` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分（1.0-5.0）',
  `badge` VARCHAR(20) DEFAULT NULL COMMENT '标签（如：热卖/新品/精品/推荐）',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '主图URL',
  `images` JSON DEFAULT NULL COMMENT '图片列表（JSON数组格式）',
  `specs` JSON DEFAULT NULL COMMENT '规格选项列表（JSON数组格式）',
  `detail` TEXT DEFAULT NULL COMMENT '商品详情（HTML图文）',
  `is_on` TINYINT DEFAULT 1 COMMENT '上架状态：1=上架，0=下架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_name` (`name`),
  KEY `idx_sales_count` (`sales_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ------------------------------
-- 【表5】sys_cart（购物车表）
-- 说明：存储用户购物车中的商品记录
-- ------------------------------
DROP TABLE IF EXISTS `sys_cart`;
CREATE TABLE `sys_cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联sys_user）',
  `product_id` BIGINT NOT NULL COMMENT '商品ID（关联sys_product）',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `spec` VARCHAR(100) DEFAULT NULL COMMENT '选中的规格选项（如：红色/42码）',
  `checked` TINYINT DEFAULT 1 COMMENT '是否选中（0未选/1已选），用于结算计算',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入购物车时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ------------------------------
-- 【表6】sys_order（订单表）
-- 说明：存储用户订单主表信息
-- ------------------------------
DROP TABLE IF EXISTS `sys_order`;
CREATE TABLE `sys_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（全局唯一）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联sys_user）',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额（原价合计）',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额（优惠券/活动减免）',
  `points_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '积分抵扣金额',
  `level_discount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '会员等级折扣金额',
  `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额（最终支付）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态（0待付款/1待发货/2运输中/3待收货/4待评价/5已完成/6已取消）',
  `address_id` BIGINT DEFAULT NULL COMMENT '收货地址ID（关联sys_user_address）',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
  `express_company` VARCHAR(50) DEFAULT NULL COMMENT '快递公司名称',
  `express_no` VARCHAR(50) DEFAULT NULL COMMENT '快递单号',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `sign_time` DATETIME DEFAULT NULL COMMENT '签收时间',
  `refund_status` TINYINT DEFAULT 0 COMMENT '退款状态（0无退款/1申请中/2已退款/3已拒绝）',
  `refund_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID（后台管理员ID）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ------------------------------
-- 【表7】sys_order_item（订单商品表）
-- 说明：存储订单中包含的商品明细，支持订单完成后评价
-- ------------------------------
DROP TABLE IF EXISTS `sys_order_item`;
CREATE TABLE `sys_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '订单ID（关联sys_order）',
  `product_id` BIGINT NOT NULL COMMENT '商品ID（关联sys_product）',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称（快照，冗余存储）',
  `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片（快照）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '购买单价（快照）',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `spec` VARCHAR(100) DEFAULT NULL COMMENT '规格选项（快照）',
  `points_earned` BIGINT DEFAULT 0 COMMENT '获得积分数量',
  `rating` TINYINT DEFAULT NULL COMMENT '用户评分（1-5星）',
  `review_content` TEXT DEFAULT NULL COMMENT '评价内容',
  `review_images` JSON DEFAULT NULL COMMENT '评价图片列表（JSON数组）',
  `review_time` DATETIME DEFAULT NULL COMMENT '评价时间',
  `reply_content` TEXT DEFAULT NULL COMMENT '商家回复内容',
  `reply_time` DATETIME DEFAULT NULL COMMENT '商家回复时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品表';

-- ------------------------------
-- 【表8】sys_points_record（积分记录表）
-- 说明：记录用户积分的每一笔收入和支出流水
-- ------------------------------
DROP TABLE IF EXISTS `sys_points_record`;
CREATE TABLE `sys_points_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联sys_user）',
  `type` TINYINT NOT NULL COMMENT '类型（1收入/2支出）',
  `points` BIGINT NOT NULL COMMENT '积分数量（正数）',
  `source` VARCHAR(50) NOT NULL COMMENT '来源渠道（order订单奖励/signin签到/exchange兑换/refund退款返还）',
  `reference_no` VARCHAR(50) DEFAULT NULL COMMENT '关联单号（订单号/兑换单号等）',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- ------------------------------
-- 【表9】sys_points_gift（积分商品表）
-- 说明：存储可用积分兑换的商品
-- ------------------------------
DROP TABLE IF EXISTS `sys_points_gift`;
CREATE TABLE `sys_points_gift` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '积分商品名称',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片',
  `points_price` INT NOT NULL COMMENT '所需兑换积分',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '商品描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_points_price` (`points_price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品表';

-- ------------------------------
-- 【表10】sys_points_exchange_order（积分兑换订单表）
-- 说明：记录用户积分兑换商品的订单信息
-- ------------------------------
DROP TABLE IF EXISTS `sys_points_exchange_order`;
CREATE TABLE `sys_points_exchange_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` VARCHAR(32) NOT NULL COMMENT '兑换单号（全局唯一）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联sys_user）',
  `gift_id` BIGINT NOT NULL COMMENT '积分商品ID（关联sys_points_gift）',
  `gift_name` VARCHAR(100) NOT NULL COMMENT '商品名称（快照）',
  `gift_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片（快照）',
  `points_price` INT NOT NULL COMMENT '消耗积分',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待发货/1已发货/2已完成',
  `address_id` BIGINT DEFAULT NULL COMMENT '收货地址ID（关联sys_user_address）',
  `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名（快照）',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话（快照）',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份（快照）',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市（快照）',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县（快照）',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址（快照）',
  `logistics_company` VARCHAR(50) DEFAULT NULL COMMENT '物流公司',
  `logistics_no` VARCHAR(50) DEFAULT NULL COMMENT '物流单号',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换订单表';

-- ------------------------------
-- 【表11】sys_coupon（优惠券表）
-- 说明：存储平台发行的优惠券模板
-- ------------------------------
DROP TABLE IF EXISTS `sys_coupon`;
CREATE TABLE `sys_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '优惠券名称（如：满100减20）',
  `type` TINYINT NOT NULL COMMENT '类型（1满减券/2折扣券）',
  `value` DECIMAL(10,2) NOT NULL COMMENT '优惠值（满减金额或折扣率）',
  `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '使用门槛（满X元可用）',
  `points_cost` INT DEFAULT 0 COMMENT '领取所需积分（0表示免费领）',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量（0表示已领完）',
  `start_time` DATETIME NOT NULL COMMENT '生效开始时间',
  `end_time` DATETIME NOT NULL COMMENT '有效期截止时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- ------------------------------
-- 【表12】sys_user_coupon（用户优惠券表）
-- 说明：记录用户已领取的优惠券及使用状态
-- ------------------------------
DROP TABLE IF EXISTS `sys_user_coupon`;
CREATE TABLE `sys_user_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID（关联sys_user）',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID（关联sys_coupon）',
  `status` TINYINT DEFAULT 0 COMMENT '使用状态（0未使用/1已使用/2已过期）',
  `order_id` BIGINT DEFAULT NULL COMMENT '使用订单ID（关联sys_order，已使用时填写）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ----------------------------
-- 【表13】sys_admin（管理员表）
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(50) NOT NULL COMMENT '管理员账号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用/1启用)',
  `role` TINYINT DEFAULT 2 COMMENT '角色(1=超级管理员/2=普通管理员)',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ----------------------------
-- 【表14】sys_operate_log（操作日志表）
-- ----------------------------
DROP TABLE IF EXISTS `sys_operate_log`;
CREATE TABLE `sys_operate_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_id`      BIGINT       DEFAULT NULL             COMMENT '操作管理员ID',
  `username`      VARCHAR(50)  DEFAULT NULL             COMMENT '操作管理员用户名（快照）',
  `module`        VARCHAR(50)  NOT NULL                 COMMENT '业务模块',
  `type`          VARCHAR(50)  NOT NULL                 COMMENT '操作类型',
  `description`   VARCHAR(255) DEFAULT NULL             COMMENT '操作描述',
  `method`        VARCHAR(10)  DEFAULT NULL             COMMENT 'HTTP方法',
  `url`           VARCHAR(255) DEFAULT NULL             COMMENT '请求URL',
  `params`        TEXT         DEFAULT NULL             COMMENT '请求参数',
  `result`        VARCHAR(20)  DEFAULT NULL             COMMENT '操作结果（success/fail）',
  `error_msg`     TEXT         DEFAULT NULL             COMMENT '错误信息',
  `ip`            VARCHAR(50)  DEFAULT NULL             COMMENT '操作IP',
  `cost_time`     BIGINT       DEFAULT NULL             COMMENT '耗时（毫秒）',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id`  (`admin_id`),
  KEY `idx_module`    (`module`),
  KEY `idx_type`      (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 【表15】sys_statistics_daily（每日统计表）
-- ----------------------------
DROP TABLE IF EXISTS `sys_statistics_daily`;
CREATE TABLE `sys_statistics_daily` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `new_users` INT DEFAULT 0 COMMENT '新增用户数',
  `active_users` INT DEFAULT 0 COMMENT '活跃用户数',
  `new_orders` INT DEFAULT 0 COMMENT '新增订单数',
  `order_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '订单总金额',
  `pay_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '支付金额',
  `refund_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日统计表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 建表完成，共计 15 张表
-- 建议执行顺序：按表序号依次执行（1→15）
-- ============================================