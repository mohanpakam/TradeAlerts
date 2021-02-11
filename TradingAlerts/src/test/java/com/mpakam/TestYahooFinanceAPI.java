package com.mpakam;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mpakam.dataapi.YahooFinanceAPIService;
import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;

@SpringBootTest
public class TestYahooFinanceAPI {

	@Autowired
	private YahooFinanceAPIService yahooSvc;
		
	
	/**Tests the Retrieve Daily Stock quote Method
	 * @throws IOException
	 */
	@Test
	public void test_retrieveDailyStockQuote() throws IOException {
		Stock stock  = new Stock();
		stock.setTicker("AAPL");
		StockQuote sq = new StockQuote();
		sq.setStock(stock);
		yahooSvc.retrieveDailyStockQuote(sq);
	}
	
}
