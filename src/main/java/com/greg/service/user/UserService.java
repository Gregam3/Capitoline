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
import com.greg.service.StockService;
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


    /**
     * Upon first log in by a user this method is used to set the user to return data for, the "currentUser"
     * @param email The login attempts email
     * @param password The login attempts password
     * @return the user object who just made the attempt to login
     * @throws IOException
     * @throws InvalidAccessAttemptException If password is invalid
     */
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

    /**
     * The get method in the {@link AbstractService} class is not secure so @Override has been implemented
     * so when it is called an exception will be thrown.
     */
    @Override
    public User get(String email) {
        throw new AssertionError("getUserSecure() method should be used instead.");
    }

    /**
     * Appends {@link Transaction} to old holdings/adds new holding with a new {@link Transaction}. Handles both adding an removing holdings and updates
     * any necessary values in said holding such as acquisitionCost.
     * @param holdingNode The node sent from the JS front-end containing an "acronym", "holdingType","quantity","name" (optional) and "dateBought"
     * @throws Exception
     */
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
                price = currencyService.getValueAtDate(
                        acronym,
                        date.getTime(),
                        currentUser.getSettings().getUserCurrency().getAcronym());
        }


        Transaction transaction = new Transaction(
                holdingNode.get("quantity").asDouble(),
                price,
                date
        );

        int holdingIndex = indexOfHolding(acronym, holdingType);

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

        JsonNode holdingToReduceNode = holdingNode.get("holdingToReduce");

        if (holdingToReduceNode != null && !holdingToReduceNode.isNull())
            removeValueFromOtherHolding(
                    indexOfHolding(
                            holdingToReduceNode.get("acronym").asText(),
                            HoldingType.valueOf(holdingToReduceNode.get("holdingType").asText())
                    ),
                    transaction.getQuantity() * transaction.getPrice(),
                    transaction.getDate()
            );


        currentUser.configureChildren();
        userDao.update(currentUser);
    }

    //If the user chooses to remove the value from another holding when buying this keeps track of it.

    /**
     * If the {@link User} chooses to bought a holding at the expense of another this method is used
     * Not stable enough for 1.3, will be used in future.
     * @param holdingIndexToReduce The index of the {@link UserHolding} List found in {@link User}
     * @param valueToReduce how much to reduce the old value by
     * @param date the date in which the {@link Transaction} takes place
     * @throws Exception
     */
    private void removeValueFromOtherHolding(int holdingIndexToReduce, double valueToReduce, Date date) throws Exception {
        List<UserHolding> holdings = currentUser.getHoldings();
        UserHolding holdingToReduce = holdings.get(holdingIndexToReduce);
        double price;

        price = (holdingToReduce.getHoldingType().equals(HoldingType.STOCK)) ?
                stockService.getStockPriceAtDate(holdingToReduce.getAcronym(), date.getTime()) :
                currencyService.getValueAtDate(holdingToReduce.getAcronym(),
                        date.getTime(),
                        currentUser.getSettings().getUserCurrency().getAcronym());


        if (valueToReduce > (holdingToReduce.getTotalQuantity() * price)) {
            holdings.get(holdingIndexToReduce).setUser(null);
            holdings.remove(holdingIndexToReduce);
        } else {
            Transaction transaction = new Transaction(
                    (valueToReduce / price) * -1,
                    price,
                    new java.sql.Date(date.getTime())
            );

            holdingToReduce.addTransaction(transaction);

            holdings.set(holdingIndexToReduce, holdingToReduce);
        }

        currentUser.setHoldings(holdings);
        update(currentUser);
    }

    /**
     * If a {@link UserHolding} is included in the {@link User}'s list will return its index
     * @param acronym The acronym of the {@link UserHolding} to find
     * @param holdingType The holdingType of the {@link UserHolding} to find
     * @return the index of the {@link UserHolding} or -1 if not found
     */
    private int indexOfHolding(String acronym, HoldingType holdingType) {
        List<UserHolding> userHoldings = currentUser.getHoldings();
        for (int i = 0; i < userHoldings.size(); i++)
            if (userHoldings.get(i).getAcronym().equals(acronym) &&
                    userHoldings.get(i).getHoldingType().equals(holdingType))
                return i;
        return -1;
    }

    /**
     * Sets current user to null
     */
    public void clearCurrentUser() {
        currentUser = null;
    }

    /**
     * Fetches {@link GraphHoldingData} for each holding in the {@link UserHolding} List in {@link User}
     * @return A map containing "total","stock","crypto" and "fiat". Each of these keys contains a value of a List of {@link GraphHoldingData}
     * @throws UnirestException
     * @throws IOException
     * @throws ParseException
     * @throws InvalidHoldingException
     */
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
                            userHolding
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

                    if (currentDataHoldingMap.keySet().size() == 0)
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

    /**
     * Reduces the {@link UserHolding} quantity, if quantity to remove is greater than total quantity, method removes {@link UserHolding} entirely
     * @param acronym The acronym of the {@link UserHolding} to remove
     * @param holdingType the holdingType of the {@link UserHolding} to remove
     * @param amountToRemove the amount to remove from the {@link UserHolding}
     * @throws IOException
     * @throws UnirestException
     * @throws ParseException
     * @throws InvalidHoldingException
     */
    public void reduceHoldingQuantity(String acronym, HoldingType holdingType, double amountToRemove) throws IOException, UnirestException, ParseException, InvalidHoldingException {
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
                                    currencyService.getCurrentPrice(
                                            acronym,
                                            currentUser.getSettings().getUserCurrency().getAcronym())),
                            new java.sql.Date(new Date().getTime())
                    );

                    transaction.setUserHolding(userHolding);
                    userHolding.addTransaction(transaction);

                    //Is this an issue? Not changing user directly.
                    update(currentUser);
                    break;
                }
            }
        }
    }

    /**
     * Merges two maps into one, used to merge {@link UserHolding} histories
     * @param map1 the larger map
     * @param map2 the smaller map
     * @return An amalgam Map of map1 and map2 with a key of Date and a value of Double
     */
    private Map<Date, Double> mergeHoldingMap(Map<Date, Double> map1, Map<Date, Double> map2) {
        for (Map.Entry<Date, Double> day : map2.entrySet()) {
            if (map1.containsKey(day.getKey()))
                map1.put(day.getKey(), map1.get(day.getKey()) + day.getValue());
            else
                map1.put(day.getKey(), day.getValue());
        }

        return map1;
    }

    /**
     * Converts the Maps generated by getGraphHoldingData() into Lists of {@link GraphHoldingData}
     * @param mapToConvert the map to convert to a list
     * @return
     */
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

    /**
     * Changes {@link User} settings to values sent in http post, currently only used to change currency
     * @param settingsNode The new values, values necessary to change currency "currency" and "acronym"
     * @throws IOException
     * @throws UnirestException
     */
    public void updateSettings(JsonNode settingsNode) throws IOException, UnirestException {
        String oldCurrencyAcronym = currentUser.getSettings().getUserCurrency().getAcronym();

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

        List<UserHolding> userHoldings = currentUser.getHoldings();

        double modifier = currencyService.getCurrentPrice(
                oldCurrencyAcronym,
                currentUser.getSettings().getUserCurrency().getAcronym()
        );

        for (UserHolding userHolding : userHoldings) {
            userHolding.setAcquisitionCost(userHolding.getAcquisitionCost() * modifier);

            for (Transaction transaction : userHolding.getTransactions())
                transaction.setPrice(transaction.getPrice() * modifier);
        }

        currentUser.setHoldings(userHoldings);

        update(currentUser);
    }

    /**
     * @return the user for the session set by getUserSecure()
     */
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