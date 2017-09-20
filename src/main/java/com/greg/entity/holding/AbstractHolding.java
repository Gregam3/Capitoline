package com.greg.entity.holding;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class AbstractHolding {
    private String name;
    private String value;
    private boolean exchangeable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isExchangeable() {
        return exchangeable;
    }

    public void setExchangeable(boolean exchangeable) {
        this.exchangeable = exchangeable;
    }
}
