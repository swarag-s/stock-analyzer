package com.stockanalyzer.observer;

import com.stockanalyzer.model.Stock;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class StockAlert implements StockObserver {
    private String stockSymbol;
    private double targetPrice;
    private boolean above;
    private boolean triggered;

    public StockAlert(String stockSymbol, double targetPrice, boolean above) {
        this.stockSymbol = stockSymbol;
        this.targetPrice = targetPrice;
        this.above = above;
        this.triggered = false;
    }

    @Override
    public void update(Stock stock) {
        if (stock.getSymbol().equals(stockSymbol) && !triggered) {
            if ((above && stock.getPrice() >= targetPrice) ||
                    (!above && stock.getPrice() <= targetPrice)) {
                triggered = true;
                showAlert(stock);
            }
        }
    }

    private void showAlert(Stock stock) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Price Alert");
            alert.setHeaderText("Price target reached for " + stock.getSymbol());
            alert.setContentText(String.format("%s has reached your target price of $%.2f. Current price: $%.2f",
                    stock.getSymbol(), targetPrice, stock.getPrice()));
            alert.showAndWait();
        });
    }

    // Getters
    public String getStockSymbol() { return stockSymbol; }
    public double getTargetPrice() { return targetPrice; }
    public boolean isAbove() { return above; }
    public boolean isTriggered() { return triggered; }

    public void reset() { triggered = false; }
}