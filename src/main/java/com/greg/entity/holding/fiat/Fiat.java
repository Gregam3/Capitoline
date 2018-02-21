package com.greg.entity.holding.fiat;

import com.greg.entity.holding.Holding;
import com.greg.entity.holding.HoldingType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_CURRENCY")
public class Fiat extends Holding {

    private String symbol;

    public Fiat() {
        super();
        this.setHoldingType(HoldingType.FIAT);
    }

    public Fiat(String acronym, String name) {
        super(acronym, name);
    }

    public Fiat(String acronym, String name, String symbol) {
        super(acronym, name);
        this.symbol = symbol;
        this.setHoldingType(HoldingType.FIAT);
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
