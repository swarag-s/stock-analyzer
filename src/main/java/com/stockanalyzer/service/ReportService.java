package com.stockanalyzer.service;

import com.stockanalyzer.model.AnalysisReport;
import com.stockanalyzer.model.Portfolio;
import com.stockanalyzer.model.Stock;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReportService {
    private PortfolioService portfolioService;

    public ReportService(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    public AnalysisReport generatePortfolioReport() {
        Portfolio portfolio = portfolioService.getPortfolio();

        // Update prices before generating report
        portfolioService.updatePortfolioPrices();

        // Calculate performance metrics
        Map<String, Double> performanceMetrics = portfolioService.getPortfolioPerformance();

        // Calculate risk metrics (simplified)
        Map<String, Double> riskMetrics = calculateRiskMetrics(portfolio);

        // Generate summary
        String summary = generateSummary(portfolio, performanceMetrics, riskMetrics);

        return new AnalysisReport(portfolio, LocalDate.now(), summary,
                performanceMetrics, riskMetrics);
    }

    private Map<String, Double> calculateRiskMetrics(Portfolio portfolio) {
        Map<String, Double> riskMetrics = new HashMap<>();

        // Simplified risk calculation
        double totalValue = portfolio.getCurrentValue();
        double maxDrawdown = 0.0;
        double volatility = 0.0;

        // For a real implementation, we would need historical data
        riskMetrics.put("maxDrawdown", maxDrawdown);
        riskMetrics.put("volatility", volatility);
        riskMetrics.put("sharpeRatio", 1.2); // Placeholder

        return riskMetrics;
    }

    private String generateSummary(Portfolio portfolio,
                                   Map<String, Double> performanceMetrics,
                                   Map<String, Double> riskMetrics) {
        StringBuilder summary = new StringBuilder();

        summary.append("Portfolio Analysis Summary for ").append(portfolio.getName()).append("\n\n");
        summary.append("Performance:\n");
        summary.append(String.format("  Current Value: $%.2f%n", performanceMetrics.get("currentValue")));
        summary.append(String.format("  Initial Investment: $%.2f%n", performanceMetrics.get("initialInvestment")));
        summary.append(String.format("  Gain/Loss: $%.2f (%.2f%%)%n",
                performanceMetrics.get("gainLoss"), performanceMetrics.get("gainLossPercent")));

        summary.append("\nRisk Metrics:\n");
        summary.append(String.format("  Volatility: %.2f%n", riskMetrics.get("volatility")));
        summary.append(String.format("  Max Drawdown: %.2f%%%n", riskMetrics.get("maxDrawdown")));
        summary.append(String.format("  Sharpe Ratio: %.2f%n", riskMetrics.get("sharpeRatio")));

        summary.append("\nRecommendation: ");
        if (performanceMetrics.get("gainLossPercent") > 10) {
            summary.append("Consider taking some profits.");
        } else if (performanceMetrics.get("gainLossPercent") < -5) {
            summary.append("Consider reviewing your positions.");
        } else {
            summary.append("Portfolio is performing within expected ranges.");
        }

        return summary.toString();
    }
}