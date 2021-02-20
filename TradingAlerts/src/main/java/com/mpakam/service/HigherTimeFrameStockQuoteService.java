package com.mpakam.service;

import java.time.LocalDate;
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

	/**
	 * Makes up the Weekly the StockQuote from the given StockQuote Set
	 * 
	 * @param sq
	 * @return
	 */
	public StockQuote getFinalWeekly(StockQuote sq, Set<StockQuote> sqSet) {

		LocalDate date = sq.getQuoteDatetime().toLocalDate();
		System.out.println("Date = " + date);
		final LocalDate start = DateUtil.startOfWeek(date);

		LocalDate end = DateUtil.endOfWeek(date);
		return aggregateStockQuote(sq, sqSet, start, end);
	}

	public StockQuote getFinalMonthly(StockQuote sq, Set<StockQuote> sqSet) {

		LocalDate date = sq.getQuoteDatetime().toLocalDate();
		System.out.println("Date = " + date);
		final LocalDate start = DateUtil.firstDayOfMonth(date);

		LocalDate end = DateUtil.lastDayOfMonth(date);

		return aggregateStockQuote(sq, sqSet, start, end);
	}

	private StockQuote aggregateStockQuote(StockQuote sq, Set<StockQuote> sqSet, LocalDate startDate,
			LocalDate endDate) {
		StockQuote aggregateSq = new StockQuote(sq);

		System.out.println("End of the Week = " + endDate);

		sqSet.stream().filter(m -> startDate.isBefore(m.getQuoteDatetime().toLocalDate())
				&& endDate.isAfter(m.getQuoteDatetime().toLocalDate())).forEach(tsq -> {
					// open
					System.out.println(tsq.getQuoteDatetime() + "-Open:" + tsq.getOpen() + "-Close:" + tsq.getClose()
							+ "-High:" + tsq.getHigh() + "-Low:" + tsq.getLow());
					if (aggregateSq.getOpen() == null) {
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
		System.out.println(aggregateSq.getQuoteDatetime() + "-Open:" + aggregateSq.getOpen() + "-Close:"
				+ aggregateSq.getClose() + "-High:" + aggregateSq.getHigh() + "-Low:" + aggregateSq.getLow());
		return aggregateSq;
	}

	/**
	 * Creates Weekly StockQuote 
	 * 
	 * @param daily StockQuote
	 * @return Weekly StockQuote
	 */
	public StockQuote getWeekly(StockQuote dailySQ, Set<StockQuote> sqSet) {
		// TODO:
		// 1. Check to see if the Weekly exists in DB.
		// 2. If it doesn't exist, then find the SQs from Start of the Week and current
		// DailySQ's QuoteDatetime stocks
		// 3. If current DailySQ is for day of the week, then Calculate the Weekly SQ
		// and save it the DB.
		return null;
	}

	/**
	 * Creates Monthly StockQuote on the Fly
	 * 
	 * @param daily StockQuote
	 * @return Monthly StockQuote
	 */
	public StockQuote getMonthly(StockQuote dailySQ, Set<StockQuote> sqSet) {
		// TODO:
		// 1. Check to see if the Monthly exists in DB.
		// 2. If it doesn't exist, then find the SQs from Start of the Month and current
		// DailySQ's QuoteDatetime stocks
		// 3. If current DailySQ is for the last day of the month , then Calculate the
		// Monthly SQ and save it the DB.
		return null;
	}

}
