package com.greg.entity.holding;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@MappedSuperclass
public abstract class Holding {
    @Id
    private String acronym;
    private String name;
    private HoldingType holdingType;

    public Holding(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    public Holding() {
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
