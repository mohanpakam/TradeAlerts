CREATE DATABASE  IF NOT EXISTS `trading_alerts` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `trading_alerts`;

--Cleanup Database;
--drop table customer_ticker_tracker;
--drop table stocks;
--drop table stock_quotes;
--drop table stock_alerts;
--drop table customers;

--Individual table creation STARTS
DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customers` (
  `customerid` int(11) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(256) NOT NULL,
  `email_id` varchar(256) NOT NULL,
  PRIMARY KEY (`customerid`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `stocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stocks` (
  `stocknum` int(11) NOT NULL AUTO_INCREMENT,
  `ticker` varchar(20) NOT NULL,
  `stock_name` varchar(256) not null,
  `interval` int(11) ,
  PRIMARY KEY (`stocknum`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 PACK_KEYS=0;
/*!40101 SET character_set_client = @saved_cs_client */;


DROP TABLE IF EXISTS `stock_quotes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stock_quotes` (
	`stock_quote_id` int(11) NOT NULL AUTO_INCREMENT,
  `stocknum` int(11) NOT NULL,
  `quote_datetime` datetime NOT NULL,
  `open` decimal(10,6) NOT NULL,
  `close` decimal(10,6) NOT NULL,
  `high` decimal(10,6) NOT NULL,
  `low` decimal(10,6) NOT NULL,
  PRIMARY KEY (`stock_quote_id`),
  KEY `stocks_sotkcquotes_fk_idx` (`stocknum`),
  CONSTRAINT `stocks_sotkcquotes_fk` FOREIGN KEY (`stocknum`) REFERENCES `stocks` (`stocknum`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

CREATE INDEX quote_time_index ON stock_quotes (quote_datetime);

DROP TABLE IF EXISTS `customer_ticker_tracker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customer_ticker_tracker` (
  `quote_tick_id` int(11) NOT NULL AUTO_INCREMENT,
  `customerid` int(11) NOT NULL,
  `stocknum` int(11) NOT NULL,
  `strategy_id` int(11) NOT NULL,
  `description` varchar(256),
  PRIMARY KEY (`quote_tick_id`),
  KEY `tkrtrkr_customers_fk_idx` (`customerid`),
  KEY `tkrtrkr_stocks_fk_idx` (`stocknum`),
  CONSTRAINT `tkrtrkr_customers_fk` FOREIGN KEY (`customerid`) REFERENCES `customers` (`customerid`),
  CONSTRAINT `tkrtrkr_stocks_fk` FOREIGN KEY (`stocknum`) REFERENCES `stocks` (`stocknum`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8  PACK_KEYS=0;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `stock_alerts`;

CREATE TABLE `stock_alerts`(
	`stock_alert_id` int(11) NOT NULL AUTO_INCREMENT,
	`stocknum` int(11) NOT NULL,
	`stock_price` decimal(10,6),
	`customerid` int(11),
	`buy_sell_signal` int,
	`strategy_stock_quote_id` int(11),
	PRIMARY KEY(`stock_alert_id`),
	KEY `tkrtrkr_customers_fk_idx` (`customerid`),
  	KEY `tkrtrkr_stocks_fk_idx` (`stocknum`),
  	KEY `sa_ssq_fk_idx` (strategy_stock_quote_id),
  CONSTRAINT `stktrkr_customers_fk` FOREIGN KEY (`customerid`) REFERENCES `customers` (`customerid`),
  CONSTRAINT `stktrkr_stocks_fk` FOREIGN KEY (`stocknum`) REFERENCES `stocks` (`stocknum`),
  CONSTRAINT `sa_ssq_fk` FOREIGN KEY (`strategy_stock_quote_id`) REFERENCES `strategy_stock_quotes` (`strategy_stock_quote_id`)
)ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `strategy`;

CREATE TABLE `strategy`(
	`strategy_id` int(11) NOT NULL AUTO_INCREMENT,
	`strategy_name` varchar(256) NOT NULL,
	`strategy_interval` int(11),
	PRIMARY KEY(`strategy_id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `strategy_stock_quotes`;

CREATE TABLE `strategy_stock_quotes`(
	`strategy_stock_quote_id` int(11) NOT NULL AUTO_INCREMENT,
	`strategy_id` int(11),
	`stock_quote_id` int(11),
	`xopen` decimal(10,6) ,
  	`xclose` decimal(10,6) ,
  	`xhigh` decimal(10,6) ,
  	`xlow` decimal(10,6) ,
  	`rsi` decimal(10,6) ,
  	`highrsi` decimal(10,6) ,
  	`lowrsi` decimal(10,6) ,
  	`avg_gain` decimal(10,6) ,
  	`avg_loss` decimal(10,6) ,
  	`stoch_rsi` decimal(10,6) ,
  	`stoch_rsi_k` decimal(10,6) ,
  	`stoch_rsi_d` decimal(10,6) ,
	PRIMARY KEY(`strategy_stock_quote_id`),
	KEY `ssq_sq_fk_idx` (`stock_quote_id`),
  	KEY `ssq_s_fk_idx` (`strategy_id`),
  	CONSTRAINT `ssq_sq_fk` FOREIGN KEY (`stock_quote_id`) REFERENCES `stock_quotes` (`stock_quote_id`),
  	CONSTRAINT `ssq_s_fk` FOREIGN KEY (`strategy_id`) REFERENCES `strategy` (`strategy_id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


insert into `customers` (`first_name`,`last_name`,`username`,`password`,`email_id`) values ('Mohan','Pakam','mpakam','mpakam','mohaneee221@gmail.com');
insert into stocks (`ticker`,`stock_name`,`interval`) values('BA','BOEING AIRCRAFTS',30);
insert into stocks (`ticker`,`stock_name`,`interval`) values('AAPL','AAPLE INC',30);
insert into `customer_ticker_tracker` (`customerid`,`stocknum`,`description`,`strategy_id`) values (5,8,'added',1);
insert into strategy (`strategy_name`,`strategy_interval`) values('HeikenAshi + StochRsi',30);
insert into customer_ticker_tracker (`customerid`,`stocknum`,`strategy_id`,`description`) values (5,7,1,'HeikenAshi Strategy');
