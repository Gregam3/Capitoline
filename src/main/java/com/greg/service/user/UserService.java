package com.greg.service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.greg.dao.user.UserDao;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.user.Transaction;
import com.greg.entity.user.User;
import com.greg.entity.user.UserHolding;
import com.greg.service.crypto.CryptoService;
import com.greg.service.stock.StockService;
import com.greg.utils.JSONUtils;
import com.mashape.unirest.http.exceptions.UnirestException;
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
public class UserService {

    private UserDao userDao;
    private StockService stockService;
    private CryptoService cryptoService;
    private JSONUtils jsonUtils;

    @Autowired
    public UserService(UserDao userDao, StockService stockService, CryptoService cryptoService, JSONUtils jsonUtils) {
        this.userDao = userDao;
        this.stockService = stockService;
        this.jsonUtils = jsonUtils;
        this.cryptoService = cryptoService;
    }

    public User get(String email) throws IOException {
        return userDao.get(email);
    }

    public void update(User user) {
        userDao.update(user);
    }

    public void addTransaction(JsonNode holdingNode) throws UnirestException, IOException {
        double price = 0;
        String email = holdingNode.get("email").asText();
        String acronym = holdingNode.get("acronym").asText();

        switch (HoldingType.valueOf(holdingNode.get("holdingType").asText())) {
            case STOCK:
                price = stockService.getStockPrice(acronym);
                break;
            case FIAT:
            case CRYPTO:
                price = cryptoService.getCryptoPrice(acronym);
                break;
        }

        Transaction transaction = new Transaction(
                holdingNode.get("quantity").asDouble(),
                price,
                new java.sql.Date(new Date().getTime())
        );

        int holdingIndex = userDao.indexOfHolding(email, acronym);

        if (holdingIndex >= 0)
            userDao.appendTransaction(email, holdingIndex, transaction);
        else {
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);
            userDao.addHolding(email,
                    new UserHolding(
                            holdingNode.get("acronym").asText(),
                            holdingNode.get("name").asText(),
                            HoldingType.valueOf(holdingNode.get("holdingType").asText()),
                            transactions
                    ));
        }

    }
}
