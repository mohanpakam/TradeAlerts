CREATE DATABASE  IF NOT EXISTS `trading_alerts` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `trading_alerts`;
-- MySQL dump 10.13  Distrib 8.0.20, for Win64 (x86_64)
--
-- Host: localhost    Database: trading_alerts
-- ------------------------------------------------------
-- Server version	8.0.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `backtest_stock_orders`
--

DROP TABLE IF EXISTS `backtest_stock_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `backtest_stock_orders` (
  `backtest_stock_order_id` int NOT NULL AUTO_INCREMENT,
  `open_price` decimal(10,2) DEFAULT NULL,
  `close_price` decimal(10,2) DEFAULT NULL,
  `open_datetime` datetime DEFAULT CURRENT_TIMESTAMP,
  `close_datetime` datetime DEFAULT NULL,
  `entry_datetime` datetime DEFAULT CURRENT_TIMESTAMP,
  `profit_loss` decimal(10,2) DEFAULT NULL,
  `order_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `stocknum` int NOT NULL,
  `stoploss_price` decimal(10,2) DEFAULT NULL,
  `strategy_id` int DEFAULT '1',
  `alerted` int DEFAULT '0',
  PRIMARY KEY (`backtest_stock_order_id`),
  KEY `FK_backtest_stock_orders_1` (`stocknum`) USING BTREE,
  CONSTRAINT `FK_backtest_stock_orders_1` FOREIGN KEY (`stocknum`) REFERENCES `stocks` (`stocknum`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `backtest_stock_orders`
--

LOCK TABLES `backtest_stock_orders` WRITE;
/*!40000 ALTER TABLE `backtest_stock_orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `backtest_stock_orders` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-02-11  7:00:17
