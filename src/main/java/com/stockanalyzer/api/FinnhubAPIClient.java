package com.stockanalyzer.api;

import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.MarketIndex;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;


public class FinnhubAPIClient {
    private static final String BASE_URL = "https://finnhub.io/api/v1";
    private static final String API_KEY = "d35dt89r01qhqkb206m0d35dt89r01qhqkb206mg"; // your API key

    private OkHttpClient client;

    public FinnhubAPIClient() {
        this.client = new OkHttpClient();
    }

    public Stock getStockData(String symbol) throws IOException {
        String url = BASE_URL + "/quote?symbol=" + symbol + "&token=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " for symbol: " + symbol);
            }

            String responseData = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseData);

            // Get company name from another endpoint
            String companyName = getCompanyName(symbol);

            double price = jsonResponse.getDouble("c");
            double change = jsonResponse.getDouble("d");
            double changePercent = jsonResponse.getDouble("dp");
            long volume = jsonResponse.optLong("v", 0);

            if (jsonResponse.has("v") && !jsonResponse.isNull("v")) {
                volume = jsonResponse.getLong("v");
            }

            return new Stock(symbol, companyName, price, change, changePercent, volume);
        }
    }

    private String getCompanyName(String symbol) throws IOException {
        String url = BASE_URL + "/stock/profile2?symbol=" + symbol + "&token=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return symbol; // Return symbol as fallback name
            }

            String responseData = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseData);

            return jsonResponse.optString("name", symbol);
        }
    }

    public List<MarketIndex> getMarketIndices() throws IOException {
        List<MarketIndex> indices = new ArrayList<>();

        // Major indices
        String[][] indexData = {
                {"^GSPC", "S&P 500"},
                {"^IXIC", "NASDAQ Composite"},
                {"^DJI", "Dow Jones Industrial Average"}
        };

        for (String[] index : indexData) {
            String symbol = index[0];
            String name = index[1];

            try {
                Stock indexStock = getStockData(symbol);
                indices.add(new MarketIndex(symbol, name, indexStock.getPrice(),
                        indexStock.getChange(), indexStock.getChangePercent()));
            } catch (IOException e) {
                System.err.println("Error fetching index data for " + symbol + ": " + e.getMessage());
            }
        }

        return indices;
    }

    /*public Map<String, Double> getHistoricalData(String symbol, String range) throws IOException {
        waitIfNeeded();

        // Determine timeframe based on range
        String resolution = "D"; // Daily
        long to = System.currentTimeMillis() / 1000;
        long from = to;

        switch (range) {
            case "1mo":
                from = to - (30 * 24 * 60 * 60); // 30 days
                break;
            case "3mo":
                from = to - (90 * 24 * 60 * 60); // 90 days
                break;
            case "6mo":
                from = to - (180 * 24 * 60 * 60); // 180 days
                break;
            case "1y":
                from = to - (365 * 24 * 60 * 60); // 365 days
                break;
            default:
                from = to - (30 * 24 * 60 * 60); // Default to 30 days
        }

        String url = BASE_URL + "/stock/candle?symbol=" + symbol +
                "&resolution=" + resolution + "&from=" + from + "&to=" + to +
                "&token=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        Map<String, Double> historicalData = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " for historical data: " + symbol);
            }

            String responseData = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseData);

            if (jsonResponse.getString("s").equals("ok")) {
                JSONArray timestamps = jsonResponse.getJSONArray("t");
                JSONArray closes = jsonResponse.getJSONArray("c");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (int i = 0; i < timestamps.length(); i++) {
                    long timestamp = timestamps.getLong(i);
                    double close = closes.getDouble(i);

                    String date = dateFormat.format(new Date(timestamp * 1000));
                    historicalData.put(date, close);
                }
            }

            return historicalData;
        }
    }*/

    public Map<String, Double> getHistoricalData(String symbol, String range) throws IOException {
        String url = BASE_URL + "/stock/candle?symbol=" + symbol +
                "&resolution=D&count=30&token=" + API_KEY;

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            JSONObject json = new JSONObject(response.body().string());

            Map<String, Double> historicalData = new LinkedHashMap<>();

            if (json.has("t") && json.has("c")) {
                JSONArray timestamps = json.getJSONArray("t");
                JSONArray closes = json.getJSONArray("c");

                for (int i = 0; i < timestamps.length(); i++) {
                    long epoch = timestamps.getLong(i) * 1000; // convert to ms
                    LocalDate date = Instant.ofEpochMilli(epoch)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    double close = closes.getDouble(i);
                    historicalData.put(date.toString(), close);
                }
            }

            return historicalData;
        }
    }


    // Helper method for API rate limiting
    private void waitIfNeeded() {
        try {
            Thread.sleep(1000); // wait 1 second between requests
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
