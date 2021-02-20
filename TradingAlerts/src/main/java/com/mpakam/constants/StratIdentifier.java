package com.mpakam.constants;

public enum StratIdentifier {
	_1U("1U",1),
	_1D("1D",1),
	_2U("2U",2),
	_2D("2D",2),
	_3U("3U",3),
	_3D("3D",3);
	
	private final int stratId;
	private final String stratIdWithDirection;
	
	StratIdentifier(String stratIdWithDirection, int stratNum){
		this.stratId = stratNum;
		this.stratIdWithDirection = stratIdWithDirection;
	}
	
	public int getStratId() {
		return this.stratId;
	}
	
	public String getStratIdWithDirection() {
		return this.stratIdWithDirection;
	}
	
	public StratIdentifier getStrat(String identifier) {
		return StratIdentifier.valueOf(identifier);
	}
}
