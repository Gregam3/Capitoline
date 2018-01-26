package com.greg.entity.holding.crypto;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class Crypto {
    private String name;
    private double price;

    public Crypto(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
