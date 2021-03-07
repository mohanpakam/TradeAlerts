package com.mpakam.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.mpakam.model.StockQuote;
import com.mpakam.util.DateUtil;

/**
 * Generates the Higher Time Frame Stock Quotes from a given Stock Quote
 * 
 * @author mohan
 *
 */
@Service
public class HigherTimeFrameStockQuoteService {
	
	private Map<String, StockQuote> weeklyFinalSQs = new HashMap<String, StockQuote> ();  

	/**
	 * Makes up the Weekly the StockQuote from the given StockQuote Set
	 * 
	 * @param sq
	 * @return
	 */
	public StockQuote getFinalWeekly(StockQuote sq, Set<StockQuote> sqSet) {

		LocalDate date = sq.getQuoteDatetime().toLocalDate();
//		System.out.println("Date = " + date);
		final LocalDate start = DateUtil.startOfWeek(date);

		LocalDate end = DateUtil.endOfWeek(date);
		return aggregateStockQuote(sq, sqSet, start, end);
	}

	public StockQuote getFinalMonthly(StockQuote sq, Set<StockQuote> sqSet) {

		LocalDate date = sq.getQuoteDatetime().toLocalDate();
//		System.out.println("Date = " + date);
		final LocalDate start = DateUtil.firstDayOfMonth(date);

		LocalDate end = DateUtil.lastDayOfMonth(date);

		return aggregateStockQuote(sq, sqSet, start, end);
	}

	private StockQuote aggregateStockQuote(StockQuote sq, Set<StockQuote> sqSet, LocalDate startDate,
			LocalDate endDate) {
		if(sq == null) {
			System.out.println("why is this null");
		}
		StockQuote aggregateSq = new StockQuote(sq);


		sqSet.stream().filter(m -> startDate.isBefore(m.getQuoteDatetime().toLocalDate())
				&& endDate.isAfter(m.getQuoteDatetime().toLocalDate())).forEach(tsq -> {
					// open
//					System.out.println(tsq.getQuoteDatetime() + "-Open:" + tsq.getOpen() + "-Close:" + tsq.getClose()
//							+ "-High:" + tsq.getHigh() + "-Low:" + tsq.getLow());
//					System.out.println(aggregateSq.getQuoteDatetime() + "-Open:" + aggregateSq.getOpen() + "-Close:" + aggregateSq.getClose()
//					+ "-High:" + aggregateSq.getHigh() + "-Low:" + aggregateSq.getLow());
					if (aggregateSq.getQuoteDatetime() == null) {
						aggregateSq.setOpen(tsq.getOpen());
						aggregateSq.setQuoteDatetime(tsq.getQuoteDatetime());
					}
					// high
					if (aggregateSq.getHigh().compareTo(tsq.getHigh()) == -1) {
						aggregateSq.setHigh(tsq.getHigh()); // High of the Week
					}
					// low
					if (aggregateSq.getLow().compareTo(tsq.getLow()) == 1) {
						aggregateSq.setLow(tsq.getLow()); // High of the Week
					}
					// close
					aggregateSq.setClose(tsq.getClose());
				});
		
		if (aggregateSq.getQuoteDatetime() == null) 
			aggregateSq.setQuoteDatetime(sq.getQuoteDatetime());
		
//		System.out.println(aggregateSq.getQuoteDatetime() + "-Open:" + aggregateSq.getOpen() + "-Close:"
//				+ aggregateSq.getClose() + "-High:" + aggregateSq.getHigh() + "-Low:" + aggregateSq.getLow());
		return aggregateSq;
	}

	/**
	 * Creates Weekly StockQuote 
	 * 
	 * @param daily StockQuote
	 * @return Weekly StockQuote
	 */
	public StockQuote getWeekly(StockQuote dailySQ, Set<StockQuote> sqSet) {
		LocalDate date =dailySQ.getQuoteDatetime().toLocalDate();
		final LocalDate start = DateUtil.startOfWeek(date);
		return aggregateStockQuote(dailySQ, sqSet, start, date);
	}

	/**
	 * Creates Monthly StockQuote on the Fly
	 * 
	 * @param daily StockQuote
	 * @return Monthly StockQuote
	 */
	public StockQuote getMonthly(StockQuote dailySQ, Set<StockQuote> sqSet) {
		LocalDate date = dailySQ.getQuoteDatetime().toLocalDate();
//		System.out.println("Date = " + date);
		final LocalDate start = DateUtil.firstDayOfMonth(date);
		return aggregateStockQuote(dailySQ, sqSet, start, date);
	}
	
	/**
	 * Creates Previous Week's Weekly StockQuote 
	 * 
	 * @param daily StockQuote
	 * @return Weekly StockQuote
	 */
	public StockQuote getPreviousWeek(StockQuote dailySQ, Set<StockQuote> sqSet) {
		LocalDate date = dailySQ.getQuoteDatetime().toLocalDate();
		date = date.minusDays(7); //Last Week
//		System.out.println("Date = " + date);
		LocalDate start = DateUtil.startOfWeek(date).plusDays(1); // Get next day
		String key = dailySQ.getStock().getStocknum() + "-" +start.toString();

		if(weeklyFinalSQs.containsKey(key))
			return weeklyFinalSQs.get(key);		
		
		LocalDate end = DateUtil.endOfWeek(date);
		StockQuote lastWeekSq = getStockQuoteByDate(start, sqSet);
		// If the Opening day of the week is market closed, then find the next day
		for(int days=1;(lastWeekSq == null && days<5);days++) {
			System.out.println("NOT FOUND - Stock Quote for Date:" + start);
			start = start.plusDays(1); // to find the next day
			lastWeekSq = getStockQuoteByDate(start, sqSet);
		}
		if(lastWeekSq != null) {
			StockQuote prevW=aggregateStockQuote(lastWeekSq, sqSet, start, end);
			if(prevW !=null) {
				weeklyFinalSQs.put(key, prevW);
				return prevW;
			}
		}
		return dailySQ;
	}
	
	public StockQuote getStockQuoteByDate(LocalDate start,Set<StockQuote> sqSet) {
//		System.out.println("START:" +start + "SQ Count -"+ sqSet.size());
		return sqSet.stream().filter(m -> start.isEqual(m.getQuoteDatetime().toLocalDate())).findFirst().orElse(null);		
	}

}
