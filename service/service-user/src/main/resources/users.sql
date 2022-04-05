/*
SQLyog Ultimate v13.1.1 (64 bit)
MySQL - 8.0.23 : Database - users
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`users` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `users`;

/*Table structure for table `tbl_pms_auth` */

DROP TABLE IF EXISTS `tbl_pms_auth`;

CREATE TABLE `tbl_pms_auth` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `VERSION` int NOT NULL DEFAULT '0' COMMENT 'VERSION',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `NAME` varchar(90) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '名称',
  `NUMBER` varchar(30) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '序号',
  `PARENT_ID` bigint NOT NULL DEFAULT '0' COMMENT '父节点，一级菜单为0',
  `PERMISSION_FLAG` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '权限标识(前后端共用)',
  `AUTH_TYPE` smallint NOT NULL COMMENT '权限类型(1:菜单 2:功能)',
  `URL` varchar(150) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '菜单URL(AUTH_TYPE为1时有值)',
  `ICON` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '图标',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='pms权限表';

/*Data for the table `tbl_pms_auth` */

insert  into `tbl_pms_auth`(`ID`,`VERSION`,`CREATE_TIME`,`NAME`,`NUMBER`,`PARENT_ID`,`PERMISSION_FLAG`,`AUTH_TYPE`,`URL`,`ICON`) values 
(1,0,'2020-05-06 11:11:58','系统管理','001',0,'sys:pms:manage',1,'sysMange','system'),
(2,1,'2020-05-06 11:12:44','权限管理','001001',1,'sys:auth:view',1,'/system/authManage','permission'),
(3,0,'2020-05-06 11:13:35','角色管理','001002',1,'sys:role:view',1,'/system/roleManage','role'),
(4,0,'2020-05-06 11:14:16','用户管理','001003',1,'sys:user:view',1,'/system/userManage','user'),
(5,0,'2020-05-06 11:16:24','添加','001001001',2,'sys:auth:add',2,'',''),
(6,0,'2020-05-06 11:16:46','修改','001001002',2,'sys:auth:edit',2,'',''),
(7,0,'2020-05-06 11:17:22','删除','001001003',2,'sys:auth:delete',2,'',''),
(8,0,'2020-05-06 11:18:11','添加','001002001',3,'sys:role:add',2,'',''),
(9,0,'2020-05-06 11:18:41','修改','001002002',3,'sys:role:edit',2,'',''),
(10,0,'2020-05-06 11:19:11','删除','001002003',3,'sys:role:delete',2,'',''),
(11,0,'2020-05-06 11:19:35','分配权限','001002004',3,'sys:role:assignAuth',2,'',''),
(12,0,'2020-05-06 11:21:24','添加','001003001',4,'sys:user:add',2,'',''),
(13,0,'2020-05-06 11:21:49','修改','001003002',4,'sys:user:edit',2,'',''),
(14,0,'2020-05-06 11:22:11','审核/冻结','001003003',4,'sys:user:changeStatus',2,'',''),
(15,0,'2020-05-06 11:22:40','重置密码','001003004',4,'sys:user:resetPwd',2,'',''),
(16,0,'2020-05-06 11:23:04','删除','001003005',4,'sys:user:delete',2,'',''),
(17,0,'2020-05-08 19:09:46','关联角色','001003006',4,'sys:user:assignRoles',2,'',''),
(18,0,'2020-05-06 11:24:13','操作日志','001004',1,'sys:operateLog:view',1,'/system/userOperateLog','log'),
(39,4,'2021-01-27 18:59:31','商户管理','002',0,'merchant:merchantManage',1,'merchantManage','mch_manage'),
(40,3,'2021-01-27 19:01:02','商户信息','002001',39,'merchant:merchant:list',1,'/merchant/base/merchantManage','mch_info'),
(42,1,'2021-01-27 19:07:02','添加商户','002001001',40,'merchant:merchant:add',2,'',''),
(43,1,'2021-01-27 19:07:22','查看详情','002001002',40,'merchant:merchant:view',2,'',''),
(46,1,'2021-01-29 14:16:56','重置支付密码','002001003',40,'merchant:pwd:resetTradePwd',2,'',''),
(47,1,'2021-01-29 16:06:08','编辑商户','002001004',40,'merchant:merchant:edit',2,'',''),
(62,8,'2021-05-06 18:24:43','商户通知','002002',39,'merchant:notify:list',1,'/merchant/notify/notifyRecord','notify'),
(63,6,'2021-05-06 18:25:13','通知管理','002002001',62,'merchant:notify:manage',2,'',''),
(64,2,'2021-05-08 19:59:29','基础配置','003',0,'base:config',1,'/baseConfig','setting'),
(65,2,'2021-05-08 20:02:29','产品管理','003001',64,'baseConfig:product:list',1,'/baseConfig/productManage','tab'),
(66,2,'2021-05-08 20:03:55','产品开通','003002',64,'baseConfig:productOpen:list',1,'/baseConfig/productOpenManage','table'),
(67,2,'2021-05-08 20:37:37','添加','003001001',65,'baseConfig:product:add',2,'',''),
(68,1,'2021-05-08 20:37:55','修改','003001002',65,'baseConfig:product:edit',2,'',''),
(69,1,'2021-05-08 20:38:18','添加','003002001',66,'baseConfig:productOpen:add',2,'',''),
(70,1,'2021-05-08 20:38:26','修改','003002002',66,'baseConfig:productOpen:edit',2,'',''),
(71,4,'2021-05-19 09:21:16','后台管理','002003',39,'portal:manage',1,'portalManage','admin_sys'),
(72,2,'2021-05-19 09:22:09','权限管理','002003001',71,'portal:auth:view',1,'/portal/portalAuthManage','permission'),
(73,2,'2021-05-19 09:23:06','角色管理','002003002',71,'portal:role:view',1,'/portal/portalRoleManage','role'),
(74,2,'2021-05-19 09:23:32','用户管理','002003003',71,'portal:user:view',1,'/portal/portalUserManage','user'),
(75,2,'2021-05-19 09:23:53','权限回收','002003004',71,'portal:auth:revoke',1,'/portal/portalAuthRevoke','retrieve'),
(76,2,'2021-05-19 09:24:10','添加','002003001001',72,'portal:auth:add',2,'',''),
(77,2,'2021-05-19 09:24:20','修改','002003001002',72,'portal:auth:edit',2,'',''),
(78,2,'2021-05-19 09:24:32','删除','002003001003',72,'portal:auth:delete',2,'',''),
(79,2,'2021-05-19 09:24:45','添加','002003002001',73,'portal:role:addAdmin',2,'',''),
(80,2,'2021-05-19 09:24:56','修改','002003002002',73,'portal:role:editAdmin',2,'',''),
(81,2,'2021-05-19 09:25:09','删除','002003002003',73,'portal:role:deleteAdmin',2,'',''),
(82,2,'2021-05-19 09:25:22','分配权限','002003002004',73,'portal:role:assignAdminRoleAuth',2,'',''),
(83,2,'2021-05-19 09:25:45','添加管理员','002003003001',74,'portal:user:addAdmin',2,'',''),
(84,2,'2021-05-19 09:25:57','修改','002003003002',74,'portal:user:edit',2,'',''),
(85,2,'2021-05-19 09:26:14','审核/冻结','002003003003',74,'portal:user:changeStatus',2,'',''),
(86,2,'2021-05-19 09:26:31','重置密码','002003003004',74,'portal:user:resetPwd',2,'',''),
(87,2,'2021-05-19 09:26:47','关联角色','002003003005',74,'portal:user:assignAdminRoles',2,'',''),
(88,3,'2021-05-19 09:28:09','操作日志','002003005',71,'portal:operateLog:view',1,'/portal/portalOperateLog','log'),
(89,2,'2021-05-19 09:28:59','提交任务','002003004001',75,'portal:auth:doRevoke',2,'',''),
(90,0,'2021-05-19 09:48:32','运维管理','001005',1,'devops:manage',1,'devopsManage','devops'),
(91,0,'2021-05-19 09:48:56','定时任务','001005001',90,'devops:timer:list',1,'/devops/scheduleJobManage','timer'),
(92,0,'2021-05-19 09:49:29','消息轨迹','001005002',90,'devops:mqTrace:list',1,'/devops/mqTraceManage','trail_line'),
(93,0,'2021-05-19 09:49:46','添加','001005001001',91,'devops:timer:manage',2,'',''),
(94,0,'2021-05-19 09:49:59','编辑','001005001002',91,'devops:timer:manage',2,'',''),
(95,0,'2021-05-19 09:50:11','触发','001005001003',91,'devops:timer:operate',2,'',''),
(96,0,'2021-05-19 09:50:22','暂停','001005001004',91,'devops:timer:operate',2,'',''),
(97,0,'2021-05-19 09:50:34','恢复','001005001005',91,'devops:timer:operate',2,'',''),
(98,0,'2021-05-19 09:50:52','删除','001005001006',91,'devops:timer:manage',2,'',''),
(99,0,'2021-05-19 09:51:11','实例管理','001005001007',91,'devops:timer:instanceManage',2,'',''),
(100,0,'2021-05-19 09:51:28','操作日志','001005001008',91,'devops:timer:opLogList',2,'',''),
(101,0,'2021-05-19 09:51:49','管理','001005002001',92,'devops:mqTrace:manage',2,'',''),
(102,1,'2021-05-19 09:56:21','运营邮件','003003',64,'baseConfig:mailReceive:list',1,'/baseConfig/mailManage','email'),
(103,1,'2021-05-19 09:56:41','管理','003003001',102,'baseConfig:mailReceive:manage',2,'',''),
(104,0,'2021-05-26 16:04:07','上线管理','001005003',90,'devops:publish:view',1,'/devops/publishRecordManage','people'),
(105,3,'2021-05-26 16:04:50','发布管理','001005003001',104,'devops:publish:manage',2,'',''),
(106,0,'2021-05-26 16:05:38','流量切换','001005003002',104,'devops:publish:flowSwitch',2,'',''),
(107,0,'2021-05-26 16:05:56','同步代码','001005003003',104,'devops:publish:syncIdcPublish',2,'','');

/*Table structure for table `tbl_pms_operate_log` */

DROP TABLE IF EXISTS `tbl_pms_operate_log`;

CREATE TABLE `tbl_pms_operate_log` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `LOGIN_NAME` varchar(50) NOT NULL COMMENT '操作员登录名',
  `OPERATE_TYPE` smallint NOT NULL COMMENT '操作类型(1=登录 2=退出 3=添加 4=修改 5=删除 6=查询)',
  `CONTENT` text COMMENT '操作内容',
  PRIMARY KEY (`ID`,`CREATE_TIME`),
  KEY `IDX_LOGIN_NAME` (`LOGIN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=COMPACT COMMENT='pms用户操作日志表'
/*!50100 PARTITION BY RANGE (to_days(`CREATE_TIME`))
(PARTITION p202005 VALUES LESS THAN (737942) ENGINE = InnoDB,
 PARTITION p202006 VALUES LESS THAN (737972) ENGINE = InnoDB,
 PARTITION p202007 VALUES LESS THAN (738003) ENGINE = InnoDB,
 PARTITION p202008 VALUES LESS THAN (738034) ENGINE = InnoDB,
 PARTITION p202009 VALUES LESS THAN (738064) ENGINE = InnoDB,
 PARTITION p202010 VALUES LESS THAN (738095) ENGINE = InnoDB,
 PARTITION p202011 VALUES LESS THAN (738125) ENGINE = InnoDB,
 PARTITION p202012 VALUES LESS THAN (738156) ENGINE = InnoDB,
 PARTITION p202101 VALUES LESS THAN (738187) ENGINE = InnoDB,
 PARTITION p202102 VALUES LESS THAN (738215) ENGINE = InnoDB,
 PARTITION p202103 VALUES LESS THAN (738246) ENGINE = InnoDB,
 PARTITION p202104 VALUES LESS THAN (738276) ENGINE = InnoDB,
 PARTITION p202105 VALUES LESS THAN (738307) ENGINE = InnoDB,
 PARTITION p202106 VALUES LESS THAN (738337) ENGINE = InnoDB,
 PARTITION p202107 VALUES LESS THAN (738368) ENGINE = InnoDB,
 PARTITION p202108 VALUES LESS THAN (738399) ENGINE = InnoDB,
 PARTITION p202109 VALUES LESS THAN (738429) ENGINE = InnoDB,
 PARTITION p202110 VALUES LESS THAN (738460) ENGINE = InnoDB,
 PARTITION p202111 VALUES LESS THAN (738490) ENGINE = InnoDB,
 PARTITION p202112 VALUES LESS THAN (738521) ENGINE = InnoDB,
 PARTITION p202201 VALUES LESS THAN (738552) ENGINE = InnoDB,
 PARTITION p202202 VALUES LESS THAN (738580) ENGINE = InnoDB,
 PARTITION P202303 VALUES LESS THAN (738976) ENGINE = InnoDB,
 PARTITION P202304 VALUES LESS THAN (739006) ENGINE = InnoDB,
 PARTITION P202305 VALUES LESS THAN (739037) ENGINE = InnoDB,
 PARTITION P202306 VALUES LESS THAN (739067) ENGINE = InnoDB,
 PARTITION p202307 VALUES LESS THAN MAXVALUE ENGINE = InnoDB) */;

/*Data for the table `tbl_pms_operate_log` */

insert  into `tbl_pms_operate_log`(`ID`,`CREATE_TIME`,`LOGIN_NAME`,`OPERATE_TYPE`,`CONTENT`) values 
(1,'2021-05-06 15:33:04','admin',3,'添加角色，角色名称[加油站通用]'),
(2,'2021-05-06 16:42:30','admin',1,'登录成功，IP=127.0.0.1'),
(3,'2021-05-06 16:46:11','admin',3,'添加功能[运维管理]'),
(4,'2021-05-06 16:46:23','admin',4,'修改功能，功能名称[运维管理]'),
(5,'2021-05-06 16:46:40','admin',4,'修改功能，功能名称[运维管理]'),
(6,'2021-05-06 16:47:33','admin',3,'添加功能[定时任务]'),
(7,'2021-05-06 16:48:33','admin',3,'添加功能[消息轨迹]'),
(8,'2021-05-06 16:49:34','admin',3,'添加功能[运营邮件]'),
(9,'2021-05-06 16:50:11','admin',3,'添加功能[添加任务]'),
(10,'2021-05-06 16:50:37','admin',4,'修改功能，功能名称[添加]'),
(11,'2021-05-06 16:50:48','admin',3,'添加功能[编辑]'),
(12,'2021-05-06 16:51:30','admin',3,'添加功能[触发]'),
(13,'2021-05-06 16:51:46','admin',3,'添加功能[暂停]'),
(14,'2021-05-06 16:51:59','admin',3,'添加功能[恢复]'),
(15,'2021-05-06 16:52:18','admin',3,'添加功能[删除]'),
(16,'2021-05-06 16:53:12','admin',3,'添加功能[实例管理]'),
(17,'2021-05-06 16:53:34','admin',3,'添加功能[操作日志]'),
(18,'2021-05-06 16:54:37','admin',3,'添加功能[管理]'),
(19,'2021-05-06 16:55:05','admin',3,'添加功能[管理]'),
(20,'2021-05-06 16:55:09','admin',2,'退出成功，IP=127.0.0.1'),
(21,'2021-05-06 16:55:21','admin',1,'登录成功，IP=127.0.0.1'),
(22,'2021-05-06 16:56:57','admin',4,'修改功能，功能名称[定时任务]'),
(23,'2021-05-06 16:57:12','admin',4,'修改功能，功能名称[消息轨迹]'),
(24,'2021-05-06 16:57:21','admin',4,'修改功能，功能名称[运营邮件]'),
(25,'2021-05-06 16:57:36','admin',2,'退出成功，IP=127.0.0.1'),
(26,'2021-05-06 16:57:42','admin',1,'登录成功，IP=127.0.0.1'),
(27,'2021-05-06 17:31:27','admin',4,'修改功能，功能名称[运维管理]'),
(28,'2021-05-06 17:31:41','admin',4,'修改功能，功能名称[定时任务]'),
(29,'2021-05-06 17:32:54','admin',4,'修改功能，功能名称[运营邮件]'),
(30,'2021-05-06 17:33:45','admin',4,'修改功能，功能名称[消息轨迹]'),
(31,'2021-05-06 17:35:05','admin',4,'修改功能，功能名称[消息轨迹]'),
(32,'2021-05-06 18:23:33','admin',1,'登录成功，IP=127.0.0.1'),
(33,'2021-05-06 18:24:41','admin',3,'添加功能[商户通知]'),
(34,'2021-05-06 18:25:11','admin',3,'添加功能[通知管理]'),
(35,'2021-05-06 18:25:31','admin',2,'退出成功，IP=127.0.0.1'),
(36,'2021-05-06 18:25:39','admin',1,'登录成功，IP=127.0.0.1'),
(37,'2021-05-07 11:49:20','admin',1,'登录成功，IP=127.0.0.1'),
(38,'2021-05-07 12:44:15','admin',1,'登录成功，IP=127.0.0.1'),
(39,'2021-05-07 12:56:57','admin',1,'登录成功，IP=127.0.0.1'),
(40,'2021-05-07 13:14:35','admin',2,'退出成功，IP=127.0.0.1'),
(41,'2021-05-07 13:14:43','admin',1,'登录成功，IP=127.0.0.1'),
(42,'2021-05-07 14:09:51','admin',1,'登录成功，IP=127.0.0.1'),
(43,'2021-05-07 14:12:45','admin',4,'修改功能，功能名称[商户信息]'),
(44,'2021-05-07 14:13:03','admin',4,'修改功能，功能名称[产品开通]'),
(45,'2021-05-07 14:14:27','admin',4,'修改功能，功能名称[商户通知]'),
(46,'2021-05-07 18:10:43','admin',1,'登录成功，IP=127.0.0.1'),
(47,'2021-05-08 09:06:35','admin',1,'登录成功，IP=127.0.0.1'),
(48,'2021-05-08 18:16:58','admin',1,'登录成功，IP=127.0.0.1'),
(49,'2021-05-08 18:55:00','admin',1,'登录成功，IP=127.0.0.1'),
(50,'2021-05-08 19:42:15','admin',1,'登录成功，IP=127.0.0.1'),
(51,'2021-05-08 19:51:01','admin',1,'登录成功，IP=127.0.0.1'),
(52,'2021-05-08 19:59:27','admin',3,'添加功能[基础配置]'),
(53,'2021-05-08 19:59:35','admin',5,'删除功能，功能名称[产品开通]'),
(54,'2021-05-08 20:01:31','admin',1,'登录成功，IP=127.0.0.1'),
(55,'2021-05-08 20:02:27','admin',3,'添加功能[产品管理]'),
(56,'2021-05-08 20:03:52','admin',3,'添加功能[产品开通]'),
(57,'2021-05-08 20:18:28','admin',1,'登录成功，IP=127.0.0.1'),
(58,'2021-05-08 20:34:38','admin',2,'退出成功，IP=127.0.0.1'),
(59,'2021-05-08 20:34:44','admin',1,'登录成功，IP=127.0.0.1'),
(60,'2021-05-08 20:37:34','admin',3,'添加功能[添加]'),
(61,'2021-05-08 20:37:53','admin',3,'添加功能[修改]'),
(62,'2021-05-08 20:37:58','admin',4,'修改功能，功能名称[添加]'),
(63,'2021-05-08 20:38:16','admin',3,'添加功能[添加]'),
(64,'2021-05-08 20:38:24','admin',3,'添加功能[修改]'),
(65,'2021-05-08 20:38:34','admin',2,'退出成功，IP=127.0.0.1'),
(66,'2021-05-08 20:38:39','admin',1,'登录成功，IP=127.0.0.1'),
(67,'2021-05-08 22:20:11','admin',1,'登录成功，IP=127.0.0.1'),
(68,'2021-05-08 23:11:55','admin',4,'修改功能，功能名称[产品管理]'),
(69,'2021-05-08 23:12:06','admin',4,'修改功能，功能名称[产品开通]'),
(70,'2021-05-12 09:58:15','admin',1,'登录成功，IP=127.0.0.1'),
(71,'2021-05-12 11:21:28','admin',1,'登录成功，IP=127.0.0.1'),
(72,'2021-05-12 12:50:18','admin',1,'登录成功，IP=127.0.0.1'),
(73,'2021-05-12 12:54:18','admin',4,'修改功能，功能名称[基础配置]'),
(74,'2021-05-17 16:04:07','admin',1,'登录成功，IP=127.0.0.1'),
(75,'2021-05-17 17:40:30','admin',1,'登录成功，IP=127.0.0.1'),
(76,'2021-05-18 10:35:05','admin',1,'登录成功，IP=127.0.0.1'),
(77,'2021-05-18 11:25:18','admin',2,'退出成功，IP=127.0.0.1'),
(78,'2021-05-18 11:25:29','admin',1,'登录成功，IP=127.0.0.1'),
(79,'2021-05-18 11:25:34','admin',2,'退出成功，IP=127.0.0.1'),
(80,'2021-05-18 11:25:44','admin',1,'登录成功，IP=127.0.0.1'),
(81,'2021-05-18 12:40:28','admin',1,'登录成功，IP=127.0.0.1'),
(82,'2021-05-18 16:24:41','admin',1,'登录成功，IP=127.0.0.1'),
(83,'2021-05-18 16:27:14','admin',2,'退出成功，IP=127.0.0.1'),
(84,'2021-05-18 16:27:31','admin',1,'登录成功，IP=127.0.0.1'),
(85,'2021-05-18 16:37:11','admin',1,'登录成功，IP=127.0.0.1'),
(86,'2021-05-18 16:50:28','admin',1,'登录成功，IP=127.0.0.1'),
(87,'2021-05-18 18:09:13','admin',1,'登录成功，IP=127.0.0.1'),
(88,'2021-05-19 09:13:47','admin',1,'登录成功，IP=127.0.0.1'),
(89,'2021-05-19 09:21:12','admin',3,'添加权限[后台管理]'),
(90,'2021-05-19 09:22:05','admin',3,'添加权限[权限管理]'),
(91,'2021-05-19 09:23:02','admin',3,'添加权限[角色管理]'),
(92,'2021-05-19 09:23:28','admin',3,'添加权限[用户管理]'),
(93,'2021-05-19 09:23:49','admin',3,'添加权限[权限回收]'),
(94,'2021-05-19 09:24:06','admin',3,'添加权限[添加]'),
(95,'2021-05-19 09:24:16','admin',3,'添加权限[修改]'),
(96,'2021-05-19 09:24:28','admin',3,'添加权限[删除]'),
(97,'2021-05-19 09:24:40','admin',3,'添加权限[添加]'),
(98,'2021-05-19 09:24:51','admin',3,'添加权限[修改]'),
(99,'2021-05-19 09:25:05','admin',3,'添加权限[删除]'),
(100,'2021-05-19 09:25:18','admin',3,'添加权限[分配权限]'),
(101,'2021-05-19 09:25:41','admin',3,'添加权限[添加]'),
(102,'2021-05-19 09:25:53','admin',3,'添加权限[修改]'),
(103,'2021-05-19 09:26:09','admin',3,'添加权限[审核/冻结]'),
(104,'2021-05-19 09:26:27','admin',3,'添加权限[重置密码]'),
(105,'2021-05-19 09:26:43','admin',3,'添加权限[关联角色]'),
(106,'2021-05-19 09:28:04','admin',3,'添加权限[操作日志]'),
(107,'2021-05-19 09:28:17','admin',4,'修改权限，权限名称[操作日志]'),
(108,'2021-05-19 09:28:28','admin',5,'删除权限，权限名称[操作日志]'),
(109,'2021-05-19 09:28:55','admin',3,'添加权限[提交任务]'),
(110,'2021-05-19 09:29:12','admin',5,'删除权限，权限名称[提交任务]'),
(111,'2021-05-19 09:29:15','admin',5,'删除权限，权限名称[权限回收]'),
(112,'2021-05-19 09:29:26','admin',5,'删除权限，权限名称[关联角色]'),
(113,'2021-05-19 09:29:28','admin',5,'删除权限，权限名称[重置密码]'),
(114,'2021-05-19 09:29:30','admin',5,'删除权限，权限名称[审核/冻结]'),
(115,'2021-05-19 09:29:32','admin',5,'删除权限，权限名称[修改]'),
(116,'2021-05-19 09:29:35','admin',5,'删除权限，权限名称[添加]'),
(117,'2021-05-19 09:29:38','admin',5,'删除权限，权限名称[用户管理]'),
(118,'2021-05-19 09:29:47','admin',5,'删除权限，权限名称[分配权限]'),
(119,'2021-05-19 09:29:49','admin',5,'删除权限，权限名称[删除]'),
(120,'2021-05-19 09:29:51','admin',5,'删除权限，权限名称[修改]'),
(121,'2021-05-19 09:29:53','admin',5,'删除权限，权限名称[添加]'),
(122,'2021-05-19 09:29:55','admin',5,'删除权限，权限名称[角色管理]'),
(123,'2021-05-19 09:30:03','admin',5,'删除权限，权限名称[删除]'),
(124,'2021-05-19 09:30:05','admin',5,'删除权限，权限名称[修改]'),
(125,'2021-05-19 09:30:07','admin',5,'删除权限，权限名称[添加]'),
(126,'2021-05-19 09:30:09','admin',5,'删除权限，权限名称[权限管理]'),
(127,'2021-05-19 09:30:12','admin',5,'删除权限，权限名称[商户后台管理]'),
(128,'2021-05-19 09:32:08','admin',4,'修改权限，权限名称[后台管理]'),
(129,'2021-05-19 09:33:40','admin',4,'修改权限，权限名称[后台管理]'),
(130,'2021-05-19 09:34:01','admin',1,'登录成功，IP=127.0.0.1'),
(131,'2021-05-19 09:42:39','admin',4,'修改权限，权限名称[商户管理]'),
(132,'2021-05-19 09:42:46','admin',4,'修改权限，权限名称[商户信息]'),
(133,'2021-05-19 09:48:28','admin',3,'添加权限[运维管理]'),
(134,'2021-05-19 09:48:52','admin',3,'添加权限[定时任务]'),
(135,'2021-05-19 09:49:25','admin',3,'添加权限[消息轨迹]'),
(136,'2021-05-19 09:49:42','admin',3,'添加权限[添加]'),
(137,'2021-05-19 09:49:54','admin',3,'添加权限[编辑]'),
(138,'2021-05-19 09:50:06','admin',3,'添加权限[触发]'),
(139,'2021-05-19 09:50:17','admin',3,'添加权限[暂停]'),
(140,'2021-05-19 09:50:30','admin',3,'添加权限[恢复]'),
(141,'2021-05-19 09:50:48','admin',3,'添加权限[删除]'),
(142,'2021-05-19 09:51:07','admin',3,'添加权限[实例管理]'),
(143,'2021-05-19 09:51:24','admin',3,'添加权限[操作日志]'),
(144,'2021-05-19 09:51:44','admin',3,'添加权限[管理]'),
(145,'2021-05-19 09:51:56','admin',5,'删除权限，权限名称[管理]'),
(146,'2021-05-19 09:51:58','admin',5,'删除权限，权限名称[消息轨迹]'),
(147,'2021-05-19 09:52:23','admin',5,'删除权限，权限名称[操作日志]'),
(148,'2021-05-19 09:52:25','admin',5,'删除权限，权限名称[实例管理]'),
(149,'2021-05-19 09:52:27','admin',5,'删除权限，权限名称[删除]'),
(150,'2021-05-19 09:52:29','admin',5,'删除权限，权限名称[恢复]'),
(151,'2021-05-19 09:52:31','admin',5,'删除权限，权限名称[暂停]'),
(152,'2021-05-19 09:52:34','admin',5,'删除权限，权限名称[触发]'),
(153,'2021-05-19 09:52:36','admin',5,'删除权限，权限名称[编辑]'),
(154,'2021-05-19 09:52:38','admin',5,'删除权限，权限名称[添加]'),
(155,'2021-05-19 09:52:40','admin',5,'删除权限，权限名称[定时任务]'),
(156,'2021-05-19 09:56:17','admin',3,'添加权限[运营邮件]'),
(157,'2021-05-19 09:56:36','admin',3,'添加权限[管理]'),
(158,'2021-05-19 09:56:49','admin',5,'删除权限，权限名称[管理]'),
(159,'2021-05-19 09:56:52','admin',5,'删除权限，权限名称[运营邮件]'),
(160,'2021-05-19 09:56:54','admin',5,'删除权限，权限名称[运维管理]'),
(161,'2021-05-19 10:01:44','admin',1,'登录成功，IP=127.0.0.1'),
(162,'2021-05-19 10:04:01','admin',2,'退出成功，IP=127.0.0.1'),
(163,'2021-05-19 10:04:06','admin',1,'登录成功，IP=127.0.0.1'),
(164,'2021-05-19 10:46:49','admin',2,'退出成功，IP=127.0.0.1'),
(165,'2021-05-19 14:25:57','admin',1,'登录成功，IP=127.0.0.1'),
(166,'2021-05-19 14:38:11','admin',1,'登录成功，IP=127.0.0.1'),
(167,'2021-05-19 14:41:24','admin',1,'登录成功，IP=127.0.0.1'),
(170,'2021-05-19 14:42:27','admin',4,'修改角色[加油站通用]的权限，权限ID[[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22]]'),
(173,'2021-05-19 15:16:50','admin',4,'修改商户后台权限，权限名称[数据统计]'),
(174,'2021-05-19 15:17:14','admin',4,'修改商户后台权限，权限名称[商户中心]'),
(175,'2021-05-19 15:17:56','admin',4,'修改商户后台权限，权限名称[系统设置]'),
(176,'2021-05-19 15:18:35','admin',4,'修改商户后台权限，权限名称[商户信息]'),
(177,'2021-05-19 15:45:39','admin',1,'登录成功，IP=127.0.0.1'),
(178,'2021-05-19 15:48:14','admin',4,'修改商户后台权限，权限名称[商户信息]'),
(179,'2021-05-19 15:51:26','admin',1,'登录成功，IP=127.0.0.1'),
(180,'2021-05-19 15:51:54','admin',4,'修改商户后台权限，权限名称[首页]'),
(181,'2021-05-19 15:52:13','admin',4,'修改商户后台权限，权限名称[数据统计]'),
(182,'2021-05-19 15:52:37','admin',4,'修改商户后台权限，权限名称[商户中心]'),
(183,'2021-05-19 15:52:48','admin',4,'修改商户后台权限，权限名称[商户中心]'),
(184,'2021-05-19 15:53:00','admin',4,'修改商户后台权限，权限名称[商户中心]'),
(185,'2021-05-19 15:53:34','admin',4,'修改商户后台权限，权限名称[系统设置]'),
(186,'2021-05-19 15:54:27','admin',4,'修改商户后台权限，权限名称[数据统计]'),
(187,'2021-05-19 15:56:13','admin',4,'修改商户后台权限，权限名称[数据统计]'),
(188,'2021-05-19 16:15:49','admin',4,'修改商户后台权限，权限名称[数据统计]'),
(189,'2021-05-19 16:16:22','admin',4,'修改商户后台权限，权限名称[数据统计]'),
(190,'2021-05-19 16:16:53','admin',4,'修改商户后台权限，权限名称[数据统计]'),
(191,'2021-05-19 16:19:08','admin',4,'修改商户后台权限，权限名称[数据统计]'),
(192,'2021-05-19 16:59:58','admin',1,'登录成功，IP=127.0.0.1'),
(193,'2021-05-19 17:21:03','admin',1,'登录成功，IP=127.0.0.1'),
(194,'2021-05-19 18:09:19','admin',1,'登录成功，IP=127.0.0.1'),
(195,'2021-05-19 18:17:47','admin',1,'登录成功，IP=127.0.0.1'),
(196,'2021-05-19 18:19:10','admin',1,'登录成功，IP=127.0.0.1'),
(197,'2021-05-19 18:21:18','admin',1,'登录成功，IP=127.0.0.1'),
(198,'2021-05-19 18:32:41','admin',1,'登录成功，IP=127.0.0.1'),
(199,'2021-05-19 18:33:08','admin',1,'登录成功，IP=127.0.0.1'),
(200,'2021-05-20 09:11:21','admin',1,'登录成功，IP=127.0.0.1'),
(201,'2021-05-20 09:18:15','admin',1,'登录成功，IP=127.0.0.1'),
(202,'2021-05-20 09:35:24','admin',1,'登录成功，IP=127.0.0.1'),
(203,'2021-05-20 09:56:40','admin',1,'登录成功，IP=127.0.0.1'),
(204,'2021-05-20 10:06:02','admin',1,'登录成功，IP=127.0.0.1'),
(205,'2021-05-20 10:52:39','admin',1,'登录成功，IP=127.0.0.1'),
(206,'2021-05-20 10:55:13','admin',1,'登录成功，IP=127.0.0.1'),
(207,'2021-05-20 11:25:55','admin',1,'登录成功，IP=127.0.0.1'),
(208,'2021-05-26 15:59:55','admin',1,'登录成功，IP=127.0.0.1'),
(209,'2021-05-26 16:04:04','admin',3,'添加权限[上线管理]'),
(210,'2021-05-26 16:04:47','admin',3,'添加权限[发布上线]'),
(211,'2021-05-26 16:05:35','admin',3,'添加权限[流量切换]'),
(212,'2021-05-26 16:05:54','admin',3,'添加权限[机房同步]'),
(213,'2021-05-26 16:06:12','admin',2,'退出成功，IP=127.0.0.1'),
(214,'2021-05-26 16:06:18','admin',1,'登录成功，IP=127.0.0.1'),
(215,'2021-05-26 17:26:13','admin',1,'登录成功，IP=127.0.0.1'),
(216,'2021-05-26 17:50:10','admin',3,'发布项目 commitMsg=测试项目发布,idc=sz'),
(217,'2021-05-27 10:46:38','admin',1,'登录成功，IP=127.0.0.1'),
(218,'2021-05-27 10:52:38','admin',3,'发布项目 commitMsg=测试和jenkins连通,idc=sz'),
(219,'2021-05-27 11:18:39','admin',3,'发布项目 commitMsg=测试和jenkins连通2,idc=zj'),
(220,'2021-05-27 13:05:50','admin',1,'登录成功，IP=127.0.0.1'),
(221,'2021-05-27 14:02:24','admin',3,'发布项目 commitMsg=测试任务取消,idc=sz'),
(222,'2021-05-27 14:04:04','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(223,'2021-05-27 14:31:26','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(224,'2021-05-27 14:33:33','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(225,'2021-05-27 14:42:53','admin',3,'发布项目 commitMsg=测试队列取消,idc=sz'),
(226,'2021-05-27 14:43:00','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(227,'2021-05-27 14:47:12','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(228,'2021-05-27 17:19:33','admin',1,'登录成功，IP=127.0.0.1'),
(229,'2021-05-27 17:24:33','admin',4,'修改权限，权限名称[管理]'),
(230,'2021-05-27 17:24:42','admin',4,'修改权限，权限名称[上线管理]'),
(231,'2021-05-27 17:24:51','admin',4,'修改权限，权限名称[发布管理]'),
(232,'2021-05-27 17:24:56','admin',2,'退出成功，IP=127.0.0.1'),
(233,'2021-05-27 17:25:04','admin',1,'登录成功，IP=127.0.0.1'),
(234,'2021-05-27 17:25:20','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(235,'2021-05-27 17:25:30','admin',4,'取消任务发布 publishId=5'),
(236,'2021-05-27 17:30:48','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(237,'2021-05-27 17:31:47','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(238,'2021-05-27 17:32:06','admin',4,'取消任务发布 publishId=5'),
(239,'2021-05-27 17:34:57','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(240,'2021-05-27 18:10:02','admin',1,'登录成功，IP=127.0.0.1'),
(241,'2021-05-27 18:11:09','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(242,'2021-05-27 18:12:08','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(243,'2021-05-27 18:12:12','admin',4,'取消任务发布 publishId=5'),
(244,'2021-05-27 18:15:48','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(245,'2021-05-27 18:16:05','admin',4,'取消任务发布 publishId=5'),
(246,'2021-05-27 18:16:59','admin',4,'重新发布项目 publishId=5,commitMsg=测试队列取消,idc=sz'),
(247,'2021-05-27 18:25:15','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(248,'2021-05-27 18:26:15','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(249,'2021-05-27 18:27:04','admin',4,'取消任务发布 publishId=4'),
(250,'2021-05-27 18:36:30','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(251,'2021-05-27 18:39:55','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(252,'2021-05-27 18:43:43','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(253,'2021-05-27 18:44:24','admin',4,'取消任务发布 publishId=4'),
(254,'2021-05-27 18:47:41','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(255,'2021-05-27 18:51:15','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(256,'2021-05-27 18:51:47','admin',4,'取消任务发布 publishId=4'),
(257,'2021-05-28 09:21:52','admin',1,'登录成功，IP=127.0.0.1'),
(258,'2021-05-28 10:10:35','admin',4,'重新发布项目 publishId=4,commitMsg=测试任务取消,idc=sz'),
(259,'2021-05-28 10:31:32','admin',3,'发布项目 commitMsg=测试项目连贯性和取消,idc=sz'),
(260,'2021-05-28 10:34:34','admin',4,'取消任务发布 publishId=6'),
(261,'2021-05-28 10:40:34','admin',4,'重新发布项目 publishId=6,commitMsg=测试项目连贯性和取消,idc=sz'),
(262,'2021-05-28 10:42:16','admin',4,'取消任务发布 publishId=6'),
(263,'2021-05-28 10:42:30','admin',4,'重新发布项目 publishId=6,commitMsg=测试项目连贯性和取消,idc=sz');

/*Table structure for table `tbl_pms_role` */

DROP TABLE IF EXISTS `tbl_pms_role`;

CREATE TABLE `tbl_pms_role` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `VERSION` int NOT NULL DEFAULT '0' COMMENT 'VERSION',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `ROLE_NAME` varchar(90) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `REMARK` varchar(300) COLLATE utf8mb4_general_ci NOT NULL COMMENT '描述',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='pms角色表';

/*Data for the table `tbl_pms_role` */

insert  into `tbl_pms_role`(`ID`,`VERSION`,`CREATE_TIME`,`ROLE_NAME`,`REMARK`) values 
(2,0,'2022-02-08 15:53:13','第1个角色','角色1'),
(3,1,'2022-02-08 16:04:29','第2个角色2','角色2'),
(5,0,'2022-03-01 16:34:27','角色3',''),
(6,0,'2022-03-01 16:34:31','角色4',''),
(7,0,'2022-03-01 16:34:35','角色5',''),
(8,0,'2022-03-01 16:34:39','角色6',''),
(9,0,'2022-03-01 16:34:47','第7个角色',''),
(10,0,'2022-03-01 16:34:51','第8个角色',''),
(11,0,'2022-03-01 16:34:55','第9个角色',''),
(12,0,'2022-03-01 16:34:59','第10个角色','');

/*Table structure for table `tbl_pms_role_auth` */

DROP TABLE IF EXISTS `tbl_pms_role_auth`;

CREATE TABLE `tbl_pms_role_auth` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `ROLE_ID` bigint NOT NULL COMMENT '角色ID',
  `AUTH_ID` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_ROLE_AUTH_ID` (`ROLE_ID`,`AUTH_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=265 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='pms角色与权限关联表';

/*Data for the table `tbl_pms_role_auth` */

insert  into `tbl_pms_role_auth`(`ID`,`ROLE_ID`,`AUTH_ID`) values 
(73,2,1),
(74,2,2),
(75,2,3),
(76,2,4),
(77,2,5),
(78,2,6),
(79,2,7),
(80,2,8),
(81,2,9),
(82,2,10),
(83,2,11),
(84,2,12),
(85,2,13),
(86,2,14),
(87,2,15),
(88,2,16),
(89,2,17),
(90,2,18),
(91,2,39),
(92,2,71),
(93,2,72),
(94,2,73),
(95,2,74),
(96,2,75),
(97,2,76),
(98,2,77),
(99,2,78),
(100,2,79),
(101,2,80),
(102,2,81),
(103,2,82),
(104,2,83),
(105,2,84),
(106,2,85),
(107,2,86),
(108,2,87),
(109,2,88),
(110,2,89),
(233,3,1),
(234,3,18),
(235,3,39),
(236,3,62),
(237,3,64),
(238,3,65),
(239,3,66),
(240,3,67),
(241,3,68),
(242,3,69),
(243,3,70),
(244,3,71),
(245,3,72),
(246,3,73),
(247,3,74),
(248,3,75),
(249,3,76),
(250,3,77),
(251,3,78),
(252,3,79),
(253,3,80),
(254,3,81),
(255,3,82),
(256,3,83),
(257,3,84),
(258,3,85),
(259,3,86),
(260,3,87),
(261,3,88),
(262,3,89),
(263,3,102),
(264,3,103);

/*Table structure for table `tbl_pms_role_user` */

DROP TABLE IF EXISTS `tbl_pms_role_user`;

CREATE TABLE `tbl_pms_role_user` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `ROLE_ID` bigint NOT NULL COMMENT '角色ID',
  `USER_ID` bigint NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_role_id_user_id` (`ROLE_ID`,`USER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='pms操作员与角色关联表';

/*Data for the table `tbl_pms_role_user` */

insert  into `tbl_pms_role_user`(`ID`,`ROLE_ID`,`USER_ID`) values 
(2,7,2),
(3,12,2);

/*Table structure for table `tbl_pms_user` */

DROP TABLE IF EXISTS `tbl_pms_user`;

CREATE TABLE `tbl_pms_user` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `VERSION` int NOT NULL DEFAULT '0' COMMENT 'VERSION',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `LOGIN_NAME` varchar(80) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录名',
  `LOGIN_PWD` varchar(256) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码',
  `REMARK` varchar(300) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `REAL_NAME` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '真实姓名',
  `MOBILE_NO` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码',
  `EMAIL` varchar(100) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '邮箱',
  `STATUS` tinyint(1) NOT NULL COMMENT '状态',
  `TYPE` smallint NOT NULL COMMENT '用户类型（1:超级管理员，2:普通用户）',
  `CREATOR` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建者',
  `MODIFIER` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改者',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_login_name` (`LOGIN_NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='pms用户表';

/*Data for the table `tbl_pms_user` */

insert  into `tbl_pms_user`(`ID`,`VERSION`,`CREATE_TIME`,`LOGIN_NAME`,`LOGIN_PWD`,`REMARK`,`REAL_NAME`,`MOBILE_NO`,`EMAIL`,`STATUS`,`TYPE`,`CREATOR`,`MODIFIER`) values 
(1,8,'2021-01-08 18:21:56','admin','c78b6663d47cfbdb4d65ea51c104044e',NULL,'超级管理员','13800138000','pms_admin@xpay.com',1,1,'system','system'),
(2,16,'2022-02-08 16:03:43','user01','e388fb3fbc9cb74fb65a610ba3dffe95','张三','张三‧尼古拉斯','13800138001','zhangsan@xpay.com',1,2,'admin','admin');

/*Table structure for table `tbl_portal_auth` */

DROP TABLE IF EXISTS `tbl_portal_auth`;

CREATE TABLE `tbl_portal_auth` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `VERSION` int NOT NULL DEFAULT '0' COMMENT 'VERSION',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `NAME` varchar(90) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '名称',
  `NUMBER` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '序号',
  `PARENT_ID` bigint NOT NULL DEFAULT '0' COMMENT '父节点，一级菜单为0',
  `PERMISSION_FLAG` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '权限标识',
  `AUTH_TYPE` smallint NOT NULL COMMENT '权限类型(1:菜单 2:功能)',
  `URL` varchar(150) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '菜单URL(AUTH_TYPE为1时有值)',
  `ICON` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='商户权限表';

/*Data for the table `tbl_portal_auth` */

insert  into `tbl_portal_auth`(`ID`,`VERSION`,`CREATE_TIME`,`NAME`,`NUMBER`,`PARENT_ID`,`PERMISSION_FLAG`,`AUTH_TYPE`,`URL`,`ICON`) values 
(1,3,'2021-01-19 11:44:07','首页','001',0,'home:index:view',1,'home','el-icon-s-data'),
(2,10,'2021-01-19 11:45:29','数据统计','001001',1,'home:data:view',1,'/home','el-icon-data-analysis'),
(3,4,'2021-01-19 11:46:25','商户中心','999',0,'sys:sys:manage',1,'/sysManage','el-icon-user'),
(4,2,'2021-01-19 11:47:18','系统设置','999001',3,'sys:sys:setup',1,'/sysManage/sysSetup','el-icon-s-tools'),
(5,1,'2021-01-19 11:50:43','用户管理','999001001',4,'sys:user:view',1,'/sysManage/sysSetup/userManage','user'),
(6,1,'2021-01-19 11:52:32','角色管理','999001002',4,'sys:role:view',1,'/sysManage/sysSetup/roleManage','role'),
(7,1,'2021-01-19 11:53:46','操作日志','999001003',4,'sys:operateLog:view',1,'/sysManage/sysSetup/operateLog','log'),
(8,0,'2021-01-19 11:55:41','添加','999001001001',5,'sys:user:add',2,'',''),
(9,0,'2021-01-19 11:56:02','编辑','999001001002',5,'sys:user:edit',2,'',''),
(10,0,'2021-01-19 11:56:58','更改状态','999001001003',5,'sys:user:changeStatus',2,'',''),
(11,0,'2021-01-19 11:57:14','重置密码','999001001004',5,'sys:user:resetPwd',2,'',''),
(12,0,'2021-01-19 11:57:47','删除','999001001005',5,'sys:user:delete',2,'',''),
(13,0,'2021-01-19 11:58:14','分配角色','999001001006',5,'sys:user:assignRoles',2,'',''),
(14,0,'2021-01-19 11:58:42','添加','999001002001',6,'sys:role:add',2,'',''),
(15,0,'2021-01-19 11:58:56','编辑','999001002002',6,'sys:role:edit',2,'',''),
(16,0,'2021-01-19 11:59:17','删除','999001002003',6,'sys:role:delete',2,'',''),
(17,0,'2021-01-19 11:59:34','分配权限','999001002004',6,'sys:role:assignAuth',2,'',''),
(18,2,'2021-01-19 12:03:39','商户信息','999002',3,'merchant:mchInfo:view',1,'/sysManage/mchInfo','el-icon-s-custom'),
(19,0,'2021-01-19 12:46:04','安全中心','999002001',18,'merchant:mchInfo:securityCenter',1,'/sysManage/mchInfo/security',''),
(20,0,'2021-01-19 12:47:56','修改支付密码','999002001001',19,'merchant:security:changeTradePwd',2,'',''),
(21,0,'2021-01-19 12:49:03','密钥管理','999002002',18,'merchant:security:secretKeyManage',1,'/sysManage/mchInfo/secretKey',''),
(22,0,'2021-01-21 11:28:42','修改密钥','999002002001',21,'merchant:security:changeSecretKey',2,'',''),
(31,3,'2022-02-11 14:06:26','样例演示','003',0,'examples:module',1,'/examples','el-icon-sugar'),
(32,3,'2022-02-11 14:07:52','语音播报','003001',31,'examples:audio:transfer',1,'/examples/audioPlay','el-icon-bell'),
(33,0,'2022-02-25 15:36:39','重置支付密码','999002001002',19,'merchant:security:resetTradePwd',2,'',''),
(34,1,'2022-03-07 10:37:59','上传下载','003002',31,'examples:file:operate',1,'/examples/fileOperate','el-icon-upload2'),
(35,0,'2022-03-08 13:14:20','上传','003002001',34,'examples:file:upload',2,'',''),
(36,0,'2022-03-08 13:14:35','下载','003002002',34,'examples:file:download',2,'',''),
(37,0,'2022-03-08 13:14:48','删除','003002003',34,'examples:file:delete',2,'','');

/*Table structure for table `tbl_portal_operate_log` */

DROP TABLE IF EXISTS `tbl_portal_operate_log`;

CREATE TABLE `tbl_portal_operate_log` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `CREATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `LOGIN_NAME` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录名',
  `MCH_NO` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户编号',
  `OPERATE_TYPE` smallint NOT NULL COMMENT '操作类型(1=登录 2=退出 3=添加 4=修改 5=删除 6=查询)',
  `STATUS` smallint NOT NULL COMMENT '操作状态（1:成功，-1:失败）',
  `IP` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'IP地址',
  `CONTENT` text COLLATE utf8mb4_general_ci COMMENT '操作内容',
  PRIMARY KEY (`ID`,`CREATE_TIME`),
  KEY `IDX_LOGIN_NAME` (`LOGIN_NAME`),
  KEY `IDX_MCH_NO` (`MCH_NO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='商户操作日志表'
/*!50100 PARTITION BY RANGE (to_days(`CREATE_TIME`))
(PARTITION p202005 VALUES LESS THAN (737942) ENGINE = InnoDB,
 PARTITION p202006 VALUES LESS THAN (737972) ENGINE = InnoDB,
 PARTITION p202007 VALUES LESS THAN (738003) ENGINE = InnoDB,
 PARTITION p202008 VALUES LESS THAN (738034) ENGINE = InnoDB,
 PARTITION p202009 VALUES LESS THAN (738064) ENGINE = InnoDB,
 PARTITION p202010 VALUES LESS THAN (738095) ENGINE = InnoDB,
 PARTITION p202011 VALUES LESS THAN (738125) ENGINE = InnoDB,
 PARTITION p202012 VALUES LESS THAN (738156) ENGINE = InnoDB,
 PARTITION p202101 VALUES LESS THAN (738187) ENGINE = InnoDB,
 PARTITION p202102 VALUES LESS THAN (738215) ENGINE = InnoDB,
 PARTITION p202103 VALUES LESS THAN (738246) ENGINE = InnoDB,
 PARTITION p202104 VALUES LESS THAN (738276) ENGINE = InnoDB,
 PARTITION p202105 VALUES LESS THAN (738307) ENGINE = InnoDB,
 PARTITION p202106 VALUES LESS THAN (738337) ENGINE = InnoDB,
 PARTITION p202107 VALUES LESS THAN (738368) ENGINE = InnoDB,
 PARTITION p202108 VALUES LESS THAN (738399) ENGINE = InnoDB,
 PARTITION p202109 VALUES LESS THAN (738429) ENGINE = InnoDB,
 PARTITION p202110 VALUES LESS THAN (738460) ENGINE = InnoDB,
 PARTITION p202111 VALUES LESS THAN (738490) ENGINE = InnoDB,
 PARTITION p202112 VALUES LESS THAN (738521) ENGINE = InnoDB,
 PARTITION p202201 VALUES LESS THAN (738552) ENGINE = InnoDB,
 PARTITION p202202 VALUES LESS THAN (738580) ENGINE = InnoDB,
 PARTITION P202303 VALUES LESS THAN (738976) ENGINE = InnoDB,
 PARTITION P202304 VALUES LESS THAN (739006) ENGINE = InnoDB,
 PARTITION P202305 VALUES LESS THAN (739037) ENGINE = InnoDB,
 PARTITION P202306 VALUES LESS THAN (739067) ENGINE = InnoDB,
 PARTITION p202307 VALUES LESS THAN MAXVALUE ENGINE = InnoDB) */;

/*Data for the table `tbl_portal_operate_log` */

insert  into `tbl_portal_operate_log`(`ID`,`CREATE_TIME`,`LOGIN_NAME`,`MCH_NO`,`OPERATE_TYPE`,`STATUS`,`IP`,`CONTENT`) values 
(1,'2021-05-19 16:45:29','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(11,'2021-05-19 16:47:28','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色，角色名称[第一角色]'),
(12,'2021-05-19 16:50:32','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','新增用户[portal_normal_user@xpay.com]'),
(13,'2021-05-19 16:50:56','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户状态成功[portal_normal_user@xpay.com],oldStatus1,newStatus:1'),
(14,'2021-05-19 16:50:59','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户状态成功[portal_normal_user@xpay.com],oldStatus2,newStatus:2'),
(15,'2022-02-10 14:17:07','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(16,'2022-02-10 14:49:57','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(17,'2022-02-10 15:35:58','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(18,'2022-02-10 15:36:24','portal_user@xpay.com','100100000001',5,1,'127.0.0.1','删除角色，名称:第一角色'),
(19,'2022-02-10 15:49:02','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644479357104,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六\",\"remark\":\"赵大六\",\"roleIds\":[]}'),
(20,'2022-02-10 15:50:32','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户状态成功[portal_normal_user@xpay.com],oldStatus:1,newStatus:2'),
(21,'2022-02-10 15:50:36','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户状态成功[portal_normal_user@xpay.com],oldStatus:2,newStatus:1'),
(22,'2022-02-10 15:50:54','portal_user@xpay.com','100100000001',3,1,'127.0.0.1','添加角色，角色名称[第一个角色]'),
(23,'2022-02-10 15:51:04','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第一个角色, id: 4, authIds: [1,2,3,4,7,18,19,20]]'),
(24,'2022-02-10 15:58:30','portal_user@xpay.com','100100000001',2,1,'127.0.0.1','退出成功，IP=127.0.0.1'),
(25,'2022-02-10 15:58:38','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(26,'2022-02-10 16:16:06','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户角色[portal_normal_user@xpay.com]，更改后角色[[4]]'),
(27,'2022-02-10 16:16:06','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户角色[portal_normal_user@xpay.com]，更改后角色[[4]]'),
(28,'2022-02-10 16:22:45','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户角色[portal_normal_user@xpay.com]，更改后角色[[4]]'),
(29,'2022-02-10 16:23:00','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644481395999,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六\",\"remark\":\"赵大六\",\"roleIds\":[4]}'),
(30,'2022-02-10 16:28:18','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第一个角色, id: 4, authIds: [1,2,3,4,7,18,19,20]]'),
(31,'2022-02-10 16:28:24','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色，[名称: 第一个角色,id: 4]'),
(32,'2022-02-10 16:28:41','portal_user@xpay.com','100100000001',3,1,'127.0.0.1','添加角色，角色名称[第二个角色]'),
(33,'2022-02-10 16:28:52','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色，[名称: 第一个角色,id: 4]'),
(34,'2022-02-10 16:33:42','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第二个角色, id: 5, authIds: []]'),
(35,'2022-02-10 16:33:46','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第二个角色, id: 5, authIds: [1,2]]'),
(36,'2022-02-10 16:33:49','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第二个角色, id: 5, authIds: [1,2,3,18,21,22]]'),
(37,'2022-02-10 16:33:52','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色，[名称: 第二个角色,id: 5]'),
(38,'2022-02-10 16:33:55','portal_user@xpay.com','100100000001',3,1,'127.0.0.1','添加角色，角色名称[第二个角色]'),
(39,'2022-02-10 16:36:39','portal_user@xpay.com','100100000001',5,1,'127.0.0.1','删除角色，名称:第二个角色'),
(40,'2022-02-10 16:38:30','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色，[名称: 第二个角色,id: 5]'),
(41,'2022-02-10 16:38:51','portal_user@xpay.com','100100000001',3,1,'127.0.0.1','添加角色，角色名称[第三个角色]'),
(42,'2022-02-10 16:42:21','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第三个角色, id: 7, authIds: []]'),
(43,'2022-02-10 16:42:25','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第三个角色, id: 7, authIds: [3,18,19,20]]'),
(44,'2022-02-10 16:42:30','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第三个角色, id: 7, authIds: [3,18,19,20,21,22]]'),
(45,'2022-02-10 16:42:33','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色，[名称: 第三个角色,id: 7]'),
(46,'2022-02-10 16:42:36','portal_user@xpay.com','100100000001',5,1,'127.0.0.1','删除角色，名称:第三个角色'),
(47,'2022-02-10 16:42:48','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644482583882,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六\",\"remark\":\"赵大六\",\"roleIds\":[4]}'),
(48,'2022-02-10 16:42:51','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户角色[portal_normal_user@xpay.com]，更改后角色[[4, 5]]'),
(49,'2022-02-10 16:42:54','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户角色[portal_normal_user@xpay.com]，更改后角色[[4]]'),
(50,'2022-02-10 16:43:27','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644482622549,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六\",\"remark\":\"赵大六\",\"roleIds\":[4]}'),
(51,'2022-02-10 16:43:30','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644482625871,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六\",\"remark\":\"赵大六\",\"roleIds\":[4]}'),
(52,'2022-02-10 16:48:53','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户角色[portal_normal_user@xpay.com]，更改后角色[[4]]'),
(53,'2022-02-10 16:48:55','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644482950587,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六\",\"remark\":\"赵大六\",\"roleIds\":[4]}'),
(54,'2022-02-10 16:48:59','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户角色[portal_normal_user@xpay.com]，更改后角色[[4, 5]]'),
(55,'2022-02-10 16:49:07','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644482962922,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六\",\"remark\":\"赵大六1\",\"roleIds\":[4,5]}'),
(56,'2022-02-10 16:49:14','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644482969596,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六六\",\"remark\":\"赵大六1\",\"roleIds\":[4,5]}'),
(57,'2022-02-10 16:49:19','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户：{\"createTime\":1644482974885,\"email\":\"portal_normal_user@xpay.com\",\"id\":3,\"realName\":\"赵大六\",\"remark\":\"赵大六\",\"roleIds\":[4,5]}'),
(58,'2022-02-10 16:49:27','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改用户角色[portal_normal_user@xpay.com]，更改后角色[[4]]'),
(59,'2022-02-10 16:50:55','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色，[名称: 第二个角色,id: 5]'),
(60,'2022-02-11 14:11:53','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(61,'2022-02-11 14:24:04','portal_user@xpay.com','100100000001',2,1,'127.0.0.1','退出成功，IP=127.0.0.1'),
(62,'2022-02-11 14:24:10','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(63,'2022-02-11 15:41:38','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(64,'2022-02-11 16:14:39','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(65,'2022-02-11 18:04:45','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(66,'2022-02-12 16:22:14','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(67,'2022-02-12 20:32:55','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(68,'2022-02-23 17:20:22','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(69,'2022-02-23 17:22:13','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(70,'2022-02-24 16:28:19','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(71,'2022-02-24 17:30:01','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(72,'2022-02-24 17:35:31','portal_user@xpay.com','100100000001',2,1,'127.0.0.1','退出成功，IP=127.0.0.1'),
(73,'2022-02-24 17:35:39','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(74,'2022-02-24 17:39:35','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改角色权限[名称: 第二个角色, id: 5, authIds: [1,2,3,18,21,22]]'),
(75,'2022-02-24 17:44:56','portal_user@xpay.com','100100000001',2,1,'127.0.0.1','退出成功，IP=127.0.0.1'),
(76,'2022-02-24 17:47:56','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(77,'2022-02-24 17:49:11','portal_user@xpay.com','100100000001',2,1,'127.0.0.1','退出成功，IP=127.0.0.1'),
(78,'2022-02-25 14:47:48','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(79,'2022-02-25 15:36:46','portal_user@xpay.com','100100000001',2,1,'127.0.0.1','退出成功，IP=127.0.0.1'),
(80,'2022-02-25 15:36:53','portal_user@xpay.com','100100000001',1,1,'127.0.0.1','登录成功，IP=127.0.0.1'),
(81,'2022-02-25 15:52:39','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','重置支付密码'),
(82,'2022-02-25 16:12:51','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改支付密码成功'),
(83,'2022-02-25 16:12:51','portal_user@xpay.com','100100000001',4,1,'127.0.0.1','修改支付密码成功'),
(84,'2022-02-25 16:20:23','portal_user@xpay.com','100100000001',2,1,'127.0.0.1','退出成功，IP=127.0.0.1');

/*Table structure for table `tbl_portal_revoke_auth` */

DROP TABLE IF EXISTS `tbl_portal_revoke_auth`;

CREATE TABLE `tbl_portal_revoke_auth` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `VERSION` int NOT NULL DEFAULT '0' COMMENT '版本号',
  `CREATOR` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `REVOKE_TYPE` smallint NOT NULL COMMENT '回收类型',
  `STATUS` tinyint NOT NULL COMMENT '状态',
  `MCH_NOS` json DEFAULT NULL COMMENT '被回收权限的商户编号(json数组)',
  `CURR_MCH_NO` varchar(30) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '当前已回收到的商户编号',
  `OBJECT_KEY` varchar(80) COLLATE utf8mb4_general_ci NOT NULL COMMENT '被操作对象',
  `REMARK` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商户权限回收记录表';

/*Data for the table `tbl_portal_revoke_auth` */

insert  into `tbl_portal_revoke_auth`(`ID`,`CREATE_TIME`,`VERSION`,`CREATOR`,`REVOKE_TYPE`,`STATUS`,`MCH_NOS`,`CURR_MCH_NO`,`OBJECT_KEY`,`REMARK`) values 
(1,'2022-02-10 10:08:49',2,'admin',1,3,'[\"100100000001\"]','','加油站通用','调整角色权限'),
(2,'2022-03-08 11:53:40',3,'admin',1,3,'[\"100100000001\"]','100100000001','加油站通用','调整角色权限'),
(3,'2022-03-08 11:53:58',2,'admin',2,3,'[\"100100000001\"]','','portal_user@xpay.com','调整商户用户的权限');

/*Table structure for table `tbl_portal_role` */

DROP TABLE IF EXISTS `tbl_portal_role`;

CREATE TABLE `tbl_portal_role` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `VERSION` int NOT NULL DEFAULT '0' COMMENT 'VERSION',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `MCH_NO` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户编码(ROLE_TYPE为1时赋值空字符串)',
  `MCH_TYPE` smallint NOT NULL DEFAULT '0' COMMENT '商户类型',
  `ROLE_TYPE` smallint NOT NULL COMMENT '角色类型（1:模版角色，2:普通角色）',
  `AUTO_ASSIGN` tinyint(1) NOT NULL DEFAULT '-1' COMMENT '自动分配(-1:否 1:是)',
  `ROLE_NAME` varchar(90) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `REMARK` varchar(300) COLLATE utf8mb4_general_ci NOT NULL COMMENT '描述',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='商户角色表';

/*Data for the table `tbl_portal_role` */

insert  into `tbl_portal_role`(`ID`,`VERSION`,`CREATE_TIME`,`MCH_NO`,`MCH_TYPE`,`ROLE_TYPE`,`AUTO_ASSIGN`,`ROLE_NAME`,`REMARK`) values 
(1,4,'2021-05-06 15:33:06','',1,1,1,'加油站通用','加油站通用角色'),
(4,2,'2022-02-10 15:51:10','100100000001',1,2,2,'第一个角色','角色1'),
(5,3,'2022-02-10 16:28:57','100100000001',1,2,2,'第二个角色','角色2'),
(8,0,'2022-03-01 16:22:48','100100000001',1,2,2,'第3个角色','第3个角色'),
(9,0,'2022-03-01 16:22:56','100100000001',1,2,2,'第4个角色','第4个角色'),
(10,0,'2022-03-01 16:23:03','100100000001',1,2,2,'第5个角色','第5个角色'),
(11,0,'2022-03-01 16:23:08','100100000001',1,2,2,'第6个角色','第6个角色'),
(12,0,'2022-03-01 16:23:15','100100000001',1,2,2,'第7个角色','第7个角色'),
(13,0,'2022-03-01 16:23:21','100100000001',1,2,2,'第8个角色','第8个角色'),
(14,0,'2022-03-01 16:23:26','100100000001',1,2,2,'第9个角色','第9个角色'),
(15,0,'2022-03-01 16:23:32','100100000001',1,2,2,'第10个角色','第10个角色'),
(16,0,'2022-03-01 16:40:07','100100000001',1,2,2,'第222222222222222222222222222个角色','第222222222222222222222222222个角色');

/*Table structure for table `tbl_portal_role_auth` */

DROP TABLE IF EXISTS `tbl_portal_role_auth`;

CREATE TABLE `tbl_portal_role_auth` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `ROLE_ID` bigint NOT NULL COMMENT '角色ID',
  `AUTH_ID` bigint NOT NULL COMMENT '权限ID',
  `MCH_NO` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商户编号',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_ROLE_AUTH_ID` (`ROLE_ID`,`AUTH_ID`),
  KEY `IDX_MCH_NO` (`MCH_NO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='商户角色与权限关联表';

/*Data for the table `tbl_portal_role_auth` */

insert  into `tbl_portal_role_auth`(`ID`,`ROLE_ID`,`AUTH_ID`,`MCH_NO`) values 
(125,4,1,'100100000001'),
(126,4,2,'100100000001'),
(127,4,3,'100100000001'),
(128,4,4,'100100000001'),
(129,4,7,'100100000001'),
(130,4,18,'100100000001'),
(131,4,19,'100100000001'),
(132,4,20,'100100000001'),
(175,5,1,'100100000001'),
(176,5,2,'100100000001'),
(177,5,3,'100100000001'),
(178,5,18,'100100000001'),
(179,5,21,'100100000001'),
(180,5,22,'100100000001'),
(283,1,1,''),
(284,1,2,''),
(285,1,3,''),
(286,1,4,''),
(287,1,5,''),
(288,1,6,''),
(289,1,7,''),
(290,1,8,''),
(291,1,9,''),
(292,1,10,''),
(293,1,11,''),
(294,1,12,''),
(295,1,13,''),
(296,1,14,''),
(297,1,15,''),
(298,1,16,''),
(299,1,17,''),
(300,1,18,''),
(301,1,19,''),
(302,1,20,''),
(303,1,21,''),
(304,1,22,''),
(305,1,31,''),
(306,1,32,''),
(307,1,33,''),
(308,1,34,''),
(309,1,35,''),
(310,1,36,''),
(311,1,37,'');

/*Table structure for table `tbl_portal_role_user` */

DROP TABLE IF EXISTS `tbl_portal_role_user`;

CREATE TABLE `tbl_portal_role_user` (
  `ID` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `ROLE_ID` bigint NOT NULL COMMENT '角色ID',
  `USER_ID` bigint NOT NULL COMMENT '用户ID',
  `MCH_NO` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商户编号',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_role_id_user_id` (`ROLE_ID`,`USER_ID`),
  KEY `IDX_MCH_NO` (`MCH_NO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='商户用户与角色关联表';

/*Data for the table `tbl_portal_role_user` */

insert  into `tbl_portal_role_user`(`ID`,`ROLE_ID`,`USER_ID`,`MCH_NO`) values 
(12,4,3,'100100000001'),
(13,1,2,'100100000001');

/*Table structure for table `tbl_portal_user` */

DROP TABLE IF EXISTS `tbl_portal_user`;

CREATE TABLE `tbl_portal_user` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `VERSION` int NOT NULL DEFAULT '0' COMMENT 'VERSION',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `LOGIN_NAME` varchar(80) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录名',
  `LOGIN_PWD` varchar(256) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码',
  `REAL_NAME` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '真实姓名',
  `MOBILE_NO` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码',
  `EMAIL` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '邮箱',
  `STATUS` tinyint(1) NOT NULL COMMENT '状态',
  `TYPE` smallint NOT NULL COMMENT '用户类型（1:商户管理员，2:商户普通用户）',
  `MCH_NO` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户编号',
  `MCH_TYPE` smallint NOT NULL COMMENT '商户类型',
  `ORG_NO` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '集团编号',
  `CREATOR` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建者',
  `MODIFIER` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '修改者',
  `REMARK` varchar(300) COLLATE utf8mb4_general_ci NOT NULL COMMENT '描述',
  `EXTRA_INFO` json DEFAULT NULL COMMENT '附加信息',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_login_name` (`LOGIN_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='商户用户表';

/*Data for the table `tbl_portal_user` */

insert  into `tbl_portal_user`(`ID`,`VERSION`,`CREATE_TIME`,`LOGIN_NAME`,`LOGIN_PWD`,`REAL_NAME`,`MOBILE_NO`,`EMAIL`,`STATUS`,`TYPE`,`MCH_NO`,`MCH_TYPE`,`ORG_NO`,`CREATOR`,`MODIFIER`,`REMARK`,`EXTRA_INFO`) values 
(2,6,'2021-05-19 14:45:50','portal_user@xpay.com','c78b6663d47cfbdb4d65ea51c104044e','王小五','15919621523','portal_user@xpay.com',1,1,'100100000001',1,NULL,'admin','admin','11',NULL),
(3,14,'2021-05-19 16:50:35','portal_normal_user@xpay.com','f3fa8a508be240ad680ffa17fd700362','赵大六','13800138002','portal_normal_user@xpay.com',1,2,'100100000001',1,'','portal_user@xpay.com','portal_user@xpay.com','赵大六',NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
