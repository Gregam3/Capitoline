//package com.greg.entity.user;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.greg.entity.holding.UserHolding;
//import com.greg.entity.holding.HoldingType;
//import com.greg.entity.holding.crypto.Crypto;
//import com.greg.entity.holding.fiat.Fiat;
//import com.greg.entity.holding.stock.Stock;
//import com.greg.utils.JSONUtils;
//import org.apache.log4j.Logger;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author Greg Mitten (i7676925)
// * gregoryamitten@gmail.com
// */
//public class UserHoldings {
//    private static final Logger LOG = Logger.getLogger(UserHoldings.class);
//    private Map<Crypto, Double> cryptoList;
//    private static final StringBuilder STRING_BUILDER = new StringBuilder();
//
//    public UserHoldings() {
//        this.cryptoList = new HashMap<>();
//        this.stockList = new HashMap<>();
//        this.fiatList = new HashMap<>();
//    }
//
//    public UserHoldings(Map<Crypto, Double> cryptoList, Map<Stock, Long> stockList, Map<Fiat, Double> fiatList) {
//        this.cryptoList = cryptoList;
//        this.stockList = stockList;
//        this.fiatList = fiatList;
//    }
//
//    public String getCryptoList() throws JsonProcessingException {
//        return JSONUtils.OBJECT_MAPPER.writeValueAsString(cryptoList);
//    }
//
//
//    public void addHolding(UserHolding holding, double quantity) {
//        cryptoList.put(holding.asJson(), quantity);
//    }
//
//    public void setCryptoList(Map<Crypto, Double> cryptoList) {
//        this.cryptoList = cryptoList;
//    }
//
//    public Map<Stock, Long> getStockList() {
//        return stockList;
//    }
//
//    public void setStockList(Map<Stock, Long> stockList) {
//        this.stockList = stockList;
//    }
//
//    public Map<Fiat, Double> getFiatList() {
//        return fiatList;
//    }
//
//    public void setFiatList(Map<Fiat, Double> fiatList) {
//        this.fiatList = fiatList;
//    }
//
//    @Override
//    public String toString() {
//        STRING_BUILDER.delete(0, STRING_BUILDER.length());
//
//        try {
//            STRING_BUILDER.append(JSONUtils.OBJECT_MAPPER.writeValueAsString(cryptoList));
//            STRING_BUILDER.append("|");
//            STRING_BUILDER.append(JSONUtils.OBJECT_MAPPER.writeValueAsString(stockList));
//            STRING_BUILDER.append("|");
//            STRING_BUILDER.append(JSONUtils.OBJECT_MAPPER.writeValueAsString(fiatList));
//        } catch (JsonProcessingException e) {
//            LOG.error(e);
//            e.printStackTrace();
//        }
//
//        return STRING_BUILDER.toString();
//    }
//}
