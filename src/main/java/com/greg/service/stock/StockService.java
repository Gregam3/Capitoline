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


//        return addWeekends(stockHistory, earliestDateInRange);
        return addWeekends(stockHistory, earliestDateInRange);
    }

    private JSONArray sortStringDates(JSONArray names) throws ParseException {
        JSONArray recentStringDates = new JSONArray();

        for (int i = 0; i < names.length(); i++) {
            String[] dateSplit = names.getString(i).split("-");
            Date currentDate = new Date();

            //+1900 as the getYear() method minuses it, necessary for comparison
            //5 as only 5 years of data is desired
            if (Integer.valueOf(dateSplit[0]) > (currentDate.getYear() + 1900) - 5) {
                recentStringDates.put(names.getString(i));
            }
        }
        return recentStringDates;
    }

    private Map<Date, Double> addWeekends(Map<Date, Double> stockHistory, long earliestDateInRange) {
        long currentUnixTime = new Date().getTime();

        for(long unixIterator = earliestDateInRange;
            unixIterator < currentUnixTime;
            //Iterate by week rather than by day to speed up processing
            unixIterator += DateUtils.MILLIS_PER_DAY * 7) {
            double valueBeforeWeekend = getClosestValue(stockHistory, unixIterator + DateUtils.MILLIS_PER_DAY * 4);

            //Add saturday value
            stockHistory.put(
                    DateUtils.round(new Date(unixIterator + DateUtils.MILLIS_PER_DAY * 5), Calendar.DAY_OF_MONTH),
                    valueBeforeWeekend
            );
            //Add sunday value
            stockHistory.put(
                    DateUtils.round(new Date(unixIterator + DateUtils.MILLIS_PER_DAY * 6), Calendar.DAY_OF_MONTH),
                    valueBeforeWeekend
            );
        }


        return stockHistory;
    }

    //If the date on the friday cannot be retrieved then it will recurse backwards until it finds the next closest value
    private double getClosestValue(Map<Date, Double> stockHistory, long unixIterator) {
        Double value = stockHistory.get(new Date(unixIterator));

        if (value == null)
            return getClosestValue(stockHistory, unixIterator - DateUtils.MILLIS_PER_DAY);

        return value;
    }
}
