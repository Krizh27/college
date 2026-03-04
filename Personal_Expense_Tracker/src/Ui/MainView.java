package Ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import Model.Category;
import Service.ExpenseService;
import Model.Expense;
import Model.Category;
import Model.Budget;
import java.time.LocalDate;

public class MainView extends Application {
    private ExpenseService service = new ExpenseService();
    private ObservableList<String> items = FXCollections.observableArrayList();
    private ListView<String> listView;
    private BarChart<String, Number> chart;
    private ChoiceBox<String> intervalChoice;
    private int nextId = 1;
    private Budget budget;
    private Label budgetLabel;
    private int warningThreshold = 500; // Default warning threshold

    // new collection for removed‑ids
    private List<Integer> removedIds = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        TextInputDialog bd = new TextInputDialog("1000");
        bd.setTitle("Initial Budget");
        bd.setHeaderText("Set initial budget");
        bd.setContentText("Budget:");
        int initial = 1000;
        try {
            String res = bd.showAndWait().orElse("1000");
            initial = Integer.parseInt(res);
        } catch (Exception e) {
            initial = 1000;
        }
        budget = new Budget(initial);

        listView = new ListView<>(items);
        refreshList();

        Button addBtn = new Button("Add Expense");
        addBtn.setOnAction(e -> addExpenseDialog());

        Button removeBtn = new Button("Remove Selected");
        removeBtn.setOnAction(e -> removeSelected());

        Button summaryBtn = new Button("Show Summary");
        summaryBtn.setOnAction(e -> showSummary());

        Button historyBtn = new Button("Show History");                // new button
        historyBtn.setOnAction(e -> showHistory());

        Button addFundsBtn = new Button("Add Funds");
        addFundsBtn.setOnAction(e -> addFundsDialog());

        Button setLimitBtn = new Button("Set Warning Limit");
        setLimitBtn.setOnAction(e -> setWarningLimitDialog());

        budgetLabel = new Label("Budget Remaining: " + budget.getLimit());
        budgetLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        HBox controls = new HBox(8, addBtn, removeBtn, summaryBtn,
                                 historyBtn, addFundsBtn, setLimitBtn); // include historyBtn
        controls.setPadding(new Insets(5));
        
        VBox leftBox = new VBox(10, budgetLabel, listView, controls);
        leftBox.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Period");
        yAxis.setLabel("Amount");
        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Expenses");

        intervalChoice = new ChoiceBox<>(FXCollections.observableArrayList("Daily", "Weekly", "Monthly", "Yearly"));
        intervalChoice.setValue("Daily");
        intervalChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateChart(newV));

        VBox chartBox = new VBox(8, intervalChoice, chart);
        chartBox.setPadding(new Insets(10));
        chartBox.setPrefWidth(380);

        BorderPane root = new BorderPane();
        root.setLeft(leftBox);
        root.setCenter(chartBox);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Personal Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateChart("Daily");
    }

    private void addExpenseDialog() {
        TextInputDialog amtDlg = new TextInputDialog();
        amtDlg.setTitle("Add Expense");
        amtDlg.setHeaderText(null);
        amtDlg.setContentText("Amount:");
        Optional<String> amtResult = amtDlg.showAndWait();
        if (!amtResult.isPresent()) {            // user cancelled
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(amtResult.get().trim());
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Invalid amount").showAndWait();
            return;
        }

        // Check if expense exceeds budget
        if (amount > budget.getLimit()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Budget Exceeded");
            alert.setHeaderText("Insufficient Budget!");
            alert.setContentText("This expense (" + amount + ") exceeds your remaining budget (" + budget.getLimit() + ").\nDo you want to continue?");
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) {
                return;
            }
        }

        TextInputDialog titleDlg = new TextInputDialog();
        titleDlg.setTitle("Add Expense");
        titleDlg.setContentText("Title:");
        Optional<String> titleResult = titleDlg.showAndWait();
        if (!titleResult.isPresent()) {          // user cancelled
            return;
        }
        String t = titleResult.get().trim();
        if (t.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Title cannot be empty").showAndWait();
            return;
        }

        ChoiceDialog<Category> catDlg = new ChoiceDialog<>(Category.FOOD, Category.values());
        catDlg.setTitle("Category");
        catDlg.setContentText("Choose category:");
        Optional<Category> catResult = catDlg.showAndWait();
        if (!catResult.isPresent()) {            // user cancelled
            return;
        }
        Category c = catResult.get();

        Expense e = new Expense(nextId++, t, c, amount);   // use the String and Category, not the dialogs
        service.addexpense(e);
        service.budgetupdater(amount, budget);
        
        // Check if remaining budget is below warning threshold
        if (budget.getLimit() < warningThreshold && budget.getLimit() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Low Budget Warning");
            alert.setHeaderText("Budget Running Low!");
            alert.setContentText("Your remaining budget (" + budget.getLimit() + ") is below the warning limit (" + warningThreshold + ").");
            alert.showAndWait();
        }

        refreshList();
        updateBudgetLabel();
        updateChart(intervalChoice.getValue());
    }

    private void addFundsDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Funds");
        dialog.setHeaderText("Add money to your budget");
        dialog.setContentText("Amount to add:");
        try {
            int amount = Integer.parseInt(dialog.showAndWait().orElse("0"));
            if (amount > 0) {
                budget.setLimit(budget.getLimit() + amount);
                service.recordHistory("Inflow – added funds " + amount);  // record inflow
                updateBudgetLabel();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Added " + amount + " to budget. New balance: " + budget.getLimit());
                alert.showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Invalid amount").showAndWait();
        }
    }

    private void setWarningLimitDialog() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(warningThreshold));
        dialog.setTitle("Set Warning Limit");
        dialog.setHeaderText("Set the minimum budget amount to trigger warnings");
        dialog.setContentText("Warning threshold:");
        try {
            int limit = Integer.parseInt(dialog.showAndWait().orElse(String.valueOf(warningThreshold)));
            if (limit >= 0) {
                warningThreshold = limit;
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Warning threshold set to: " + warningThreshold);
                alert.showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Invalid amount").showAndWait();
        }
    }

    private void updateBudgetLabel() {
        budgetLabel.setText("Budget Remaining: " + budget.getLimit());
        if (budget.getLimit() < warningThreshold && budget.getLimit() > 0) {
            budgetLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: orange;");
        } else if (budget.getLimit() <= 0) {
            budgetLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: red;");
        } else {
            budgetLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: green;");
        }
    }

    private void removeSelected() {
        String sel = listView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            String[] parts = sel.split("\\|");
            String idPart = parts[0].trim();
            int id = Integer.parseInt(idPart.replaceAll("[^0-9]", ""));
            Expense removed = service.remove(id);                // now returns the object
            removedIds.add(id);                                  // remember for display
            refreshList();                                       // will keep the removed entry
            updateBudgetLabel();
            updateChart(intervalChoice.getValue());
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to remove expense").showAndWait();
        }
    }

    private void showSummary() {
        List<Expense> expenses = service.getExpenses();
        if (expenses.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No expenses to summarize").showAndWait();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Expense Summary\n");
        sb.append("================\n\n");
        for (Expense e : expenses) {
            sb.append(e.toString()).append("\n");
        }
        sb.append("\nTotal Budget: ").append(budget.getLimit()).append("\n");
        
        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefSize(400, 300);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Expense Summary");
        alert.getDialogPane().setContent(ta);
        alert.showAndWait();
    }

    private void showHistory() {
        List<String> hist = service.getHistory();
        if (hist.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No history available").showAndWait();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : hist) {
            sb.append(s).append("\n");
        }
        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefSize(400, 300);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction History");
        alert.getDialogPane().setContent(ta);
        alert.showAndWait();
    }

    private void updateChart(String interval){
        if(interval==null) interval = "Daily";
        chart.getData().clear();
        java.util.Map<String, java.util.Map<Category, Integer>> totals =
                service.getCategoryTotalsByInterval(interval);

        List<String> periods = new ArrayList<>(totals.keySet());

        if(periods.isEmpty()){
            ((CategoryAxis)chart.getXAxis()).setCategories(FXCollections.observableArrayList());
            return;
        }

        ((CategoryAxis)chart.getXAxis()).setCategories(FXCollections.observableArrayList(periods));

        for(Category cat : Category.values()){
            XYChart.Series<String,Number> series = new XYChart.Series<>();
            series.setName(cat.name());
            for(String p : periods){
                int amount = 0;
                java.util.Map<Category,Integer> m = totals.get(p);
                if(m!=null) amount = m.getOrDefault(cat, 0);
                series.getData().add(new XYChart.Data<>(p, amount));
            }
            chart.getData().add(series);
        }
    }

    private void refreshList() {
        items.clear();
        for (Expense e : service.getExpenses()) {
            items.add(e.toString());
        }
        // append removed‑item placeholders
        for (Integer id : removedIds) {
            items.add("Expense " + id + " removed");
        }
    }
}
