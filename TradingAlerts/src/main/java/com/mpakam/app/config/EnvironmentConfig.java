package com.mpakam.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {
	
	@Value("${tick.interval}")
	private int TICK_INTERVAL;
	
	@Value("${session.start}")
	private String SESSION_START;
	
	@Value("${stooq.filepath}")
	private String STOOQ_FILEPATH;
	
	@Value("${stooq.intraday.filepath}")
	private String STOOQ_INTRADAY_FILEPATH;
	
	@Value("${ema.length}")
	private int emaLength;

	public int getTICK_INTERVAL() {
		return TICK_INTERVAL;
	}

	public void setTICK_INTERVAL(int tICK_INTERVAL) {
		TICK_INTERVAL = tICK_INTERVAL;
	}

	public String getSESSION_START() {
		return SESSION_START;
	}

	public void setSESSION_START(String sESSION_START) {
		SESSION_START = sESSION_START;
	}

	public String getSTOOQ_FILEPATH() {
		return STOOQ_FILEPATH;
	}

	public void setSTOOQ_FILEPATH(String sTOOQ_FILEPATH) {
		STOOQ_FILEPATH = sTOOQ_FILEPATH;
	}

	public String getSTOOQ_INTRADAY_FILEPATH() {
		return STOOQ_INTRADAY_FILEPATH;
	}

	public void setSTOOQ_INTRADAY_FILEPATH(String sTOOQ_INTRADAY_FILEPATH) {
		STOOQ_INTRADAY_FILEPATH = sTOOQ_INTRADAY_FILEPATH;
	}

	public int getEmaLength() {
		return emaLength;
	}

	public void setEmaLength(int emaLength) {
		this.emaLength = emaLength;
	}

}
