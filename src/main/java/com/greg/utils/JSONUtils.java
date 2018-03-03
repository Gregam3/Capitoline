package com.greg.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.entity.holding.UserHolding;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.Transaction;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Service
public class JSONUtils {
    private static final Logger LOG = Logger.getLogger(JSONUtils.class);
    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static List<UserHolding> convertToHoldingList(String holdingsJson) throws IOException {
        ArrayList holdings = OBJECT_MAPPER.readValue(holdingsJson, ArrayList.class);
        List<UserHolding> convertedUserHoldings = new  ArrayList<>();

        for (Object holding : holdings) {
            LinkedHashMap linkedHashMap = (LinkedHashMap) holding;
            convertedUserHoldings.add(new UserHolding(
                    String.valueOf(linkedHashMap.get("acronym")),
                    String.valueOf(linkedHashMap.get("name")),
                    HoldingType.valueOf(linkedHashMap.get("holdingType").toString()),
                    (List<Transaction>) linkedHashMap.get("transactions")
                    )
            );
        }

        return convertedUserHoldings;
    }

    public static List<Transaction> convertToTransactionList(String transactionsJson) throws IOException {
        ArrayList transactions = OBJECT_MAPPER.readValue(transactionsJson, ArrayList.class);
        List<Transaction> convertedTransactions = new  ArrayList<>();

        for (Object transaction : transactions) {
            LinkedHashMap linkedHashMap = (LinkedHashMap) transaction;
            convertedTransactions.add(new Transaction(
                    (Double) linkedHashMap.get("quantity"),
                    (Double) linkedHashMap.get("price"),
                    new Date()
                    )
            );
        }

        return convertedTransactions;
    }
}