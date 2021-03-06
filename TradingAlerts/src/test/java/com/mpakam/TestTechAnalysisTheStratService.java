package com.mpakam;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockQuoteDao;
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

	@Test
	public void test_getWeekly() {
		List<MonitoredStock> list =mStockDao.retrievegetActivelyMonitoredStocks();
		list.forEach(ms->{
			StockQuote sq = quoteDao.findLastStockQuote(ms.getStock());
			Set<StockQuote> sqSet = quoteDao.findAllSetByStock(ms.getStock());
			stratService.getWeekly(sq, sqSet);
		});
		
	}
	
	@Test
	public void test_getMonthly() {
		List<MonitoredStock> list =mStockDao.retrievegetActivelyMonitoredStocks();
		list.forEach(ms->{
			StockQuote sq = quoteDao.findLastStockQuote(ms.getStock());
			Set<StockQuote> sqSet = quoteDao.findAllSetByStock(ms.getStock());
			stratService.getMonthly(sq, sqSet);	
		});
		
	}
}
