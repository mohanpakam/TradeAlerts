package com.mpakam.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtil {
	
	/** Returns the LocalDate of the Start of the Week - Sunday is the Start of the Week
	 * @param start
	 * @return
	 */
	public static LocalDate startOfWeek(LocalDate start) {
		while (start.getDayOfWeek() != DayOfWeek.SUNDAY) {
	         start = start.minusDays(1);
	    }
		return start;
	}
	
	/** Returns the LocalDate of the End of the Week - Saturday is the Start of the Week
	 * @param end
	 * @return
	 */
	public static LocalDate endOfWeek(LocalDate end) {
		while (end.getDayOfWeek() != DayOfWeek.SATURDAY) {
	         end = end.plusDays(1);
	      }
		return end;
	}
	
	/** Returns the LocalDate of First Day of Month - Calendar day 1 of the Month
	 * @param start
	 * @return
	 */
	public static LocalDate firstDayOfMonth(LocalDate start) {
		while (start.getDayOfMonth() != 1) {
	         start = start.minusDays(1);
	    }
		return start.minusDays(1); // Last Day of Last Month
	}
	
	/** Returns the LocalDate of Last day of the Month- varies by Month. like 31st for January, 30th April etc.,
	 * @param end
	 * @return
	 */
	public static LocalDate lastDayOfMonth(LocalDate end) {
		while (end.getDayOfMonth() != end.lengthOfMonth()) {
	         end = end.plusDays(1);
	      }
		return end;
	}

}
