package com.stockanalyzer.service;

import com.stockanalyzer.model.AnalysisReport;
import com.stockanalyzer.model.Portfolio;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ReportService {
    private PortfolioService portfolioService;

    public ReportService(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * Generates a comprehensive and easy-to-understand analysis report for the user's portfolio.
     * @return An AnalysisReport object containing the detailed breakdown.
     */
    public AnalysisReport generatePortfolioReport() {
        Portfolio portfolio = portfolioService.getPortfolio();

        // Ensure all stock prices are up-to-date before generating the report.
        portfolioService.updatePortfolioPrices();

        // Calculate the portfolio's performance metrics.
        Map<String, Double> performanceMetrics = portfolioService.getPortfolioPerformance();

        // Calculate risk metrics to understand potential volatility and downturns.
        Map<String, Double> riskMetrics = calculateRiskMetrics(portfolio);

        // Generate a plain-language summary that explains everything clearly.
        String summary = generateEasyToUnderstandSummary(portfolio, performanceMetrics, riskMetrics);

        return new AnalysisReport(portfolio, LocalDate.now(), summary,
                performanceMetrics, riskMetrics);
    }

    /**
     * Calculates simplified risk metrics based on simulated historical data.
     * In a real-world application, this would use actual historical price data.
     *
     * @param portfolio The user's portfolio.
     * @return A map of calculated risk metrics.
     */
    private Map<String, Double> calculateRiskMetrics(Portfolio portfolio) {
        Map<String, Double> riskMetrics = new HashMap<>();

        // Let's simulate the last 30 days of portfolio value to calculate risk.
        List<Double> historicalValues = new ArrayList<>();
        double currentValue = portfolio.getCurrentValue();
        // Generate 30 days of fake historical data fluctuating around the current value.
        for (int i = 0; i < 30; i++) {
            double simulatedValue = currentValue * (1 + (ThreadLocalRandom.current().nextDouble(-0.05, 0.05)));
            historicalValues.add(simulatedValue);
        }
        historicalValues.add(currentValue); // Add the actual current value as the last point.

        // 1. Calculate Volatility (Standard Deviation of daily returns)
        List<Double> dailyReturns = new ArrayList<>();
        for (int i = 1; i < historicalValues.size(); i++) {
            double previousDayValue = historicalValues.get(i - 1);
            double currentDayValue = historicalValues.get(i);
            dailyReturns.add((currentDayValue - previousDayValue) / previousDayValue);
        }
        double meanReturn = dailyReturns.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = dailyReturns.stream().mapToDouble(d -> Math.pow(d - meanReturn, 2)).average().orElse(0.0);
        double volatility = Math.sqrt(variance) * 100; // As a percentage

        // 2. Calculate Max Drawdown
        double peak = 0.0;
        double maxDrawdown = 0.0;
        for (double value : historicalValues) {
            if (value > peak) {
                peak = value;
            }
            double drawdown = (peak - value) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        maxDrawdown *= 100; // As a percentage

        riskMetrics.put("volatility", volatility);
        riskMetrics.put("maxDrawdown", maxDrawdown);
        riskMetrics.put("sharpeRatio", 1.2); // Placeholder, as this requires risk-free rate data.

        return riskMetrics;
    }

    /**
     * Creates a detailed, beginner-friendly summary of the portfolio analysis.
     * It explains each term and provides actionable advice.
     *
     * @param portfolio The user's portfolio.
     * @param performanceMetrics Calculated performance data.
     * @param riskMetrics Calculated risk data.
     * @return A formatted string containing the full, easy-to-read report.
     */
    private String generateEasyToUnderstandSummary(Portfolio portfolio,
                                                   Map<String, Double> performanceMetrics,
                                                   Map<String, Double> riskMetrics) {
        StringBuilder summary = new StringBuilder();
        double gainLossPercent = performanceMetrics.get("gainLossPercent");

        summary.append("simple breakdown of your").append(portfolio.getName()).append("investment portfolio:\n");

        summary.append("Your Portfolio's Performance\n");
        summary.append(String.format("How Much You Put In (Initial Investment): $%.2f%n", performanceMetrics.get("initialInvestment")));
        summary.append(String.format("What It's Worth Today (Current Value):   $%.2f%n", performanceMetrics.get("currentValue")));
        summary.append(String.format("Your Total Profit or Loss:                $%.2f (that's a %.2f%% change)%n",
                performanceMetrics.get("gainLoss"), gainLossPercent));

        summary.append("\nUnderstanding Your Portfolio's Risk\n");
        summary.append(String.format("Volatility: %.2f%%%n", riskMetrics.get("volatility")));
        summary.append("This number shows how much your portfolio's value fluctuates—higher means more risk.\n\n\n");

        summary.append(String.format("Max Drawdown: %.2f%%%n", riskMetrics.get("maxDrawdown")));
        summary.append("This shows your portfolio’s largest drop from peak to bottom, indicating its worst-case loss.\n\n\n");

        summary.append(String.format("Sharpe Ratio: %.2f (Estimate)%n", riskMetrics.get("sharpeRatio")));
        summary.append("This metric shows how much return you earn for the risk taken—higher is better.\n\n");


        summary.append("\nOur Suggestion\n");
        if (gainLossPercent > 15) {
            summary.append("Excellent work! Your portfolio is performing well. Consider securing some profits by selling a small part of your top investments to protect against market shifts.\n");
        } else if (gainLossPercent < -10) {
            summary.append("Your investments are down a bit—don’t worry, it’s normal. Check if you still trust them long-term, and if yes, just stay patient.\n");
        } else {
            summary.append("Your portfolio is doing fine and steady. Keep an eye on it and stay with your long-term plan.\n");
        }
        summary.append("\n\nDisclaimer!!!!!!!\nThis is a simplified analysis and not professional financial advice.");

        return summary.toString();
    }
}
