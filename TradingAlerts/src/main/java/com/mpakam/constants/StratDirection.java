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
	
	public static StratDirection valueOfLabel(int directionId) {
	    for (StratDirection e : values()) {
	        if (e.directionId == directionId) {
	            return e;
	        }
	    }
	    return null;
	}
}
