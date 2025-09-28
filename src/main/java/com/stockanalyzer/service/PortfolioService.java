package com.stockanalyzer.service;

import com.stockanalyzer.model.Portfolio;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.api.FinnhubAPIClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PortfolioService {
    private Portfolio portfolio;
    private FinnhubAPIClient apiClient;

    public PortfolioService(Portfolio portfolio) {
        this.portfolio = portfolio;
        this.apiClient = new FinnhubAPIClient();
    }

    public void updatePortfolioPrices() {
        for (Stock stock : portfolio.getStocks()) {
            try {
                Stock updatedStock = apiClient.getStockData(stock.getSymbol());
                stock.setPrice(updatedStock.getPrice());
                stock.setChange(updatedStock.getChange());
                stock.setChangePercent(updatedStock.getChangePercent());
                stock.setVolume(updatedStock.getVolume());
            } catch (IOException e) {
                System.err.println("Error updating price for " + stock.getSymbol() + ": " + e.getMessage());
            }
        }
    }

    public Map<String, Double> getPortfolioPerformance() {
        Map<String, Double> performance = new HashMap<>();

        performance.put("currentValue", portfolio.getCurrentValue());
        performance.put("initialInvestment", portfolio.getInitialInvestment());
        performance.put("gainLoss", portfolio.getGainLoss());
        performance.put("gainLossPercent", portfolio.getGainLossPercent());

        return performance;
    }

    // Getters and setters
    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
}