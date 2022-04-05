CREATE DATABASE sequence;

use sequence;

CREATE TABLE `id_alloc` (
  `biz_key` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '业务编码(主键)',
  `max_id` bigint NOT NULL DEFAULT '1' COMMENT '已使用的最大id',
  `min_step` int NOT NULL COMMENT '最小步长',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `description` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`biz_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='id分段发号表';
