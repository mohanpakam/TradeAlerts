package com.mpakam.constants;

public enum StratDirection {
	NONE(0),
	UP(1),
	DOWN(-1);
	
	private final int directionId;
	
	StratDirection(int directionId){
		this.directionId =directionId;
	}
	
	public int getDirectionId() {
		return directionId;
	}
	
}
