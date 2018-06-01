-- ----------------------------------------------------------------
--  DATABASE trading_alerts
-- ----------------------------------------------------------------

CREATE DATABASE trading_alerts
   CHARACTER SET `utf8`
   COLLATE `utf8_general_ci`;
   
CREATE TABLE trading_alerts.stocks
(
   stocknum           int(11) NOT NULL AUTO_INCREMENT,
   ticker             varchar(20)
                      CHARACTER SET utf8
                      COLLATE utf8_general_ci
                      NOT NULL,
   stock_name         varchar(256)
                      CHARACTER SET utf8
                      COLLATE utf8_general_ci
                      NOT NULL,
   `interval`         int(11) NULL,
   trend              int(8) NULL DEFAULT 0,
   stooq_file_path    varchar(960)
                      CHARACTER SET utf8
                      COLLATE utf8_general_ci
                      NULL,
   highest_high       decimal(10, 2) NULL,
   lowest_low         decimal(10, 2) NULL,
   renko_box_size     decimal(10, 2) NULL,
   PRIMARY KEY(stocknum)
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
PACK_KEYS 0
ROW_FORMAT DEFAULT
   
   CREATE TABLE trading_alerts.strategy
(
   strategy_id          int(11) NOT NULL AUTO_INCREMENT,
   strategy_name        varchar(256)
                        CHARACTER SET utf8
                        COLLATE utf8_general_ci
                        NOT NULL,
   strategy_interval    int(11) NULL,
   PRIMARY KEY(strategy_id)
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT

CREATE TABLE trading_alerts.customers
(
   customerid    int(11) NOT NULL AUTO_INCREMENT,
   first_name    varchar(50)
                 CHARACTER SET utf8
                 COLLATE utf8_general_ci
                 NOT NULL,
   last_name     varchar(50)
                 CHARACTER SET utf8
                 COLLATE utf8_general_ci
                 NOT NULL,
   username      varchar(50)
                 CHARACTER SET utf8
                 COLLATE utf8_general_ci
                 NOT NULL,
   password      varchar(256)
                 CHARACTER SET utf8
                 COLLATE utf8_general_ci
                 NOT NULL,
   email_id      varchar(256)
                 CHARACTER SET utf8
                 COLLATE utf8_general_ci
                 NOT NULL,
   PRIMARY KEY(customerid)
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT
CREATE TABLE trading_alerts.stock_hl_data
(
   stock_hl_data_id      int(20) NOT NULL AUTO_INCREMENT,
   stocknum              int(11) NOT NULL,
   high                  decimal(8, 2) NULL,
   low                   decimal(8, 2) NULL,
   `interval`            int(8) NULL,
   hl_datetime           datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
   recorded_timestamp    datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
   open                  decimal(8, 2) NULL,
   close                 decimal(8, 2) NULL,
   PRIMARY KEY(stock_hl_data_id),
   CONSTRAINT `FK_stock_hl_data_1` FOREIGN KEY(stocknum)
   REFERENCES stocks(stocknum) ON UPDATE RESTRICT ON DELETE RESTRICT
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX stockhl_interval
   ON trading_alerts.stock_hl_data(`interval`, stocknum, hl_datetime)
   USING BTREE;

CREATE INDEX `FK_stock_hl_data_1`
   ON trading_alerts.stock_hl_data(stocknum)
   USING BTREE;
CREATE TABLE trading_alerts.stock_tick_data
(
   stock_tick_data_id    int(20) NOT NULL AUTO_INCREMENT,
   stocknum              int(11) NOT NULL,
   tick_datetime         datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
   price                 decimal(8, 2) NULL,
   PRIMARY KEY(stock_tick_data_id),
   CONSTRAINT `FK_stock_tick_data_1` FOREIGN KEY(stocknum)
   REFERENCES stocks(stocknum) ON UPDATE RESTRICT ON DELETE RESTRICT
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX stock_time
   ON trading_alerts.stock_tick_data(stocknum, tick_datetime)
   USING BTREE;

   CREATE TABLE trading_alerts.monitored_stocks
(
   monitored_stock_id    int(20) NOT NULL AUTO_INCREMENT,
   stocknum              int(20) NOT NULL,
   `interval`            int(20) NOT NULL,
   added_date            datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
   added_by              int(20) NOT NULL DEFAULT 0,
   trennd                int(8) NULL,
   CONSTRAINT `FK_STOCKNUM` FOREIGN KEY(stocknum)
   REFERENCES stocks(stocknum) ON UPDATE RESTRICT ON DELETE RESTRICT,
   PRIMARY KEY(monitored_stock_id)
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX `FK_STOCKNUM`
   ON trading_alerts.monitored_stocks(stocknum)
   USING BTREE;
CREATE TABLE trading_alerts.stock_quotes
(
   stock_quote_id        int(11) NOT NULL AUTO_INCREMENT,
   stocknum              int(11) NOT NULL,
   quote_datetime        datetime(0) NOT NULL,
   open                  decimal(10, 6) NOT NULL,
   close                 decimal(10, 6) NOT NULL,
   high                  decimal(10, 6) NOT NULL,
   low                   decimal(10, 6) NOT NULL,
   `interval`            int(8) NULL DEFAULT 30,
   recorded_timestamp    timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT stocks_sotkcquotes_fk FOREIGN KEY(stocknum)
   REFERENCES stocks(stocknum) ON UPDATE CASCADE ON DELETE RESTRICT,
   PRIMARY KEY(stock_quote_id)
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX stock_quotes_interval
   ON trading_alerts.stock_quotes(`interval`, quote_datetime)
   USING BTREE;

CREATE INDEX stocks_sotkcquotes_fk_idx
   ON trading_alerts.stock_quotes(stocknum)
   USING BTREE;

CREATE INDEX quote_time_index
   ON trading_alerts.stock_quotes(quote_datetime)
   USING BTREE;
   
   
   CREATE TABLE trading_alerts.strategy_stock_quotes
(
   strategy_stock_quote_id    int(11) NOT NULL AUTO_INCREMENT,
   strategy_id                int(11) NULL,
   stock_quote_id             int(11) NULL,
   xopen                      decimal(10, 6) NULL,
   xclose                     decimal(10, 6) NULL,
   xhigh                      decimal(10, 6) NULL,
   xlow                       decimal(10, 6) NULL,
   rsi                        decimal(10, 6) NULL,
   highrsi                    decimal(10, 6) NULL,
   lowrsi                     decimal(10, 6) NULL,
   avg_gain                   decimal(10, 6) NULL,
   avg_loss                   decimal(10, 6) NULL,
   stoch_rsi                  decimal(10, 6) NULL,
   stoch_rsi_k                decimal(10, 6) NULL,
   stoch_rsi_d                decimal(10, 6) NULL,
   CONSTRAINT ssq_s_fk FOREIGN KEY(strategy_id)
   REFERENCES strategy(strategy_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
   CONSTRAINT ssq_sq_fk FOREIGN KEY
      (stock_quote_id)
      REFERENCES stock_quotes(stock_quote_id)
         ON UPDATE RESTRICT
         ON DELETE RESTRICT,
   PRIMARY KEY(strategy_stock_quote_id)
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX ssq_s_fk_idx
   ON trading_alerts.strategy_stock_quotes(strategy_id)
   USING BTREE;

CREATE INDEX ssq_sq_fk_idx
   ON trading_alerts.strategy_stock_quotes(stock_quote_id)
   USING BTREE;

CREATE INDEX ssq_strategy
   ON trading_alerts.strategy_stock_quotes(strategy_id)
   USING BTREE;
   
   CREATE TABLE trading_alerts.backtest_stock_orders
(
   backtest_stock_order_id    int(20) NOT NULL AUTO_INCREMENT,
   open_price                 decimal(10, 2) NULL,
   close_price                decimal(10, 2) NULL,
   open_datetime              datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
   close_datetime             datetime(0) NULL,
   entry_datetime             datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
   profit_loss                decimal(10, 2) NULL,
   order_type                 varchar(20)
                              CHARACTER SET utf8
                              COLLATE utf8_general_ci
                              NULL,
   stocknum                   int(11) NOT NULL,
   stoploss_price             decimal(10, 2) NULL,
   strategy_id                int(20) NULL DEFAULT 1,
   PRIMARY KEY(backtest_stock_order_id),
   CONSTRAINT `FK_backtest_stock_orders_1` FOREIGN KEY(stocknum)
   REFERENCES stocks(stocknum) ON UPDATE RESTRICT ON DELETE RESTRICT
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX `FK_backtest_stock_orders_1`
   ON trading_alerts.backtest_stock_orders(stocknum)
   USING BTREE;
   
CREATE TABLE trading_alerts.customer_ticker_tracker
(
   quote_tick_id    int(11) NOT NULL AUTO_INCREMENT,
   customerid       int(11) NOT NULL,
   stocknum         int(11) NOT NULL,
   strategy_id      int(11) NOT NULL,
   description      varchar(256)
                    CHARACTER SET utf8
                    COLLATE utf8_general_ci
                    NULL,
   PRIMARY KEY(quote_tick_id),
   CONSTRAINT tkrtrkr_customers_fk FOREIGN KEY(customerid)
   REFERENCES customers(customerid) ON UPDATE RESTRICT ON DELETE RESTRICT,
   CONSTRAINT tkrtrkr_stocks_fk FOREIGN KEY(stocknum)
   REFERENCES stocks(stocknum) ON UPDATE RESTRICT ON DELETE RESTRICT
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
PACK_KEYS 0
ROW_FORMAT DEFAULT;

CREATE INDEX tkrtrkr_stocks_fk_idx
   ON trading_alerts.customer_ticker_tracker(stocknum)
   USING BTREE;

CREATE INDEX tkrtrkr_customers_fk_idx
   ON trading_alerts.customer_ticker_tracker(customerid)
   USING BTREE;
   
   CREATE TABLE trading_alerts.renko_charts
(
   renko_box_id      int(20) NOT NULL AUTO_INCREMENT,
   stocknum          int(11) NOT NULL,
   stock_quote_id    int(11) NOT NULL,
   open_price        decimal(10, 2) NULL,
   close_price       decimal(10, 2) NULL,
   entry_datetime    datetime(0) NULL DEFAULT CURRENT_TIMESTAMP,
   ema_14_price      decimal(10, 2) NULL,
   PRIMARY KEY(renko_box_id),
   CONSTRAINT `FK_renko_charts_1` FOREIGN KEY(stocknum)
   REFERENCES stocks(stocknum) ON UPDATE RESTRICT ON DELETE RESTRICT,
   CONSTRAINT `FK_renko_charts_2` FOREIGN KEY
      (stock_quote_id)
      REFERENCES stock_quotes(stock_quote_id)
         ON UPDATE RESTRICT
         ON DELETE RESTRICT
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX `FK_renko_charts_1`
   ON trading_alerts.renko_charts(stocknum)
   USING BTREE;

CREATE INDEX `FK_renko_charts_2`
   ON trading_alerts.renko_charts(stock_quote_id)
   USING BTREE;
   
   CREATE TABLE trading_alerts.stock_alerts
(
   stock_alert_id             int(11) NOT NULL AUTO_INCREMENT,
   stocknum                   int(11) NOT NULL,
   stock_price                decimal(10, 6) NULL,
   customerid                 int(11) NULL,
   buy_sell_signal            int(11) NULL,
   strategy_stock_quote_id    int(11) NULL,
   monitored                  int(8) NULL DEFAULT 0,
   alert_status               int(2) NULL,
   sent_timestamp             timestamp(0) NULL,
   CONSTRAINT stktrkr_stocks_fk FOREIGN KEY(stocknum)
   REFERENCES stocks(stocknum) ON UPDATE RESTRICT ON DELETE RESTRICT,
   CONSTRAINT stktrkr_customers_fk FOREIGN KEY(customerid)
   REFERENCES customers(customerid) ON UPDATE RESTRICT ON DELETE RESTRICT,
   PRIMARY KEY(stock_alert_id),
   CONSTRAINT sa_ssq_fk FOREIGN KEY
      (strategy_stock_quote_id)
      REFERENCES strategy_stock_quotes(strategy_stock_quote_id)
         ON UPDATE RESTRICT
         ON DELETE RESTRICT
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX sa_ssq_fk_idx
   ON trading_alerts.stock_alerts(strategy_stock_quote_id)
   USING BTREE;

CREATE INDEX tkrtrkr_stocks_fk_idx
   ON trading_alerts.stock_alerts(stocknum)
   USING BTREE;

CREATE INDEX alerts_indexes
   ON trading_alerts.stock_alerts(stock_price,
                                  sent_timestamp,
                                  strategy_stock_quote_id,
                                  buy_sell_signal)
   USING BTREE;

CREATE INDEX tkrtrkr_customers_fk_idx
   ON trading_alerts.stock_alerts(customerid)
   USING BTREE;
   
CREATE TABLE trading_alerts.tech_analysis_ichimoku
(
   ichimoku_id       int(20) NOT NULL AUTO_INCREMENT,
   stock_quote_id    int(11) NOT NULL,
   tenken_sen        decimal(10, 2) NULL,
   kijun_sen         decimal(10, 2) NULL,
   senkou_span_a     decimal(10, 2) NULL,
   senkou_span_b     decimal(10, 2) NULL,
   ema_9             decimal(10, 2) NULL,
   PRIMARY KEY(ichimoku_id),
   CONSTRAINT `FK_tech_analysis_ichimoku_1` FOREIGN KEY
      (stock_quote_id)
      REFERENCES stock_quotes(stock_quote_id)
         ON UPDATE RESTRICT
         ON DELETE RESTRICT
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX `FK_tech_analysis_ichimoku_1`
   ON trading_alerts.tech_analysis_ichimoku(stock_quote_id)
   USING BTREE;
   
CREATE TABLE trading_alerts.tech_analysis_heikenashi
(
   ha_id             int(20) NOT NULL AUTO_INCREMENT,
   xopen             decimal(10, 2) NULL,
   xclose            decimal(10, 2) NULL,
   xhigh             decimal(10, 2) NULL,
   xlow              decimal(10, 2) NULL,
   stock_quote_id    int(11) NOT NULL,
   PRIMARY KEY(ha_id),
   CONSTRAINT `FK_tech_analysis_heikenashi_1` FOREIGN KEY
      (stock_quote_id)
      REFERENCES stock_quotes(stock_quote_id)
         ON UPDATE RESTRICT
         ON DELETE RESTRICT
)
ENGINE InnoDB
COLLATE 'utf8_general_ci'
ROW_FORMAT DEFAULT;

CREATE INDEX `FK_tech_analysis_heikenashi_1`
   ON trading_alerts.tech_analysis_heikenashi(stock_quote_id)
   USING BTREE;