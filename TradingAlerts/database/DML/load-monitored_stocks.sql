insert into monitored_stocks (stocknum, `interval`, added_date,added_by,trennd) select stocknum, `interval`, current_timestamp, 1,0 from stocks;