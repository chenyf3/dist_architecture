/*
SQLyog Ultimate v13.1.1 (64 bit)
MySQL - 8.0.23 : Database - base_config
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`base_config` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `base_config`;

/*Table structure for table `tbl_lock` */

DROP TABLE IF EXISTS `tbl_lock`;

CREATE TABLE `tbl_lock` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `VERSION` bigint unsigned NOT NULL DEFAULT '0' COMMENT '版本号',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `RESOURCE_ID` char(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源id',
  `RESOURCE_STATUS` smallint NOT NULL COMMENT '资源状态(1=空闲 2=锁定)',
  `CLIENT_ID` char(32) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端id(锁持有者)',
  `CLIENT_FLAG` varchar(100) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端标识',
  `LOCK_TIME` datetime DEFAULT NULL COMMENT '上锁时间',
  `EXPIRE_TIME` datetime DEFAULT NULL COMMENT '过期时间(RESOURCE_STATUS=2且EXPIRE_TIME=NULL时表示永不过期)',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `uk_resource_id` (`RESOURCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分布式锁';

/*Data for the table `tbl_lock` */

/*Table structure for table `tbl_product` */

DROP TABLE IF EXISTS `tbl_product`;

CREATE TABLE `tbl_product` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
  `VERSION` int unsigned NOT NULL DEFAULT '0' COMMENT '版本号',
  `PRODUCT_TYPE` smallint unsigned NOT NULL COMMENT '业务线(产品类型)',
  `PRODUCT_CODE` smallint unsigned NOT NULL COMMENT '产品编号',
  `STATUS` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态(1=启用 2=禁用)',
  `REMARK` varchar(200) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `udx_product_code` (`PRODUCT_CODE`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品表';

/*Data for the table `tbl_product` */

insert  into `tbl_product`(`ID`,`CREATE_TIME`,`VERSION`,`PRODUCT_TYPE`,`PRODUCT_CODE`,`STATUS`,`REMARK`) values 
(1,'2021-05-08 11:47:59',0,1,1,1,''),
(2,'2021-05-08 11:48:08',1,1,2,2,'测试禁用'),
(3,'2021-05-08 22:46:14',2,3,4,1,'垫资清零'),
(4,'2021-05-08 22:47:09',5,4,5,2,'测试禁用产品');

/*Table structure for table `tbl_product_open` */

DROP TABLE IF EXISTS `tbl_product_open`;

CREATE TABLE `tbl_product_open` (
  `ID` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `CREATE_TIME` datetime NOT NULL COMMENT '创建时间(开通时间)',
  `EXPIRE_DATE` date NOT NULL COMMENT '过期时间',
  `VERSION` int unsigned NOT NULL DEFAULT '0' COMMENT '版本号',
  `PRODUCT_TYPE` smallint unsigned NOT NULL COMMENT '业务线(产品类型)',
  `PRODUCT_CODE` smallint unsigned NOT NULL COMMENT '产品编号',
  `MCH_NO` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
  `STATUS` tinyint unsigned NOT NULL COMMENT '状态(1=启用 2=禁用)',
  `REMARK` varchar(200) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `udx_mch_no_product_code` (`MCH_NO`,`PRODUCT_CODE`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品开通表';

/*Data for the table `tbl_product_open` */

insert  into `tbl_product_open`(`ID`,`CREATE_TIME`,`EXPIRE_DATE`,`VERSION`,`PRODUCT_TYPE`,`PRODUCT_CODE`,`MCH_NO`,`STATUS`,`REMARK`) values 
(1,'2021-05-08 11:48:58','2022-02-24',2,1,1,'100100000001',1,'测试启用2'),
(3,'2021-05-08 11:49:09','2022-03-22',5,1,2,'100100000001',1,'测试启用'),
(4,'2021-05-08 22:56:02','2022-03-30',8,3,4,'100100000001',1,'开通垫资清零6666');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
