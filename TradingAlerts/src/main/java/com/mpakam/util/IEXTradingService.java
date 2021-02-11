package com.mpakam.util;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mpakam.app.config.EnvironmentConfig;
import com.mpakam.dao.StockDao;
import com.mpakam.dao.StockHlDataDao;
import com.mpakam.dao.StockQuoteDao;
import com.mpakam.dao.StockTickDataDao;
import com.mpakam.model.MonitoredStock;
import com.mpakam.model.Stock;
import com.mpakam.model.StockHlData;
import com.mpakam.model.StockQuote;
import com.mpakam.model.StockTickData;

import pl.zankowski.iextrading4j.api.stocks.Chart;
import pl.zankowski.iextrading4j.api.stocks.ChartRange;
import pl.zankowski.iextrading4j.api.stocks.v1.BatchStocks;
import pl.zankowski.iextrading4j.client.IEXCloudClient;
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder;
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.manager.RestRequest;
import pl.zankowski.iextrading4j.client.rest.request.stocks.v1.BatchMarketStocksRequestBuilder;

@Service
public class IEXTradingService  {	
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	final static String START_TIME = "ST";
	final static String END_TIME = "ET";

	IEXCloudClient cloudClient = IEXTradingClient.create(IEXTradingApiVersion.IEX_CLOUD_STABLE,
            new IEXCloudTokenBuilder()
                    .withPublishableToken("pk_1426fe3d39c944c5887c27955744a494")
                    .withSecretToken("sk_b38be90a8c134c45870f17676095b581")
                    .build());
	
	@Autowired
	StockDao stockDao;
	
	@Autowired
	StockHlDataDao hlDao;

	@Autowired
	StockQuoteDao stockQuoteDao;
	
	@Autowired
	StockTickDataDao tickerDao;
	
	@Autowired
	AlphaVantageService dataProviderSvc;
	
	@Autowired
	BigDecimalUtil bigDUtil;
	
	@Autowired
	EnvironmentConfig eConfig;
	
	
	public TreeSet<StockQuote> retrieveCandleData(Stock stockObj) throws Exception {
		
		return null;
	}

	
	public Map<Stock,TreeSet<StockQuote>> retrieveBatchDailyCandleData(String symbols) throws Exception {
		return retrieveBatchDailyCandleData(symbols,false);
	}

	
	@Transactional
	public Map<Stock,TreeSet<StockQuote>> retrieveBatchDailyCandleData(String symbols, boolean initialLoad) throws Exception {
//		RestRequest<Map<String, Map<String, List<Chart>>>> builder= new BatchChartRequestBuilder()
//        .withSymbol(symbols)
//        .build();
//		if(initialLoad) {
//			builder =new BatchChartRequestBuilder()
//			        .withSymbol(symbols)
//			        .withChartRange(ChartRange.THREE_MONTHS)
//			        .build();
//		}else {
//			builder =new BatchChartRequestBuilder()
//	        .withSymbol(symbols)
//	        .withChartRange(ChartRange.THREE_MONTHS) //TODO: Added 3 months at both levels. remove as needed
//	        .build();
//		}
		//TODO: Implement Batch Daily Data
		final Map<String, Map<String, List<Chart>>> chartList = new HashMap<String, Map<String, List<Chart>>>();
		Map<Stock,  TreeSet<StockQuote>> rsp = new HashMap<>();

        chartList.entrySet().parallelStream().forEach(p->{
        	List<Chart> chart =  p.getValue().get("chart");
        	TreeSet<StockQuote> stockQuotes = new TreeSet<>();
        	Stock s = stockDao.findBySymbol(p.getKey());
        	chart.forEach(q->{
        		if(q.getHigh() == null) {
        			System.out.println(p.getKey() + " high " + q.getHigh());
        			
        		}else {
        		StockQuote sq = new StockQuote();
        		sq.setClose(q.getClose());
        		sq.setHigh(q.getHigh());
        		sq.setInterval(480);
        		sq.setLow(q.getLow());
        		sq.setOpen(q.getOpen());
        		//2018-01-05
        		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        		LocalDateTime dateTime = LocalDateTime
        				.parse(q.getDate() + " 16:00", formatter);
        		sq.setQuoteDatetime(dateTime);
        		sq.setStock(s);
        		
        		stockQuotes.add(sq);
        		}
        	});
        	rsp.put(s, stockQuotes);
        });
		return rsp;
	}
	
	/**Retrieves the Tick Data at the given moment
	 * @param mList
	 * @return
	 */
	public Map<Stock, BigDecimal> getBatchPrice(List<MonitoredStock>  mList){
		
		String symbols = mList.stream().map(p->p.getStock().getTicker()).collect(Collectors.joining(","));
		//TODO: Retrieves the current price of a given list of stocks
//        final Map<String, Price> chartList = iexTradingClient.executeRequest(new BatchPriceRequestBuilder()
//                .withSymbol(symbols)
//                .build());
//		final Map<String, Price> chartList = iexTradingClient.executeRequest(new BatchPriceRequestBuilder()
//              .withSymbol(symbols)
//              .build());
        Map<Stock, BigDecimal> rsp = new HashMap<>();
//        chartList.entrySet().forEach(p->{
//        	Price price=  p.getValue();
//        	rsp.put(stockDao.findBySymbol(p.getKey()), price.getPrice());
////        	System.out.println(p.getKey()+ " price is " + price.getPrice());
//        });
		return rsp;
	}
	
	/**Retrieves the Daily Chart for a given list of Stocks
	 * @param mList
	 * @return
	 * @throws Exception
	 */
	public List<StockHlData> retrieveBatchStockHL(List<MonitoredStock> mList) throws Exception {
		
		List<String> symbols = mList.stream().map(p->p.getStock().getTicker()).collect(Collectors.toList());
		
    	final Map<String, BatchStocks> resultMap = cloudClient.executeRequest(new BatchMarketStocksRequestBuilder()
    	        .withSymbols(symbols)
    	        .withChartRange(ChartRange.THREE_MONTHS)
    	        .build());
        
        List<StockHlData> stockHLList= new ArrayList<>();
        //TODO: Implement Retrieve the Batch Stock OHLC 
//        if(!resultMap.isEmpty())
//        	resultMap.entrySet().parallelStream().forEach(eS->{
//        	Stock s= stockDao.findBySymbol(eS.getKey());
//        	List<DailyChart> chartList = eS.getValue().get("chart");
//        	StockHlData lastStockHl = hlDao.findLastByStock(s);
//        	LocalDateTime lastStockHlEndTime =null;
//        	if(lastStockHl != null ) {
//        		lastStockHlEndTime = lastStockHl.getHlDatetime().plusMinutes(eConfig.getTICK_INTERVAL());
//        	}else {
//        		lastStockHlEndTime = LocalDateTime.now().minusDays(30);
//        	}
//        	TreeSet<StockHlData> stockHlList = create5MinutesData(chartList,s, eConfig.getTICK_INTERVAL(),lastStockHlEndTime);
//        	if(!stockHlList.isEmpty())
//        		stockHLList.addAll(stockHlList);
//        });
		return stockHLList;
	}

	@Transactional
	public Map<Stock,TreeSet<StockQuote>> generateStockQuotes(List<MonitoredStock> mStockList){
		
		Map<Stock,TreeSet<StockQuote>> stockQuotesMap = new HashMap<Stock,TreeSet<StockQuote>>(); 
		
		mStockList.parallelStream().forEach(mStock->{
			TreeSet<StockQuote> stockQuotes = new TreeSet<>();
			log.error("Processin generateStockQuotes-mStock :" + mStock.getStock().getTicker() + "; interval: " +mStock.getInterval());
			//retrieve the latest StockQuote for this stock and interval
			StockQuote lastItem=stockQuoteDao.findLastStockQuote(mStock.getStock());
			
			List<HashMap<String, LocalDateTime>> timeSeries = null;
			LocalDateTime lastItemTime =null;
			
			if(lastItem != null)
				lastItemTime = lastItem.getQuoteDatetime();
			else
				lastItemTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).minusDays(30).toLocalDateTime();
			
			timeSeries = getTimerSeries(lastItemTime, mStock.getStock().getInterval());
//			timeNow.atStartOfDay(ZoneId.systemDefault()).toInstant())
			
			//Find High & Low
			LinkedList<StockHlData> stockHlList = hlDao.findAllEntriesAfter(mStock.getStock().getStocknum(),lastItemTime);
			try {
			timeSeries.stream().forEach(ts->{
				LocalDateTime startTime = ts.get(IEXTradingService.START_TIME);
				LocalDateTime endTime = ts.get(IEXTradingService.END_TIME);
				
				List<StockHlData> stockList =  stockHlList.stream().filter(m -> ((m.getHlDatetime().isAfter(startTime) || m.getHlDatetime().equals(startTime)) 
						&& m.getHlDatetime().isBefore(endTime) || m.getHlDatetime().equals(endTime)))
						.collect(Collectors.toList());
				
				
				Optional<BigDecimal> highOpt =stockList.stream().map(a->a.getHigh()).filter(m->bigDUtil.isValid(m)).max(BigDecimal::compareTo);
				Optional<BigDecimal> lowOpt =stockList.stream().map(a->a.getLow()).filter(m->bigDUtil.isValid(m)).min(BigDecimal::compareTo);
				
				StockQuote sq = new StockQuote();
				sq.setHigh(highOpt.isPresent()?highOpt.get():BigDecimalUtil.ZERO);
				sq.setLow(lowOpt.isPresent()?lowOpt.get():BigDecimalUtil.ZERO);
				
				if(!bigDUtil.isValid(sq.getHigh()) ||!bigDUtil.isValid(sq.getLow())) {
//					throw new RuntimeException("Either High or Low is ZERO"); TODO: what can be done if high/low is zero
					log.error(mStock.getStock().getTicker() + " [HL ISSUE] skipping to add stock quote @ " + startTime);
					return; // no point in adding this entry.
				}

				BigDecimal open = BigDecimal.ZERO; 
				BigDecimal close = BigDecimal.ZERO;

	        	if(stockList.size()>0) {
	        		int i =0;
	        		open = stockList.get(i).getOpen();
	        		
	        		for(;!bigDUtil.isValid(open) && i<stockList.size()-1;) {
	        			open = stockList.get(++i).getOpen();
	        		}
	        		int closeI =stockList.size()-1;
	        		
	        		close = stockList.get(closeI).getClose();
	        		
	        		for(;!bigDUtil.isValid(close) && closeI>=1;) {
	        			close = stockList.get(--closeI).getClose();
	        		}
	        	}else {
	        		open = BigDecimal.ZERO;
	        		close = BigDecimal.ZERO;
//	        		throw new RuntimeException("no open tick data found for "+ s.getTicker());
	        	}
				
				if(!bigDUtil.isValid(open) ||  !bigDUtil.isValid(close)) {
					log.error(mStock.getStock().getTicker() + " [OC ISSUE] skipping to add stock quote @ " + startTime);
					return; // there is no point in adding an entry with no open/close.
				}
				
				sq.setOpen(open);
				sq.setClose(close);
				sq.setInterval(mStock.getInterval());
				sq.setStock(mStock.getStock());
				sq.setQuoteDatetime(startTime);
				stockQuotes.add(sq);
			});
			}catch(RuntimeException re) {
				re.printStackTrace();
				System.out.println("Error while generating Stock Quotes - " + mStock.getStock().getTicker());
				/*TreeSet<StockQuote> quoteList=null;
				try {
					quoteList = dataProviderSvc.retrieveFailProofCandleData(mStock.getStock());
				} catch (Exception e) {
					e.printStackTrace();
				}
				stockQuotesMap.put(mStock.getStock(), quoteList);*/
				return;
			}			
			stockQuotesMap.put(mStock.getStock(), stockQuotes);
		});
		
		return stockQuotesMap;
	}	
	
	
	public List<HashMap<String, LocalDateTime>> getTimerSeries(LocalDateTime currentQuoteTime, int interval) {
		List<HashMap<String, LocalDateTime>> quoteTimes = new ArrayList<HashMap<String, LocalDateTime>>();


		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
 
		LocalDateTime startTime=currentQuoteTime.plusMinutes(interval);
		while(startTime.isBefore(LocalDateTime.now())) {
			
			HashMap<String, LocalDateTime> startEndTime= new HashMap<>();
			
			if(startTime.getHour() >15) {
				startTime = LocalDateTime
						.parse(startTime.plusDays(1).toLocalDate().toString() + " " +eConfig.getSESSION_START(), formatter);
			}else {
				
				if(startTime.getDayOfWeek() == DayOfWeek.SATURDAY || startTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
					startTime =startTime.plusMinutes(interval);;
					continue;
				}
				if(startTime.plusMinutes(interval-1).isAfter(LocalDateTime.now()))
					break; // if the end time is after now, then dont add it.
				
				startEndTime.put(IEXTradingService.START_TIME, startTime);
				if(startTime.plusMinutes(interval-1).getHour() >15 && interval == 60)
					startEndTime.put(IEXTradingService.END_TIME, startTime.plusMinutes(29) );
				else
					startEndTime.put(IEXTradingService.END_TIME, startTime.plusMinutes(interval-1) );
				log.debug("Start Time " + startTime +" End Time " + startTime.plusMinutes(interval-1)) ;
				startTime=startTime.plusMinutes(interval);
				quoteTimes.add(startEndTime);
			}
		}
		return quoteTimes;
	}
	


}