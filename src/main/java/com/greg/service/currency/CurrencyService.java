package com.greg.service.currency;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class CurrencyService {
    private final static String CRYPTO_HISTORY_FIRST_PART = "https://min-api.cryptocompare.com/data/histoday?fsym=";
    private final static String CRYPTO_HISTORY_SECOND_PART = "&tsym=USD&limit=1825";

    public Map<Date, Double> getCurrencyHistory(String acronym, double quantity) throws UnirestException {
        Map<Date, Double> graphHoldingDataMap = new HashMap<>();

        JSONArray history = Unirest.get(CRYPTO_HISTORY_FIRST_PART + acronym + CRYPTO_HISTORY_SECOND_PART)
                .asJson().getBody().getObject().getJSONArray("Data");

        if (!acronym.equals("USD")) {
            for (int i = 0; i < history.length(); i++) {
                JSONObject day = history.getJSONObject(i);
                double price = day.getDouble("close");
                Date unixIteratorAsDate = DateUtils.round(new Date(day.getLong("time") * 1000), Calendar.DAY_OF_MONTH);

                if (price > 0)
                    graphHoldingDataMap.put(
                            unixIteratorAsDate,
                            price * quantity
                    );
            }

            return graphHoldingDataMap;
        } else return convertUsd(quantity);
    }

    private Map<Date, Double> convertUsd(double quantity) throws UnirestException {
        Map<Date, Double> graphHoldingDataMap = new HashMap<>();

        //fixme work around for dollar value
        JSONArray history = Unirest.get(CRYPTO_HISTORY_FIRST_PART + "EUR" + CRYPTO_HISTORY_SECOND_PART)
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