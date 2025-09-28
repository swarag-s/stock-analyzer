package com.stockanalyzer.model;

import java.time.LocalDate;
import java.util.Map;

public class AnalysisReport {
    private Portfolio portfolio;
    private LocalDate date;
    private String summary;
    private Map<String, Double> performanceMetrics;
    private Map<String, Double> riskMetrics;

    public AnalysisReport(Portfolio portfolio, LocalDate date, String summary,
                          Map<String, Double> performanceMetrics,
                          Map<String, Double> riskMetrics) {
        this.portfolio = portfolio;
        this.date = date;
        this.summary = summary;
        this.performanceMetrics = performanceMetrics;
        this.riskMetrics = riskMetrics;
    }

    // Getters
    public Portfolio getPortfolio() { return portfolio; }
    public LocalDate getDate() { return date; }
    public String getSummary() { return summary; }
    public Map<String, Double> getPerformanceMetrics() { return performanceMetrics; }
    public Map<String, Double> getRiskMetrics() { return riskMetrics; }

    public void setSummary(String summary) { this.summary = summary; }

    @Override
    public String toString() {
        return String.format("Analysis Report for %s (%s)%n%s",
                portfolio.getName(), date, summary);
    }
}