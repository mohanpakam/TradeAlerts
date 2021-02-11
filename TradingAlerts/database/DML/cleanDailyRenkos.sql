create temporary table if not exists stock_quotes_tmp as (select stock_quote_id from stock_quotes where quote_datetime >time('2018-03-21 00:00:00'));

delete from tech_analysis_atrs where stock
delete from renko_charts

delete ssq from tech_analysis_atrs ssq 
		inner join stock_quotes_tmp sqt on (ssq.stock_quote_id = sqt.stock_quote_id )

delete ssq from renko_charts ssq 
		inner join stock_quotes_tmp sqt on (ssq.stock_quote_id = sqt.stock_quote_id )



delete ssq from stock_quotes  ssq 
		inner join stock_quotes_tmp sqt on (ssq.stock_quote_id = sqt.stock_quote_id )
		
		
		

ALTER TABLE stock_quotes AUTO_INCREMENT = 1;
ALTER TABLE renko_charts AUTO_INCREMENT = 1;
ALTER TABLE stock_hl_data AUTO_INCREMENT = 1;
ALTER TABLE stock_tick_data AUTO_INCREMENT = 1;
ALTER TABLE tech_analysis_atrs AUTO_INCREMENT = 1;