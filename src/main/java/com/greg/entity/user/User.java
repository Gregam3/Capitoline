package com.greg.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Map;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name  = "PT_USER")
public class User {
    @Id
    private String email;
    private String name;
    private String settings;

    @Transient
    private Map<String, Double> holdings;

    @JsonIgnore
    private String holdingsJson;

    public User() {
    }

    public User(String email, String name, String settingsJson,  Map<String, Double> holdings) {
        this.email = email;
        this.name = name;
        this.settings = settingsJson;
        this.holdings = holdings;
        this.holdingsJson = holdings.toString();
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
    public Map<String, Double> getHoldings() {
        return holdings;
    }

    public void setHoldings(Map<String, Double> holdings) {
        this.holdings = holdings;
    }

    @JsonIgnore
    public String getHoldingsJson() {
        return holdingsJson;
    }

    public void setHoldings(String holdings) {
        this.holdingsJson = holdings;
    }
}