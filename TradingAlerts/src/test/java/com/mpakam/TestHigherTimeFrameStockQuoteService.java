package com.mpakam;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.StockQuote;
import com.mpakam.service.HigherTimeFrameStockQuoteService;
import com.mpakam.util.DateUtil;

@SpringBootTest
public class TestHigherTimeFrameStockQuoteService {

	@Autowired
	private HigherTimeFrameStockQuoteService higherTFSvc;

	@Autowired
	private StockQuoteDao quoteDao;

	@Autowired
	MonitoredStockDao mStockDao;

	@Test
	public void test_getWeekly() {
		List<MonitoredStock> list = mStockDao.retrievegetActivelyMonitoredStocks();
		list.forEach(ms -> {
			StockQuote sq = quoteDao.findLastStockQuote(ms.getStock());
			Set<StockQuote> sqSet = quoteDao.findAllSetByStock(ms.getStock());
			higherTFSvc.getFinalWeekly(sq, sqSet);
		});
	}

	@Test
	public void test_getMonthly() {
		List<MonitoredStock> list = mStockDao.retrievegetActivelyMonitoredStocks();
		list.forEach(ms -> {
			StockQuote sq = quoteDao.findLastStockQuote(ms.getStock());
			Set<StockQuote> sqSet = quoteDao.findAllSetByStock(ms.getStock());
			higherTFSvc.getFinalMonthly(sq, sqSet);
		});
	}

	@Test
	public void test_getPreviousWeek() {
		List<MonitoredStock> list = mStockDao.retrievegetActivelyMonitoredStocks();
		MonitoredStock ms = list.get(0);
		StockQuote sq = quoteDao.findStockQuoteByQuoteDate(ms.getStock(),DateUtil.toLocalDateTime("2019-03-06 00:00:00"));
		Set<StockQuote> sqSet = quoteDao.findAllSetByStock(ms.getStock());
		higherTFSvc.getPreviousWeek(sq, sqSet);
	}
	
	@Test
	@Transactional
	public void test_getStockQuoteByDate() {
		List<MonitoredStock> list = mStockDao.retrievegetActivelyMonitoredStocks();
		MonitoredStock ms = list.get(0);
		StockQuote sq = quoteDao.findStockQuoteByQuoteDate(ms.getStock(),DateUtil.toLocalDateTime("2019-08-16 00:00:00"));
		LocalDate date = sq.getQuoteDatetime().toLocalDate();
		date = date.minusDays(7); //Last Week
		Set<StockQuote> sqSet = quoteDao.findAllSetByStock(ms.getStock());
		System.out.println(higherTFSvc.getStockQuoteByDate(date, sqSet));
	}
}
