package com.mpakam;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.dao.TechAnalysisStratDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.StockQuote;
import com.mpakam.service.TechAnalysisTheStratService;

@SpringBootTest
public class TestTechAnalysisTheStratService {

	@Autowired
	private TechAnalysisTheStratService stratService;
	
	@Autowired
	private StockQuoteDao quoteDao;
	
	@Autowired
	MonitoredStockDao mStockDao;
	
	
	private Set<StockQuote> getStockQuote() {
		List<MonitoredStock> list = mStockDao.retrievegetActivelyMonitoredStocks();
		MonitoredStock ms = list.get(0); // not a good practice.
		//StockQuote sq = quoteDao.findLastStockQuote(ms.getStock());
		Set<StockQuote> sqSet = quoteDao.findAllSetByStock(ms.getStock());
		return sqSet;
	}
	
	@Test
	public void test_analysis() {
		Set<StockQuote> sqSet = getStockQuote();
		StockQuote lastSq=null;
		for(StockQuote currentSq : sqSet) {
			stratService.createStrat(currentSq,lastSq);			
			lastSq = currentSq;
		}
		//TODO: Add a known Day's Candle ID Validition
	}
	
}
