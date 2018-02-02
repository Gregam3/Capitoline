package com.greg.entity.currency;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_CURRENCY")
public class Currency {

    private String acronym;
    private String name;
    private String symbol;

    public Currency(String acronym, String name, String symbol) {
        this.acronym = acronym;
        this.name = name;
        this.symbol = symbol;
    }

    @Id
    public String getAcronym() {
        return acronym;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }
}
