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
-- Table structure for table `stock_alerts`
--

DROP TABLE IF EXISTS `stock_alerts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_alerts` (
  `stock_alert_id` int NOT NULL AUTO_INCREMENT,
  `stocknum` int NOT NULL,
  `stock_price` decimal(10,6) DEFAULT NULL,
  `customerid` int DEFAULT NULL,
  `buy_sell_signal` int DEFAULT NULL,
  `strategy_stock_quote_id` int DEFAULT NULL,
  `monitored` int DEFAULT '0',
  `alert_status` int DEFAULT NULL,
  `sent_timestamp` timestamp NULL DEFAULT NULL,
  `stock_quote_id` int DEFAULT NULL,
  PRIMARY KEY (`stock_alert_id`),
  KEY `sa_ssq_fk_idx` (`strategy_stock_quote_id`) USING BTREE,
  KEY `tkrtrkr_stocks_fk_idx` (`stocknum`) USING BTREE,
  KEY `alerts_indexes` (`stock_price`,`sent_timestamp`,`strategy_stock_quote_id`,`buy_sell_signal`) USING BTREE,
  KEY `tkrtrkr_customers_fk_idx` (`customerid`) USING BTREE,
  KEY `stock_quote_id_idx` (`stock_quote_id`),
  CONSTRAINT `sa_ssq_fk` FOREIGN KEY (`strategy_stock_quote_id`) REFERENCES `strategy_stock_quotes` (`strategy_stock_quote_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `stktrkr_customers_fk` FOREIGN KEY (`customerid`) REFERENCES `customers` (`customerid`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `stktrkr_stocks_fk` FOREIGN KEY (`stocknum`) REFERENCES `stocks` (`stocknum`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `stock_quote_fk` FOREIGN KEY (`stock_quote_id`) REFERENCES `stock_quotes` (`stock_quote_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_alerts`
--

LOCK TABLES `stock_alerts` WRITE;
/*!40000 ALTER TABLE `stock_alerts` DISABLE KEYS */;
/*!40000 ALTER TABLE `stock_alerts` ENABLE KEYS */;
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
