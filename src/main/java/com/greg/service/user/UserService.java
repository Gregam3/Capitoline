package com.greg.service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.greg.dao.user.UserDao;
import com.greg.entity.GraphHoldingData;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.user.Transaction;
import com.greg.entity.user.User;
import com.greg.entity.user.UserHolding;
import com.greg.service.crypto.CryptoService;
import com.greg.service.stock.StockService;
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

    private static final long DAY_IN_MS = 86400000;
    private UserDao userDao;
    private StockService stockService;
    private CryptoService cryptoService;

    @Autowired
    public UserService(UserDao userDao, StockService stockService, CryptoService cryptoService) {
        this.userDao = userDao;
        this.stockService = stockService;
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

        JsonNode dateBought = holdingNode.get("dateBought");

        Transaction transaction = new Transaction(
                holdingNode.get("quantity").asDouble(),
                price,
                new java.sql.Date(
                        (dateBought != null) ? dateBought.asLong() * 1000 : new Date().getTime()
                )
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

    public  Map<String, List<GraphHoldingData>>  getGraphHoldingData(String email) throws UnirestException, IOException, ParseException {
        List<GraphHoldingData> graphHoldingData;
        List<GraphHoldingData> cryptoGraphHoldingData;
        List<GraphHoldingData> stockGraphHoldingData;
        List<GraphHoldingData> fiatGraphHoldingData;

        Map<Date, Double> graphHoldingDataMap = new HashMap<>();
        Map<Date, Double> cryptoGraphHoldingDataMap = new HashMap<>();
        Map<Date, Double> stockGraphHoldingDataMap = new HashMap<>();
        Map<Date, Double> fiatGraphHoldingDataMap = new HashMap<>();

        User user = userDao.get(email);

        for (UserHolding userHolding : user.getHoldings()) {
            Map<Date, Double> currentDataHoldingMap = new LinkedHashMap<>();
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

        holdingsMap.put("Total", graphHoldingData);
        holdingsMap.put(HoldingType.CRYPTO.toString(), cryptoGraphHoldingData);
        holdingsMap.put(HoldingType.STOCK.toString(), stockGraphHoldingData);
        holdingsMap.put(HoldingType.FIAT.toString(), fiatGraphHoldingData);

        return holdingsMap;
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

    private Map<Date, Double> fillEmptyDates(Map<Date, Double> portfolioHistory, long earliestDateInRange) {
        long currentUnixTime = new Date().getTime();
        double lastValue = 0;

        for(long unixIterator = earliestDateInRange;
            unixIterator < currentUnixTime;
            unixIterator += DAY_IN_MS * 7) {
            Date unixIteratorAsDate = new Date(unixIterator);
            Double value = portfolioHistory.get(unixIteratorAsDate);

            if(value != null) {
                lastValue = value;
            } else {
                portfolioHistory.put(unixIteratorAsDate, lastValue);
            }
        }

        return portfolioHistory;
    }

    private List<GraphHoldingData> convertMapToList(Map<Date, Double> mapToConvert) {
        List<GraphHoldingData> list = new ArrayList<>();

        for (Map.Entry<Date, Double> day : mapToConvert.entrySet()) {
            list.add(
                    new GraphHoldingData(
                            day.getKey(),
                            day.getValue()
                    )
            );
        }

        return list;
    }
}
