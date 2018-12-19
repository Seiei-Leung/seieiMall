SET NAMES utf8mb4;
/*
  初始化项目数据库
*/

/*
 * 用户数据表
    · ID（主键）
    · 用户名
    · 用户密码（MD5）
    · 用户邮箱（默认 null）
    · 用户手机（默认 null）
    · 用户权限（0 表示管理员，1 表示普通用户，默认 1）
    · 找回密码问题（默认 null）
    · 找回密码问题答案（默认 null）
    · 创建时间
    · 更新时间
*/
DROP TABLE IF exists mall_users;

CREATE  TABLE mall_users (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '用户表ID',
  username varchar(50) NOT NULL COMMENT '用户名称',
  password varchar(50) NOT NULL COMMENT '用户密码',
  email varchar(50) DEFAULT NULL COMMENT '用户邮箱',
  phone varchar(20) DEFAULT NULL COMMENT '用户手机',
  question varchar(100) DEFAULT NULL COMMENT '密保问题',
  answer varchar(100) DEFAULT NULL COMMENT '密保答案',
  role int(4) DEFAULT 1 COMMENT '用户权限',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY user_name_unique (username) USING BTREE -- 唯一，使用二叉树
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;

BEGIN;
INSERT INTO mall_users VALUES ('1', 'admin', '427338237BD929443EC5D48E24FD2B1A', 'admin@happymmall.com', '13800138000', '问题', '答案', '1', '2016-11-06 16:56:45', '2017-04-04 19:27:36'), ('13', 'geely', '08E9A6EA287E70E7E3F7C982BF7923AC', 'geely@happymmall.com', '13800138000', '问题', '答案', '0', '2016-11-19 22:19:25', '2016-11-19 22:19:25'), ('17', 'rosen', '095AC193FE2212EEC7A93E8FEFF11902', 'rosen1@happymmall.com', '13800138000', '问题', '答案', '0', '2017-03-17 10:51:33', '2017-04-09 23:13:26'), ('21', 'soonerbetter', 'DE6D76FE7C40D5A1A8F04213F2BEFBEE', 'test06@happymmall.com', '13800138000', '105204', '105204', '0', '2017-04-13 21:26:22', '2017-04-13 21:26:22');
COMMIT;

/*
 * 类别数据表
    · 类别主键（主键）
    · 父类别主键，当父类别主键是0时，表示为根类别
    · 类别名字
    · 类别状态：1 表示正常，0 表示弃用
    · 同类别之间的索引，用于同类别的排序
    · 创建时间
    · 更新时间
*/
DROP TABLE IF EXISTS mall_category;

CREATE TABLE mall_category (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '类别主键',
  parent_id int(11) NOT NULL COMMENT '父类别主键',
  name varchar(50) NOT NULL COMMENT '类别名字',
  status tinyint(1) DEFAULT  1 COMMENT '类别状态',
  sort_order int(4) DEFAULT NULL COMMENT '同类别之间的索引',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;

BEGIN;
INSERT INTO mall_category VALUES ('100001', '0', '家用电器', '1', null, '2017-03-25 16:46:00', '2017-03-25 16:46:00'), ('100002', '0', '数码3C', '1', null, '2017-03-25 16:46:21', '2017-03-25 16:46:21'), ('100003', '0', '服装箱包', '1', null, '2017-03-25 16:49:53', '2017-03-25 16:49:53'), ('100004', '0', '食品生鲜', '1', null, '2017-03-25 16:50:19', '2017-03-25 16:50:19'), ('100005', '0', '酒水饮料', '1', null, '2017-03-25 16:50:29', '2017-03-25 16:50:29'), ('100006', '100001', '冰箱', '1', null, '2017-03-25 16:52:15', '2017-03-25 16:52:15'), ('100007', '100001', '电视', '1', null, '2017-03-25 16:52:26', '2017-03-25 16:52:26'), ('100008', '100001', '洗衣机', '1', null, '2017-03-25 16:52:39', '2017-03-25 16:52:39'), ('100009', '100001', '空调', '1', null, '2017-03-25 16:52:45', '2017-03-25 16:52:45'), ('100010', '100001', '电热水器', '1', null, '2017-03-25 16:52:54', '2017-03-25 16:52:54'), ('100011', '100002', '电脑', '1', null, '2017-03-25 16:53:18', '2017-03-25 16:53:18'), ('100012', '100002', '手机', '1', null, '2017-03-25 16:53:27', '2017-03-25 16:53:27'), ('100013', '100002', '平板电脑', '1', null, '2017-03-25 16:53:35', '2017-03-25 16:53:35'), ('100014', '100002', '数码相机', '1', null, '2017-03-25 16:53:56', '2017-03-25 16:53:56'), ('100015', '100002', '3C配件', '1', null, '2017-03-25 16:54:07', '2017-03-25 16:54:07'), ('100016', '100003', '女装', '1', null, '2017-03-25 16:54:44', '2017-03-25 16:54:44'), ('100017', '100003', '帽子', '1', null, '2017-03-25 16:54:51', '2017-03-25 16:54:51'), ('100018', '100003', '旅行箱', '1', null, '2017-03-25 16:55:02', '2017-03-25 16:55:02'), ('100019', '100003', '手提包', '1', null, '2017-03-25 16:55:09', '2017-03-25 16:55:09'), ('100020', '100003', '保暖内衣', '1', null, '2017-03-25 16:55:18', '2017-03-25 16:55:18'), ('100021', '100004', '零食', '1', null, '2017-03-25 16:55:30', '2017-03-25 16:55:30'), ('100022', '100004', '生鲜', '1', null, '2017-03-25 16:55:37', '2017-03-25 16:55:37'), ('100023', '100004', '半成品菜', '1', null, '2017-03-25 16:55:47', '2017-03-25 16:55:47'), ('100024', '100004', '速冻食品', '1', null, '2017-03-25 16:55:56', '2017-03-25 16:55:56'), ('100025', '100004', '进口食品', '1', null, '2017-03-25 16:56:06', '2017-03-25 16:56:06'), ('100026', '100005', '白酒', '1', null, '2017-03-25 16:56:22', '2017-03-25 16:56:22'), ('100027', '100005', '红酒', '1', null, '2017-03-25 16:56:30', '2017-03-25 16:56:30'), ('100028', '100005', '饮料', '1', null, '2017-03-25 16:56:37', '2017-03-25 16:56:37'), ('100029', '100005', '调制鸡尾酒', '1', null, '2017-03-25 16:56:45', '2017-03-25 16:56:45'), ('100030', '100005', '进口洋酒', '1', null, '2017-03-25 16:57:05', '2017-03-25 16:57:05');
COMMIT;

/*
 * 商品数据表
    · 商品ID（主键）
    · 分类ID，对应类别表中的主键
    · 商品名称
    · 商品的副标题
    · 商品的主图URL，相对路径
    · 商品的子图集URL，是一个 JSON 对象数组，其中第一个为主图
    · 商品详情
    · 商品价格，使用的 decimal 类型，涉及到精度精确计算的都是用它
    · 产品数量
    · 商品状态，1 表示在售，2 表示下架， 3 表示删除
    · 创建时间
    · 更新时间
*/
DROP TABLE IF EXISTS mall_product;

 CREATE TABLE mall_product (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  category_id int(11) NOT NULL COMMENT '分类ID',
  name varchar(100) NOT NULL COMMENT '商品名称',
  subtitle varchar(200) DEFAULT NULL COMMENT '商品的副标题',
  main_image varchar(200) DEFAULT NULL COMMENT '商品的主图URL',
  sub_imags text DEFAULT NULL COMMENT '商品的子图集URL',
  detail text DEFAULT NULL COMMENT '商品详情',
  price decimal(20, 2) NOT NULL COMMENT '商品价格',
  stock int(11) NOT NULL COMMENT '产品数量',
  status int(4) NOT NULL COMMENT '商品状态',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;

BEGIN;
INSERT INTO mall_product VALUES
('26', '100002', 'Apple iPhone 7 Plus (A1661) 128G 玫瑰金色 移动联通电信4G手机', 'iPhone 7，现更以红色呈现。', '241997c4-9e62-4824-b7f0-7425c3c28917.jpeg', '241997c4-9e62-4824-b7f0-7425c3c28917.jpeg,b6c56eb0-1748-49a9-98dc-bcc4b9788a54.jpeg,92f17532-1527-4563-aa1d-ed01baa0f7b2.jpeg,3adbe4f7-e374-4533-aa79-cc4a98c529bf.jpeg', '<p><img alt=\"10000.jpg\" src=\"http://img.happymmall.com/00bce8d4-e9af-4c8d-b205-e6c75c7e252b.jpg\" width=\"790\" height=\"553\"><br></p><p><img alt=\"20000.jpg\" src=\"http://img.happymmall.com/4a70b4b4-01ee-46af-9468-31e67d0995b8.jpg\" width=\"790\" height=\"525\"><br></p><p><img alt=\"30000.jpg\" src=\"http://img.happymmall.com/0570e033-12d7-49b2-88f3-7a5d84157223.jpg\" width=\"790\" height=\"365\"><br></p><p><img alt=\"40000.jpg\" src=\"http://img.happymmall.com/50515c02-3255-44b9-a829-9e141a28c08a.jpg\" width=\"790\" height=\"525\"><br></p><p><img alt=\"50000.jpg\" src=\"http://img.happymmall.com/c138fc56-5843-4287-a029-91cf3732d034.jpg\" width=\"790\" height=\"525\"><br></p><p><img alt=\"60000.jpg\" src=\"http://img.happymmall.com/c92d1f8a-9827-453f-9d37-b10a3287e894.jpg\" width=\"790\" height=\"525\"><br></p><p><br></p><p><img alt=\"TB24p51hgFkpuFjSspnXXb4qFXa-1776456424.jpg\" src=\"http://img.happymmall.com/bb1511fc-3483-471f-80e5-c7c81fa5e1dd.jpg\" width=\"790\" height=\"375\"><br></p><p><br></p><p><img alt=\"shouhou.jpg\" src=\"http://img.happymmall.com/698e6fbe-97ea-478b-8170-008ad24030f7.jpg\" width=\"750\" height=\"150\"><br></p><p><img alt=\"999.jpg\" src=\"http://img.happymmall.com/ee276fe6-5d79-45aa-8393-ba1d210f9c89.jpg\" width=\"790\" height=\"351\"><br></p>', '6999.00', '9991', '1', '2017-04-13 21:45:41', '2017-04-13 21:45:41'),
('27', '100006', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', '送品牌烤箱，五一大促', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg,4bb02f1c-62d5-48cc-b358-97b05af5740d.jpeg,36bdb49c-72ae-4185-9297-78829b54b566.jpeg', '<p><img alt=\"miaoshu.jpg\" src=\"http://img.happymmall.com/9c5c74e6-6615-4aa0-b1fc-c17a1eff6027.jpg\" width=\"790\" height=\"444\"><br></p><p><img alt=\"miaoshu2.jpg\" src=\"http://img.happymmall.com/31dc1a94-f354-48b8-a170-1a1a6de8751b.jpg\" width=\"790\" height=\"1441\"><img alt=\"miaoshu3.jpg\" src=\"http://img.happymmall.com/7862594b-3063-4b52-b7d4-cea980c604e0.jpg\" width=\"790\" height=\"1442\"><img alt=\"miaoshu4.jpg\" src=\"http://img.happymmall.com/9a650563-dc85-44d6-b174-d6960cfb1d6a.jpg\" width=\"790\" height=\"1441\"><br></p>', '3299.00', '8876', '1', '2017-04-13 18:51:54', '2017-04-13 21:45:41'),
('28', '100012', '4+64G送手环/Huawei/华为 nova 手机P9/P10plus青春', 'NOVA青春版1999元', '0093f5d3-bdb4-4fb0-bec5-5465dfd26363.jpeg', '0093f5d3-bdb4-4fb0-bec5-5465dfd26363.jpeg,13da2172-4445-4eb5-a13f-c5d4ede8458c.jpeg,58d5d4b7-58d4-4948-81b6-2bae4f79bf02.jpeg', '<p><img alt=\"11TB2fKK3cl0kpuFjSsziXXa.oVXa_!!1777180618.jpg\" src=\"http://img.happymmall.com/5c2d1c6d-9e09-48ce-bbdb-e833b42ff664.jpg\" width=\"790\" height=\"966\"><img alt=\"22TB2YP3AkEhnpuFjSZFpXXcpuXXa_!!1777180618.jpg\" src=\"http://img.happymmall.com/9a10b877-818f-4a27-b6f7-62887f3fb39d.jpg\" width=\"790\" height=\"1344\"><img alt=\"33TB2Yyshk.hnpuFjSZFpXXcpuXXa_!!1777180618.jpg\" src=\"http://img.happymmall.com/7d7fbd69-a3cb-4efe-8765-423bf8276e3e.jpg\" width=\"790\" height=\"700\"><img alt=\"TB2diyziB8kpuFjSspeXXc7IpXa_!!1777180618.jpg\" src=\"http://img.happymmall.com/1d7160d2-9dba-422f-b2a0-e92847ba6ce9.jpg\" width=\"790\" height=\"393\"><br></p>', '1999.00', '9994', '1', '2017-04-13 18:57:18', '2017-04-13 21:45:41'),
('29', '100008', 'Haier/海尔HJ100-1HU1 10公斤滚筒洗衣机全自动带烘干家用大容量 洗烘一体', '门店机型 德邦送货', '173335a4-5dce-4afd-9f18-a10623724c4e.jpeg', '173335a4-5dce-4afd-9f18-a10623724c4e.jpeg,42b1b8bc-27c7-4ee1-80ab-753d216a1d49.jpeg,2f1b3de1-1eb1-4c18-8ca2-518934931bec.jpeg', '<p><img alt=\"1TB2WLZrcIaK.eBjSspjXXXL.XXa_!!2114960396.jpg\" src=\"http://img.happymmall.com/ffcce953-81bd-463c-acd1-d690b263d6df.jpg\" width=\"790\" height=\"920\"><img alt=\"2TB2zhOFbZCO.eBjSZFzXXaRiVXa_!!2114960396.jpg\" src=\"http://img.happymmall.com/58a7bd25-c3e7-4248-9dba-158ef2a90e70.jpg\" width=\"790\" height=\"1052\"><img alt=\"3TB27mCtb7WM.eBjSZFhXXbdWpXa_!!2114960396.jpg\" src=\"http://img.happymmall.com/2edbe9b3-28be-4a8b-a9c3-82e40703f22f.jpg\" width=\"790\" height=\"820\"><br></p>', '4299.00', '9993', '1', '2017-04-13 19:07:47', '2017-04-13 21:45:41');
COMMIT;

/*
 * 购物车数据表
    · 购物车ID（主键）
    · 用户ID，对应用户表中的主键
    · 商品ID，对应商品表中的主键
    · 商品数量
    · 是否已经勾选，1 表示已勾，0 表示未勾
    · 创建时间
    · 更新时间
*/
DROP TABLE IF EXISTS mall_cart;

CREATE TABLE mall_cart (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id int(11) NOT NULL COMMENT '用户ID',
  product_id int(11) NOT NULL COMMENT '商品ID',
  quantity int(11) NOT NULL COMMENT '商品数量',
  checked tinyint(1) DEFAULT 0 COMMENT '是否已经勾选',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY user_id_index (user_id) USING BTREE -- 添加user_id的索引，因为会经常使用user_id进行查询，使用二叉树查询，提高查询效率，这一般在经常使用非主键查询时使用
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;

BEGIN;
INSERT INTO mall_cart VALUES ('126', '21', '26', '1', '1', '2017-04-13 21:27:06', '2017-04-13 21:27:06');
COMMIT;

/*
 * 订单数据表
    · 订单ID（主键）
    · 订单号，唯一索引
    · 用户ID
    · 发货地址表ID
    · 实际交付金额，单位元，保留两个小数点
    · 付款方式，1 表示在线支付
    · 运费
    · 订单状态
      ·0 表示已取消
      ·10 表示未付款
      ·20 表示已付款
      ·30 表示已发货
      ·40 表示申请退款
      ·50 表示申请换货
      ·60 表示交易成功
      ·61 表示退款失败，交易成功
      ·62 表示换货成功，交易成功
      ·63 表示换货失败，交易成功
      ·70 表示交易关闭
      ·71 表示退款成功
    · 支付时间
    · 发货时间
    · 交易完成期限，确认收货后，交易完成期限，此期限过后不能申请退款以及换货
    · 交易完成时间
    · 交款期限，下了订单，过了期限没有付款，订单作废
    · 创建时间
    · 更新时间
*/
DROP TABLE IF EXISTS mall_order;

CREATE TABLE mall_order (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_no bigint(20) NOT NULL COMMENT '订单号',
  uesr_id int(11) NOT NULL COMMENT '用户ID',
  shipping_id int(11) DEFAULT NULL COMMENT '发货地址表ID',
  payment decimal(20, 2) DEFAULT NULL COMMENT '实际交付金额',
  payment_type int(4) DEFAULT 1 COMMENT '交付方式',
  postage int(10) DEFAULT NULL COMMENT '运费',
  status int(10) DEFAULT NULL COMMENT '订单状态',
  payment_time datetime DEFAULT NULL COMMENT '交付时间',
  send_time datetime DEFAULT NULL COMMENT '发货时间',
  end_time datetime DEFAULT NULL COMMENT '交易完成期限',
  complete_time datetime DEFAULT NULL COMMENT '交易完成时间',
  close_time datetime DEFAULT NULL COMMENT '交款期限',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY order_no_index (order_no) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;

BEGIN;
INSERT INTO mall_order VALUES
('103', '1491753014256', '1', '25', '13998.00', '1', '0', '10', null, null, null, null, null, '2017-04-09 23:50:14', '2017-04-09 23:50:14'), 
('104', '1491830695216', '1', '26', '13998.00', '1', '0', '10', null, null, null, null, null, '2017-04-10 21:24:55', '2017-04-10 21:24:55'), 
('105', '1492089528889', '1', '29', '3299.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:18:48', '2017-04-13 21:18:48'), 
('106', '1492090946105', '1', '29', '27894.00', '1', '0', '20', '2017-04-13 21:42:40', null, null, null, null, '2017-04-13 21:42:26', '2017-04-13 21:42:41'), 
('107', '1492091003128', '1', '29', '8597.00', '1', '0', '20', '2017-04-13 21:43:38', null, null, null, null, '2017-04-13 21:43:23', '2017-04-13 21:43:38'), 
('108', '1492091051313', '1', '29', '1999.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:44:11', '2017-04-13 21:44:11'), 
('109', '1492091061513', '1', '29', '6598.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:44:21', '2017-04-13 21:44:21'), 
('110', '1492091069563', '1', '29', '3299.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:44:29', '2017-04-13 21:44:29'), 
('111', '1492091076073', '1', '29', '4299.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:44:36', '2017-04-13 21:44:36'), 
('112', '1492091083720', '1', '29', '3299.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:44:43', '2017-04-13 21:44:43'), 
('113', '1492091089794', '1', '29', '6999.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:44:49', '2017-04-13 21:44:49'), 
('114', '1492091096400', '1', '29', '6598.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:44:56', '2017-04-13 21:44:56'), 
('115', '1492091102371', '1', '29', '3299.00', '1', '0', '10', null, null, null, null, null, '2017-04-13 21:45:02', '2017-04-13 21:45:02'), 
('116', '1492091110004', '1', '29', '8598.00', '1', '0', '40', '2017-04-13 21:55:16', '2017-04-13 21:55:31', null, null, null, '2017-04-13 21:45:09', '2017-04-13 21:55:31'), 
('117', '1492091141269', '1', '29', '22894.00', '1', '0', '20', '2017-04-13 21:46:06', null, null, null, null, '2017-04-13 21:45:41', '2017-04-13 21:46:07');
COMMIT;

/*
 * 订单明细数据表
    · 订单ID（主键）
    · 用户ID，这里再存储一个用户ID，而不是通过订单总表获取用户ID，是考虑到用户ID可能会经常使用到，减少联表查询
    · 订单状态
      ·0 表示已取消
      ·10 表示未付款
      ·20 表示已付款
      ·30 表示已发货
      ·40 表示申请退款
      ·50 表示申请换货
      ·60 表示交易成功
      ·61 表示退款失败，交易成功
      ·62 表示换货成功，交易成功
      ·63 表示换货失败，交易成功
      ·70 表示交易关闭
      ·71 表示退款成功
    · 订单号，上述总订单号
    · 商品表ID号
    · 商品当时名称
    · 商品当时单价
    · 商品图片地址
    · 商品数量
    · 商品总价，存储下来不用每次都计算
    · 创建时间
    · 更新时间
*/
DROP TABLE IF EXISTS mall_order_item;

CREATE TABLE mall_order_item (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id int(11) NOT NULL COMMENT '用户ID',
  order_no bigint(20) NOT NULL COMMENT '订单号',
  status int(10) DEFAULT NULL COMMENT '订单状态',
  product_id int(11) DEFAULT NULL COMMENT '商品表ID号',
  product_name varchar(100) NOT NULL COMMENT '商品当时名称',
  product_image varchar(200) NOT NULL COMMENT '商品图片地址',
  current_unit_price decimal(20, 2) NOT NULL COMMENT '商品当时单价',
  quantity int(10) NOT NULL COMMENT '商品数量',
  total_price decimal(20, 2) NOT NULL COMMENT '商品总价',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY order_no_index (order_no) USING BTREE,
  KEY order_no_user_id_index (user_id, order_no) USING BTREE -- 复合索引
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;

BEGIN;
INSERT INTO mall_order_item VALUES 
('113', '1', '1491753014256', '10', '26', 'Apple iPhone 7 Plus (A1661) 128G 玫瑰金色 移动联通电信4G手机', '241997c4-9e62-4824-b7f0-7425c3c28917.jpeg', '6999.00', '2', '13998.00', '2017-04-09 23:50:14', '2017-04-09 23:50:14'), 
('114', '1', '1491830695216', '10', '26', 'Apple iPhone 7 Plus (A1661) 128G 玫瑰金色 移动联通电信4G手机', '241997c4-9e62-4824-b7f0-7425c3c28917.jpeg', '6999.00', '2', '13998.00', '2017-04-10 21:24:55', '2017-04-10 21:24:55'), 
('115', '1', '1492089528889', '10', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '1', '3299.00', '2017-04-13 21:18:48', '2017-04-13 21:18:48'), 
('116', '1', '1492090946105', '20', '29', 'Haier/海尔HJ100-1HU1 10公斤滚筒洗衣机全自动带烘干家用大容量 洗烘一体', '173335a4-5dce-4afd-9f18-a10623724c4e.jpeg', '4299.00', '2', '8598.00', '2017-04-13 21:42:26', '2017-04-13 21:42:26'), 
('117', '1', '1492090946105', '20', '28', '4+64G送手环/Huawei/华为 nova 手机P9/P10plus青春', '0093f5d3-bdb4-4fb0-bec5-5465dfd26363.jpeg', '1999.00', '1', '1999.00', '2017-04-13 21:42:26', '2017-04-13 21:42:26'), 
('118', '1', '1492090946105', '20', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '1', '3299.00', '2017-04-13 21:42:26', '2017-04-13 21:42:26'), 
('119', '1', '1492090946105', '20', '26', 'Apple iPhone 7 Plus (A1661) 128G 玫瑰金色 移动联通电信4G手机', '241997c4-9e62-4824-b7f0-7425c3c28917.jpeg', '6999.00', '2', '13998.00', '2017-04-13 21:42:26', '2017-04-13 21:42:26'), 
('120', '1', '1492091003128', '20', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '2', '6598.00', '2017-04-13 21:43:23', '2017-04-13 21:43:23'), 
('121', '1', '1492091003128', '20', '28', '4+64G送手环/Huawei/华为 nova 手机P9/P10plus青春', '0093f5d3-bdb4-4fb0-bec5-5465dfd26363.jpeg', '1999.00', '1', '1999.00', '2017-04-13 21:43:23', '2017-04-13 21:43:23'), 
('122', '1', '1492091051313', '10', '28', '4+64G送手环/Huawei/华为 nova 手机P9/P10plus青春', '0093f5d3-bdb4-4fb0-bec5-5465dfd26363.jpeg', '1999.00', '1', '1999.00', '2017-04-13 21:44:11', '2017-04-13 21:44:11'), 
('123', '1', '1492091061513', '10', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '2', '6598.00', '2017-04-13 21:44:21', '2017-04-13 21:44:21'), 
('124', '1', '1492091069563', '10', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '1', '3299.00', '2017-04-13 21:44:29', '2017-04-13 21:44:29'), 
('125', '1', '1492091076073', '10', '29', 'Haier/海尔HJ100-1HU1 10公斤滚筒洗衣机全自动带烘干家用大容量 洗烘一体', '173335a4-5dce-4afd-9f18-a10623724c4e.jpeg', '4299.00', '1', '4299.00', '2017-04-13 21:44:36', '2017-04-13 21:44:36'), 
('126', '1', '1492091083720', '10', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '1', '3299.00', '2017-04-13 21:44:43', '2017-04-13 21:44:43'), 
('127', '1', '1492091089794', '10', '26', 'Apple iPhone 7 Plus (A1661) 128G 玫瑰金色 移动联通电信4G手机', '241997c4-9e62-4824-b7f0-7425c3c28917.jpeg', '6999.00', '1', '6999.00', '2017-04-13 21:44:49', '2017-04-13 21:44:49'), 
('128', '1', '1492091096400', '10', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '2', '6598.00', '2017-04-13 21:44:56', '2017-04-13 21:44:56'), 
('129', '1', '1492091102371', '10', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '1', '3299.00', '2017-04-13 21:45:02', '2017-04-13 21:45:02'), 
('130', '1', '1492091110004', '40', '29', 'Haier/海尔HJ100-1HU1 10公斤滚筒洗衣机全自动带烘干家用大容量 洗烘一体', '173335a4-5dce-4afd-9f18-a10623724c4e.jpeg', '4299.00', '2', '8598.00', '2017-04-13 21:45:09', '2017-04-13 21:45:09'), 
('131', '1', '1492091141269', '20', '26', 'Apple iPhone 7 Plus (A1661) 128G 玫瑰金色 移动联通电信4G手机', '241997c4-9e62-4824-b7f0-7425c3c28917.jpeg', '6999.00', '1', '6999.00', '2017-04-13 21:45:41', '2017-04-13 21:45:41'), 
('132', '1', '1492091141269', '20', '27', 'Midea/美的 BCD-535WKZM(E)冰箱双开门对开门风冷无霜智能电家用', 'ac3e571d-13ce-4fad-89e8-c92c2eccf536.jpeg', '3299.00', '1', '3299.00', '2017-04-13 21:45:41', '2017-04-13 21:45:41'), 
('133', '1', '1492091141269', '20', '29', 'Haier/海尔HJ100-1HU1 10公斤滚筒洗衣机全自动带烘干家用大容量 洗烘一体', '173335a4-5dce-4afd-9f18-a10623724c4e.jpeg', '4299.00', '2', '8598.00', '2017-04-13 21:45:41', '2017-04-13 21:45:41'), 
('134', '1', '1492091141269', '20', '28', '4+64G送手环/Huawei/华为 nova 手机P9/P10plus青春', '0093f5d3-bdb4-4fb0-bec5-5465dfd26363.jpeg', '1999.00', '2', '3998.00', '2017-04-13 21:45:41', '2017-04-13 21:45:41');
COMMIT;

/*
 * 支付信息数据表
    · ID（主键）
    · 用户ID
    · 总订单ID
    · 支付平台，1 表示支付宝（默认)，2 表示微信
    · 支付流水号（默认支付宝）
    · 支付状态（默认支付宝）
    · 创建时间
    · 更新时间
*/
DROP TABLE IF EXISTS mall_pay_info;

CREATE TABLE mall_pay_info (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id int(11) NOT NULL COMMENT '用户ID',
  order_no bigint(20) NOT NULL COMMENT '总订单ID',
  pay_platform int(10) DEFAULT NULL COMMENT '支付平台',
  platform_number varchar(200) DEFAULT NULL COMMENT '支付流水号',
  platform_status varchar(20) DEFAULT NULL COMMENT '支付状态',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;

BEGIN;
INSERT INTO mall_pay_info VALUES ('53', '1', '1492090946105', '1', '2017041321001004300200116250', 'WAIT_BUYER_PAY', '2017-04-13 21:42:33', '2017-04-13 21:42:33'), ('54', '1', '1492090946105', '1', '2017041321001004300200116250', 'TRADE_SUCCESS', '2017-04-13 21:42:41', '2017-04-13 21:42:41'), ('55', '1', '1492091003128', '1', '2017041321001004300200116251', 'WAIT_BUYER_PAY', '2017-04-13 21:43:31', '2017-04-13 21:43:31'), ('56', '1', '1492091003128', '1', '2017041321001004300200116251', 'TRADE_SUCCESS', '2017-04-13 21:43:38', '2017-04-13 21:43:38'), ('57', '1', '1492091141269', '1', '2017041321001004300200116252', 'WAIT_BUYER_PAY', '2017-04-13 21:45:59', '2017-04-13 21:45:59'), ('58', '1', '1492091141269', '1', '2017041321001004300200116252', 'TRADE_SUCCESS', '2017-04-13 21:46:07', '2017-04-13 21:46:07'), ('59', '1', '1492091110004', '1', '2017041321001004300200116396', 'WAIT_BUYER_PAY', '2017-04-13 21:55:08', '2017-04-13 21:55:08'), ('60', '1', '1492091110004', '1', '2017041321001004300200116396', 'TRADE_SUCCESS', '2017-04-13 21:55:17', '2017-04-13 21:55:17');
COMMIT;

/*
 * 收货地址数据表
    · 订单ID（主键）
    · 用户ID
    · 收货姓名
    · 收货电话
    · 省
    · 城市
    · 区/县
    · 详细地址
    · 邮编
    · 创建时间
    · 更新时间
*/
DROP TABLE IF EXISTS mall_shipping;

CREATE TABLE mall_shipping (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id int(11) NOT NULL COMMENT '用户ID',
  receiver_name varchar(20) DEFAULT NULL COMMENT '收货姓名',
  receiver_phone varchar(20) DEFAULT NULL COMMENT '收货电话',
  receiver_province varchar(20) DEFAULT NULL COMMENT '省',
  receiver_city varchar(20) DEFAULT NULL COMMENT '城市',
  receiver_district varchar(20) DEFAULT NULL COMMENT '区/县',
  receiver_address varchar(200) DEFAULT NULL COMMENT '详细地址',
  receiver_zip varchar(6) DEFAULT NULL COMMENT '邮编',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;

BEGIN;
INSERT INTO mall_shipping VALUES
('4', '13', 'geely', '18688888888', '北京', '北京市', '海淀区', '中关村', '100000', '2017-01-22 14:26:25', '2017-01-22 14:26:25'),
('7', '17', 'Rosen', '13800138000', '北京', '北京', null, '中关村', '100000', '2017-03-29 12:11:01', '2017-03-29 12:11:01'),
('29', '1', '吉利', '13800138000', '北京', '北京', '海淀区', '海淀区中关村', '100000', '2017-04-09 18:33:32', '2017-04-09 18:33:32');
COMMIT;

/*
 * 快递单信息
    · ID（主键）
    · 对应的订单单号
    · 快递单单号
    · 快递公司
    · 快递单运费
    · 创建时间
    · 更新时间
*/

DROP TABLE IF EXISTS mall_express;

CREATE TABLE mall_express (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_no bigint(20) NOT NULL COMMENT '订单单号',
  express_no bigint(20) NOT NULL COMMENT '快递单单号',
  express_company varchar(20) NOT NULL COMMENT '快递公司',
  express_pay decimal(20, 2) NOT NULL COMMENT '快递单运费',
  create_time datetime NOT NULL COMMENT '创建时间',
  update_time datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT  CHARSET=utf8mb4;