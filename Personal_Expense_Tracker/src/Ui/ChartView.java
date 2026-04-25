package Ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import Model.Category;
import Service.ExpenseService;

public class ChartView extends Stage {
    private BarChart<String, Number> chart;
    private ChoiceBox<String> intervalChoice;
    private ExpenseService service;

    /**
     * Create a new chart window
     * @param service the ExpenseService to get data from
     */
    public ChartView(ExpenseService service) {
        this.service = service;
        initializeUI();
    }

    private void initializeUI() {
        // Create chart axes
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Period");
        yAxis.setLabel("Amount");
        
        // Create bar chart
        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Expense Analysis");
        chart.setPrefHeight(500);

        // Create interval choice box
        intervalChoice = new ChoiceBox<>(FXCollections.observableArrayList("Daily", "Weekly", "Monthly", "Yearly"));
        intervalChoice.setValue("Daily");
        intervalChoice.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldV, newV) -> updateChart(newV)
        );

        // Create layout
        VBox root = new VBox(8, intervalChoice, chart);
        root.setPadding(new Insets(10));

        // Create scene and set up stage
        Scene scene = new Scene(root, 800, 600);
        this.setTitle("Expense Charts");
        this.setScene(scene);
        this.setOnShown(e -> updateChart("Daily"));
    }

    /**
     * Update the chart based on selected interval
     */
    private void updateChart(String interval) {
        if (interval == null) interval = "Daily";
        chart.getData().clear();
        
        Map<String, Map<Category, Integer>> totals = service.getCategoryTotalsByInterval(interval);

        List<String> periods = new ArrayList<>(totals.keySet());

        if (periods.isEmpty()) {
            ((CategoryAxis) chart.getXAxis()).setCategories(FXCollections.observableArrayList());
            return;
        }

        ((CategoryAxis) chart.getXAxis()).setCategories(FXCollections.observableArrayList(periods));

        for (Category cat : Category.values()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(cat.name());
            for (String p : periods) {
                int amount = 0;
                Map<Category, Integer> m = totals.get(p);
                if (m != null) amount = m.getOrDefault(cat, 0);
                series.getData().add(new XYChart.Data<>(p, amount));
            }
            chart.getData().add(series);
        }
    }

    /**
     * Show this chart window
     */
    public void showCharts() {
        if (!this.isShowing()) {
            this.show();
            updateChart(intervalChoice.getValue());
        }
    }
}
