package com.greg.service.crypto;

import com.greg.entity.holding.crypto.Crypto;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.stereotype.Service;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class CryptoPriceRetrievalService {

    private final static String CRYPTO_API_URL = "https://min-api.cryptocompare.com/";

    public Crypto retrieveCryptoPrice(String cryptoId, String userCurrency) throws UnirestException {
        Object responsePrice = Unirest.get(CRYPTO_API_URL + "data/price?fsym=" + cryptoId
                + "&tsyms=" + userCurrency).asJson().getBody().getObject().get(userCurrency);

        if(responsePrice instanceof Double)
            return new Crypto(cryptoId, (double) responsePrice);

        return null;
    }
}