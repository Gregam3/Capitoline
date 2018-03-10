package com.greg.service.crypto;

import com.greg.entity.holding.crypto.Crypto;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class CryptoService {

    private final static String CRYPTO_API_URL = "https://min-api.cryptocompare.com/";
    private final static String CRYPTO_HISTORY_FIRST_PART = "https://min-api.cryptocompare.com/data/histoday?fsym=";
    private final static String CRYPTO_HISTORY_SECOND_PART = "&tsym=USD&limit=1825&aggregate=3&e=CCCAGG";

    public List<Crypto> list() throws UnirestException {
        List<Crypto> cryptos = new ArrayList<>();
        JSONObject data = Unirest.get(CRYPTO_API_URL + "/data/all/coinlist").asJson().getBody().getObject().getJSONObject("Data");
        Iterator keys = data.keys();

        while (keys.hasNext()) {
            String currentKey = (String) keys.next();
            cryptos.add(
                    new Crypto(
                            currentKey,
                            data.getJSONObject(currentKey).getString("CoinName")
                    )
            );
        }

        return cryptos;
    }


    public double getCryptoPrice(String acronym) throws UnirestException {
        JSONObject response = Unirest.get(CRYPTO_API_URL + "/data/price?fsym=" + acronym + "&tsyms=USD")
                .asJson().getBody().getObject();

        return (response.length() == 1) ? response.getDouble("USD") : -1;
    }

    public Map<Date, Double> getCryptoHistory(String acronym, double quantity) throws UnirestException {
        LinkedHashMap<Date, Double> graphHoldingDataMap = new LinkedHashMap<>();

        JSONArray history = Unirest.get(CRYPTO_HISTORY_FIRST_PART + acronym + CRYPTO_HISTORY_SECOND_PART)
                .asJson().getBody().getObject().getJSONArray("Data");

        for (int i = 0; i < history.length(); i++) {
            JSONObject day = history.getJSONObject(i);
            double price = day.getDouble("close");

            if (price > 0)
                graphHoldingDataMap.put(
                        new Date(day.getLong("time") * 1000),
                        price * quantity
                );
        }

        return graphHoldingDataMap;
    }
}