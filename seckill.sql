/*
 Navicat MySQL Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 50731
 Source Host           : 127.0.0.1:3306
 Source Schema         : seckill

 Target Server Type    : MySQL
 Target Server Version : 50731
 File Encoding         : 65001

 Date: 26/09/2021 17:29:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_order
-- ----------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`
(
    `id`           int(11) UNSIGNED                                              NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_no`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单序列号',
    `user_id`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `product_id`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '产品ID',
    `order_status` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci    NULL     DEFAULT NULL COMMENT '订单状态',
    `content`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '订单内容',
    `deleted`      int(1)                                                        NOT NULL DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    `created_time` datetime(0)                                                   NULL     DEFAULT NULL COMMENT '更新时间',
    `updated_time` datetime(0)                                                   NULL     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '模板表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_transaction_log
-- ----------------------------
DROP TABLE IF EXISTS `t_transaction_log`;
CREATE TABLE `t_transaction_log`
(
    `id`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '事务ID',
    `business`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '业务标识',
    `foreign_key`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '对应业务表中的主键',
    `created_time` datetime(0)                                           NULL DEFAULT NULL COMMENT '更新时间',
    `updated_time` datetime(0)                                           NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
