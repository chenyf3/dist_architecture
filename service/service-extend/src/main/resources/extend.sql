CREATE DATABASE /*!32312 IF NOT EXISTS*/`extend` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `extend`;

/*Table structure for table `tbl_publish_record` */

DROP TABLE IF EXISTS `tbl_publish_record`;

CREATE TABLE `tbl_publish_record` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `VERSION` int NOT NULL COMMENT '版本号',
  `CREATOR` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `JOB_NAME` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发的任务名',
  `BUILD_NO` varchar(32) NOT NULL COMMENT '构建流水号',
  `BUILD_MSG` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '构建说明',
  `APPS` varchar(6000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '发布项目',
  `IDC` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '发布的机房',
  `RELAY_APP` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '中继项目',
  `NOTIFY_EMAIL` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知邮件',
  `NOTIFY_URL` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL COMMENT '回调通知地址',
  `PUBLISH_TIMES` int NOT NULL DEFAULT '1' COMMENT '发布次数',
  `MODIFIER` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '修改者',
  `STATUS` tinyint(1) NOT NULL COMMENT '状态 1=待处理 2=排队中 3=处理中 4=成功 5=失败 6=不稳定 7=已取消 8=已超时',
  `QUEUE_ID` int DEFAULT NULL COMMENT 'jenkins队列id',
  `BUILD_ID` int DEFAULT NULL COMMENT 'jenkins构建id',
  `PROCESS_TIMES` int NOT NULL COMMENT '已处理次数',
  `REMARK` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目发布任务表';

