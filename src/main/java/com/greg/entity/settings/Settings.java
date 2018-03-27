package com.greg.entity.settings;

import com.greg.entity.holding.fiat.Fiat;

import javax.persistence.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_SETTINGS")
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long settingsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="acronym")
    private Fiat userCurrency = new Fiat();

    public Settings() {
    }

    public Settings(Fiat userCurrency) {
        this.userCurrency = userCurrency;
    }

    public long getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(long settingsId) {
        this.settingsId = settingsId;
    }

    public Fiat getUserCurrency() {
        return userCurrency;
    }

    public void setUserCurrency(Fiat userCurrency) {
        this.userCurrency = userCurrency;
    }
}
