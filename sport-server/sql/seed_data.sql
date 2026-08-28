-- ============================================
-- SportZone 体育商城 商品模块模拟数据
-- 版本：V1.0
-- 日期：2026-05-21
-- 说明：在 sport_db.sql 建表后执行
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------
-- 1. sys_category（商品分类）
-- ------------------------------
INSERT INTO `sys_category` (`id`, `name`, `image`, `icon`, `description`, `sort_order`) VALUES
(1,  '篮球',     'https://sportzone-oss.oss-cn-shenzhen.aliyuncs.com/category/basketball.png',  'basketball',   '篮球鞋、球衣、护具等篮球装备',              1),
(2,  '羽毛球',   'https://sportzone-oss.oss-cn-shenzhen.aliyuncs.com/category/badminton.png',  'badminton',    '羽毛球拍、球鞋、羽球等羽球装备',             2),
(4,  '乒乓球',   'https://sportzone-oss.oss-cn-shenzhen.aliyuncs.com/category/table-tennis.png','table-tennis', '乒乓球拍、胶皮、乒乓球等装备',               4);

-- ------------------------------
-- 2. sys_product（商品）
-- ------------------------------

-- 篮球分类（category_id=1）
INSERT INTO `sys_product` (`id`, `category_id`, `name`, `subtitle`, `price`, `original_price`, `stock`, `sales_count`, `rating_score`, `badge`, `image`, `images`, `specs`, `detail`) VALUES
(1, 1, '斯伯丁经典篮球 7号标准球', 'NBA官方比赛用球，PU材质', 299.00, 399.00, 500, 3200, 4.8, '热卖',
 'https://images.sportzone.com/products/basketball-1.jpg',
 '["https://images.sportzone.com/products/basketball-1-1.jpg","https://images.sportzone.com/products/basketball-1-2.jpg"]',
 '{"specGroups":[{"name":"尺寸","options":["7号标准","6号女款","5号儿童"]},{"name":"颜色","options":["棕色","橙色"]}],"specItems":[{"specs":"7号标准 / 棕色","price":299.00,"originalPrice":399.00,"stock":100},{"specs":"7号标准 / 橙色","price":299.00,"originalPrice":399.00,"stock":100},{"specs":"6号女款 / 棕色","price":279.00,"originalPrice":379.00,"stock":80},{"specs":"6号女款 / 橙色","price":279.00,"originalPrice":379.00,"stock":80},{"specs":"5号儿童 / 棕色","price":249.00,"originalPrice":349.00,"stock":70},{"specs":"5号儿童 / 橙色","price":249.00,"originalPrice":349.00,"stock":70}]}',
 '斯伯丁经典篮球，采用优质PU材质，耐磨防滑，适合室内外场地。'),
(2, 1, '李宁篮球鞋 闪击10', '轻质透气，缓震回弹', 599.00, 799.00, 300, 1800, 4.7, '推荐',
 'https://images.sportzone.com/products/basketball-shoe-1.jpg',
 '["https://images.sportzone.com/products/basketball-shoe-1-1.jpg","https://images.sportzone.com/products/basketball-shoe-1-2.jpg"]',
 '{"specGroups":[{"name":"尺码","options":["39","40","41","42","43","44"]},{"name":"颜色","options":["黑白","蓝白","红黑"]}],"specItems":[{"specs":"39 / 黑白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"39 / 蓝白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"39 / 红黑","price":599.00,"originalPrice":799.00,"stock":16},{"specs":"40 / 黑白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"40 / 蓝白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"40 / 红黑","price":599.00,"originalPrice":799.00,"stock":16},{"specs":"41 / 黑白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"41 / 蓝白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"41 / 红黑","price":599.00,"originalPrice":799.00,"stock":16},{"specs":"42 / 黑白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"42 / 蓝白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"42 / 红黑","price":599.00,"originalPrice":799.00,"stock":16},{"specs":"43 / 黑白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"43 / 蓝白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"43 / 红黑","price":599.00,"originalPrice":799.00,"stock":16},{"specs":"44 / 黑白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"44 / 蓝白","price":599.00,"originalPrice":799.00,"stock":17},{"specs":"44 / 红黑","price":599.00,"originalPrice":799.00,"stock":16}]}',
 '李宁闪击10代篮球鞋，中底搭载䨻科技，轻盈缓震，快速启动。'),
(3, 1, 'NBA湖人队球衣 詹姆斯#23', '球迷版，速干面料', 399.00, 499.00, 200, 2800, 4.9, '热卖',
 'https://images.sportzone.com/products/jersey-1.jpg',
 '["https://images.sportzone.com/products/jersey-1-1.jpg"]',
 '{"specGroups":[{"name":"尺码","options":["S","M","L","XL","2XL"]}],"specItems":[{"specs":"S","price":399.00,"originalPrice":499.00,"stock":30},{"specs":"M","price":399.00,"originalPrice":499.00,"stock":40},{"specs":"L","price":399.00,"originalPrice":499.00,"stock":60},{"specs":"XL","price":399.00,"originalPrice":499.00,"stock":50},{"specs":"2XL","price":399.00,"originalPrice":499.00,"stock":20}]}',
 'NBA官方授权湖人队球衣，透气速干面料，印有詹姆斯姓名和号码。'),
(4, 1, '李宁篮球护膝 专业运动', '弹簧支撑，防撞缓冲', 129.00, 169.00, 400, 1500, 4.5, '新品',
 'https://images.sportzone.com/products/knee-pad-1.jpg',
 '["https://images.sportzone.com/products/knee-pad-1-1.jpg"]',
 '{"specGroups":[{"name":"尺码","options":["M","L","XL"]},{"name":"颜色","options":["黑色","深灰"]}],"specItems":[{"specs":"M / 黑色","price":129.00,"originalPrice":169.00,"stock":70},{"specs":"M / 深灰","price":129.00,"originalPrice":169.00,"stock":70},{"specs":"L / 黑色","price":129.00,"originalPrice":169.00,"stock":70},{"specs":"L / 深灰","price":129.00,"originalPrice":169.00,"stock":70},{"specs":"XL / 黑色","price":129.00,"originalPrice":169.00,"stock":60},{"specs":"XL / 深灰","price":129.00,"originalPrice":169.00,"stock":60}]}',
 '李宁专业篮球护膝，内置弹簧支撑，EVA防撞缓冲垫，有效保护膝盖。'),
(5, 1, '威尔胜篮球 街头款', '室外耐磨，手感出色', 169.00, 219.00, 600, 2100, 4.6, '',
 'https://images.sportzone.com/products/basketball-2.jpg',
 '["https://images.sportzone.com/products/basketball-2-1.jpg"]',
 '{"specGroups":[{"name":"尺寸","options":["7号标准","6号女款"]},{"name":"颜色","options":["棕红","蓝橙"]}],"specItems":[{"specs":"7号标准 / 棕红","price":169.00,"originalPrice":219.00,"stock":150},{"specs":"7号标准 / 蓝橙","price":169.00,"originalPrice":219.00,"stock":150},{"specs":"6号女款 / 棕红","price":159.00,"originalPrice":209.00,"stock":150},{"specs":"6号女款 / 蓝橙","price":159.00,"originalPrice":209.00,"stock":150}]}',
 '威尔胜街头篮球，加厚PU表皮，深沟槽设计，室外水泥地耐磨耐用。');

-- 羽毛球分类（category_id=2）
INSERT INTO `sys_product` (`id`, `category_id`, `name`, `subtitle`, `price`, `original_price`, `stock`, `sales_count`, `rating_score`, `badge`, `image`, `images`, `specs`, `detail`) VALUES
(6, 2, '尤尼克斯天斧99 Pro', '进攻型高端羽毛球拍', 1299.00, 1599.00, 150, 1200, 4.9, '精品',
 'https://images.sportzone.com/products/racket-1.jpg',
 '["https://images.sportzone.com/products/racket-1-1.jpg","https://images.sportzone.com/products/racket-1-2.jpg"]',
 '{"specGroups":[{"name":"磅数","options":["24lbs","26lbs","28lbs"]},{"name":"颜色","options":["深红","藏青"]}],"specItems":[{"specs":"24lbs / 深红","price":1299.00,"originalPrice":1599.00,"stock":25},{"specs":"24lbs / 藏青","price":1299.00,"originalPrice":1599.00,"stock":25},{"specs":"26lbs / 深红","price":1399.00,"originalPrice":1699.00,"stock":25},{"specs":"26lbs / 藏青","price":1399.00,"originalPrice":1699.00,"stock":25},{"specs":"28lbs / 深红","price":1499.00,"originalPrice":1799.00,"stock":25},{"specs":"28lbs / 藏青","price":1499.00,"originalPrice":1799.00,"stock":25}]}',
 '尤尼克斯天斧99 Pro，搭载新次元碳素，进攻强劲，更适合高水平选手。'),
(7, 2, '李宁羽毛球鞋 变色龙3', '防滑耐磨，启动迅速', 359.00, 459.00, 400, 2100, 4.6, '热卖',
 'https://images.sportzone.com/products/shoe-badminton-1.jpg',
 '["https://images.sportzone.com/products/shoe-badminton-1-1.jpg"]',
 '{"specGroups":[{"name":"尺码","options":["39","40","41","42","43"]},{"name":"颜色","options":["荧光绿","白色"]}],"specItems":[{"specs":"39 / 荧光绿","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"39 / 白色","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"40 / 荧光绿","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"40 / 白色","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"41 / 荧光绿","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"41 / 白色","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"42 / 荧光绿","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"42 / 白色","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"43 / 荧光绿","price":359.00,"originalPrice":459.00,"stock":40},{"specs":"43 / 白色","price":359.00,"originalPrice":459.00,"stock":40}]}',
 '李宁变色龙3代羽毛球鞋，耐磨橡胶大底，防滑性能优异。'),
(8, 2, '尤尼克斯AS-05 鹅毛球', '比赛级鹅毛球，飞行稳定', 89.00, 109.00, 1000, 5200, 4.8, '热卖',
 'https://images.sportzone.com/products/shuttlecock-1.jpg',
 '["https://images.sportzone.com/products/shuttlecock-1-1.jpg"]',
 '{"specGroups":[{"name":"规格","options":["12只装","6只装"]}],"specItems":[{"specs":"12只装","price":89.00,"originalPrice":109.00,"stock":600},{"specs":"6只装","price":49.00,"originalPrice":69.00,"stock":400}]}',
 '尤尼克斯AS-05比赛级鹅毛球，飞行轨迹稳定，耐打性好。'),
(9, 2, '李宁羽毛球拍包 6支装', '大容量，分隔收纳', 199.00, 259.00, 300, 1100, 4.4, '',
 'https://images.sportzone.com/products/bag-1.jpg',
 '["https://images.sportzone.com/products/bag-1-1.jpg"]',
 '{"specGroups":[{"name":"颜色","options":["黑色","蓝白","红黑"]}],"specItems":[{"specs":"黑色","price":199.00,"originalPrice":259.00,"stock":100},{"specs":"蓝白","price":199.00,"originalPrice":259.00,"stock":100},{"specs":"红黑","price":199.00,"originalPrice":259.00,"stock":100}]}',
 '李宁羽毛球拍包，可收纳6支球拍，独立鞋仓分隔设计，背带舒适。'),
(10, 2, '李宁羽毛球 比赛级鹅毛', '飞行稳定，耐打持久', 79.00, 99.00, 800, 3800, 4.5, '推荐',
 'https://images.sportzone.com/products/shuttlecock-2.jpg',
 '["https://images.sportzone.com/products/shuttlecock-2-1.jpg"]',
 '{"specGroups":[{"name":"规格","options":["12只装","6只装"]}],"specItems":[{"specs":"12只装","price":79.00,"originalPrice":99.00,"stock":500},{"specs":"6只装","price":45.00,"originalPrice":59.00,"stock":300}]}',
 '李宁比赛级鹅毛羽毛球，精选鹅毛片，飞行弧度稳定，耐打性优异。');

-- 乒乓球分类（category_id=4）
INSERT INTO `sys_product` (`id`, `category_id`, `name`, `subtitle`, `price`, `original_price`, `stock`, `sales_count`, `rating_score`, `badge`, `image`, `images`, `specs`, `detail`) VALUES
(11, 4, '红双喜狂飙龙5 乒乓球拍', '马龙同款，芳碳底板', 899.00, 1099.00, 180, 4200, 4.9, '精品',
 'https://images.sportzone.com/products/table-tennis-1.jpg',
 '["https://images.sportzone.com/products/table-tennis-1-1.jpg","https://images.sportzone.com/products/table-tennis-1-2.jpg"]',
 '{"specGroups":[{"name":"拍型","options":["横拍FL","横拍ST","直拍CS"]}],"specItems":[{"specs":"横拍FL","price":899.00,"originalPrice":1099.00,"stock":60},{"specs":"横拍ST","price":899.00,"originalPrice":1099.00,"stock":60},{"specs":"直拍CS","price":899.00,"originalPrice":1099.00,"stock":60}]}',
 '红双喜狂飙龙5，马龙冠军同款，芳碳混编底板，攻守兼备。'),
(12, 4, '红双喜赛顶D40+ 训练球', 'ABS材质，圆度精准', 19.90, 29.90, 2000, 7800, 4.6, '热卖',
 'https://images.sportzone.com/products/ball-tt-1.jpg',
 '["https://images.sportzone.com/products/ball-tt-1-1.jpg"]',
 '{"specGroups":[{"name":"包装","options":["10只装","30只装","100只装"]}],"specItems":[{"specs":"10只装","price":19.90,"originalPrice":29.90,"stock":800},{"specs":"30只装","price":49.90,"originalPrice":69.90,"stock":700},{"specs":"100只装","price":139.00,"originalPrice":189.00,"stock":500}]}',
 '红双喜赛顶D40+三星乒乓球，ABS环保材质，圆度误差≤0.1mm。'),
(13, 4, '蝴蝶泰克西姆 乒乓球拍', '碳素底板，进攻利器', 1299.00, 1599.00, 120, 2800, 4.8, '精品',
 'https://images.sportzone.com/products/table-tennis-2.jpg',
 '["https://images.sportzone.com/products/table-tennis-2-1.jpg","https://images.sportzone.com/products/table-tennis-2-2.jpg"]',
 '{"specGroups":[{"name":"拍型","options":["横拍FL","直拍CS"]}],"specItems":[{"specs":"横拍FL","price":1299.00,"originalPrice":1599.00,"stock":60},{"specs":"直拍CS","price":1299.00,"originalPrice":1599.00,"stock":60}]}',
 '蝴蝶泰克西姆碳素底板，搭载T能源胶皮，弹力充沛，适合快攻结合弧圈打法。'),
(14, 4, '红双喜狂飙3 胶皮', '粘性胶面，旋转强劲', 139.00, 179.00, 500, 5600, 4.7, '热卖',
 'https://images.sportzone.com/products/rubber-1.jpg',
 '["https://images.sportzone.com/products/rubber-1-1.jpg"]',
 '{"specGroups":[{"name":"厚度","options":["2.1mm","2.15mm","2.2mm"]},{"name":"硬度","options":["39度","40度","41度"]}],"specItems":[{"specs":"2.1mm / 39度","price":139.00,"originalPrice":179.00,"stock":55},{"specs":"2.1mm / 40度","price":139.00,"originalPrice":179.00,"stock":60},{"specs":"2.1mm / 41度","price":139.00,"originalPrice":179.00,"stock":55},{"specs":"2.15mm / 39度","price":139.00,"originalPrice":179.00,"stock":55},{"specs":"2.15mm / 40度","price":139.00,"originalPrice":179.00,"stock":60},{"specs":"2.15mm / 41度","price":139.00,"originalPrice":179.00,"stock":55},{"specs":"2.2mm / 39度","price":149.00,"originalPrice":189.00,"stock":50},{"specs":"2.2mm / 40度","price":149.00,"originalPrice":189.00,"stock":55},{"specs":"2.2mm / 41度","price":149.00,"originalPrice":189.00,"stock":55}]}',
 '红双喜狂飙3专业胶皮，粘性胶面设计，摩擦系数高，弧圈球旋转强烈。'),
(15, 4, '红双喜乒乓球网架 折叠式', '便携折叠，适合家用', 69.00, 89.00, 600, 3200, 4.3, '',
 'https://images.sportzone.com/products/net-1.jpg',
 '["https://images.sportzone.com/products/net-1-1.jpg"]',
 '{"specGroups":[{"name":"规格","options":["标准款","加厚款"]}],"specItems":[{"specs":"标准款","price":69.00,"originalPrice":89.00,"stock":300},{"specs":"加厚款","price":89.00,"originalPrice":109.00,"stock":300}]}',
 '红双喜折叠式乒乓球网架，便携收纳，夹扣式安装，适合家庭及训练使用。');

-- ------------------------------
-- 3. sys_points_gift（积分商品）
-- ------------------------------
INSERT INTO `sys_points_gift` (`id`, `name`, `image`, `points_price`, `stock`, `description`) VALUES
(1, '李宁运动毛巾', 'https://images.sportzone.com/gifts/towel-1.jpg', 500, 100, '速干运动毛巾，吸汗透气'),
(2, '运动水杯 500ml', 'https://images.sportzone.com/gifts/bottle-1.jpg', 800, 80, 'Tritan材质运动水杯'),
(3, '李宁运动手环', 'https://images.sportzone.com/gifts/band-1.jpg', 1200, 50, '智能运动手环，计步心率'),
(4, '羽毛球挂饰钥匙扣', 'https://images.sportzone.com/gifts/keychain-1.jpg', 200, 200, '创意羽毛球造型钥匙扣'),
(5, '50元优惠券', 'https://images.sportzone.com/gifts/coupon-50.jpg', 2000, 30, '满200元减50元优惠券');

-- ------------------------------
-- 4. sys_coupon（优惠券模板）
-- ------------------------------
INSERT INTO `sys_coupon` (`id`, `name`, `type`, `value`, `min_amount`, `points_cost`, `stock`, `start_time`, `end_time`) VALUES
(1, '满100减10',   1, 10.00,  100.00,  0,    500,  '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(2, '满200减30',   1, 30.00,  200.00,  500,  200,  '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(3, '满500减80',   1, 80.00,  500.00,  1500, 100,  '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
(4, '全场9折券',    2, 0.10,  0.00,    1000, 300,  '2026-01-01 00:00:00', '2026-12-31 23:59:59');

-- ------------------------------
-- 5. sys_admin（管理员）
-- ------------------------------
INSERT INTO `sys_admin` (`id`, `username`, `password`, `nickname`, `phone`, `role`) VALUES
(1, 'admin',   '$2a$10$PFH92X2NWFmOgd12QnrVr.tEoRmOAxMqgNvsfx7Nr/7e7iZhrnvVq', '超级管理员', '13800000001', 1),
(2, 'operator','$2a$10$PFH92X2NWFmOgd12QnrVr.tEoRmOAxMqgNvsfx7Nr/7e7iZhrnvVq', '运营小张',   '13800000002', 2),
(3, 'service', '$2a$10$PFH92X2NWFmOgd12QnrVr.tEoRmOAxMqgNvsfx7Nr/7e7iZhrnvVq', '客服小王',   '13800000003', 2);

SET FOREIGN_KEY_CHECKS = 1;