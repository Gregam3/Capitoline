package com.greg.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.entity.holding.Holding;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.Transaction;
import com.greg.entity.user.User;
import com.greg.service.crypto.CryptoService;
import com.greg.service.currency.FiatService;
import com.greg.service.stock.StockService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class JSONUtils {
    private static final Logger LOG = Logger.getLogger(JSONUtils.class);
    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private StockService stockService;
    private CryptoService cryptoService;
    private FiatService fiatService;

    @Autowired
    public JSONUtils(StockService stockService, CryptoService cryptoService, FiatService fiatService) {
        this.stockService = stockService;
        this.cryptoService = cryptoService;
        this.fiatService = fiatService;
    }

    public User convertToUserWithNewHolding(JsonNode jsonNode) throws UnirestException, JsonProcessingException {
        double price = 0;

        //jsonNode.get("acronym").asText() does not need to be declared as a local variable as even though
        //it is written multiple times it will only be accessed once
        switch (HoldingType.valueOf(jsonNode.get("holdings")
                .get(jsonNode.size() - 1).get("holdingType").asText())) {
            case STOCK:
                price = stockService.getStockPrice(jsonNode.get("holdings").get(jsonNode.size() - 1).get("acronym").asText());
                break;
            case FIAT:
            case CRYPTO:
                price = cryptoService.getCryptoPrice(jsonNode.get("holdings").get(jsonNode.size() - 1).get("acronym").asText());
                break;
        }

        User user = convertToUser(jsonNode);
        user.setMostRecentlyAddedHoldingPrice(price);

        return user;
    }


    public User convertToUser(JsonNode userNode) throws JsonProcessingException {
        List<Holding> userHoldings = new ArrayList<>();

        if (userNode.get("email") == null) throw new InvalidPropertyException(User.class, "email", "Email is missing");

        String email = userNode.get("email").asText();
        String name = (userNode.get("name") != null) ? userNode.asText() : null;

        for (JsonNode next : userNode.get("holdings")) {
            JsonNode transactionNode = next.get("transaction");

            userHoldings.add(
                    new Holding(next.get("acronym").asText(),
                            next.get("name").asText(),
                            HoldingType.valueOf(next.get("holdingType").asText()),
                            new Transaction(
                                    transactionNode.get("quantity").asDouble(),
                                    transactionNode.get("price").asDouble(),
                                    new Date()
                            )
                    ));
        }

        return new User(email, name, null, userHoldings);
    }

    public ArrayList convertToHoldingsList(String holdingsJson) throws IOException {
        String[] split = holdingsJson.split("\\|");
        return OBJECT_MAPPER.readValue(split[0], ArrayList.class);
    }

}