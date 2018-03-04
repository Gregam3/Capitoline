package com.greg.entity.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.entity.holding.Holding;
import com.greg.entity.holding.HoldingType;
import com.greg.utils.JSONUtils;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_HOLDING")
public class UserHolding extends Holding {

    @EmbeddedId
    private HoldingId holdingId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "PT_HOLDING_TRANSACTION",
            joinColumns = {@JoinColumn(name = "ACRONYM")},
            inverseJoinColumns = {@JoinColumn(name = "TRANSACTION_NUMBER")})
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
