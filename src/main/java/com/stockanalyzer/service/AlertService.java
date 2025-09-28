package com.stockanalyzer.service;

import com.stockanalyzer.observer.StockObserver;
import com.stockanalyzer.observer.StockAlert;
import com.stockanalyzer.model.Stock;

import java.util.ArrayList;
import java.util.List;

public class AlertService {
    private List<StockObserver> observers;

    public AlertService() {
        this.observers = new ArrayList<>();
    }

    public void addObserver(StockObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Stock stock) {
        for (StockObserver observer : observers) {
            observer.update(stock);
        }
    }

    public StockAlert createPriceAlert(String symbol, double targetPrice, boolean above) {
        StockAlert alert = new StockAlert(symbol, targetPrice, above);
        addObserver(alert);
        return alert;
    }

    public List<StockAlert> getAlerts() {
        List<StockAlert> alerts = new ArrayList<>();
        for (StockObserver observer : observers) {
            if (observer instanceof StockAlert) {
                alerts.add((StockAlert) observer);
            }
        }
        return alerts;
    }
}