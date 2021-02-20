package com.mpakam.constants;

public enum Interval {
	MIN1(1),
	MIN10(2),
	MIN15(3),	
	MIN30(4),
	HOURLY(5),
	DAILY(6),
	WEEKLY(7),
	MONTHLY(8),
	QUARTERLY(9),
	YEARLY(10);
	
	private final int tag;
    
    Interval(int tag) {
        this.tag = tag;
    }
    
    public int getInterval() {
        return this.tag;
    }
}
