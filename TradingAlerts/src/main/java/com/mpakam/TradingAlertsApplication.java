package com.mpakam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
public class TradingAlertsApplication {

    public static void main(String[] args) {
//    	 System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(TradingAlertsApplication.class, args);
    }
}

