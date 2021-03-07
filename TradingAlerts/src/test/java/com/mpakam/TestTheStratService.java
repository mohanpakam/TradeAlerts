package com.mpakam;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mpakam.constants.BackTestOrder;
import com.mpakam.constants.TheStrat;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.model.BacktestStockOrder;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;
import com.mpakam.service.HigherTimeFrameStockQuoteService;
import com.mpakam.service.TechAnalysisTheStratService;
import com.mpakam.service.strategy.strat.TheStratService;
import com.mpakam.util.DateUtil;

@SpringBootTest
public class TestTheStratService {
	
	
	@Autowired
	private MonitoredStockDao mStockDao;
	
	@Autowired
	private TechAnalysisTheStratService stratSvc;
	
	@Autowired
	private StockQuoteDao quoteDao;
	
	@Autowired
	private HigherTimeFrameStockQuoteService higherTFSvc;
	
	@Autowired
	private TheStratService strat;

	@Test
	public void test_backTestMonitoredStock() {
		List<MonitoredStock> list = mStockDao.retrievegetActivelyMonitoredStocks();
		MonitoredStock ms = list.get(0); // not a good practice.
		stratSvc.backTestMonitoredStock(ms);
	}
	
	@Test
	public void test_closeOnLastWeekBreak() {
		List<MonitoredStock> list = mStockDao.retrievegetActivelyMonitoredStocks();
		MonitoredStock ms = list.get(0); // not a good practice.

		StockQuote sq = quoteDao.findStockQuoteByQuoteDate(ms.getStock(),DateUtil.toLocalDateTime("2019-09-24 00:00:00"));
		Set<StockQuote> sqSet = quoteDao.findAllSetByStock(ms.getStock());
		
		
		StockQuote weeklySQ = higherTFSvc.getWeekly(sq, sqSet);

		
		Set<StockQuote> dailySQs = quoteDao.findAllDailySetByStock(ms.getStock());
		
		strat.closeOnLastWeekBreak(createBackTestOrder(ms.getStock()),sq,weeklySQ,dailySQs);
	}
	
	private BacktestStockOrder createBackTestOrder(Stock s) {
		
		StockQuote today = quoteDao.findStockQuoteByQuoteDate(s,DateUtil.toLocalDateTime("2019-08-16 00:00:00"));
		
		StockQuote ystday = quoteDao.findStockQuoteByQuoteDate(s,DateUtil.toLocalDateTime("2019-08-15 00:00:00"));
		
		System.out.println("Today:" +today.getQuoteDatetime() + " Yesterday "+ystday.getQuoteDatetime());
		return strat.createNewBackTestOrder(today,ystday, BackTestOrder.BUY,TheStrat._122);
	}
	
}
