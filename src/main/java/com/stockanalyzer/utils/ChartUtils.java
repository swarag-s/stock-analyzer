package com.stockanalyzer.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ChartUtils {

    public static JFreeChart createStockChart(String symbol, Map<String, Double> historicalData) {
        // Create time series
        TimeSeries series = new TimeSeries(symbol + " Price History");

        // Add data to series
        historicalData.forEach((dateStr, price) -> {
            try {
                LocalDate localDate = LocalDate.parse(dateStr.substring(0, 10));
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                series.add(new Day(date), price);
            } catch (Exception e) {
                System.err.println("Error parsing date: " + dateStr);
            }
        });

        // Create dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        // Create chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                symbol + " Price History",  // title
                "Date",                     // x-axis label
                "Price (USD)",              // y-axis label
                dataset,                    // data
                true,                       // include legend
                true,                       // generate tooltips
                false                       // generate URLs
        );

        // Apply Chinese font support
        applyChineseFontSupport(chart);

        // Customize appearance
        customizeChartAppearance(chart);

        return chart;
    }

    private static void applyChineseFontSupport(JFreeChart chart) {
        // Set fonts that support Chinese characters
        Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 16);

        // Apply fonts to all chart elements
        chart.getTitle().setFont(titleFont);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getDomainAxis().setLabelFont(chineseFont);
        plot.getDomainAxis().setTickLabelFont(chineseFont);
        plot.getRangeAxis().setLabelFont(chineseFont);
        plot.getRangeAxis().setTickLabelFont(chineseFont);

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(chineseFont);
        }

        // Format date axis for better readability
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM dd", Locale.getDefault()));
    }

    private static void customizeChartAppearance(JFreeChart chart) {
        XYPlot plot = (XYPlot) chart.getPlot();

        // Set background colors
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Customize renderer
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 102, 204)); // Blue line
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, false); // Hide data points

        // Set anti-aliasing for smoother lines
        chart.setAntiAlias(true);
    }

    public static void applyChartTheme(JFreeChart chart) {
        // This method can be used to apply a comprehensive theme
        // For Chinese language support, we use the method above
    }

    // Alternative font selection that works across platforms
    private static Font getChineseFont() {
        String[] fontNames = {
                "Microsoft YaHei", "SimSun", "SimHei", "KaiTi",
                "Microsoft JhengHei", "Noto Sans CJK SC", "WenQuanYi Micro Hei"
        };

        for (String fontName : fontNames) {
            Font font = new Font(fontName, Font.PLAIN, 12);
            if (font.getFamily().equals(fontName)) {
                return font;
            }
        }

        // Fallback to default sans-serif
        return new Font("SansSerif", Font.PLAIN, 12);
    }
}