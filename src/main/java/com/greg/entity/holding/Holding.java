package com.greg.entity.holding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greg.utils.JSONUtils;

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
    private String acronym;
    private String name;
    private HoldingType holdingType;
    private double acquisitionCost;
    @Transient
    private double quantity;

    public Holding(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    public Holding(String acronym, String name, HoldingType holdingType, double quantity) {
        this.acronym = acronym;
        this.name = name;
        this.holdingType = holdingType;
        this.quantity = quantity;
    }

    public Holding(String acronym, String name, HoldingType holdingType, double quantity, double acquisitionCost) {
        this.acronym = acronym;
        this.name = name;
        this.holdingType = holdingType;
        this.quantity = quantity;
        this.acquisitionCost = acquisitionCost;
    }

    public Holding() {
    }

    public double getAcquisitionCost() {
        return acquisitionCost;
    }

    public void setAcquisitionCost(double acquisitionCost) {
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

    @Transient
    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String asJson() throws JsonProcessingException {
        return JSONUtils.OBJECT_MAPPER.writeValueAsString(this);
    }
}
