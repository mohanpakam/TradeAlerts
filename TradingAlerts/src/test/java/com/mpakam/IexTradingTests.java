package com.mpakam;

import pl.zankowski.iextrading4j.api.stocks.Quote;
import pl.zankowski.iextrading4j.api.stocks.v1.BatchStocks;
import pl.zankowski.iextrading4j.client.IEXCloudClient;
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder;
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.v1.BatchStocksRequestBuilder;
import pl.zankowski.iextrading4j.client.rest.request.stocks.v1.BatchStocksType;

public class IexTradingTests {

	final IEXCloudClient cloudClient = IEXTradingClient.create(IEXTradingApiVersion.IEX_CLOUD_STABLE_SANDBOX,
            new IEXCloudTokenBuilder()
                    .withPublishableToken("Tpk_18dfe6cebb4f41ffb219b9680f9acaf2")
                    .withSecretToken("Tsk_3eedff6f5c284e1a8b9bc16c54dd1af3")
                    .build());

    public static void main(String[] args) {
        final IexTradingTests sampleSuite = new IexTradingTests();
        sampleSuite.batchRequestSample();
        sampleSuite.quoteRequestSample();
    }

    private void batchRequestSample() {
    	final BatchStocks result = cloudClient.executeRequest(new BatchStocksRequestBuilder()
    	        .withSymbol("AAPL")
    	        .addType(BatchStocksType.LARGEST_TRADES)
    	        .addType(BatchStocksType.PRICE_TARGET)
    	        .addType(BatchStocksType.QUOTE)
    	        .build());
    	System.out.println(result);
    }
    
    private void quoteRequestSample() {
    	final Quote quote = cloudClient.executeRequest(new QuoteRequestBuilder()
    	        .withSymbol("AAPL")
    	        .build());
    	System.out.println(quote);
    }

}
