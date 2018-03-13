package com.greg.entity;

import java.util.Date;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */
public class GraphHoldingData implements Comparable<GraphHoldingData> {
    private Date time;
    private double value;

    public GraphHoldingData(Date time, double value) {
        this.time = time;
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public int compareTo(GraphHoldingData graphHoldingData) {
        return Long.compare(getTime().getTime(), graphHoldingData.getTime().getTime());
    }

}
