
## 这个是JdbcAppender的示例

CREATE DATABASE `app_logs` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

use app_logs;

# 表结构, 根据自己的实际需要来进行调整, 如增减字段, 建立分区等等
CREATE TABLE `remote_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态',
  `createDate` date NOT NULL COMMENT '创建日期',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `marker` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务标识',
  `level` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日志级别',
  `logger` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'logger',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日志内容',
  `exception` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '异常信息',
  PRIMARY KEY (`id`),
  KEY `idx_date` (`createDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='远程日志表';