package com.mpakam.exception;

import com.mpakam.model.BacktestStockOrder;

public class BackTestOrderCreatedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 923636384687403933L;

	public BackTestOrderCreatedException(BacktestStockOrder bso){
		System.out.println("Back test Order Created - " + bso);
	}

}
