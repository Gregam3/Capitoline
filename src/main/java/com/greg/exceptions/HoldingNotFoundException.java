package com.greg.exceptions;

import com.greg.entity.holding.HoldingType;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class HoldingNotFoundException extends Exception {
    public HoldingNotFoundException(HoldingType holdingType) {
        super(holdingType.toString() + "Could not be found");
    }
}
