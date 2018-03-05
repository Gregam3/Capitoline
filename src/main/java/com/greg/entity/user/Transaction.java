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
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long transactionId;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "holding_id", nullable = false)
    private UserHolding UserHolding;
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

    public UserHolding getUserHolding() {
        return UserHolding;
    }

    public void setUserHolding(UserHolding UserHolding) {
        this.UserHolding = UserHolding;
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
}
