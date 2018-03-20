package com.greg.service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.greg.dao.user.UserDao;
import com.greg.entity.GraphHoldingData;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.user.Transaction;
import com.greg.entity.user.User;
import com.greg.entity.user.UserHolding;
import com.greg.service.currency.crypto.CryptoService;
import com.greg.service.currency.fiat.FiatService;
import com.greg.service.stock.StockService;
//import com.greg.service.userholding.UserHoldingService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class UserService {

    private UserDao userDao;
    private StockService stockService;
    private CryptoService cryptoService;
    private FiatService fiatService;
    //    private UserHoldingService userHoldingService;
    private final String currentUserEmail = "gregoryamitten@gmail.com";

    @Autowired
    public UserService(
            UserDao userDao,
            StockService stockService,
            CryptoService cryptoService,
            FiatService fiatService
//            UserHoldingService userHoldingService
    ) {
        this.userDao = userDao;
        this.stockService = stockService;
        this.cryptoService = cryptoService;
        this.fiatService = fiatService;
//        this.userHoldingService = userHoldingService;
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

        JsonNode dateBought = holdingNode.get("dateBought");

        Transaction transaction = new Transaction(
                holdingNode.get("quantity").asDouble(),
                price,
                new java.sql.Date(
                        (dateBought == null || dateBought.asLong() * 1000 > new Date().getTime()) ?
                                new Date().getTime() :
                                dateBought.asLong() * 1000
                )
        );

        int holdingIndex = userDao.indexOfHolding(email, acronym);

        User user = get(email);

        if (holdingIndex >= 0)
            user
                    .getHoldings()
                    .get(holdingIndex)
                    .addTransaction(transaction);
        else {
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            List<UserHolding> userHoldings = user.getHoldings();
            userHoldings.add(
                    new UserHolding(
                            holdingNode.get("acronym").asText(),
                            holdingNode.get("name").asText(),
                            HoldingType.valueOf(holdingNode.get("holdingType").asText()),
                            transactions
                    )
            );

            user.setHoldings(userHoldings);
        }

        updateUser(user);
    }

    private void updateUser(User user) {
        user.configureChildren();
        userDao.update(user);
    }

    public Map<String, List<GraphHoldingData>> getGraphHoldingData(String email) throws UnirestException, IOException, ParseException {
        List<GraphHoldingData> graphHoldingData;
        List<GraphHoldingData> cryptoGraphHoldingData;
        List<GraphHoldingData> stockGraphHoldingData;
        List<GraphHoldingData> fiatGraphHoldingData;

        Map<Date, Double> graphHoldingDataMap = new HashMap<>();
        Map<Date, Double> cryptoGraphHoldingDataMap = new HashMap<>();
        Map<Date, Double> stockGraphHoldingDataMap = new HashMap<>();
        Map<Date, Double> fiatGraphHoldingDataMap = new LinkedHashMap<>();

        User user = userDao.get(email);

        for (UserHolding userHolding : user.getHoldings()) {
            Map<Date, Double> currentDataHoldingMap = new HashMap<>();
            switch (userHolding.getHoldingType()) {
                case CRYPTO:
                    currentDataHoldingMap =
                            cryptoService.getCryptoHistory(
                                    userHolding.getAcronym(),
                                    userHolding.getTotalQuantity()
                            );
                    cryptoGraphHoldingDataMap = mergeHoldingMap(cryptoGraphHoldingDataMap, currentDataHoldingMap);
                    break;
                case FIAT:
                    currentDataHoldingMap =
                            fiatService.getFiatHistory(
                                    userHolding.getAcronym(),
                                    userHolding.getTotalQuantity()
                            );

                    fiatGraphHoldingDataMap = mergeHoldingMap(fiatGraphHoldingDataMap, currentDataHoldingMap);
                    break;
                case STOCK:
                    currentDataHoldingMap =
                            stockService.getStockHistory(
                                    userHolding.getAcronym(),
                                    userHolding.getTotalQuantity()
                            );
                    stockGraphHoldingDataMap = mergeHoldingMap(stockGraphHoldingDataMap, currentDataHoldingMap);
                    break;
            }

            graphHoldingDataMap = mergeHoldingMap(graphHoldingDataMap, currentDataHoldingMap);
        }

        graphHoldingData = convertMapToList(graphHoldingDataMap);

        cryptoGraphHoldingData = convertMapToList(cryptoGraphHoldingDataMap);
        stockGraphHoldingData = convertMapToList(stockGraphHoldingDataMap);
        fiatGraphHoldingData = convertMapToList(fiatGraphHoldingDataMap);

        Collections.sort(graphHoldingData);
        Collections.sort(cryptoGraphHoldingData);
        Collections.sort(stockGraphHoldingData);
        Collections.sort(fiatGraphHoldingData);

        Map<String, List<GraphHoldingData>> holdingsMap = new HashMap<>();

        holdingsMap.put("total", graphHoldingData);
        holdingsMap.put("crypto", cryptoGraphHoldingData);
        holdingsMap.put("stock", stockGraphHoldingData);
        holdingsMap.put("fiat", fiatGraphHoldingData);

        return holdingsMap;
    }

    public boolean deleteHolding(String acronym, HoldingType holdingType, double amountToRemove) throws IOException {
        User user = get(currentUserEmail);

        for (UserHolding userHolding : user.getHoldings()) {
            if (userHolding.getAcronym().equals(acronym) &&
                    userHolding.getHoldingType().equals(holdingType)) {
                if (userHolding.getTotalQuantity() <= amountToRemove) {
                    //UserHolding's orphan will be deleted automatically
                    userHolding.setUser(null);
                    user.getHoldings().remove(userHolding);
                    update(user);
                    return true;
                } else {
                    Transaction transaction = new Transaction(0 - amountToRemove);
                    transaction.setUserHolding(userHolding);
                    userHolding.addTransaction(transaction);
                    update(user);
                    return true;
                }
            }
        }

        return false;
    }

    private Map<Date, Double> mergeHoldingMap(Map<Date, Double> map1, Map<Date, Double> map2) {
        for (Map.Entry<Date, Double> day : map2.entrySet()) {
            if (map1.containsKey(day.getKey()))
                map1.put(day.getKey(), map1.get(day.getKey()) + day.getValue());
            else
                map1.put(day.getKey(), day.getValue());
        }

        return map1;
    }

    private List<GraphHoldingData> convertMapToList(Map<Date, Double> mapToConvert) {
        List<GraphHoldingData> list = new ArrayList<>();

        for (Map.Entry<Date, Double> day : mapToConvert.entrySet())
            list.add(
                    new GraphHoldingData(
                            day.getKey(),
                            day.getValue()
                    )
            );

        return list;
    }
}
