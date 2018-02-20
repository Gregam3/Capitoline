package com.greg.entity.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.entity.holding.crypto.Crypto;
import com.greg.entity.holding.fiat.Fiat;
import com.greg.entity.holding.stock.Stock;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class UserHoldings {
    private static final Logger LOG = Logger.getLogger(UserHoldings.class);
    private Map<Crypto, Double> cryptoList;
    private Map<Stock, Integer> stockList;
    private Map<Fiat, Double> fiatList;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final StringBuilder STRING_BUILDER = new StringBuilder();

    public UserHoldings() {
    }

    public UserHoldings(String holdingsJson) {
        try {
            OBJECT_MAPPER.readValue(holdingsJson, UserHoldings.class);
        } catch (IOException e) {
            LOG.error(e);
            e.printStackTrace();
        }

        this.cryptoList = cryptoList;
        this.stockList = stockList;
        this.fiatList = fiatList;
    }

    public String getCryptoList() throws JsonProcessingException {
        return UserHoldings.OBJECT_MAPPER.writeValueAsString(cryptoList);
    }

    public void setCryptoList(Map<Crypto, Double> cryptoList) {
        this.cryptoList = cryptoList;
    }

    public Map<Stock, Integer> getStockList() {
        return stockList;
    }

    public void setStockList(Map<Stock, Integer> stockList) {
        this.stockList = stockList;
    }

    public Map<Fiat, Double> getFiatList() {
        return fiatList;
    }

    public void setFiatList(Map<Fiat, Double> fiatList) {
        this.fiatList = fiatList;
    }

    @Override
    public String toString() {
        STRING_BUILDER.delete(0, STRING_BUILDER.length());

        try {
            STRING_BUILDER.append(OBJECT_MAPPER.writeValueAsString(cryptoList));
            STRING_BUILDER.append("|");
            STRING_BUILDER.append(OBJECT_MAPPER.writeValueAsString(stockList));
            STRING_BUILDER.append("|");
            STRING_BUILDER.append(OBJECT_MAPPER.writeValueAsString(fiatList));
        } catch (JsonProcessingException e) {
            LOG.error(e);
            e.printStackTrace();
        }

        return STRING_BUILDER.toString();
    }
}
