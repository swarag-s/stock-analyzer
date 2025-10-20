package com.stockanalyzer.model;

import java.time.LocalDate;
import java.util.Map;

public class AnalysisReport {
    private Portfolio portfolio;
    private LocalDate date;
    private String summary;
    private Map<String, Double> performanceMetrics;
    private Map<String, Double> riskMetrics;
    private String recommendation; // Added for the recommendation line

    public AnalysisReport(Portfolio portfolio, LocalDate date, String summary,
                          Map<String, Double> performanceMetrics,
                          Map<String, Double> riskMetrics) {
        this.portfolio = portfolio;
        this.date = date;
        this.summary = summary;
        this.performanceMetrics = performanceMetrics;
        this.riskMetrics = riskMetrics;
        // Default recommendation, can be changed later
        this.recommendation = "Portfolio is performing within expected ranges.";
    }

    // Getters
    public Portfolio getPortfolio() { return portfolio; }
    public LocalDate getDate() { return date; }
    public String getSummary() { return summary; }
    public Map<String, Double> getPerformanceMetrics() { return performanceMetrics; }
    public Map<String, Double> getRiskMetrics() { return riskMetrics; }
    public String getRecommendation() { return recommendation; }


    public void setSummary(String summary) { this.summary = summary; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }


    // --- THIS IS THE FIXED METHOD ---
    @Override
    public String toString() {
        // Use a StringBuilder for efficient string construction
        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append(String.format("Analysis Report for %s (%s)%n", portfolio.getName(), date));
        reportBuilder.append("--------------------------------------------------\n");
        reportBuilder.append(summary).append("\n\n");

        // Add Performance Metrics to the report
        reportBuilder.append("Performance:\n");
        if (performanceMetrics != null && !performanceMetrics.isEmpty()) {
            double gainLoss = performanceMetrics.getOrDefault("gainLoss", 0.0);
            double gainLossPercent = performanceMetrics.getOrDefault("gainLossPercent", 0.0);

            reportBuilder.append(String.format("  Current Value: $%.2f%n", performanceMetrics.getOrDefault("currentValue", 0.0)));
            reportBuilder.append(String.format("  Initial Investment: $%.2f%n", performanceMetrics.getOrDefault("initialInvestment", 0.0)));
            reportBuilder.append(String.format("  Gain/Loss: $%.2f (%.2f%%)%n", gainLoss, gainLossPercent));

        } else {
            reportBuilder.append("  No performance data available.\n");
        }
        reportBuilder.append("\n");

        // Add Risk Metrics to the report
        reportBuilder.append("Risk Metrics:\n");
        if (riskMetrics != null && !riskMetrics.isEmpty()) {
            reportBuilder.append(String.format("  Volatility: %.2f%n", riskMetrics.getOrDefault("volatility", 0.0)));
            reportBuilder.append(String.format("  Max Drawdown: %.2f%%%n", riskMetrics.getOrDefault("maxDrawdown", 0.0)));
            reportBuilder.append(String.format("  Sharpe Ratio: %.2f%n", riskMetrics.getOrDefault("sharpeRatio", 0.0)));
        } else {
            reportBuilder.append("  No risk metrics available.\n");
        }
        reportBuilder.append("\n");

        // Add Recommendation
        reportBuilder.append("Recommendation: ").append(recommendation).append("\n");

        return reportBuilder.toString();
    }
}

