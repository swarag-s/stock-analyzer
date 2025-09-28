package com.stockanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {
    private String name;
    private List<Stock> stocks;
    private double initialInvestment;

    public Portfolio(String name) {
        this.name = name;
        this.stocks = new ArrayList<>();
        this.initialInvestment = 0;
    }

    public void addStock(Stock stock, int shares, double purchasePrice) {
        stocks.add(stock);
        initialInvestment += shares * purchasePrice;
    }

    public void removeStock(Stock stock) {
        stocks.remove(stock);
    }

    public double getCurrentValue() {
        return stocks.stream().mapToDouble(Stock::getPrice).sum();
    }

    public double getGainLoss() {
        return getCurrentValue() - initialInvestment;
    }

    public double getGainLossPercent() {
        return initialInvestment > 0 ? (getGainLoss() / initialInvestment) * 100 : 0;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Stock> getStocks() { return stocks; }

    public double getInitialInvestment() { return initialInvestment; }
    public void setInitialInvestment(double initialInvestment) {
        this.initialInvestment = initialInvestment;
    }
}