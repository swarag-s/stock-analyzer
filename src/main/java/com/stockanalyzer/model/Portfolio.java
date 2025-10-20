package com.stockanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {
    private String name;
    // We need a new class to hold the stock and the number of shares
    public static class Holding {
        private Stock stock;
        private int shares;
        private double purchasePrice;

        public Holding(Stock stock, int shares, double purchasePrice) {
            this.stock = stock;
            this.shares = shares;
            this.purchasePrice = purchasePrice;
        }

        public Stock getStock() { return stock; }
        public int getShares() { return shares; }
        public double getPurchasePrice() { return purchasePrice; }

        public double getCurrentValue() {
            return stock.getPrice() * shares;
        }

        public double getInitialCost() {
            return purchasePrice * shares;
        }
    }

    private List<Holding> holdings; // Changed from List<Stock>
    private double initialInvestment; // This will now be calculated

    public Portfolio(String name) {
        this.name = name;
        this.holdings = new ArrayList<>();
        this.initialInvestment = 0; // Will be set in addStock
    }

    public void addStock(Stock stock, int shares, double purchasePrice) {
        // Add a new holding to our list
        Holding newHolding = new Holding(stock, shares, purchasePrice);
        holdings.add(newHolding);

        // Update the initial investment
        this.initialInvestment += newHolding.getInitialCost();
    }

    public void removeStock(Stock stock) {
        // Find and remove the holding associated with this stock
        holdings.removeIf(holding -> holding.getStock().getSymbol().equals(stock.getSymbol()));

        // Recalculate investment (this is simpler than tracking removals)
        recalculateInitialInvestment();
    }

    private void recalculateInitialInvestment() {
        this.initialInvestment = 0;
        for (Holding h : holdings) {
            this.initialInvestment += h.getInitialCost();
        }
    }

    public double getCurrentValue() {
        // Sum the value of all holdings (price * shares)
        double totalValue = 0;
        for (Holding h : holdings) {
            totalValue += h.getCurrentValue();
        }
        return totalValue;
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

    // Updated getter to return the list of holdings
    public List<Holding> getHoldings() { return holdings; }

    public double getInitialInvestment() { return initialInvestment; }
    public void setInitialInvestment(double initialInvestment) {
        this.initialInvestment = initialInvestment;
    }
}