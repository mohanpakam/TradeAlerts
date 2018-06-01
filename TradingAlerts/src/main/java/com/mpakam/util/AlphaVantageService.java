package com.mpakam.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeSet;

import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.timeseries.DailyAdjusted;
import org.patriques.output.timeseries.IntraDay;
import org.patriques.output.timeseries.data.StockData;
import org.springframework.stereotype.Service;

import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;

@Service
public class AlphaVantageService implements IQuoteDataProviderService {
	
	private final String apiKey= "VZ5VN30W0C05IUWA";
	//I5GL0TITZGD68LM1

	@Override
	public TreeSet<StockQuote> retrieveCandleData(Stock stockObj) throws Exception{
		
		String symbol=stockObj.getTicker();
		int interval = stockObj.getInterval();
		if(Interval.getByTime(interval) == null) {
			throw new Exception("Interval " + interval + " provided is incorrect");
		}
		
	    int timeout = 30000;
	    AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
	    TimeSeries stockTimeSeries = new TimeSeries(apiConnector);
	    
		// configurable
		IntraDay response = stockTimeSeries.intraDay(symbol, Interval.getByTime(interval), OutputSize.COMPACT);
		TreeSet<StockQuote> quotesList = castStockQuote(response.getStockData(),stockObj);
		return quotesList;
	}
	
	@Override
	public TreeSet<StockQuote> retrieveFailProofCandleData(Stock stockObj){
		boolean retry = true;
		int attempts = 0;
		while(attempts<2) {
			try {
				System.out.println("Synchronizing data for "+stockObj.getTicker() + " @ " + stockObj.getInterval());
				return retrieveCandleData(stockObj);
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println(stockObj.getTicker() + " Error - " + e.getLocalizedMessage());
				++attempts;
			}
		}
		return new TreeSet<StockQuote>();
	}
	
	@Override
	public TreeSet<StockQuote> retrieveDailyCandleData(Stock stockObj) throws Exception{

	    int timeout = 30000;
	    AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
	    TimeSeries stockTimeSeries = new TimeSeries(apiConnector);

		// configurable
		DailyAdjusted response = stockTimeSeries.dailyAdjusted(stockObj.getTicker());
		TreeSet<StockQuote> quotesList = castStockQuote(response.getStockData(),stockObj);
		return quotesList;
	}
	
	private TreeSet<StockQuote> castStockQuote(List<StockData> stockData,Stock stockObj){
		TreeSet<StockQuote> quotesList = new TreeSet<StockQuote>();
		stockData.forEach(stock -> {
			StockQuote quote = new StockQuote();
			quote.setClose(new BigDecimal(stock.getClose()));
			quote.setOpen(new BigDecimal(stock.getOpen()));
			quote.setHigh(new BigDecimal(stock.getHigh()));
			quote.setLow(new BigDecimal(stock.getLow()));
			quote.setQuoteDatetime(stock.getDateTime());
			quote.setInterval(stockObj.getInterval());
			quote.setStock(stockObj);
			quotesList.add(quote);
		});

		return quotesList;
	}

}
