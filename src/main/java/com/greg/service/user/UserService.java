package com.greg.service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.greg.dao.user.UserDao;
import com.greg.entity.GraphHoldingData;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.settings.Settings;
import com.greg.entity.user.Transaction;
import com.greg.entity.user.User;
import com.greg.entity.user.UserHolding;
import com.greg.exceptions.InvalidAccessAttemptException;
import com.greg.exceptions.InvalidHoldingException;
import com.greg.exceptions.InvalidRegisterCredentialsException;
import com.greg.service.AbstractService;
import com.greg.service.currency.CurrencyService;
import com.greg.service.currency.fiat.FiatService;
import com.greg.user.StockService;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
public class UserService extends AbstractService<User> {

    private UserDao userDao;
    private StockService stockService;
    private CurrencyService currencyService;
    private FiatService fiatService;

    private User currentUser = null;

    @Autowired
    public UserService(
            UserDao userDao,
            StockService stockService,
            CurrencyService currencyService,
            FiatService fiatService
    ) {
        this.userDao = userDao;
        this.stockService = stockService;
        this.currencyService = currencyService;
        this.fiatService = fiatService;
        this.setDao(userDao);
    }

    public User getUserSecure(String email, String password) throws IOException, InvalidAccessAttemptException {
        currentUser = userDao.get(email);
        if (currentUser == null)
            return null;

        if (!currentUser.getPassword().equals(password)) {
            currentUser = null;
            throw new InvalidAccessAttemptException("Password is incorrect");
        }

        return currentUser;
    }

    @Override
    public User get(String email) {
        throw new AssertionError("getUserSecure() method should be used instead.");
    }

    public void addTransaction(JsonNode holdingNode) throws Exception {
        double price = 0;
        String acronym = holdingNode.get("acronym").asText();
        HoldingType holdingType = HoldingType.valueOf(holdingNode.get("holdingType").asText());

        JsonNode dateNode = holdingNode.get("dateBought");
        java.sql.Date date = new java.sql.Date(
                (dateNode == null || dateNode.asLong() > new Date().getTime()) ?
                        new Date().getTime() :
                        dateNode.asLong()
        );

        switch (holdingType) {
            case STOCK:
                int dayAsInt = new Date(date.getTime()).getDay();

                if (dayAsInt == 0 || dayAsInt == 6)
                    throw new InvalidHoldingException("The Stock market is only open on week days.");
                price = stockService.getStockPriceAtDate(acronym, date.getTime());
                break;
            case FIAT:
            case CRYPTO:
                price = currencyService.getValueAtDate(acronym, date.getTime());
        }


        Transaction transaction = new Transaction(
                holdingNode.get("quantity").asDouble(),
                price,
                date
        );

        int holdingIndex = userDao.indexOfHolding(currentUser, acronym, holdingType);

        if (holdingIndex >= 0) {
            currentUser
                    .getHoldings()
                    .get(holdingIndex)
                    .addTransaction(transaction);
        } else {
            List<UserHolding> userHoldings = currentUser.getHoldings();
            userHoldings.add(
                    new UserHolding(
                            holdingNode.get("acronym").asText(),
                            holdingNode.get("name").asText(),
                            holdingType,
                            transaction
                    )
            );

            currentUser.setHoldings(userHoldings);
        }

        currentUser.configureChildren();
        userDao.update(currentUser);
    }

    public Map<String, List<GraphHoldingData>> getGraphHoldingData() throws UnirestException, IOException, ParseException, InvalidHoldingException {
        //Map used as only one instance of a day should be in each map
        Map<Date, Double> graphHoldingDataMap = new HashMap<>();
        Map<Date, Double> cryptoGraphHoldingDataMap = new HashMap<>();
        Map<Date, Double> stockGraphHoldingDataMap = new HashMap<>();
        Map<Date, Double> fiatGraphHoldingDataMap = new HashMap<>();

        double userCurrencyModifier = currencyService.getCurrentPrice(
                "USD",
                currentUser.getSettings().getUserCurrency().getAcronym()
        );

        for (UserHolding userHolding : currentUser.getHoldings()) {
            Map<Date, Double> currentDataHoldingMap = new HashMap<>();
            switch (userHolding.getHoldingType()) {
                case CRYPTO:
                case FIAT:
                    currentDataHoldingMap = currencyService.getCurrencyHistory(
                            userHolding,
                            userCurrencyModifier
                    );

                    if (currentDataHoldingMap == null)
                        break;

                    if (userHolding.getHoldingType().equals(HoldingType.CRYPTO))
                        cryptoGraphHoldingDataMap = mergeHoldingMap(cryptoGraphHoldingDataMap, currentDataHoldingMap);
                    else
                        fiatGraphHoldingDataMap = mergeHoldingMap(fiatGraphHoldingDataMap, currentDataHoldingMap);

                    break;
                case STOCK:
                    currentDataHoldingMap = stockService.getStockHistory(
                            userHolding,
                            userCurrencyModifier
                    );

                    if (currentDataHoldingMap == null)
                        break;

                    stockGraphHoldingDataMap = mergeHoldingMap(stockGraphHoldingDataMap, currentDataHoldingMap);
                    break;
            }

            if (currentDataHoldingMap != null)
                graphHoldingDataMap = mergeHoldingMap(graphHoldingDataMap, currentDataHoldingMap);
        }

        //Converts Maps into sorted lists so it can be put into the n3 charts
        List<GraphHoldingData> graphHoldingData;
        List<GraphHoldingData> cryptoGraphHoldingData;
        List<GraphHoldingData> stockGraphHoldingData;
        List<GraphHoldingData> fiatGraphHoldingData;

        graphHoldingData =
                convertHoldingDataMapToList(graphHoldingDataMap);
        cryptoGraphHoldingData =
                convertHoldingDataMapToList(cryptoGraphHoldingDataMap);
        stockGraphHoldingData =
                convertHoldingDataMapToList(stockGraphHoldingDataMap);
        fiatGraphHoldingData =
                convertHoldingDataMapToList(fiatGraphHoldingDataMap);

        Collections.sort(graphHoldingData);
        Collections.sort(cryptoGraphHoldingData);
        Collections.sort(stockGraphHoldingData);
        Collections.sort(fiatGraphHoldingData);

        //Creates a map allowing for easy access of each list in JavaScript
        Map<String, List<GraphHoldingData>> holdingsMap = new HashMap<>();

        holdingsMap.put("total", graphHoldingData);
        holdingsMap.put("crypto", cryptoGraphHoldingData);
        holdingsMap.put("stock", stockGraphHoldingData);
        holdingsMap.put("fiat", fiatGraphHoldingData);

        return holdingsMap;
    }

    public void removeItemsFromHolding(String acronym, HoldingType holdingType, double amountToRemove) throws IOException, UnirestException, ParseException, InvalidHoldingException {
        for (UserHolding userHolding : currentUser.getHoldings()) {
            if (userHolding.getAcronym().equals(acronym) &&
                    userHolding.getHoldingType().equals(holdingType)) {
                if (userHolding.getTotalQuantity() <= amountToRemove) {
                    //Orphan will be deleted automatically
                    userHolding.setUser(null);
                    for (Transaction transaction : userHolding.getTransactions())
                        transaction.setUserHolding(null);


                    currentUser.getHoldings().remove(userHolding);
                    update(currentUser);
                    break;
                } else {
                    Transaction transaction = new Transaction(
                            0 - amountToRemove,
                            (holdingType.equals(HoldingType.STOCK) ?
                                    stockService.getCurrentStockPrice(acronym) :
                                    currencyService.getCurrentPrice(acronym, "USD")),
                            new java.sql.Date(new Date().getTime())
                    );

                    transaction.setUserHolding(userHolding);
                    userHolding.addTransaction(transaction);

                    update(currentUser);
                    break;
                }
            }
        }
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

    private List<GraphHoldingData> convertHoldingDataMapToList(Map<Date, Double> mapToConvert) {
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

    public void updateSettings(JsonNode settingsNode) throws IOException {
        currentUser.setSettings(
                new Settings(
                        fiatService.get(
                                settingsNode
                                        .get("settings")
                                        .get("currency")
                                        .get("acronym")
                                        .asText()
                        )
                )
        );

        update(currentUser);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void addUser(String email, String name, String password)
            throws IOException, InvalidRegisterCredentialsException, InvalidAccessAttemptException {
        if (getUserSecure(email, password) != null)
            throw new InvalidRegisterCredentialsException("That email is already in use.");

        User user = new User(
                email,
                name,
                password,
                new Settings(fiatService.get("USD"))
        );

        currentUser = user;
        userDao.add(user);
    }
}