package com.greg.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.entity.holding.Holding;
import com.greg.entity.holding.crypto.Crypto;
import com.greg.entity.holding.fiat.Fiat;
import com.greg.entity.holding.stock.Stock;
import com.greg.entity.user.User;
import com.greg.entity.user.UserHoldings;
import com.greg.exceptions.InvalidHoldingException;
import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;
import org.apache.log4j.Logger;
import org.springframework.beans.InvalidPropertyException;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class JSONUtils {
    private static final Logger LOG = Logger.getLogger(JSONUtils.class);
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static UserHoldings convertJsonUserHoldings(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, UserHoldings.class);
        } catch (IOException e) {
            LOG.error(e);
            e.printStackTrace();
        }

        return null;
    }

    public static User convertToUser(JsonNode userNode) {
        UserHoldings userHoldings = new UserHoldings();

        if(userNode.get("email") == null) throw new InvalidPropertyException(User.class, "email", "Email is missing");

        String email =  userNode.get("email").asText();
        String name = (userNode.get("name") != null) ? userNode.asText() : null;

        for (JsonNode next : userNode.get("holdings")) {
            switch (next.get("holdingType").asText()) {
                case "STOCK":
                    userHoldings.addStock(new Stock(next.get("acronym").asText(), next.get("name").asText()), 0);
                    break;
                case "CRYPTO":
                    userHoldings.addCrypto(new Crypto(next.get("acronym").asText(), next.get("name").asText()), 0);
                    break;
                case "FIAT":
                    userHoldings.addFiat(new Fiat(next.get("acronym").asText(), next.get("name").asText()), 0);
                    break;
                default:
                    System.err.println(next.get("name") + " has no holdingType");
            }
        }

        return  new User(email, name, null, userHoldings);
    }
}