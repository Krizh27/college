package Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileService {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = "data/users.txt";

    public FileService() {
        createDataDirectory();
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all users from text file
     * Format: username|password (one per line)
     */
    public Map<String, String> loadUsers() {
        Map<String, String> users = new HashMap<>();
        try {
            File file = new File(USERS_FILE);
            if (!file.exists()) {
                return users;
            }

            List<String> lines = Files.readAllLines(Paths.get(USERS_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Save all users to text file
     * Format: username|password (one per line)
     */
    public void saveUsers(Map<String, String> users) {
        try {
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, String> entry : users.entrySet()) {
                lines.add(entry.getKey() + "|" + entry.getValue());
            }
            Files.write(Paths.get(USERS_FILE), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load expenses for a specific user
     * Format: id|amount|title|category|date (one per line)
     */
    public List<String> loadExpensesByUser(String username) {
        List<String> expenses = new ArrayList<>();
        String expenseFile = DATA_DIR + "/" + username + "_expenses.txt";
        try {
            File file = new File(expenseFile);
            if (!file.exists()) {
                return expenses;
            }
            expenses = Files.readAllLines(Paths.get(expenseFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    /**
     * Save expenses for a specific user
     * Format: id|amount|title|category|date (one per line)
     */
    public void saveExpensesByUser(String username, List<String> expenses) {
        String expenseFile = DATA_DIR + "/" + username + "_expenses.txt";
        try {
            Files.write(Paths.get(expenseFile), expenses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load budget for a specific user
     */
    public int loadBudgetByUser(String username) {
        String budgetFile = DATA_DIR + "/" + username + "_budget.txt";
        try {
            File file = new File(budgetFile);
            if (!file.exists()) {
                return -1;
            }
            List<String> lines = Files.readAllLines(Paths.get(budgetFile));
            if (!lines.isEmpty()) {
                return Integer.parseInt(lines.get(0).trim());
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Save budget for a specific user
     */
    public void saveBudgetByUser(String username, int budget) {
        String budgetFile = DATA_DIR + "/" + username + "_budget.txt";
        try {
            List<String> lines = new ArrayList<>();
            lines.add(String.valueOf(budget));
            Files.write(Paths.get(budgetFile), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load history for a specific user
     * Format: one history entry per line
     */
    public List<String> loadHistoryByUser(String username) {
        List<String> history = new ArrayList<>();
        String historyFile = DATA_DIR + "/" + username + "_history.txt";
        try {
            File file = new File(historyFile);
            if (!file.exists()) {
                return history;
            }
            history = Files.readAllLines(Paths.get(historyFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return history;
    }

    /**
     * Save history for a specific user
     * Format: one history entry per line
     */
    public void saveHistoryByUser(String username, List<String> history) {
        String historyFile = DATA_DIR + "/" + username + "_history.txt";
        try {
            Files.write(Paths.get(historyFile), history);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
