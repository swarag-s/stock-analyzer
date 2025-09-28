package com.stockanalyzer.model;

import java.time.LocalDateTime;

public class Stock {
    private String symbol;
    private String name;
    private double price;
    private double change;
    private double changePercent;
    private long volume;
    private LocalDateTime lastUpdated;

    public Stock(String symbol, String name, double price, double change,
                 double changePercent, long volume) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
        this.volume = volume;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        this.price = price;
        this.lastUpdated = LocalDateTime.now();
    }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }

    @Override
    public String toString() {
        return String.format("%s: $%.2f (%.2f%%)", symbol, price, changePercent);
    }
}