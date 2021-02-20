package com.mpakam.constants;

public enum TheStrat {
	//a. 122 RevStrat2
	// b. 123 RevStrat3
	// c. 1-2
	// d. 1-3
	// e. 2-2
	// f. 2-3
	// g. 3-2
	_122(122), //RevStrat2
	_123(123),
	_12(12),
	_13(13),
	_22(22),
	_23(23),
	_32(32);
	
	private final int strat;
	
	TheStrat(int stratNum){
		this.strat = stratNum;
	}
	
	public int getStratNum() {
        return this.strat;
    }
	
}
