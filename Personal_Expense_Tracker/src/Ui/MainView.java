package Ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import Model.Category;
import Service.ExpenseService;
import Service.FileService;
import Model.Expense;
import Model.Budget;
import java.time.LocalDate;
import Exception.InvalidAmountException;
import Exception.EmptyFieldException;

public class MainView extends Application {
    private ExpenseService service = new ExpenseService();
    private FileService fileService = new FileService();
    private static String currentUser; // Will be set by AuthView
    private ObservableList<String> items = FXCollections.observableArrayList();
    private ListView<String> listView;
    private ChartView chartView;
    private int nextId = 1;
    private Budget budget;
    private Label budgetLabel;
    private int warningThreshold = 500; // Default warning threshold

    // new collection for removed‑ids
    private List<Integer> removedIds = new ArrayList<>();

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize ExpenseService with current user to load their data
        service.initializeForUser(currentUser);
        
        // Check if user has existing budget
        int existingBudget = fileService.loadBudgetByUser(currentUser);
        int initial = 1000;
        
        if (existingBudget == -1) {
            // No existing budget, ask for initial budget
            TextInputDialog bd = new TextInputDialog("1000");
            bd.setTitle("Initial Budget");
            bd.setHeaderText("Set initial budget");
            bd.setContentText("Budget:");
            try {
                String res = bd.showAndWait().orElse("1000");
                initial = Integer.parseInt(res);
                if (initial <= 0) {
                    initial = 1000;
                    new Alert(Alert.AlertType.WARNING, "Invalid budget amount. Using default 1000.").showAndWait();
                }
            } catch (Exception e) {
                initial = 1000;
                new Alert(Alert.AlertType.WARNING, "Invalid budget amount. Using default 1000.").showAndWait();
            }
            // Save the budget
            fileService.saveBudgetByUser(currentUser, initial);
        } else {
            // Use existing budget
            initial = existingBudget;
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

        Button chartsBtn = new Button("Show Charts");
        chartsBtn.setOnAction(e -> showCharts());

        Button addFundsBtn = new Button("Add Funds");
        addFundsBtn.setOnAction(e -> addFundsDialog());

        Button setLimitBtn = new Button("Set Warning Limit");
        setLimitBtn.setOnAction(e -> setWarningLimitDialog());

        budgetLabel = new Label("Budget Remaining: " + budget.getLimit());
        budgetLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        HBox controls1 = new HBox(8, addBtn, removeBtn, summaryBtn, historyBtn);
        controls1.setPadding(new Insets(5));
        
        HBox controls2 = new HBox(8, chartsBtn, addFundsBtn, setLimitBtn);
        controls2.setPadding(new Insets(5));
        
        VBox root = new VBox(10, budgetLabel, listView, controls1, controls2);
        root.setPadding(new Insets(10));

        // Initialize ChartView
        chartView = new ChartView(service);

        Scene scene = new Scene(root, 650, 550);
        primaryStage.setTitle("Personal Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
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
        if (amount <= 0) {
            new Alert(Alert.AlertType.ERROR, "Amount must be a positive integer").showAndWait();
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

        try {
            Expense e = new Expense(nextId++, t, c, amount);   // use the String and Category, not the dialogs
            service.addexpense(e);
            service.budgetupdater(amount, budget);
            fileService.saveBudgetByUser(currentUser, budget.getLimit()); // Save budget
            
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
        } catch (InvalidAmountException | EmptyFieldException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
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
                fileService.saveBudgetByUser(currentUser, budget.getLimit()); // Save budget
                updateBudgetLabel();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Added " + amount + " to budget. New balance: " + budget.getLimit());
                alert.showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, "Amount must be a positive integer").showAndWait();
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
            } else {
                new Alert(Alert.AlertType.ERROR, "Warning threshold must be non-negative").showAndWait();
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
            if (removed != null) {
                // Refund the amount back to budget
                budget.setLimit(budget.getLimit() + removed.getamount());
                fileService.saveBudgetByUser(currentUser, budget.getLimit()); // Save budget
            }
            removedIds.add(id);                                  // remember for display
            refreshList();                                       // will keep the removed entry
            updateBudgetLabel();
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

    private void showCharts() {
        chartView.showCharts();
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
