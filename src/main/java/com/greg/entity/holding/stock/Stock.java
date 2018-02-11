package com.greg.entity.holding.stock;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@Entity
@Table(name = "PT_NASDAQ_NYSE")
public class Stock {
    private String acronym;
    private String name;

    public Stock() {
    }

    public Stock(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    @Id
    @Column(name = "ACRONYM")
    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
