package com.greg.service.crypto;

        import com.greg.entity.holding.HoldingType;
        import com.greg.entity.holding.crypto.Crypto;
        import com.mashape.unirest.http.Unirest;
        import com.mashape.unirest.http.exceptions.UnirestException;
        import org.json.JSONObject;
        import org.springframework.http.ResponseEntity;
        import org.springframework.stereotype.Service;

        import java.util.ArrayList;
        import java.util.Iterator;
        import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class CryptoService {

    private final static String CRYPTO_API_URL = "https://min-api.cryptocompare.com/";

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
        return Unirest.get(CRYPTO_API_URL + "/data/price?fsym=" + acronym + "&tsyms=USD")
                .asJson().getBody().getObject().getDouble("USD");
    }
}