package com.greg.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Date;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_TRANSACTION")
public class Transaction implements Comparable<Transaction> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long transactionId;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "HOLDING_ID", nullable = false)
    private UserHolding userHolding;
    private double quantity;
    private double price;
    private Date dateBought;

    public Transaction(double quantity, double price, Date dateBought) {
        this.quantity = quantity;
        this.price = price;
        this.dateBought = dateBought;
    }

    public Transaction() {
    }

    public Transaction(double quantity) {
        this.quantity = quantity;
        this.dateBought = new Date(new java.util.Date().getTime());
    }

    public UserHolding getUserHolding() {
        return userHolding;
    }

    public void setUserHolding(UserHolding UserHolding) {
        this.userHolding = UserHolding;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
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
        return dateBought;
    }

    public void setDate(Date dateBought) {
        this.dateBought = dateBought;
    }

    @Override
    public int compareTo(Transaction transaction) {
        return Long.compare(getDate().getTime(), transaction.getDate().getTime());
    }
}
