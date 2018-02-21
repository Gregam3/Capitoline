package com.greg.exceptions;

import com.greg.entity.holding.HoldingType;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class InvalidHoldingException extends Exception {
    public InvalidHoldingException(HoldingType holdingType) {
        super(holdingType.toString() + "Could not be found");
    }
    public InvalidHoldingException(String message) {
        super(message);
    }
}
