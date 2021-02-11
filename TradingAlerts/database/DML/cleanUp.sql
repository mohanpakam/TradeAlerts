drop procedure `cleanUp`;
create procedure cleanUp()
 
begin
	declare countOfRecords int;
	delete from stock_tick_data  where tick_datetime<SUBDATE(NOW(),1); 
 
	delete  from stock_hl_data where  hl_datetime<SUBDATE(NOW(),1) 	;
	
			
	create temporary table if not exists stock_quotes_tmp as (select stock_quote_id, quote_datetime from stock_quotes where quote_datetime <SUBDATE(NOW(),7) and `interval`!=480);
	
	delete from stock_alerts where strategy_stock_quote_id in (select strategy_stock_quote_id from strategy_stock_quotes where stock_quote_id in (select stock_quote_id from stock_quotes_tmp));

	delete ssq from strategy_stock_quotes  ssq 
		inner join stock_quotes_tmp sqt on (ssq.stock_quote_id = sqt.stock_quote_id )

	delete ssq from stock_quotes  ssq 
		inner join stock_quotes_tmp sqt on (ssq.stock_quote_id = sqt.stock_quote_id )

	select countOfRecords=count(1) from stock_quotes_tmp; 
	drop table stock_quotes_tmp;
	
end

call cleanup();