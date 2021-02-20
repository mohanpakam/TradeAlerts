/**
 * 
 */
package com.mpakam.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.dao.CustomerTickerTrackerDao;
import com.mpakam.dao.MonitoredStockDao;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.dataapi.YahooFinanceAPIService;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockQuote;
import com.mpakam.service.strategy.IStrategyService;
import com.mpakam.service.strategy.RenkoChartStrategyService;
import com.mpakam.util.AlphaVantageService;
import com.mpakam.util.IEXTradingService;

/**
 * @author LuckyMonaA
 *
 */
@Service
public class StockQuoteService implements IStockQuoteService {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	AlphaVantageService dataProviderSvc;
	
	@Autowired
	IEXTradingService iexService;
	
	@Autowired
	StockQuoteDao quoteDao;
	
	@Autowired
	CustomerTickerTrackerDao customerTkrDao;
	
	@Autowired
	IStrategyService strategySvc;
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	MonitoredStockDao mStockDao;
	
	@Autowired
	IMonitoredStockService monitorSvc;
	
	@Autowired
	EmailService emailSvc;
	
	@Autowired
	RenkoChartStrategyService renkoSvc;
	
	@Autowired
	YahooFinanceAPIService yahooSvc;

	/* (non-Javadoc)
	 * @see com.mpakam.service.StockQuoteService#syncBySymbol(java.lang.String)
	 */
	@Transactional
	@Override
	public void analyzeStock(Stock symbol) throws Exception {
		Set<StockQuote> quoteList =dataProviderSvc.retrieveCandleData(symbol);
		StockQuote lastItem=quoteDao.findLastStockQuote(symbol);
		//TODO: Validate - Do we have any new quotes to retreive ?
		//TODO: Call the strategy selector
		TreeSet<StockQuote> quotes = saveQuotes(symbol,quoteList);
		if(!quotes.isEmpty())
			strategySvc.executeStrategy(quotes);
		else
			System.out.println("Stock - No new records were found- Market may have probably closed - " +symbol.getTicker());
		return ;
	}
	
	@Transactional
	public TreeSet<StockQuote> saveQuotes(Stock stock, Set<StockQuote> quoteList) {
		TreeSet<StockQuote> newRecords = new TreeSet<StockQuote>();
		StockQuote lastItem=quoteDao.findLastStockQuote(stock);
		quoteList.forEach(p->{
			try {
				if(lastItem ==null || p.getQuoteDatetime().isAfter(lastItem.getQuoteDatetime())){
					if(p.getInterval() != 480) p.setInterval(stock.getInterval());
					p.setStockQuoteId(quoteDao.save(p));
					System.out.println(LocalDateTime.now()+" Found new entry for "+
					stock.getTicker() +"; stocknum: "+stock.getStocknum()+" @ "+
							p.getQuoteDatetime()  + "id: " + p.getStockQuoteId());
					newRecords.add(p);
				}
			}catch(RuntimeException re) {
				System.out.println(" Run time Error " + re.getLocalizedMessage());
				return; // If there is an error, lets handle it next time
			}
		});
		return newRecords;
	}

	@Override
	public void cleanupOldData() {
		// TODO Auto-generated method stub
		
	}

	private void analyzeDailyStock(Stock symbol) throws Exception {
		Set<StockQuote> quoteList =dataProviderSvc.retrieveDailyCandleData(symbol);
		int interval= symbol.getInterval();
		symbol.setInterval(480);
		TreeSet<StockQuote> quotes = saveQuotes(symbol,quoteList);
		int trend=0;
		if(!quotes.isEmpty())
			trend = strategySvc.executeStrategy(quotes);
		else
			System.out.println("Daily - No new records were found- Market may have probably closed - " + symbol.getTicker());
		if(trend!=0) {
			symbol.setInterval(interval); 
			symbol.setTrend(trend);
			MonitoredStock mStock = new MonitoredStock();
			mStock.setAddedBy(0);
			mStock.setAddedDate(Calendar.getInstance().getTime());
			mStock.setInterval(interval);
			mStock.setStock(symbol);
			mStock.setTrennd(trend);
			mStockDao.cleanUpAndSave(mStock);
			stockDao.save(symbol);
		}
		return ;
	}

    public void analyzeStock(List<MonitoredStock> list) throws InterruptedException, ExecutionException {
    	
    	list.stream().forEach(t->{
    		processMonitoredStock(t);
    		});
    }
    
    @Override
    public void iexAnalyzeStock(List<MonitoredStock> list) throws InterruptedException, ExecutionException {
    		processIEXMonitoredStock(list);
//    		emailSvc.sendEmail();
    }
    
    @Override
    public void iexRenkoChartAnalyzeStock(List<MonitoredStock> list) throws InterruptedException, ExecutionException {
    		analyzeRenkoStockIEX(list);
    }
    
	private void processIEXMonitoredStock(List<MonitoredStock> list) {
		try {

			analyzeStockIEX(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void analyzeStockIEX(List<MonitoredStock> mStockList) {
		Map<Stock,TreeSet<StockQuote>> stockQMap =iexService.generateStockQuotes(mStockList);
		stockQMap.entrySet().parallelStream().forEach(entry->{
			log.error("Processing analyze Stock IEX for " + entry.getKey().getTicker());
			TreeSet<StockQuote> quotes = saveQuotes(entry.getKey(),entry.getValue());
			if(!quotes.isEmpty())
				strategySvc.executeStrategy(quotes);
			else
				System.out.println("IEX- No new records were found- Market may have probably closed - " +entry.getKey().getTicker());
		});
		
		return ;
	}
	
	private void analyzeRenkoStockIEX(List<MonitoredStock> mStockList) {
		Map<Stock,TreeSet<StockQuote>> stockQMap =iexService.generateStockQuotes(mStockList);
		stockQMap.entrySet().parallelStream().forEach(entry->{
			log.error("Processing analyze Stock IEX for " + entry.getKey().getTicker());
			TreeSet<StockQuote> quotes = saveQuotes(entry.getKey(),entry.getValue());
			if(!quotes.isEmpty())
				try {
					renkoSvc.executeStrategy(quotes);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
				System.out.println("IEX- No new records were found- Market may have probably closed - " +entry.getKey().getTicker());
		});
		return ;
	}

	@Transactional
    private void processMonitoredStock(MonitoredStock t) {
    	boolean retry = true;
		int attempts = 0;
		while(retry) {
			try {
				System.out.println("Synchronizing data for "+t.getStock().getTicker() + " @ " + t.getStock().getInterval());
				Thread.sleep(1000); //After sleeping try again
				analyzeStock(t.getStock());
				retry=false;
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println(t.getStock().getTicker() + " Error - " + e.getLocalizedMessage());
				try {
					Thread.sleep(++attempts*1000); //After sleeping try again
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				retry = attempts<15;
			}
		}
    }

    @Transactional
	@Override
	public void analyzeDailyStocks() throws Exception {
    	mStockDao.deleteAll();
    	
        List<Stock> stockList = stockDao.findAll();
        
		stockList.forEach(t -> {
			boolean retry = true;
			int attempts = 0;
			while (retry) {
				try {
					Thread.sleep(1000);
					analyzeDailyStock(t);
					//TODO: Also setup the history for all monitored stocks from AlphaVantage
					retry = false;
				} catch (Exception e) {
					System.out.println(t.getTicker() + " Error - " + e.getLocalizedMessage());
					try {
						Thread.sleep(++attempts * 1000); // After sleeping try again
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					retry = attempts < 15;
				}
			}
		});
		monitorSvc.loadMonitoredStockHistory();
	}    
    
    @Transactional
	@Override
	public void batchDailyAnalyzeStock() throws Exception {
    	//Cleaning up an existing monitored stocks
    	List<MonitoredStock> list =mStockDao.retrievegetActivelyMonitoredStocks();
        list.stream().forEach(m->{
        	if(m.getAddedBy() == 0)
        		mStockDao.delete(m);
        });
        
    	List<Stock> stocks=  stockDao.findAll();
    	int start =0;
		int end=0;
		List<List<Stock>> stocksArray = new ArrayList<List<Stock>>();
    	for(int i=start ;i<stocks.size();) {
    		end=i+99;
    		start = i;
    		if(end>stocks.size() )
    			end=stocks.size();
    		i=end+1;
    		List<Stock> stocksSubList= stocks.subList(start, end);
    		stocksArray.add(stocksSubList);
    	}
    	
    	stocksArray.parallelStream().forEach(stockArray->{
    		String symbols = stockArray.stream().map(q->q.getTicker()).collect(Collectors.joining(","));
    		System.out.println("Stock Size" +stockArray.size() +" \n " + symbols);
    		Map<Stock, TreeSet<StockQuote>> quotesMap;
			try {
				quotesMap = iexService.retrieveBatchDailyCandleData(symbols);
				quotesMap.keySet().forEach(p->{
					processDailyBatchStockQuotes(p,quotesMap.get(p));
	    		});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	});
    	
    	monitorSvc.loadBatchMonitoredStockHistory();	    	
		return ;
	}
    
    private void processDailyBatchStockQuotes(Stock p, TreeSet<StockQuote> stockQuotes) {
    	int interval = p.getInterval();
    	p.setInterval(480);
    	TreeSet<StockQuote> quotes = saveQuotes(p,stockQuotes);
		int trend=0;
		if(!quotes.isEmpty())
			trend =strategySvc.executeStrategy(quotes);
		else
			System.out.println("Daily Batch - No new records were found- Market may have probably closed - " +p.getTicker());
		
		//if(trend!=0) { //TODO: Changed to add all stocks even the ones with no trend
		if(true) {
			System.out.println("Adding " + p.getTicker() + " for closer monitoring");
			MonitoredStock mStock = new MonitoredStock();
			mStock.setAddedBy(0);
			mStock.setAddedDate(Calendar.getInstance().getTime());
			mStock.setInterval(interval);//Daily
			mStock.setStock(p);
			mStock.setTrennd(trend);
			mStockDao.cleanUpAndSave(mStock);
		}
    }
    
    @Transactional
	@Override
	public void initialBatchAnalyzeStock() throws Exception {
    	List<Stock> stocks=  stockDao.findAll();
    	int start =0;
		int end=0;
		List<List<Stock>> stocksArray = new ArrayList<List<Stock>>();
    	for(int i=start ;i<stocks.size();) {
    		end=i+99;
    		start = i;
    		if(end>stocks.size() )
    			end=stocks.size();
    		i=end+1;
    		List<Stock> stocksSubList= stocks.subList(start, end);
    		stocksArray.add(stocksSubList);
    	}
    	
    	stocksArray.parallelStream().forEach(stockArray->{
    		String symbols = stockArray.stream().map(q->q.getTicker()).collect(Collectors.joining(","));
    		System.out.println("Stock Size" +stockArray.size() +" \n " + symbols);
    		Map<Stock, TreeSet<StockQuote>> quotesMap;
			try {
				quotesMap = iexService.retrieveBatchDailyCandleData(symbols,true);
				quotesMap.keySet().forEach(p->{
					processDailyBatchStockQuotes(p,quotesMap.get(p));
	    		});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	});
    	monitorSvc.loadMonitoredStockHistory();
		return ;
	}
    
    @Transactional
	@Override
	public void analyzeStockFromAlphaVantage(Stock symbol) throws Exception {
		Set<StockQuote> quoteList =null;
		int retry=10;
		while(retry>0 && quoteList == null) {
			try{
				quoteList=dataProviderSvc.retrieveCandleData(symbol);
			}catch(Exception e) {
				Thread.currentThread().sleep(10000);
				retry--;
			}
		}
		StockQuote lastItem=quoteDao.findLastStockQuote(symbol);
		if(lastItem != null && iexService.getTimerSeries(lastItem.getQuoteDatetime(),symbol.getInterval()).size() == 0) {
			System.out.println(symbol.getTicker() +" Already at full sync - no longer needed");
			return; 
		}
		//TODO: Call the strategy selector
		TreeSet<StockQuote> quotes = saveQuotes(symbol,quoteList);
		if(!quotes.isEmpty())
			strategySvc.executeStrategy(quotes);
		else
			System.out.println("Stock - No new records were found- Market may have probably closed - " +symbol.getTicker());
		return ;
	}

	@Override
	public void analyzeStockYahooAPI(List<MonitoredStock> mStockList) throws InterruptedException, ExecutionException {
		mStockList.parallelStream().forEach(mStock->{
			Stock s = mStock.getStock();
			System.out.println("Analyzing - " + s.getTicker() );
			//Retrieve the most recent Stock Quote for this stock.
			StockQuote sq = quoteDao.findLastStockQuote(s);
			System.out.println(sq);
			if(sq == null) {
				sq = new StockQuote();
				sq.setStock(s);
			}
			try {
				quoteDao.save(yahooSvc.retrieveDailyStockQuote(sq));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return ;
		
	}
}
