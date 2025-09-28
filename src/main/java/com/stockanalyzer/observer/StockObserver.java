package com.stockanalyzer.observer;

import com.stockanalyzer.model.Stock;

public interface StockObserver {
    void update(Stock stock);
}