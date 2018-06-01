package com.mpakam.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockTickDataDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockTickData;
import com.mpakam.util.IEXTradingService;

@Service
public class StockTickDataService implements IStockTickDataService {

	@Autowired
	MonitoredStockDao mStockDao;
	
	@Autowired
	IEXTradingService tradingSvc;
	
	@Autowired
	StockTickDataDao tickerDao;
	
	@Transactional
	@Override
	public void saveCurrentPriceForAllMonitoredStocks() {
		LocalDateTime currentTime = LocalDateTime.now();
		List<MonitoredStock> mList =  mStockDao.retrievegetActivelyMonitoredStocks();
		int partition = mList.size()/100 + 1;
		Lists.partition(mList, partition).forEach(mListSubList->{
			Map<Stock, BigDecimal> stockPrices = tradingSvc.getBatchPrice(mListSubList);
			List<StockTickData> tickerData = new ArrayList<>();
			stockPrices.entrySet().parallelStream().forEach(e->{
				StockTickData std = new StockTickData();
				std.setPrice(e.getValue());
				std.setStock(e.getKey());
				std.setTickDatetime(currentTime);
				tickerData.add(std);
			});
			if(tickerData!=null && !tickerData.isEmpty())
				tickerDao.saveAll(tickerData);
		});
	}
		
}
