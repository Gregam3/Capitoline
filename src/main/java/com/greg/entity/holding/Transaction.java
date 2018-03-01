package com.greg.entity.holding;

import java.util.Date;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class Transaction {
    private double quantity;
    private double price;
    private Date date;

    public Transaction(double quantity, double price, Date date) {
        this.quantity = quantity;
        this.price = price;
        this.date = date;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
