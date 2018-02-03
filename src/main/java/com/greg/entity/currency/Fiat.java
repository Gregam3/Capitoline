package com.greg.entity.currency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_CURRENCY")
public class Fiat {

    private String acronym;
    private String name;
    private String symbol;

    public Fiat() {
    }

    public Fiat(String acronym, String name, String symbol) {
        this.acronym = acronym;
        this.name = name;
        this.symbol = symbol;
    }

    @Id
    @Column(name = "ACRONYM")
    public String getAcronym() {
        return acronym;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    @Column(name = "SYMBOL")
    public String getSymbol() {
        return symbol;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
