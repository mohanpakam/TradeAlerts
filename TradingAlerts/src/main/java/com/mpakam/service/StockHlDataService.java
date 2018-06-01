package com.mpakam.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.app.config.EnvironmentConfig;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockHlDataDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;
import com.mpakam.util.IEXTradingService;
import com.mpakam.util.ListUtil;

import jersey.repackaged.com.google.common.collect.Lists;

@Service
public class StockHlDataService implements IStockHlDataService {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	IEXTradingService iexService;
	
	@Autowired
	StockHlDataDao hlDao;
	
	@Autowired
	MonitoredStockDao mStockDao;
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	EnvironmentConfig eConfig;
	
	

	@Transactional
	@Override
	public void getStockHlData() throws Exception {
		List<MonitoredStock> mStockList = mStockDao.retrievegetActivelyMonitoredStocks();
		int partition = mStockList.size()/100 + 1;
		Lists.partition(mStockList, partition).parallelStream().forEach(mList->{
			try {
				hlDao.saveAll(iexService.retrieveBatchStockHL(mList));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@Transactional
	@Override
	public void saveAll(Stock s, List<StockHlData> list) {
		StockHlData lastItem = hlDao.findLastByStock(s);
		ListUtil.getLinkedListInstance(list).forEach(hl->{
			if(lastItem == null || (hl.getHlDatetime().isAfter(lastItem.getHlDatetime()))) {
				hl.setStock(s);
				hl.setStockHlDataId((int)hlDao.save(hl));
			}
		});
		return;
	}
	
	@Transactional
	@Override
	public void deleteAllForStock(Stock s) {
		hlDao.deleteForAllStock(s);
	}
	
	
	@Transactional
	@Override
	public void saveAll(List<StockHlData> list) {
		
		List<Stock> stocksList  = stockDao.findAll();
		
		Map<String, StockHlData> stockHlMap = new HashMap<>();
		
		stocksList.parallelStream().forEach(p->{
			StockHlData lastStockHl= hlDao.findLastByStock(p);
			if(lastStockHl != null ) {
				stockHlMap.put(p.getTicker(),lastStockHl);
			}
		});
		
		
		ListUtil.getLinkedListInstance(list).forEach(hl->{
			String ticker=hl.getStock().getTicker();
			StockHlData lastItem = stockHlMap.containsKey(ticker)?stockHlMap.get(ticker):null;
			if(hl.getHlDatetime().getMinute() % eConfig.getTICK_INTERVAL() == 0) {
				if(lastItem == null || (hl.getHlDatetime().isAfter(lastItem.getHlDatetime()))) {
				hl.setStockHlDataId((int)hlDao.save(hl));
				}
			}else {
				log.error("Incorrect tick quote time for " +hl.getStock().getTicker() + " @ " + hl.getHlDatetime() +" SKIPPING TO ADD NOW");
			}
		});
		return;
	}
}
