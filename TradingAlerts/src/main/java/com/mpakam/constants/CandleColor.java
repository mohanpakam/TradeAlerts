package com.mpakam.constants;

public enum CandleColor {

	RED(-1),
	GREEN(1),
	DOJI(0);
	
	private final int colorId;
	
	CandleColor(int colorId){
		this.colorId = colorId;
	}
	
	public int getColorId() {
		return this.colorId;
	}
}
