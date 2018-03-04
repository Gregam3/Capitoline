package com.greg.entity.holding;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

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

    public Holding() {
    }

    public Holding(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    public Holding(String acronym, String name, HoldingType holdingType) {
        this.acronym = acronym;
        this.name = name;
        this.holdingType = holdingType;
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
}
