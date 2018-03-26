package com.greg.entity.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.entity.settings.Settings;
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

    @OneToOne(cascade = CascadeType.ALL)
    private Settings settings = new Settings();

    @OneToMany(mappedBy = "settings", cascade= CascadeType.ALL, orphanRemoval = true)
    private List<UserHolding> holdings;

    public User() {
    }

    public User(String email,
                String name,
                List<UserHolding> holdings
    ) throws JsonProcessingException {
        this.email = email;
        this.name = name;
        this.holdings = holdings;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
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

    public List<UserHolding> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<UserHolding> holdings) {
        this.holdings = holdings;
    }

    private String convertHoldings(List<UserHolding> userHoldings) throws JsonProcessingException {
        return JSONUtils.OBJECT_MAPPER.writeValueAsString((userHoldings != null) ? userHoldings : "[]");
    }

    public void configureChildren() {
        for (UserHolding holding : this.holdings) {
            holding.setUser(this);
            holding.configureChildren();
        }
    }
}