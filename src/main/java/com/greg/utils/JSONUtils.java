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
import com.greg.service.user.UserService;
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
    private UserService userService;

    @Autowired
    public JSONUtils(StockService stockService, CryptoService cryptoService, FiatService fiatService, UserService userService) {
        this.stockService = stockService;
        this.cryptoService = cryptoService;
        this.fiatService = fiatService;
        this.userService = userService;
    }

    public Holding convertToHolding(JsonNode jsonNode) throws UnirestException, IOException {
        double price = 0;
        String acronym = "";
        JsonNode holdingNode = jsonNode.get("holdingType");

        switch (HoldingType.valueOf(holdingNode.get("holdingType").asText())) {
            case STOCK:
                price = stockService.getStockPrice(acronym);
                break;
            case FIAT:
            case CRYPTO:
                price = cryptoService.getCryptoPrice(acronym);
                break;
        }

        return new Holding(holdingNode.get("acronym").asText(),
                holdingNode.get("name").asText(),
                holdingNode.get("holdingType"))

        return new Transaction(
                holdingNode.get("totalQuantity").asDouble(),
                price,
                new Date()
        );
    }


    public User convertToUser(JsonNode userNode) throws JsonProcessingException {
        List<Holding> userHoldings = new ArrayList<>();

        if (userNode.get("email") == null) throw new InvalidPropertyException(User.class, "email", "Email is missing");

        String email = userNode.get("email").asText();
        String name = (userNode.get("name") != null) ? userNode.asText() : null;

        for (JsonNode next : userNode.get("holdings")) {
            userHoldings.add(
                    new Holding(next.get("acronym").asText(),
                            next.get("name").asText(),
                            HoldingType.valueOf(next.get("holdingType").asText()),
                            convertToTransactionList(next.get("transactions"))
                    ));
        }

        return new User(email, name, null, userHoldings);
    }

    public List<Transaction> convertToTransactionList(JsonNode holdingsNode) {
        List<Transaction> transactions = new ArrayList<>();

        for (JsonNode holdingNode : holdingsNode) {
            transactions.add(new Transaction(
                            holdingNode.get("quantity").asDouble(),
                            holdingNode.get("price").asDouble(),
                            new Date() //fixme
                    )
            );
        }

        return transactions;
    }

    public ArrayList convertToHoldingsList(String holdingsJson) throws IOException {
        return OBJECT_MAPPER.readValue(holdingsJson, ArrayList.class);
    }
}