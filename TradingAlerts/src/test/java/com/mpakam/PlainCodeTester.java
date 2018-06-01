package com.mpakam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mpakam.util.IEXTradingService;

public class PlainCodeTester {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
//		testTimeSeriesGeneration();
		listDirectories();
	}
	
	public static void printTime() {
		String time= "2018-02-13 16:00";

		LocalDateTime nextQuoteEndTime =null;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(time, formatter);

//		testTimerSeries(dateTime,15);
		System.out.println(525/100 + 1);
	}
	
	public static void testTimerSeries (LocalDateTime currentQuoteTime, int interval) {
		List<HashMap<String, LocalDateTime>> quoteTimes = new ArrayList<HashMap<String, LocalDateTime>>();
		String hourStartTimes = "09:30";

		LocalDateTime nextQuoteEndTime =null;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
 
		LocalDateTime startTime=currentQuoteTime.plusMinutes(interval);
		while(startTime.isBefore(LocalDateTime.now())) {
			
			HashMap<String, LocalDateTime> startEndTime= new HashMap<>();
			
			if(startTime.getHour() >15) {
				startTime = LocalDateTime
						.parse(startTime.plusDays(1).toLocalDate().toString() + " " +hourStartTimes, formatter);
			}else {
				
//				startTime = startTime.plusMinutes(interval);
				if(startTime.getDayOfWeek() == DayOfWeek.SATURDAY || startTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
					startTime =startTime.plusMinutes(interval);;
					continue;
				}
				
				startEndTime.put("SD", startTime);
				startEndTime.put("ED", startTime.plusMinutes(interval-1) );
				System.out.println("Start Time " + startTime +" End Time " + startTime.plusMinutes(interval-1)) ;
				startTime=startTime.plusMinutes(interval);
				quoteTimes.add(startEndTime);
			}
		}
		System.out.println("size is " + quoteTimes.size());
	}	
	
	 
	    public static void testTimeSeriesGeneration() {
	    	String timeStr  = "20180212 16:00";
	    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
			LocalDateTime dateTime = LocalDateTime
					.parse(timeStr, formatter);
			
	    	int interval = 60;
	    	List<HashMap<String, LocalDateTime>> quoteTimes = new ArrayList<HashMap<String, LocalDateTime>>();
			LocalDateTime nextQuoteEndTime = dateTime.plusMinutes(2*interval - 1 ); // for 14, 29, 44, 59
			
			while(nextQuoteEndTime.isBefore(LocalDateTime.now())  ) {
				LocalDateTime startTime=null;
				LocalDateTime endTime=null;
				HashMap<String, LocalDateTime> startEndTime= new HashMap<>();
				if(nextQuoteEndTime.minusMinutes(interval - 1).getHour() == 16) { //start time
					if(nextQuoteEndTime.plusMinutes(1).getMinute() == 0)
						nextQuoteEndTime=nextQuoteEndTime.plusMinutes(930); // 16:30 hours from 4 pm to 9:30 AM
					else
						nextQuoteEndTime=nextQuoteEndTime.plusMinutes(990); // 16:30 hours from 4 pm to 9:30 AM
					if(interval == 60 && nextQuoteEndTime.getMinute() == 0)
						nextQuoteEndTime=nextQuoteEndTime.plusMinutes(30); // 16:30 hours from 4 pm to 9:30 AM
					continue;
				}else if(nextQuoteEndTime.minusMinutes(interval - 1).getHour() >16) {
					nextQuoteEndTime = nextQuoteEndTime.plusMinutes(interval);
					continue;
				}
				startTime =nextQuoteEndTime.minusMinutes(interval - 1);
				endTime = nextQuoteEndTime;
				
				if(nextQuoteEndTime.getHour() < 9 || (nextQuoteEndTime.getHour() == 9 & nextQuoteEndTime.getMinute() <30)) {
					nextQuoteEndTime = nextQuoteEndTime.plusMinutes(interval);
					continue;
				}
				if(startTime.getDayOfWeek() == DayOfWeek.SATURDAY || startTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
					nextQuoteEndTime=nextQuoteEndTime.plusMinutes(interval); // 16:30 hours from 4 pm to 9:30 AM
					continue;
				}
				startEndTime.put("ST", startTime);
				startEndTime.put("ET", endTime);
				quoteTimes.add(startEndTime);
//				System.out.println("Start Time is "+ nextQuoteEndTime.minusMinutes(interval - 1) +" END Time is " + nextQuoteEndTime);
				
				nextQuoteEndTime = nextQuoteEndTime.plusMinutes(interval);
				
			}
			
			quoteTimes.forEach(map->{
					System.out.println("Start Time is "+ map.get("ST") +" END Time is " + map.get("ET"));
			});
			
	    }
	    
	    public static void listDirectories() throws IOException {
	    	Files.walk(Paths.get("C:\\Users\\LuckyMonaA\\Workspace\\git\\TradingAlerts\\history\\data\\5 min\\us")).
	    	filter(Files::isRegularFile).
	    	forEach(p->{
	    		System.out.println(p.getFileName().toString().split("\\.")[0].toUpperCase());
	    	});;
	    }
	    

}
