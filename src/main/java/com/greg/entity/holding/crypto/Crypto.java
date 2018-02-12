package com.greg.entity.holding.crypto;

import com.greg.entity.holding.Holding;
import com.greg.entity.holding.HoldingType;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class Crypto extends Holding{

    public Crypto(String acronym, String name) {
        super(acronym, name);
        this.setHoldingType(HoldingType.CRYPTO);
    }
}
