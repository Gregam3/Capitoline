package com.greg.entity.holding.stock;


import com.greg.entity.holding.Holding;
import com.greg.entity.holding.HoldingType;
import com.greg.entity.holding.UserHolding;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_NASDAQ_NYSE")
public class Stock extends Holding {
    public Stock() {
        super();
    }

    public Stock(String acronym, String name) {
        super(acronym, name);
        setHoldingType(HoldingType.STOCK);
    }

}
