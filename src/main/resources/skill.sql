/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50731
Source Host           : 127.0.0.1:3306
Source Database       : skill

Target Server Type    : MYSQL
Target Server Version : 50731
File Encoding         : 65001

Date: 2021-01-10 16:33:41
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `stock`
-- ----------------------------
DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `count` int(11) DEFAULT NULL COMMENT '库存数量',
  `sale` int(11) DEFAULT NULL COMMENT '已售数量',
  `version` int(11) DEFAULT NULL COMMENT '乐观锁，版本号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='商品库存表';

-- ----------------------------
-- Records of stock
-- ----------------------------
INSERT INTO `stock` VALUES ('1', '秒杀商品', '100', '0', '0');

-- ----------------------------
-- Table structure for `stock_order`
-- ----------------------------
DROP TABLE IF EXISTS `stock_order`;
CREATE TABLE `stock_order` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sid` int(20) DEFAULT NULL COMMENT '库存id',
  `name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=248 DEFAULT CHARSET=utf8mb4 COMMENT='库存订单表';

-- ----------------------------
-- Records of stock_order
-- ----------------------------

-- ----------------------------
-- Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户主键',
  `username` varchar(255) DEFAULT NULL COMMENT '用户账号',
  `password` varchar(255) DEFAULT NULL COMMENT '用户密码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', '13712341234', '123456');
