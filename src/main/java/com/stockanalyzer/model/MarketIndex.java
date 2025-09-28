package com.stockanalyzer.model;

import java.util.List;

public class MarketIndex {
    private String symbol;
    private String name;
    private double value;
    private double change;
    private double changePercent;
    private List<Stock> components;

    public MarketIndex(String symbol, String name, double value,
                       double change, double changePercent) {
        this.symbol = symbol;
        this.name = name;
        this.value = value;
        this.change = change;
        this.changePercent = changePercent;
    }

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

    public List<Stock> getComponents() { return components; }
    public void setComponents(List<Stock> components) { this.components = components; }

    @Override
    public String toString() {
        return String.format("%s: %.2f (%.2f%%)", name, value, changePercent);
    }
}