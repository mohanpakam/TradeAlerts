package com.mpakam.constants;

public enum BackTestOrder {
	NONE(0),
	BUY(1),
	SELL(2),
	CLOSE(3),
	ADD(4);
	
	private final int signal;
	
	BackTestOrder(int signal){
		this.signal = signal;
	}
	
	public int getSignal() {
		return this.signal;
	}
	
}
