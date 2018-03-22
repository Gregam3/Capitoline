package com.greg.service.stock;

import com.greg.dao.stock.StockDao;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.stock.Stock;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.time.DateUtils;
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

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final String API_KEY = "QVJRID55FX6HALQH";
    private static final String BATCH_QUOTE_URL = "https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=";
    private static final String TIME_SERIES_DAILY_URL_1 = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=";
    private static final String TIME_SERIES_DAILY_URL_2 = "&outputsize=full&apikey=" + API_KEY;

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


    public Map<Date, Double> getStockHistory(String acronym, double quantity) throws UnirestException, ParseException {
        Map<Date, Double> stockHistory = new LinkedHashMap<>();
        JSONObject historyJson = Unirest.get(TIME_SERIES_DAILY_URL_1 + acronym + TIME_SERIES_DAILY_URL_2)
                .asJson().getBody().getObject().getJSONObject("Time Series (Daily)");

        JSONArray names = sortStringDates(historyJson.names());

        long earliestDateInRange = new Date().getTime();

        for (int i = 0; i < names.length(); i++) {
            JSONObject day = historyJson.getJSONObject(names.get(i).toString());
            double price = day.getDouble("4. close");

            if (price > 0) {
                Date parsedDate = formatter.parse(names.get(i).toString());

                //Necessary for populating weekend data
                if (parsedDate.getTime() < earliestDateInRange)
                    earliestDateInRange = parsedDate.getTime();

                stockHistory.put(
                        DateUtils.round(parsedDate, Calendar.DAY_OF_MONTH),
                        price * quantity
                );
            }
        }

        return addMissingDates(stockHistory, earliestDateInRange);
//        return stockHistory;
    }

    private JSONArray sortStringDates(JSONArray names) throws ParseException {
        JSONArray recentStringDates = new JSONArray();

        for (int i = 0; i < names.length(); i++)
            //5 as only 5 years of data is desired
            if (formatter.parse(names.getString(i)).getTime() > new Date().getTime() - DateUtils.MILLIS_PER_DAY * (365 * 5))
                recentStringDates.put(names.getString(i));

        return recentStringDates;
    }

    //Stock markets close on weekends but in order for data to line up with other holdings data must be inserted on a day to day basis
    private Map<Date, Double> addWeekends(Map<Date, Double> stockHistory, long earliestDateInRange) {
        long currentUnixTime = new Date().getTime();

        //Works out how far friday is away from current day
        int baseMultiplier = 5 - new Date(earliestDateInRange).getDay();

        for (long unixIterator = earliestDateInRange;
             unixIterator < currentUnixTime;
            //Iterate by week rather than by day to speed up processing
             unixIterator += DateUtils.MILLIS_PER_DAY * 7) {
            double valueBeforeWeekend = getClosestValue(stockHistory, unixIterator + DateUtils.MILLIS_PER_DAY * baseMultiplier);

            //Add saturday value
            stockHistory.put(
                    DateUtils.round(new Date(unixIterator + DateUtils.MILLIS_PER_DAY * (baseMultiplier + 1)), Calendar.DAY_OF_MONTH),
                    valueBeforeWeekend
            );
            //Add sunday value
            stockHistory.put(
                    DateUtils.round(new Date(unixIterator + DateUtils.MILLIS_PER_DAY * (baseMultiplier + 2)), Calendar.DAY_OF_MONTH),
                    valueBeforeWeekend
            );
        }

        return stockHistory;
    }

    private Map<Date, Double> addMissingDates(Map<Date, Double> stockHistory, long earliestDateInRange) {
        long currentUnixTime = new Date().getTime();
        double lastValue = 0;

        for (long unixIterator = earliestDateInRange;
             unixIterator < currentUnixTime;
             unixIterator += DateUtils.MILLIS_PER_DAY) {
            Date date = DateUtils.round(new Date(unixIterator), Calendar.DAY_OF_MONTH);
            Double currentValue = stockHistory.get(date);

            if (currentValue != null)
                lastValue = currentValue;
            else
                stockHistory.put(date, lastValue);

        }

        return stockHistory;

    }

    //If the date on the friday cannot be retrieved then it will recurse backwards until it finds the next closest value
    private double getClosestValue(Map<Date, Double> stockHistory, long unixIterator) {
        Double value = stockHistory.get(DateUtils.round(new Date(unixIterator), Calendar.DAY_OF_MONTH));

        if (value == null)
            return getClosestValue(stockHistory, unixIterator - DateUtils.MILLIS_PER_DAY);

        return value;
    }
}
