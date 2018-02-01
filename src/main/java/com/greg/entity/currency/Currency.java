package com.greg.entity.currency;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
public class Currency {
    @Id
    private String acronym;
    private String name;
    private String symbol;

    public Currency(String acronym, String name, String symbol) {
        this.acronym = acronym;
        this.name = name;
        this.symbol = symbol;
    }
}
