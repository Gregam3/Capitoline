package com.greg.entity.user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
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
    private String settings;
    @Ele
    private Map<String, String> holdings = new HashMap<>();

    public User() {
    }

    public User(String email, String settingsJson, Map<String, String> holdings) {
        this.email = email;
        this.settings = settingsJson;
        this.holdings = holdings;
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

    public Map<String, String> getHoldings() {
        return holdings;
    }

    public void setHoldings(Map<String, String> holdings) {
        this.holdings = holdings;
    }
}
