package com.stockanalyzer.ui;
import com.stockanalyzer.utils.ChartUtils;


import com.stockanalyzer.model.Portfolio;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.service.PortfolioService;
import com.stockanalyzer.service.AlertService;
import com.stockanalyzer.service.ReportService;
import com.stockanalyzer.api.FinnhubAPIClient;
import com.stockanalyzer.observer.StockAlert;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.jfree.chart.fx.ChartViewer;




public class MainApp extends Application {
    private Portfolio portfolio;
    private PortfolioService portfolioService;
    private AlertService alertService;
    private ReportService reportService;
    private FinnhubAPIClient apiClient;

    private ObservableList<Stock> watchList;
    private ObservableList<StockAlert> alertsList;

    private TableView<Stock> stockTable;
    private TableView<StockAlert> alertsTable;
    private LineChart<String, Number> priceChart;
    private TextArea reportArea;
    private Label portfolioValueLabel;
    private ChartViewer priceChartContainer;


    @Override
    public void start(Stage primaryStage) {
        // Initialize services
        portfolio = new Portfolio("My Portfolio");
        portfolioService = new PortfolioService(portfolio);
        alertService = new AlertService();
        reportService = new ReportService(portfolioService);
        apiClient = new FinnhubAPIClient();

        watchList = FXCollections.observableArrayList();
        alertsList = FXCollections.observableArrayList();

        // Create UI layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Create top menu
        HBox topMenu = createTopMenu();
        root.setTop(topMenu);

        // Create center content with split pane
        SplitPane centerSplit = new SplitPane();
        centerSplit.setDividerPositions(0.5);

        // Left side - Stock table and portfolio info
        VBox leftPanel = createLeftPanel();

        // Right side - Chart and analysis
        VBox rightPanel = createRightPanel();

        centerSplit.getItems().addAll(leftPanel, rightPanel);
        root.setCenter(centerSplit);

        // Load initial data
        loadSampleData();

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Stock Market Analyzer with Finnhub API");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start background price updates
        startPriceUpdates();
    }

    private HBox createTopMenu() {
        HBox menu = new HBox(10);
        menu.setPadding(new Insets(0, 0, 10, 0));

        Label title = new Label("Stock Market Analyzer");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField symbolInput = new TextField();
        symbolInput.setPromptText("Enter stock symbol (e.g., AAPL)");
        symbolInput.setPrefWidth(200);

        Button addButton = new Button("Add to Watchlist");
        addButton.setOnAction(e -> addStockToWatchlist(symbolInput.getText()));

        Button refreshButton = new Button("Refresh Data");
        refreshButton.setOnAction(e -> refreshData());

        Button generateReportButton = new Button("Generate Report");
        generateReportButton.setOnAction(e -> generateReport());

        menu.getChildren().addAll(title, symbolInput, addButton, refreshButton, generateReportButton);

        return menu;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(10);

        // Portfolio value display
        HBox portfolioBox = new HBox(10);
        Label portfolioLabel = new Label("Portfolio Value:");
        portfolioValueLabel = new Label("$0.00");
        portfolioValueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        portfolioBox.getChildren().addAll(portfolioLabel, portfolioValueLabel);

        // Create stock table
        stockTable = new TableView<>();
        stockTable.setItems(watchList);

        TableColumn<Stock, String> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));

        TableColumn<Stock, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Stock, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(tc -> new TableCell<Stock, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));

                    Stock stock = getTableView().getItems().get(getIndex());
                    if (stock.getChange() < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else if (stock.getChange() > 0) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        TableColumn<Stock, Double> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(new PropertyValueFactory<>("change"));
        changeCol.setCellFactory(tc -> new TableCell<Stock, Double>() {
            @Override
            protected void updateItem(Double change, boolean empty) {
                super.updateItem(change, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", change));

                    if (change < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else if (change > 0) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        TableColumn<Stock, Double> changePercentCol = new TableColumn<>("Change %");
        changePercentCol.setCellValueFactory(new PropertyValueFactory<>("changePercent"));
        changePercentCol.setCellFactory(tc -> new TableCell<Stock, Double>() {
            @Override
            protected void updateItem(Double changePercent, boolean empty) {
                super.updateItem(changePercent, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", changePercent));

                    if (changePercent < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else if (changePercent > 0) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        TableColumn<Stock, Long> volumeCol = new TableColumn<>("Volume");
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));

        stockTable.getColumns().addAll(symbolCol, nameCol, priceCol, changeCol, changePercentCol, volumeCol);

        // Add buttons for portfolio management
        HBox buttonBox = new HBox(10);
        Button addToPortfolioBtn = new Button("Add to Portfolio");
        addToPortfolioBtn.setOnAction(e -> addToPortfolio());

        Button removeFromWatchlistBtn = new Button("Remove from Watchlist");
        removeFromWatchlistBtn.setOnAction(e -> removeFromWatchlist());

        Button setAlertBtn = new Button("Set Price Alert");
        setAlertBtn.setOnAction(e -> setPriceAlert());

        buttonBox.getChildren().addAll(addToPortfolioBtn, removeFromWatchlistBtn, setAlertBtn);

        // Alerts table
        Label alertsLabel = new Label("Price Alerts");
        alertsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        alertsTable = new TableView<>();
        alertsTable.setItems(alertsList);

        TableColumn<StockAlert, String> alertSymbolCol = new TableColumn<>("Symbol");
        alertSymbolCol.setCellValueFactory(new PropertyValueFactory<>("stockSymbol"));

        TableColumn<StockAlert, Double> alertPriceCol = new TableColumn<>("Target Price");
        alertPriceCol.setCellValueFactory(new PropertyValueFactory<>("targetPrice"));

        TableColumn<StockAlert, Boolean> alertDirectionCol = new TableColumn<>("Direction");
        alertDirectionCol.setCellValueFactory(new PropertyValueFactory<>("above"));
        alertDirectionCol.setCellFactory(tc -> new TableCell<StockAlert, Boolean>() {
            @Override
            protected void updateItem(Boolean above, boolean empty) {
                super.updateItem(above, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(above ? "Above" : "Below");
                }
            }
        });

        TableColumn<StockAlert, Boolean> alertStatusCol = new TableColumn<>("Status");
        alertStatusCol.setCellValueFactory(new PropertyValueFactory<>("triggered"));
        alertStatusCol.setCellFactory(tc -> new TableCell<StockAlert, Boolean>() {
            @Override
            protected void updateItem(Boolean triggered, boolean empty) {
                super.updateItem(triggered, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(triggered ? "Triggered" : "Active");
                    setStyle(triggered ? "-fx-text-fill: green;" : "-fx-text-fill: orange;");
                }
            }
        });

        alertsTable.getColumns().addAll(alertSymbolCol, alertPriceCol, alertDirectionCol, alertStatusCol);

        leftPanel.getChildren().addAll(portfolioBox, stockTable, buttonBox, alertsLabel, alertsTable);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);

        Label chartTitle = new Label("Price History (30 days)");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Create price chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        priceChart = new LineChart<>(xAxis, yAxis);
        priceChart.setTitle("Stock Price History");
        priceChart.setLegendVisible(true);
        priceChart.setAnimated(false);
        priceChart.setCreateSymbols(false);

        // Analysis controls
        HBox analysisBox = new HBox(10);
        Button analyzeBtn = new Button("Analyze Selected Stock");
        analyzeBtn.setOnAction(e -> analyzeSelectedStock());

        analysisBox.getChildren().addAll(analyzeBtn);

        // Report area
        Label reportLabel = new Label("Analysis Report");
        reportLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(200);

        rightPanel.getChildren().addAll(chartTitle, priceChart, analysisBox, reportLabel, reportArea);

        return rightPanel;
    }

    private void loadSampleData() {
        // Add some sample stocks to watchlist
        new Thread(() -> {
            String[] symbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA"};
            for (String symbol : symbols) {
                try {
                    Stock stock = apiClient.getStockData(symbol);
                    if (stock != null) {
                        Platform.runLater(() -> watchList.add(stock));
                    }
                    // Add delay to avoid rate limiting (60 calls per minute for free tier)
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error loading stock data for " + symbol + ": " + e.getMessage());
                }
            }
        }).start();
    }

    private void addStockToWatchlist(String query) {
        if (query == null || query.trim().isEmpty()) return;
        final String finalQuery = query.trim();

        new Thread(() -> {
            try {
                // Search for a symbol based on the user
                String foundSymbol = apiClient.searchForBestSymbol(finalQuery);
                String symbolToFetch = (foundSymbol != null) ? foundSymbol : finalQuery.toUpperCase();

                // Get the quote data for the determined symbol
                Stock stock = apiClient.getStockData(symbolToFetch);

                if (stock != null) {
                    Platform.runLater(() -> {
                        // Check if stock is already in the list before adding
                        boolean exists = watchList.stream().anyMatch(s -> s.getSymbol().equals(stock.getSymbol()));
                        if (!exists) {
                            watchList.add(stock);
                        }
                    });
                } else {
                    // If getStockData returns null or fails
                    throw new IOException("Could not retrieve quote for symbol: " + symbolToFetch);
                }

            } catch (IOException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could Not Find Stock");
                    alert.setContentText("No data found for '" + finalQuery + "'. Please check the company name or symbol.");
                    alert.showAndWait();
                });
            }
        }).start();
    }


    private void refreshData() {
        new Thread(() -> {
            for (Stock stock : watchList) {
                try {
                    Stock updatedStock = apiClient.getStockData(stock.getSymbol());
                    if (updatedStock != null) {
                        Platform.runLater(() -> {
                            int index = watchList.indexOf(stock);
                            watchList.set(index, updatedStock);

                            // Notify alert service
                            alertService.notifyObservers(updatedStock);
                        });
                    }
                    // Add delay to avoid rate limiting
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error updating stock data for " + stock.getSymbol() + ": " + e.getMessage());
                }
            }

            // Update portfolio value
            updatePortfolioValue();

            // Update alerts table
            Platform.runLater(() -> {
                alertsList.setAll(alertService.getAlerts());
            });
        }).start();
    }

    private void updatePortfolioValue() {
        double totalValue = portfolio.getCurrentValue();
        Platform.runLater(() -> {
            portfolioValueLabel.setText(String.format("$%.2f", totalValue));
        });
    }

    private void addToPortfolio() {
        Stock selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Show dialog to get shares and purchase price
            Dialog<Map<String, Double>> dialog = new Dialog<>();
            dialog.setTitle("Add to Portfolio");
            dialog.setHeaderText("Add " + selected.getSymbol() + " to portfolio");

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField sharesField = new TextField();
            sharesField.setPromptText("Number of shares");

            TextField priceField = new TextField();
            priceField.setPromptText("Purchase price per share");

            grid.add(new Label("Shares:"), 0, 0);
            grid.add(sharesField, 1, 0);
            grid.add(new Label("Purchase Price:"), 0, 1);
            grid.add(priceField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        double shares = Double.parseDouble(sharesField.getText());
                        double price = Double.parseDouble(priceField.getText());

                        Map<String, Double> result = new HashMap<>();
                        result.put("shares", shares);
                        result.put("price", price);
                        return result;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            });

            Optional<Map<String, Double>> result = dialog.showAndWait();
            result.ifPresent(data -> {
                portfolio.addStock(selected, data.get("shares").intValue(), data.get("price"));
                updatePortfolioValue();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Stock Added");
                alert.setHeaderText(null);
                alert.setContentText(selected.getSymbol() + " added to your portfolio.");
                alert.showAndWait();
            });
        }
    }

    private void removeFromWatchlist() {
        Stock selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            watchList.remove(selected);
        }
    }

    private void setPriceAlert() {
        Stock selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Dialog<StockAlert> dialog = new Dialog<>();
            dialog.setTitle("Set Price Alert");
            dialog.setHeaderText("Set alert for " + selected.getSymbol());

            ButtonType setButtonType = new ButtonType("Set", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(setButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));


            TextField priceField = new TextField();
            priceField.setPromptText("Target price");

            ComboBox<String> directionBox = new ComboBox<>();
            directionBox.getItems().addAll("Above", "Below");
            directionBox.setValue("Above");

            grid.add(new Label("Target Price:"), 0, 0);
            grid.add(priceField, 1, 0);
            grid.add(new Label("Alert When:"), 0, 1);

            grid.add(directionBox, 1, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == setButtonType) {
                    try {
                        double targetPrice = Double.parseDouble(priceField.getText());
                        boolean above = directionBox.getValue().equals("Above");
                        return alertService.createPriceAlert(selected.getSymbol(), targetPrice, above);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            });

            Optional<StockAlert> result = dialog.showAndWait();
            result.ifPresent(alert -> {
                alertsList.add(alert);

                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Alert Set");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("Price alert set for " + selected.getSymbol());
                infoAlert.showAndWait();
            });
        }
    }

    /*private void analyzeSelectedStock() {
        Stock selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            new Thread(() -> {
                try {
                    Map<String, Double> historicalData = apiClient.getHistoricalData(selected.getSymbol(), "1mo");

                    Platform.runLater(() -> {
                        priceChart.getData().clear();

                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName(selected.getSymbol());

                        historicalData.forEach((date, price) -> {
                            // Format date to be more readable
                            String formattedDate = LocalDate.parse(date.substring(0, 10)).format(DateTimeFormatter.ofPattern("MMM dd"));
                            series.getData().add(new XYChart.Data<>(formattedDate, price));
                        });

                        priceChart.getData().add(series);
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Data Unavailable");
                        alert.setContentText("Could not retrieve historical data for " + selected.getSymbol() + ": " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        }
    }*/

    private void analyzeSelectedStock() {
        Stock selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            new Thread(() -> {
                try {
                    Map<String, Double> historicalData = apiClient.getHistoricalData(selected.getSymbol(), "1mo");

                    Platform.runLater(() -> {
                        // 1. Clear the existing JavaFX chart data
                        priceChart.getData().clear();

                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName(selected.getSymbol());

                        // 2. Sort data by date to ensure the line chart draws correctly
                        List<String> sortedDates = new ArrayList<>(historicalData.keySet());
                        Collections.sort(sortedDates);

                        for (String date : sortedDates) {
                            double price = historicalData.get(date);

                            // Format date for display on the axis
                            String formattedDate;
                            try {
                                LocalDate localDate = LocalDate.parse(date.substring(0, 10));
                                formattedDate = localDate.format(DateTimeFormatter.ofPattern("MMM dd"));
                            } catch (Exception e) {
                                formattedDate = date; // Fallback to original date
                            }

                            series.getData().add(new XYChart.Data<>(formattedDate, price));
                        }

                        // 3. Add the new series to the JavaFX chart
                        priceChart.getData().add(series);
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Data Unavailable");
                        alert.setContentText("Could not retrieve historical data for " + selected.getSymbol() + ": " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        }
    }

    /*private void analyzeSelectedStock() {
        Stock selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            new Thread(() -> {
                try {
                    Map<String, Double> historicalData = apiClient.getHistoricalData(selected.getSymbol(), "1mo");

                    // Debug: Check if we have data
                    System.out.println("Historical data points for " + selected.getSymbol() + ": " + historicalData.size());
                    historicalData.forEach((date, price) -> {
                        System.out.println(date + ": " + price);
                    });

                    Platform.runLater(() -> {
                        priceChart.getData().clear();

                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName(selected.getSymbol());

                        // Sort data by date to ensure proper ordering
                        List<String> sortedDates = new ArrayList<>(historicalData.keySet());
                        Collections.sort(sortedDates);

                        for (String date : sortedDates) {
                            double price = historicalData.get(date);

                            // Format date for display
                            String formattedDate;
                            try {
                                LocalDate localDate = LocalDate.parse(date.substring(0, 10));
                                formattedDate = localDate.format(DateTimeFormatter.ofPattern("MMM dd"));
                            } catch (Exception e) {
                                formattedDate = date; // Fallback to original date if parsing fails
                            }

                            series.getData().add(new XYChart.Data<>(formattedDate, price));
                        }

                        priceChart.getData().add(series);

                        // Make sure chart is visible
                        priceChart.setVisible(true);
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Data Unavailable");
                        alert.setContentText("Could not retrieve historical data for " + selected.getSymbol() + ": " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        }
    }*/

    private void generateReport() {
        new Thread(() -> {
            var report = reportService.generatePortfolioReport();

            Platform.runLater(() -> {
                reportArea.setText(report.toString());
            });
        }).start();
    }

    private void startPriceUpdates() {
        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // Update every 30 seconds
                    refreshData();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}