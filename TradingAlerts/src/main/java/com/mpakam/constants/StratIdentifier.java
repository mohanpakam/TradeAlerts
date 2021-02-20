package com.mpakam.constants;

public enum StratIdentifier {
	ONE(1),
	TWO(2),
	THREE(3);
	
	private final int stratId;
	
	StratIdentifier(int stratNum){
		this.stratId = stratNum;
	}
	
	public int getStratId() {
		return this.stratId;
	}
	
	public StratIdentifier getStrat(String identifier) {
		return StratIdentifier.valueOf(identifier);
	}
}
