package com.mpakam.util;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class BigDecimalUtil {
	
	public static final BigDecimal ZERO  = new BigDecimal("0");
	public static final BigDecimal MINUSONE  = new BigDecimal("-1");
	
	public boolean isValid(BigDecimal decimal) {
		return (decimal !=null && decimal.compareTo(MINUSONE) !=0  && decimal.compareTo(ZERO) !=0 );
	}
}
