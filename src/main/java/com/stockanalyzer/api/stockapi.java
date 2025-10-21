package com.stockanalyzer.api;

import com.stockanalyzer.model.Stock;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class stockapi {
    private static final String BASE_URL = "https://api.twelvedata.com";
    private static final String API_KEY = "47536e857db343cc8f7e8cc131a49b7a";

    private OkHttpClient client;

    public stockapi() {
        this.client = new OkHttpClient();
    }

    /**
     * Searches for the best symbol match for a given query (e.g., "Apple" -> "AAPL").
     * @param query The company name or symbol to search for.
     * @return The best matching symbol as a String, or null if not found.
     * @throws IOException If the API call fails.
     */
    public String searchForBestSymbol(String query) throws IOException {
        String url = BASE_URL + "/symbol_search?symbol=" + query + "&apikey=" + API_KEY;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // Don't throw an error, just return null as search might fail legitimately
                return null;
            }

            JSONObject json = new JSONObject(response.body().string());
            if (json.has("data")) {
                JSONArray data = json.getJSONArray("data");
                if (data.length() > 0) {
                    // Return the symbol of the first and most relevant result
                    return data.getJSONObject(0).getString("symbol");
                }
            }
        }
        return null; // Return null if no symbol was found
    }

    /**
     * Fetches the current quote for a specific stock symbol.
     */
    public Stock getStockData(String symbol) throws IOException {
        String url = BASE_URL + "/quote?symbol=" + symbol + "&apikey=" + API_KEY;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " for symbol: " + symbol);
            }

            JSONObject json = new JSONObject(response.body().string());
            if (json.has("code") && json.getInt("code") == 404) {
                throw new IOException("Invalid response for symbol: " + symbol);
            }

            double price = json.optDouble("close", 0.0);
            double change = json.optDouble("change", 0.0);
            double changePercent = json.optDouble("percent_change", 0.0);
            long volume = (long) json.optDouble("volume", 0);
            String companyName = json.optString("name", symbol);

            return new Stock(symbol, companyName, price, change, changePercent, volume);
        }
    }

    //Fetches historical data for the chart
    public Map<String, Double> getHistoricalData(String symbol, String range) throws IOException {
        String interval = "1day";
        String url = BASE_URL + "/time_series?symbol=" + symbol
                + "&interval=" + interval
                + "&outputsize=30" //the days of data
                + "&apikey=" + API_KEY;

        Request request = new Request.Builder().url(url).build();
        Map<String, Double> historicalData = new LinkedHashMap<>();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " for historical data: " + symbol);
            }

            JSONObject json = new JSONObject(response.body().string());
            if (json.getString("status").equals("ok")) {
                JSONArray values = json.getJSONArray("values");
                for (int i = 0; i < values.length(); i++) {
                    JSONObject dayData = values.getJSONObject(i);
                    String date = dayData.getString("datetime");
                    double close = Double.parseDouble(dayData.getString("close"));
                    historicalData.put(date, close);
                }
            }
            return historicalData;
        }
    }
}

