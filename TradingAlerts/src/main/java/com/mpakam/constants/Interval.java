package com.mpakam.constants;

public enum Interval {
	
	DAILY("1D"),
	WEEKLY("1W"),
	MONTHLY("1M"),
	QUARTERLY("1Q"),
	YEARLY("1Y"),
	HOURLY("1h"),
	MIN15("15min"),
	MIN1("1min"),
	MIN10("10min"),
	MIN30("30min");
	
	private final String tag;
    
    Interval(String tag) {
        this.tag = tag;
    }
    
    public String getTag() {
        return this.tag;
    }
}
