package com.mpakam.dataapi;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import com.mpakam.model.StockQuote;

import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

@Service
public class YahooFinanceAPIService {

	/**Retrieve The Stocks OHLC based on the provided StockQuote Object
	 * @param StockQuote latest quote for a given Symbo at hand
	 * @throws IOException
	 */
	public Set<StockQuote> retrieveDailyStockQuote(StockQuote quote) throws IOException {
		Calendar from = fromDate(quote);
		Calendar to = Calendar.getInstance();
		TreeSet<StockQuote> stockQuotes = new TreeSet<>();
		yahoofinance.Stock yahooStock = YahooFinance.get(quote.getStock().getTicker());
		
			List<HistoricalQuote> yahooHistQuotes;
			try {
				yahooHistQuotes = yahooStock.getHistory(from, to, Interval.DAILY);
				yahooHistQuotes.forEach(q->{
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					System.out.println(dateFormat.format(q.getDate().getTime()) +
							"-Open:"+q.getOpen() +";Close:"+q.getClose() +
							";High:"+q.getHigh() +";Low:"+q.getLow());
					StockQuote sq = new StockQuote();
	        		sq.setClose(q.getClose());
	        		sq.setHigh(q.getHigh());
	        		sq.setInterval(480);
	        		sq.setLow(q.getLow());
	        		sq.setOpen(q.getOpen());

	        		sq.setQuoteDatetime(q.getDate().toInstant()
	        			      .atZone(ZoneId.systemDefault())
	        			      .toLocalDateTime());
	        		sq.setStock(quote.getStock());
	        		
	        		stockQuotes.add(sq);
				});
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch(Exception e) {
				e.printStackTrace();
			}
			return stockQuotes;
	}
	
	private Calendar fromDate(StockQuote sq) {
		LocalDateTime date = null;
		if(sq == null || sq.getQuoteDatetime() == null) {
			date = LocalDateTime.now().minusYears(2); // 2 Years ago
		}else
			date = sq.getQuoteDatetime();
		return new GregorianCalendar.Builder()
		.setDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
		.build();
	}
}
