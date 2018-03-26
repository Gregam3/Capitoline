package com.greg.entity.settings;

import com.greg.entity.holding.fiat.Fiat;
import com.greg.entity.user.User;

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

    @OneToOne(fetch =  FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;

//    @ManyToOne
//    @JoinColumn(name="acronym")
//    private Fiat userCurrency = new Fiat();

    public Settings() {
    }

    public Settings(User user, Fiat userCurrency) {
//        this.userCurrency = userCurrency;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(long settingsId) {
        this.settingsId = settingsId;
    }

//    public Fiat getUserCurrency() {
//        return userCurrency;
//    }
//
//    public void setUserCurrency(Fiat userCurrency) {
//        this.userCurrency = userCurrency;
//    }
}
