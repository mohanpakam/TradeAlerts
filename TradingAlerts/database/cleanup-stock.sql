select * from stocks where stocknum = 991
delete from stock_alerts where stocknum=991
delete from backtest_stock_orders  where stocknum=991
create temporary table if not exists stock_quotes_tmp as (select stock_quote_id, quote_datetime from stock_quotes where `interval`!=480 and stocknum = 991);
delete ssq from strategy_stock_quotes  ssq 
		inner join stock_quotes_tmp sqt on (ssq.stock_quote_id = sqt.stock_quote_id )

delete ssq from stock_quotes  ssq 
		inner join stock_quotes_tmp sqt on (ssq.stock_quote_id = sqt.stock_quote_id )
drop table stock_quotes_tmp

select * from stock_quotes where stocknum = 991