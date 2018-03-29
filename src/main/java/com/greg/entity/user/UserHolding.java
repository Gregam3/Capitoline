package com.greg.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.entity.holding.HoldingType;
import com.greg.utils.JSONUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.persistence.*;
import java.io.IOException;
import java.util.*;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_HOLDING")
public class UserHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long holdingId;

    private String acronym;
    private String name;
    private HoldingType holdingType;
    private double acquisitionCost;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "EMAIL")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    @Transient
    private Double totalQuantity;

    public UserHolding(String acronym, String name, HoldingType holdingType, Transaction transaction) throws IOException {
        this.acronym = acronym;
        this.name = name;
        this.holdingType = holdingType;
        this.transactions = new ArrayList<>();
        addTransaction(transaction);
        this.totalQuantity = getTotalQuantity();
        this.acquisitionCost =
                transaction.getQuantity() * transaction.getPrice();
    }

    public UserHolding() {
    }

    @Transient
    @JsonIgnore
    public long getDistanceInDaysToEarliestTransactionDate() {
        Queue<Transaction> transactionStack = new PriorityQueue<>();
        transactionStack.addAll(getTransactions());
        return (new Date().getTime() - transactionStack.poll().getDate().getTime() / DateUtils.MILLIS_PER_DAY);

    }

    public Double getAcquisitionCost() {
        return acquisitionCost;
    }

    public void setAcquisitionCost(Double acquisitionCost) {
        this.acquisitionCost = acquisitionCost;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HoldingType getHoldingType() {
        return holdingType;
    }

    public void setHoldingType(HoldingType holdingType) {
        this.holdingType = holdingType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(long holdingId) {
        this.holdingId = holdingId;
    }

    public List<Transaction> getTransactions() {
        return transactions;
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
        this.acquisitionCost +=
                transaction.getQuantity() * transaction.getPrice();
        transactions.add(transaction);
    }

    public void configureChildren() {
        for (Transaction transaction : transactions) {
            transaction.setUserHolding(this);
        }
    }
}
