package com.greg.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.entity.holding.Holding;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.user.User;
import org.apache.log4j.Logger;
import org.springframework.beans.InvalidPropertyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class JSONUtils {
    private static final Logger LOG = Logger.getLogger(JSONUtils.class);
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static User convertToUser(JsonNode userNode) {
        List<String> userHoldings = new ArrayList<>();

        if (userNode.get("email") == null) throw new InvalidPropertyException(User.class, "email", "Email is missing");

        String email = userNode.get("email").asText();
        String name = (userNode.get("name") != null) ? userNode.asText() : null;

        for (JsonNode next : userNode.get("holdings"))
            userHoldings.add(
                    new Holding(next.get("acronym").asText(),
                            next.get("name").asText(),
                            HoldingType.valueOf(next.get("holdingType").asText()),
                            0
                    ).asJson());

        return new User(email, name, null, userHoldings);
    }

    public static ArrayList convertToHoldingsList(String holdingsJson) throws IOException {
        String[] split = holdingsJson.split("\\|");
        return OBJECT_MAPPER.readValue(split[0], ArrayList.class);
    }
}