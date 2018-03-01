package com.greg.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.entity.holding.Holding;
import com.greg.utils.JSONUtils;

import javax.persistence.*;
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
    private List<Holding> holdings;

    public User() {

    }

    public User(String email, String name, String settingsJson, List<Holding> holdings) throws JsonProcessingException {
        this.email = email;
        this.name = name;
        this.settings = settingsJson;
        this.holdings = holdings;
        this.holdingsJson =
                JSONUtils.OBJECT_MAPPER.writeValueAsString((holdings != null) ? holdings : "[]");
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
    public List<Holding> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }

    public String getHoldingsJson() {
        return holdingsJson;
    }

    public void setHoldingsJson(String holdingsJson) {
        this.holdingsJson = holdingsJson;
    }

    public void setMostRecentlyAddedHoldingPrice(double price) {
        if(holdings.size() != 0)
            holdings.get(holdings.size() - 1).setAcquisitionPrice(price);
    }
}