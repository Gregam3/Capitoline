package com.greg.service.stock;

import com.greg.dao.stock.StockDao;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.stock.Stock;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class StockService {

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
    private static final String API_KEY = "QVJRID55FX6HALQH";
    private static final String BATCH_QUOTE_URL = "https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=";
    private static final String TIME_SERIES_DAILY_URL_1 = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=";
    private static final String TIME_SERIES_DAILY_URL_2 = "&outputsize=1825&apikey=" + API_KEY;

    private final StockDao stockDao;

    @Autowired
    public StockService(StockDao stockDao) {
        this.stockDao = stockDao;
    }

    public List<Stock> list() {
        List<Stock> stockList = stockDao.list();
        stockList.forEach(item -> item.setHoldingType(HoldingType.STOCK));

        return stockList;
    }

    public double getStockPrice(String acronym) throws UnirestException {
        //Get Response
        JSONObject object = Unirest.get(BATCH_QUOTE_URL + acronym + "&apikey=" + API_KEY).asJson().getBody().getObject();

        //Casting necessary in order to access JSON elements
        return ((JSONObject) (((JSONArray) object.get("Stock Quotes")).get(0))).getDouble("2. price");
    }

    public Map<Date, Double> getStockHistory(String acronym, double totalQuantity) throws UnirestException, ParseException {
        Map<Date, Double> stockHistory = new LinkedHashMap<>();
        JSONObject historyJson = Unirest.get(TIME_SERIES_DAILY_URL_1 + acronym + TIME_SERIES_DAILY_URL_2)
                .asJson().getBody().getObject().getJSONObject("Time Series (Daily)");

        JSONArray names = historyJson.names();

        for (int i = 0; i < names.length(); i++) {
            JSONObject day = historyJson.getJSONObject(names.get(i).toString());
            double price = day.getDouble("4. close");

            if (price > 0)
                stockHistory.put(
                        formatter.parse(names.get(i).toString()),
                        price
                );
        }

        return stockHistory;
    }
}
