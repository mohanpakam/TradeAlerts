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
import pl.zankowski.iextrading4j.api.stocks.DailyChart;
import pl.zankowski.iextrading4j.api.stocks.Price;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.manager.RestRequest;
import pl.zankowski.iextrading4j.client.rest.request.stocks.BatchChartRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.BatchDailyChartRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.BatchPriceRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.ChartRange;

@Service
public class IEXTradingService  {	
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	final static String START_TIME = "ST";
	final static String END_TIME = "ET";

	IEXTradingClient iexTradingClient = IEXTradingClient.create();
	
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
		RestRequest<Map<String, Map<String, List<Chart>>>> builder= new BatchChartRequestBuilder()
        .withSymbol(symbols)
        .build();
		if(initialLoad) {
			builder =new BatchChartRequestBuilder()
			        .withSymbol(symbols)
			        .withChartRange(ChartRange.THREE_MONTHS)
			        .build();
		}else {
			builder =new BatchChartRequestBuilder()
	        .withSymbol(symbols)
	        .withChartRange(ChartRange.THREE_MONTHS) //TODO: Added 3 months at both levels. remove as needed
	        .build();
		}
		final Map<String, Map<String, List<Chart>>> chartList = iexTradingClient.executeRequest(builder);
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
	
	public Map<Stock, BigDecimal> getBatchPrice(List<MonitoredStock>  mList){
		
		String symbols = mList.stream().map(p->p.getStock().getTicker()).collect(Collectors.joining(","));

        final Map<String, Price> chartList = iexTradingClient.executeRequest(new BatchPriceRequestBuilder()
                .withSymbol(symbols)
                .build());
        Map<Stock, BigDecimal> rsp = new HashMap<>();
        chartList.entrySet().forEach(p->{
        	Price price=  p.getValue();
        	rsp.put(stockDao.findBySymbol(p.getKey()), price.getPrice());
//        	System.out.println(p.getKey()+ " price is " + price.getPrice());
        });
		return rsp;
	}
	
	public List<StockHlData> retrieveBatchStockHL(List<MonitoredStock> mList) throws Exception {
		
		String symbols = mList.stream().map(p->p.getStock().getTicker()).collect(Collectors.joining(","));
		
        final Map<String, Map<String, List<DailyChart>>> rspChartList = iexTradingClient.executeRequest(new BatchDailyChartRequestBuilder()
                .withSymbol(symbols)
                .build());
        
        List<StockHlData> stockHLList= new ArrayList<>();
        if(!rspChartList.isEmpty())
        rspChartList.entrySet().parallelStream().forEach(eS->{
        	Stock s= stockDao.findBySymbol(eS.getKey());
        	List<DailyChart> chartList = eS.getValue().get("chart");
        	StockHlData lastStockHl = hlDao.findLastByStock(s);
        	LocalDateTime lastStockHlEndTime =null;
        	if(lastStockHl != null ) {
        		lastStockHlEndTime = lastStockHl.getHlDatetime().plusMinutes(eConfig.getTICK_INTERVAL());
        	}else {
        		lastStockHlEndTime = LocalDateTime.now().minusDays(30);
        	}
        	TreeSet<StockHlData> stockHlList = create5MinutesData(chartList,s, eConfig.getTICK_INTERVAL(),lastStockHlEndTime);
        	if(!stockHlList.isEmpty())
        		stockHLList.addAll(stockHlList);
        });
		return stockHLList;
	}
	
	public TreeSet<StockHlData> createMinutesData(List<DailyChart> chartList, Stock s, int interval ,StockHlData lastStockHl) {
		int i=1;
        BigDecimal high = null;
        BigDecimal low = null;
    
        TreeSet<StockHlData> stockHlList = new TreeSet<>();
        String today=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDateTime quoteDatetime = null;
        String dateTimeStr=null;
        BigDecimal lastHigh = null;
        BigDecimal lastLow =null;
        for(DailyChart dc: chartList){
        	LinkedList<StockTickData> tickDataList = tickerDao.findCurrentSessionByStockNum(s.getStocknum());
        	dateTimeStr=dc.getDate() + " " + dc.getMinute();
        	LocalDateTime dateTime =null;
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
        	try {
        		dateTime = LocalDateTime
    				.parse(dateTimeStr, formatter);
        	}catch(Exception e) {
        		System.out.println("Error while parsing the minute Candle " + dateTimeStr);
        		dateTimeStr= today + " " + dc.getMinute();
        		dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        	}

    		if(i == 1) { //Capturing the time
        		//quoteDatetime=dateTime.minusMinutes(1);
    			quoteDatetime=dateTime;
        	}
//    		System.out.println("date time : " + dateTime + " Last date time is (-1)" + lastStockHl.getHlDatetime().plusMinutes(interval));
    		if(lastStockHl !=null && !dateTime.isAfter(lastStockHl.getHlDatetime().plusMinutes(interval-1))) { // to start processing from the last interval
    			continue;
    		}
    		if(lastLow == null)
    			lastLow= lastStockHl.getLow();
    		if(lastHigh == null)
    			lastHigh = lastStockHl.getHigh();
    		
    		BigDecimal mrktHigh = !bigDUtil.isValid(dc.getMarketHigh())?dc.getHigh():dc.getMarketHigh();
    		BigDecimal mrktLow = !bigDUtil.isValid(dc.getMarketLow())?dc.getLow():dc.getMarketLow();
//    		System.out.println(dc.getDate() + " " + dc.getMinute());

    		//sometimes high or low can be -1 to represent there was no change in this value.
    		//START
    		
    		mrktHigh = bigDUtil.isValid(mrktHigh)?mrktHigh:lastHigh;
    		mrktLow = bigDUtil.isValid(mrktLow)?mrktLow:lastLow;
    		//END
    		
    		//Determining whether the high we have is higher and low is lower
    		//START #2
        	if(high == null || high.compareTo(mrktHigh)<0 )
        		high=mrktHigh;
        	
        	if(low == null || low.compareTo(mrktLow)>0 )
        		low=mrktLow;
    		//END#2
        	lastLow = mrktLow;
        	lastHigh = mrktHigh;
        	//creating the StockHlData only @ the interval which 5 by default.
        	//START#3
        	if(i == interval) {
        		StockHlData hl= new StockHlData();
        		hl.setStock(s);
        		hl.setInterval(interval);
        		hl.setHigh(high);
        		if(low.compareTo(BigDecimalUtil.ZERO) == 0)
        			throw new RuntimeException("Low cannot be ZERO "+ s.getTicker() + " @ " + dateTimeStr);
        		hl.setLow(low);
        		hl.setHlDatetime(quoteDatetime);
        		if(quoteDatetime == null )
        			throw new RuntimeException("Quote datetime is null " + s.getTicker() + " @ " + dateTimeStr);
        		stockHlList.add(hl);
        		lastStockHl=hl; //treating this as the last StockHL
        		high = null;
        		low = null;
        		i=1;
        	}else
        		i++;
        	//END#3
        }
		return stockHlList;
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
	
	
	public TreeSet<StockHlData> createOneMinuteData(List<DailyChart> chartList, Stock s, int interval ,StockHlData lastStockHl) {
        
        TreeSet<StockHlData> stockHlList = new TreeSet<>();
        String today=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dateTimeStr=null;
        BigDecimal lastHigh = null;
        BigDecimal lastLow =null;
        BigDecimal high = null;
        BigDecimal low = null;
        BigDecimal open = null;
        BigDecimal close = null;
        LinkedList<StockTickData> tickDataList = tickerDao.findCurrentSessionByStockNum(s.getStocknum());
        for(DailyChart dc: chartList){
        	dateTimeStr=dc.getDate() + " " + dc.getMinute();
        	LocalDateTime dateTime =null;
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
        	
        	try {
        		dateTime = LocalDateTime
    				.parse(dateTimeStr, formatter);
        	}catch(Exception e) {
        		System.out.println("Error while parsing the minute Candle " + dateTimeStr);
        		dateTimeStr= today + " " + dc.getMinute();
        		dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        	}
        	
        	if(lastStockHl!=null && lastStockHl.getHlDatetime().isAfter(dateTime)) {
        		continue;
        	}
        	
        	final LocalDateTime openTime = dateTime;
        	final LocalDateTime closeTime = dateTime.plusMinutes(1);//1 in interval
        	//START Retrieving the Open/close Tick
        	
//        	List<StockTickData> tickDataOpt =tickDataList.parallelStream().filter(p->p.getTickDatetime().compareTo(openTime) >= 0 && p.getTickDatetime().compareTo(closeTime) <=0).collect(Collectors.toList());
        	List<StockTickData> tickDataOpt =tickDataList.parallelStream().filter(p->p.getTickDatetime().isEqual(openTime) || p.getTickDatetime().isAfter(openTime))
        			.filter(p->p.getTickDatetime().isEqual(closeTime) || p.getTickDatetime().isBefore(closeTime))
        			.collect(Collectors.toList());
        	
        	if(tickDataOpt.size()>0) {
        		int i =0;
        		open = tickDataOpt.get(i).getPrice();
        		
        		for(;!bigDUtil.isValid(open) && i<tickDataOpt.size();) {
        			open = tickDataOpt.get(++i).getPrice();
        		}
        		int closeI =tickDataOpt.size()-1;
        		close = tickDataOpt.get(closeI).getPrice();
        		
        		for(;!bigDUtil.isValid(close) && closeI>=0;) {
        			close = tickDataOpt.get(--closeI).getPrice();
        		}
        		
//        		log.debug("using the open @ "+ tickDataOptOpen.get().getTickDatetime());
        	}else {
        		open = BigDecimal.ZERO;
        		close = BigDecimal.ZERO;
//        		throw new RuntimeException("no open tick data found for "+ s.getTicker());
        	}
        	//END Retrieving the Open/close Tick
        	
    		BigDecimal mrktHigh = !bigDUtil.isValid(dc.getMarketHigh())?dc.getHigh():dc.getMarketHigh();
    		BigDecimal mrktLow = !bigDUtil.isValid(dc.getMarketLow())?dc.getLow():dc.getMarketLow();
//    		System.out.println(dc.getDate() + " " + dc.getMinute());

    		//sometimes high or low can be -1 to represent there was no change in this value.
    		//START
    		
    		mrktHigh = bigDUtil.isValid(mrktHigh)?mrktHigh:lastHigh;
    		mrktLow = bigDUtil.isValid(mrktLow)?mrktLow:lastLow;
    		//END
    		
    		//Determining whether the high we have is higher and low is lower
    		//START #2
        	if(high == null || high.compareTo(mrktHigh)<0 )
        		high=mrktHigh;
        	
        	if((low == null || low.compareTo(mrktLow)>0 ) && bigDUtil.isValid(mrktLow))
        		low=mrktLow;
    		//END#2
        	lastLow = mrktLow;
        	lastHigh = mrktHigh;
        	//creating the StockHlData only @ the interval which 5 by default.
        	//START#3
			StockHlData hl = new StockHlData();
			hl.setStock(s);
			hl.setInterval(interval);
			hl.setHigh(high);
			if (!bigDUtil.isValid(low)) {
				//throw new RuntimeException("Low cannot be ZERO " + s.getTicker() + " @ " + dateTimeStr);
				continue; //TODO: instead of throwing out an error zero low, continue to the next one.
			}
			hl.setLow(low);
			hl.setHlDatetime(dateTime);

			//missing open/close may not be that important if we are dealing with non edge (5min, 10min) 
			// hl entries.
			
			if (bigDUtil.isValid(open) && bigDUtil.isValid(close)) {
				hl.setOpen(open);
				hl.setClose(close);
			}else {
//				throw new RuntimeException(s.getTicker() + "@" +dateTime+"Either open or close is null; Open: " +open + "close: " + close);
				hl.setOpen(BigDecimalUtil.MINUSONE);
				hl.setClose(BigDecimalUtil.MINUSONE);
			}
			
			stockHlList.add(hl);
			lastStockHl = hl; // treating this as the last StockHL
			high = null;
			low = null;

        	//END#3
        }
		return stockHlList;
	}
	
	public TreeSet<StockHlData> create5MinutesData(List<DailyChart> chartList, Stock s, int interval ,LocalDateTime lastStockHlEndTime) {
        
        TreeSet<StockHlData> stockHlList = new TreeSet<>();
        String today=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String dateTimeStr=null;
        BigDecimal lastHigh = null;
        BigDecimal lastLow =null;
        BigDecimal high = null;
        BigDecimal low = null;
        BigDecimal open = null;
        BigDecimal close = null;
        
        LinkedList<StockTickData> tickDataList = tickerDao.findCurrentSessionByStockNum(s.getStocknum());
        LocalDateTime startTime=null;
        
        int counter =0;
        for(DailyChart dc: chartList){
        	
        	dateTimeStr=dc.getDate() + " " + dc.getMinute();
        	LocalDateTime dateTime =null;
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
        	
        	try {
        		dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        	}catch(Exception e) {
        		System.out.println("Error while parsing the minute Candle " + dateTimeStr);
        		dateTimeStr= today + " " + dc.getMinute();
        		dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        	}
        	
        	if(lastStockHlEndTime.isAfter(dateTime)) {
        		continue;
        	}
        	
        	if(counter == 0) {
        		startTime= dateTime;
        		// prepare the open/close only during start tie.
        		final LocalDateTime openTime = dateTime;
            	final LocalDateTime closeTime = dateTime.plusMinutes(interval-1);//1 in interval
            	//START Retrieving the Open/close Tick
            	
            	List<StockTickData> tickDataOpt =tickDataList.parallelStream().filter(p->p.getTickDatetime().isEqual(openTime) || p.getTickDatetime().isAfter(openTime))
            			.filter(p->p.getTickDatetime().isEqual(closeTime) || p.getTickDatetime().isBefore(closeTime))
            			.collect(Collectors.toList());
            	
            	if(tickDataOpt.size()>0) {
            		int i =0;
            		open = tickDataOpt.get(i).getPrice();
            		
            		for(;!bigDUtil.isValid(open) && i<tickDataOpt.size();) {
            			open = tickDataOpt.get(++i).getPrice();
            		}
            		int closeI =tickDataOpt.size()-1;
            		close = tickDataOpt.get(closeI).getPrice();
            		
            		for(;!bigDUtil.isValid(close) && closeI>=0;) {
            			close = tickDataOpt.get(--closeI).getPrice();
            		}
            		
//            		log.debug("using the open @ "+ tickDataOptOpen.get().getTickDatetime());
            	}else {
            		open = BigDecimal.ZERO;
            		close = BigDecimal.ZERO;
            	}
        	}
        	if(counter<interval){
        		//END Retrieving the Open/close Tick
        		BigDecimal mrktHigh = dc.getHigh();
        		BigDecimal mrktLow = dc.getLow();

        		//sometimes high or low can be -1 to represent there was no change in this value.
        		//START
        		mrktHigh = bigDUtil.isValid(mrktHigh)?mrktHigh:lastHigh;
        		mrktLow = bigDUtil.isValid(mrktLow)?mrktLow:lastLow;
        		//END
    		
        		//Determining whether the high we have is higher and low is lower
        		//START #2
        		if(high == null || high.compareTo(mrktHigh)<0 )
        			high=mrktHigh;
        	
        		if((low == null || low.compareTo(mrktLow)>0 ) && bigDUtil.isValid(mrktLow))
        			low=mrktLow;
        		//END#2
        		lastLow = mrktLow;
        		lastHigh = mrktHigh;
        		counter++;
        		//creating the StockHlData only @ the interval which 5 by default.
        		//START#3
        		if(counter == interval) {
					StockHlData hl = new StockHlData();
					hl.setStock(s);
					hl.setInterval(interval);
					hl.setHigh(high);
					if (!bigDUtil.isValid(low)) {
						continue; // TODO: instead of throwing out an error zero low, continue to the next one.
					}
					hl.setLow(low);
					hl.setHlDatetime(startTime);
					// missing open/close may not be that important if we are dealing with non edge
					// (5min, 10min)
					// hl entries.

					if (bigDUtil.isValid(open) && bigDUtil.isValid(close)) {
						hl.setOpen(open);
						hl.setClose(close);
					} else {
						hl.setOpen(BigDecimalUtil.MINUSONE);
						hl.setClose(BigDecimalUtil.MINUSONE);
					}
					stockHlList.add(hl);
					lastStockHlEndTime = startTime.plusMinutes(interval - 1);
					high = null;
					low = null;
					counter = 0;
					// END#3
				}
        	}
        }
		
        return stockHlList;
	}

}