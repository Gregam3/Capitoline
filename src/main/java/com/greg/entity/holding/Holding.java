package com.greg.entity.holding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.utils.JSONUtils;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@MappedSuperclass
public class Holding {
    @Id
    private String acronym;
    private String name;
    private HoldingType holdingType;
    @Transient
    private String transactionsJson;

    @Transient
    private List<Transaction> transactions;
    @Transient
    private Double totalQuantity;

    public Holding(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    public Holding(String acronym, String name, HoldingType holdingType, Transaction transaction) throws JsonProcessingException {
        this.acronym = acronym;
        this.name = name;
        this.holdingType = holdingType;
        if (transactions == null) transactions = new ArrayList<>();
        this.transactions.add(transaction);
        this.totalQuantity = getTotalQuantity();
        this.transactionsJson =
                JSONUtils.OBJECT_MAPPER.writeValueAsString((transactions != null) ? transactions : "[]");
    }

    public Holding() {
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String getTransactionsJson() {
        return transactionsJson;
    }

    public void setTransactionsJson(String transactionsJson) {
        this.transactionsJson = transactionsJson;
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

    @Transient
    public double getTotalQuantity() {
        double totalQuantity = 0;
        if (transactions != null)
            for (Transaction transaction : transactions)
                totalQuantity += transaction.getQuantity();

        this.totalQuantity = totalQuantity;

        return this.totalQuantity;
    }

    public void setTotalQuantity(double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String asJson() throws JsonProcessingException {
        return JSONUtils.OBJECT_MAPPER.writeValueAsString(this);
    }

    public void setAcquisitionPrice(double price) {
        transactions.get(transactions.size() - 1).setPrice(price);
    }
}
