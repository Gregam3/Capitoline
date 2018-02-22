package com.greg.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.greg.utils.JSONUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.List;
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
    private List<String> holdings;

    private String holdingsJson;

    public User()  {
    }

    public User(String email, String name, String settingsJson,  List<String> holdings) {
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
    public List<String> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<String> holdings) {
        this.holdings = holdings;
    }

    public String getHoldingsJson() {
        return holdingsJson;
    }

    public void setHoldingsJson(String holdings) {
        this.holdingsJson = holdings;
    }
}