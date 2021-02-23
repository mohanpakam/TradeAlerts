package com.mpakam.constants;

public enum StratCandleIdentifier {
	ONE(1),
	TWO(2),
	THREE(3);
	
	private final int stratId;
	
	StratCandleIdentifier(int stratNum){
		this.stratId = stratNum;
	}
	
	public int getStratId() {
		return this.stratId;
	}
	
	public StratCandleIdentifier getStrat(String identifier) {
		return StratCandleIdentifier.valueOf(identifier);
	}
	
	public static StratCandleIdentifier valueOfLabel(int stratId) {
	    for (StratCandleIdentifier e : values()) {
	        if (e.stratId == stratId) {
	            return e;
	        }
	    }
	    return null;
	}
}
