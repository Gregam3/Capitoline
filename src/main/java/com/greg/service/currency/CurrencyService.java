package com.greg.service.currency;

import com.greg.entity.user.Transaction;
import com.greg.entity.user.UserHolding;
import com.greg.exceptions.InvalidHoldingException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class CurrencyService {
    private final static String BASE_URL = "https://min-api.cryptocompare.com/data/";
    private final static String HISTORY_FIRST_PART = BASE_URL + "histoday?fsym=";
    private final static String HISTORY_SECOND_PART = "&tsym=USD&limit=1825";
    private final static String PRICE_DAY = BASE_URL + "pricehistorical?fsym=";

    public Map<Date, Double> getCurrencyHistory(UserHolding userHolding) throws UnirestException, IOException {
        Map<Date, Double> graphHoldingDataMap = new HashMap<>();


        JSONArray history =
                Unirest.get(HISTORY_FIRST_PART + userHolding.getAcronym() + HISTORY_SECOND_PART)
                        .asJson()
                        .getBody()
                        .getObject()
                        .getJSONArray("Data");

        Queue<Transaction> transactionQueue = new PriorityQueue<>();
        transactionQueue.addAll(userHolding.getTransactions());
        Transaction currentTransaction = null;
        double cumulativeQuantity = 0;
        long nextDateUnix = 0;


        if (!userHolding.getAcronym().equals("USD")) {
            for (int i = 0; i < history.length(); i++) {

                JSONObject day = history.getJSONObject(i);
                double price = day.getDouble("close");
                Date currentItemUnixDate = DateUtils.round(new Date(day.getLong("time") * 1000), Calendar.DAY_OF_MONTH);

                if (currentItemUnixDate.getTime() > nextDateUnix) {
                    currentTransaction = transactionQueue.poll();
                    cumulativeQuantity += currentTransaction.getQuantity();
                    nextDateUnix = (transactionQueue.size() > 0) ?
                            transactionQueue.peek().getDate().getTime() : new Date().getTime();
                }

                if (currentItemUnixDate.getTime() > currentTransaction.getDate().getTime())
                    graphHoldingDataMap.put(
                            currentItemUnixDate,
                            price * cumulativeQuantity
                    );
            }

            return graphHoldingDataMap;
        } else return convertUsd(userHolding.getTotalQuantity());
    }

    public Double getValueAtDate(String acronym, long unixDate) throws Exception {
        JSONObject response = (JSONObject) Unirest.get(PRICE_DAY + acronym + "&tsyms=USD&ts=" + unixDate / 1000)
                .asJson().getBody().getObject().get(acronym);

        double price = response.getDouble("USD");

        if (price > 0)
            return price;

        throw new InvalidHoldingException("Could not retrieve price value for this date.");

    }

    public double getCurrencyPrice(String acronym) throws UnirestException {
        JSONObject response =
                Unirest.get(BASE_URL + "/data/price?fsym=" + acronym + "&tsyms=USD")
                        .asJson()
                        .getBody()
                        .getObject();

        return (response.length() == 1) ? response.getDouble("USD") : -1;
    }

    private Map<Date, Double> convertUsd(double quantity) throws UnirestException {
        Map<Date, Double> graphHoldingDataMap = new HashMap<>();

        //fixme work around for dollar value
        JSONArray history = Unirest.get(HISTORY_FIRST_PART + "EUR" + HISTORY_SECOND_PART)
                .asJson().getBody().getObject().getJSONArray("Data");

        for (int i = 0; i < history.length(); i++) {
            JSONObject day = history.getJSONObject(i);
            Date unixIteratorAsDate = DateUtils.round(new Date(day.getLong("time") * 1000), Calendar.DAY_OF_MONTH);

            graphHoldingDataMap.put(
                    unixIteratorAsDate,
                    quantity
            );
        }
        return graphHoldingDataMap;
    }
}