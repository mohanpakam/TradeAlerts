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
-- Table structure for table `renko_charts`
--

DROP TABLE IF EXISTS `renko_charts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `renko_charts` (
  `renko_box_id` int NOT NULL AUTO_INCREMENT,
  `stocknum` int NOT NULL,
  `stock_quote_id` int NOT NULL,
  `open_price` decimal(10,2) DEFAULT NULL,
  `close_price` decimal(10,2) DEFAULT NULL,
  `entry_datetime` datetime DEFAULT CURRENT_TIMESTAMP,
  `ema_14_price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`renko_box_id`),
  KEY `FK_renko_charts_1` (`stocknum`) USING BTREE,
  KEY `FK_renko_charts_2` (`stock_quote_id`) USING BTREE,
  CONSTRAINT `FK_renko_charts_1` FOREIGN KEY (`stocknum`) REFERENCES `stocks` (`stocknum`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK_renko_charts_2` FOREIGN KEY (`stock_quote_id`) REFERENCES `stock_quotes` (`stock_quote_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `renko_charts`
--

LOCK TABLES `renko_charts` WRITE;
/*!40000 ALTER TABLE `renko_charts` DISABLE KEYS */;
/*!40000 ALTER TABLE `renko_charts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-02-11  7:00:20
