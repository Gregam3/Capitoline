package com.greg.service.stock;

import com.greg.dao.stock.StockDao;
import com.greg.entity.holding.stock.Stock;
import com.greg.entity.user.Transaction;
import com.greg.entity.user.UserHolding;
import com.greg.exceptions.InvalidHoldingException;
import com.greg.service.AbstractService;
import com.greg.service.user.UserService;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class StockService extends AbstractService<Stock> {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final String API_KEY = "&apikey=QVJRID55FX6HALQH";
    private static final String BATCH_QUOTE_URL = "https://www.alphavantage.co/query?function=BATCH_STOCK_QUOTES&symbols=";
    private static final String TIME_SERIES_DAILY_URL_1 = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=";
    private static final String TIME_SERIES_DAILY_URL_2 = "&outputsize=full&apikey=" + API_KEY;

    private UserService userService;

    @Autowired
    public StockService(StockDao stockDao, @Lazy UserService userService) {
        setDao(stockDao);
        this.userService = userService;
    }

//    public String clearStocksWithoutData() throws UnirestException {
//        List<Stock> list = list();
//
//        StringBuilder stringBuilder = new StringBuilder();
//
//        for (Stock stock : list) {
//            System.out.println(stock.getAcronym());
//            if (getCurrentStockPrice(stock.getAcronym()) == 0) {
//                stringBuilder.append(stock.getAcronym());
//            }
//        }
//
//        return stringBuilder.toString();
//    }

    public double getCurrentStockPrice(String acronym) throws UnirestException {
        //Get Response
        JSONObject object = Unirest.get(BATCH_QUOTE_URL + acronym + API_KEY).asJson().getBody().getObject();

        //Casting necessary in order to access JSON elements
        return ((JSONObject) (((JSONArray) object.get("Stock Quotes")).get(0))).getDouble("2. price");
    }

    public double getStockPriceAtDate(String acronym, long unixDate) throws UnirestException, ParseException, InvalidHoldingException {
        JSONObject historyJson = Unirest.get(TIME_SERIES_DAILY_URL_1 + acronym + TIME_SERIES_DAILY_URL_2)
                .asJson().getBody().getObject().getJSONObject("Time Series (Daily)");

        JSONArray stringDates = historyJson.names();

        Long earliestDate = new Date().getTime();

        for (int i = 0; i < stringDates.length(); i++) {
            long currentIndexUnixTime = dateFormatter.parse(stringDates.get(i).toString()).getTime();

            if (earliestDate > currentIndexUnixTime)
                earliestDate = currentIndexUnixTime;

            if (currentIndexUnixTime == unixDate)
                return historyJson.getJSONObject(stringDates.get(i).toString()).getDouble("4. close");

        }
        throw new InvalidHoldingException(
                "The earliest data that could be for found " + acronym + " was " + new Date(earliestDate)
        );
    }


    public Map<Date, Double> getStockHistory(UserHolding userHolding, double userCurrencyModifier) throws UnirestException, ParseException, InvalidHoldingException {
        JSONObject historyJson = Unirest.get(TIME_SERIES_DAILY_URL_1 + userHolding.getAcronym() + TIME_SERIES_DAILY_URL_2)
                .asJson().getBody().getObject().getJSONObject("Time Series (Daily)");

        Queue<Transaction> transactionQueue = new PriorityQueue<>();
        transactionQueue.addAll(userHolding.getTransactions());

        //Ensures first transaction is not a watched transaction
        transactionQueue = getNextTrackedTransaction(transactionQueue);

        //If there are no non-watched transactions skip over this item
        if(transactionQueue.size() < 1)
            return null;

        //Initialising local variables after we know that processing needs to take place
        Map<Date, Double> stockHistory = new HashMap<>();

        //Initialised with current time so when comparison is done later every date will be earlier, comparison referenced with *1.
        long earliestDateInRange = new Date().getTime();

        Transaction currentTransaction = null;

        double cumulativeQuantity = 0;
        long nextDateUnix = 0;

        JSONArray stringDates = historyJson.names();

        for (int i = 0; i < stringDates.length(); i++) {
            JSONObject day = historyJson.getJSONObject(stringDates.get(i).toString());
            double price = day.getDouble("4. close");

            Date currentItemUnixDate = dateFormatter.parse(stringDates.get(i).toString());

            if (currentItemUnixDate.getTime() > nextDateUnix) {
                currentTransaction = transactionQueue.poll();
                cumulativeQuantity += currentTransaction.getQuantity();
                nextDateUnix = (transactionQueue.size() > 0) ?
                        transactionQueue.peek().getDate().getTime() : new Date().getTime();
            }

            if (currentItemUnixDate.getTime() > currentTransaction.getDate().getTime()) {
                // *1 Necessary for populating weekend data
                if (currentItemUnixDate.getTime() < earliestDateInRange)
                    earliestDateInRange = currentItemUnixDate.getTime();

                stockHistory.put(
                        DateUtils.round(currentItemUnixDate, Calendar.DAY_OF_MONTH),
                        (price * cumulativeQuantity) * userCurrencyModifier
                );
            }
        }

        return addMissingDates(stockHistory, earliestDateInRange);
    }

    private Queue<Transaction> getNextTrackedTransaction(Queue<Transaction> queue) {
        while (queue.peek() != null && queue.peek().getQuantity() == 0)
            queue.poll();

        return queue;
    }

    //Stock markets close on weekends but in order for data to line up with other holdings data must be inserted on a day to day basis
    private Map<Date, Double> addWeekendsToStockHistory(Map<Date, Double> stockHistory, long earliestDateInRange) throws InvalidHoldingException {
        long currentUnixTime = new Date().getTime();

        //Works out how far friday is away from current day
        int baseMultiplier = 5 - new Date(earliestDateInRange).getDay();

        for (long unixIterator = earliestDateInRange;
             unixIterator < currentUnixTime;
            //Iterate by week rather than by day to speed up processing
             unixIterator += DateUtils.MILLIS_PER_DAY * 7) {
            double valueBeforeWeekend = getClosestPopulatedValue(stockHistory, unixIterator + DateUtils.MILLIS_PER_DAY * baseMultiplier, 1);

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

    private Map<Date, Double> addMissingDates(Map<Date, Double> stockHistory, long earliestDateInRange) throws InvalidHoldingException {
        long currentUnixTime = new Date().getTime();
        double lastValue = 0;

        int lastValueDaysAway = 0;

        for (long unixIterator = earliestDateInRange;
             unixIterator < currentUnixTime;
             unixIterator += DateUtils.MILLIS_PER_DAY) {
            Date date = DateUtils.round(new Date(unixIterator), Calendar.DAY_OF_MONTH);
            Double currentValue = stockHistory.get(date);

            if(lastValueDaysAway > 50)
                throw new InvalidHoldingException(
                        "{\"data\": \"The provider is missing a portion of their data for your stock(s), please try again after the date: "
                    + new Date(unixIterator) +"\" }");

            if (currentValue != null) {
                lastValue = currentValue;
                lastValueDaysAway = 0;
            } else
                stockHistory.put(date, lastValue);

            lastValueDaysAway++;
        }

        return stockHistory;
    }

    //If the date on the friday cannot be retrieved then it will recurse backwards until it finds the next closest value.
    // Max recursion 20.
    private double getClosestPopulatedValue(Map<Date, Double> stockHistory, long unixIterator, int recurseCount) throws InvalidHoldingException {
        recurseCount++;
        Double value = stockHistory.get(DateUtils.round(new Date(unixIterator), Calendar.DAY_OF_MONTH));

//        if(recurseCount > 50)
//            throw new InvalidHoldingException("The provider is missing a portion of their data, please try again after the date "
//                    + new Date(unixIterator - DateUtils.MILLIS_PER_DAY * 50));

        if (value == null)
            return getClosestPopulatedValue(stockHistory, unixIterator - DateUtils.MILLIS_PER_DAY, recurseCount);

        return value;
    }

    public double getAverageMonthlyChange() throws ParseException, InvalidHoldingException, UnirestException, IOException {
        double valueOneMonthAgo = 0;
        double valueToday = 0;

        for (UserHolding userHolding : userService.getCurrentUser().getStocks()) {
            valueOneMonthAgo += getStockPriceAtDate(userHolding.getAcronym(),
                    DateUtils.truncate(new Date(new Date().getTime() - DateUtils.MILLIS_PER_DAY * 30), Calendar.DAY_OF_MONTH).getTime()) *
                    userHolding.getTotalQuantity();
            valueToday += getCurrentStockPrice(userHolding.getAcronym()) *
                    userHolding.getTotalQuantity();
        }

        return (valueToday / valueOneMonthAgo) * 100 - 100;
    }
}
