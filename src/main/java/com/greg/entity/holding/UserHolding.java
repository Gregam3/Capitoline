package com.greg.entity.holding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.utils.JSONUtils;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_HOLDING")
public class UserHolding extends Holding {
    @Transient
    private List<Transaction> transactions;
    @Transient
    private Double totalQuantity;

    public UserHolding(String acronym, String name) {
        super(acronym, name);
    }

    public UserHolding(String acronym, String name, HoldingType holdingType, List<Transaction> transactions) throws IOException {
        super(acronym, name, holdingType);
        this.transactions = transactions;
        this.totalQuantity = getTotalQuantity();
    }

    public UserHolding() {
        super();
    }



    public void setTotalQuantity(Double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Transient
    public double getTotalQuantity() throws IOException {
        double totalQuantity = 0;
        if (transactions != null)
            for (Transaction transaction : transactions)
                totalQuantity += transaction.getQuantity();

        this.totalQuantity = totalQuantity;

        return this.totalQuantity;
    }

    public String asJson() throws JsonProcessingException {
        return JSONUtils.OBJECT_MAPPER.writeValueAsString(this);
    }

    public void addTransaction(Transaction transaction) throws JsonProcessingException {
        transactions.add(transaction);
    }
}
