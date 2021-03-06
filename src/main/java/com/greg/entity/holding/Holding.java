package com.greg.entity.holding;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
@MappedSuperclass
public class Holding {
    @Id
    protected String acronym;
    private String name;

    @Transient
    private HoldingType holdingType;

    public Holding() {
        holdingType = HoldingType.FIAT;
    }

    public Holding(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
        holdingType = HoldingType.FIAT;
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
