package com.greg.entity.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.entity.holding.UserHolding;
import com.greg.utils.JSONUtils;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_USER")
public class User {
    @Id
    private String email;
    private String name;
    private String settings;
    private String holdingsJson;

    @Transient
    private List<UserHolding> holdings;

    public User() {
    }

    public User(String email, String name, String settingsJson, List<UserHolding> userHoldings) throws JsonProcessingException {
        this.email = email;
        this.name = name;
        this.settings = settingsJson;
        this.holdings = userHoldings;
        this.holdingsJson =
                convertHoldings(userHoldings);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    @Transient
    public List<UserHolding> getUserHoldings() throws IOException {
        holdings = JSONUtils.convertToHoldingList(holdingsJson);
        return this.holdings;
    }

    public void setUserHoldings(List<UserHolding> userHoldings) throws JsonProcessingException {
        this.holdings = userHoldings;
        this.holdingsJson = convertHoldings(userHoldings);
    }

    public String getHoldingsJson() {
        return holdingsJson;
    }

    public void setHoldingsJson(String holdingsJson) {

        this.holdingsJson = holdingsJson;
    }

    private String convertHoldings(List<UserHolding> userHoldings) throws JsonProcessingException {
        return JSONUtils.OBJECT_MAPPER.writeValueAsString((userHoldings != null) ? userHoldings : "[]");
    }
}