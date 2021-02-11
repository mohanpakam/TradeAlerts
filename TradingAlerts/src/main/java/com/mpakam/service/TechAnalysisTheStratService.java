package com.mpakam.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.SortedSet;

import org.springframework.stereotype.Service;

import com.mpakam.model.StockQuote;

@Service
public class TechAnalysisTheStratService {

	
	/** Makes up the Weekly the StockQuote from the given StockQuote Set
	 * @param sq
	 * @return
	 */
	public StockQuote getWeekly(StockQuote sq, Set<StockQuote> sqSet) {
		
		LocalDate date = sq.getQuoteDatetime().toLocalDate();
	      System.out.println("Date = " + date);
	      final LocalDate start = startOfWeek(date);
	      
	      LocalDate end = endOfWeek(date);
		 return aggregateStockQuote(sq, sqSet,start,end);
	}
	
	public StockQuote getMonthly(StockQuote sq, Set<StockQuote> sqSet) {
		
		LocalDate date = sq.getQuoteDatetime().toLocalDate();
	      System.out.println("Date = " + date);
	      final LocalDate start = firstDayOfMonth(date);
	      
	      LocalDate end = lastDayOfMonth(date);
	      
	      return aggregateStockQuote(sq, sqSet,start,end);
	}
	
	private StockQuote aggregateStockQuote(StockQuote sq, Set<StockQuote> sqSet, LocalDate startDate, LocalDate endDate) {
		StockQuote aggregateSq = new StockQuote(sq);
	      
	      System.out.println("End of the Week = " + endDate);
	      
		 sqSet.stream().filter(m->startDate.isBefore(m.getQuoteDatetime().toLocalDate()) 
						&& endDate.isAfter(m.getQuoteDatetime().toLocalDate())).forEach(tsq->{
							//open
							System.out.println(tsq.getQuoteDatetime() + "-Open:" +tsq.getOpen() 
							+ "-Close:"+ tsq.getClose()
							+ "-High:"+ tsq.getHigh()
							+ "-Low:"+ tsq.getLow()
							);
							if(aggregateSq.getOpen() == null) {
								aggregateSq.setOpen(tsq.getOpen());
								aggregateSq.setQuoteDatetime(tsq.getQuoteDatetime());
							}
							//high
							if(aggregateSq.getHigh().compareTo(tsq.getHigh()) == -1) {
								aggregateSq.setHigh(tsq.getHigh()); //High of the Week
							}
							//low
							if(aggregateSq.getLow().compareTo(tsq.getLow())==1) {
								aggregateSq.setLow(tsq.getLow()); //High of the Week
							}
							//close
							aggregateSq.setClose(tsq.getClose());
						});
		 System.out.println(aggregateSq.getQuoteDatetime() + "-Open:" +aggregateSq.getOpen() 
			+ "-Close:"+ aggregateSq.getClose()
			+ "-High:"+ aggregateSq.getHigh()
			+ "-Low:"+ aggregateSq.getLow()
			);
		 return aggregateSq;
	}
	
	private LocalDate startOfWeek(LocalDate start) {
		while (start.getDayOfWeek() != DayOfWeek.SUNDAY) {
	         start = start.minusDays(1);
	    }
		return start;
	}
	private LocalDate endOfWeek(LocalDate end) {
		while (end.getDayOfWeek() != DayOfWeek.SATURDAY) {
	         end = end.plusDays(1);
	      }
		return end;
	}
	
	private LocalDate firstDayOfMonth(LocalDate start) {
		while (start.getDayOfMonth() != 1) {
	         start = start.minusDays(1);
	    }
		return start.minusDays(1); // Last Day of Last Month
	}
	
	private LocalDate lastDayOfMonth(LocalDate end) {
		while (end.getDayOfMonth() != end.lengthOfMonth()) {
	         end = end.plusDays(1);
	      }
		return end;
	}
	
}
