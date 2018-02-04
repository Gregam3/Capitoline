package com.greg.entity.user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Clob;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name  = "PT_USER")
public class User {
    @Id
    private String email;
    private String encryptedPassword;
    private Clob settings;
    private Clob holdings;

    public User() {
    }

    public User(String email, String encryptedPassword, Clob settingsJson, Clob holdings) {
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.settings = settingsJson;
        this.holdings = holdings;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public Clob getSettings() {
        return settings;
    }

    public void setSettings(Clob settings) {
        this.settings = settings;
    }

    public Clob getHoldings() {
        return holdings;
    }

    public void setHoldings(Clob holdings) {
        this.holdings = holdings;
    }
}
