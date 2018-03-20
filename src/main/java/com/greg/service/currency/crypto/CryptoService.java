package com.greg.service.currency.crypto;

import com.greg.entity.holding.crypto.Crypto;
import com.greg.service.currency.CurrencyService;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class CryptoService {

    private final static String CRYPTO_API_URL = "https://min-api.cryptocompare.com/";

    private CurrencyService currencyService;

    @Autowired
    public CryptoService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

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
        JSONObject response =
                Unirest.get(CRYPTO_API_URL + "/data/price?fsym=" + acronym + "&tsyms=USD")
                        .asJson()
                        .getBody()
                        .getObject();

        return (response.length() == 1) ? response.getDouble("USD") : -1;
    }

    public Map<Date, Double> getCryptoHistory(String acronym, double quantity) throws UnirestException {
        return currencyService.getCurrencyHistory(acronym, quantity);
    }
}
