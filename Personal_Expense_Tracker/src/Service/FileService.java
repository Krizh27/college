package Service;

import org.json.JSONArray;
import org.json.JSONObject;
import Model.UserAuth;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileService {
    private static final String USERS_FILE = "data/users.json";
    private static final String USER_DATA_FILE = "data/user_data.json";

    public FileService() {
        createDataDirectory();
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all users from JSON file
     */
    public Map<String, String> loadUsers() {
        Map<String, String> users = new HashMap<>();
        try {
            File file = new File(USERS_FILE);
            if (!file.exists()) {
                return users;
            }

            String content = new String(Files.readAllBytes(Paths.get(USERS_FILE)));
            JSONObject json = new JSONObject(content);
            JSONObject usersObj = json.getJSONObject("users");

            for (String key : usersObj.keySet()) {
                users.put(key, usersObj.getString(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Save all users to JSON file
     */
    public void saveUsers(Map<String, String> users) {
        try {
            JSONObject json = new JSONObject();
            JSONObject usersObj = new JSONObject();

            for (Map.Entry<String, String> entry : users.entrySet()) {
                usersObj.put(entry.getKey(), entry.getValue());
            }

            json.put("users", usersObj);

            Files.write(Paths.get(USERS_FILE), json.toString(2).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load user data (expenses, budget, history)
     */
    private JSONObject loadUserDataFile() {
        try {
            File file = new File(USER_DATA_FILE);
            if (!file.exists()) {
                return new JSONObject();
            }

            String content = new String(Files.readAllBytes(Paths.get(USER_DATA_FILE)));
            return new JSONObject(content);
        } catch (IOException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    /**
     * Save user data (expenses, budget, history)
     */
    private void saveUserDataFile(JSONObject userData) {
        try {
            Files.write(Paths.get(USER_DATA_FILE), userData.toString(2).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load expenses for a specific user
     */
    public JSONArray loadExpensesByUser(String username) {
        JSONObject userData = loadUserDataFile();
        if (userData.has(username) && userData.getJSONObject(username).has("expenses")) {
            return userData.getJSONObject(username).getJSONArray("expenses");
        }
        return new JSONArray();
    }

    /**
     * Save expenses for a specific user
     */
    public void saveExpensesByUser(String username, JSONArray expenses) {
        JSONObject userData = loadUserDataFile();
        
        if (!userData.has(username)) {
            userData.put(username, new JSONObject());
        }
        
        JSONObject userObj = userData.getJSONObject(username);
        userObj.put("expenses", expenses);
        
        saveUserDataFile(userData);
    }

    /**
     * Load budget for a specific user
     */
    public int loadBudgetByUser(String username) {
        JSONObject userData = loadUserDataFile();
        if (userData.has(username) && userData.getJSONObject(username).has("budget")) {
            return userData.getJSONObject(username).getInt("budget");
        }
        return -1; // Indicates no existing budget
    }

    /**
     * Save budget for a specific user
     */
    public void saveBudgetByUser(String username, int budget) {
        JSONObject userData = loadUserDataFile();
        
        if (!userData.has(username)) {
            userData.put(username, new JSONObject());
        }
        
        JSONObject userObj = userData.getJSONObject(username);
        userObj.put("budget", budget);
        
        saveUserDataFile(userData);
    }

    /**
     * Load history for a specific user
     */
    public JSONArray loadHistoryByUser(String username) {
        JSONObject userData = loadUserDataFile();
        if (userData.has(username) && userData.getJSONObject(username).has("history")) {
            return userData.getJSONObject(username).getJSONArray("history");
        }
        return new JSONArray();
    }

    /**
     * Save history for a specific user
     */
    public void saveHistoryByUser(String username, JSONArray history) {
        JSONObject userData = loadUserDataFile();
        
        if (!userData.has(username)) {
            userData.put(username, new JSONObject());
        }
        
        JSONObject userObj = userData.getJSONObject(username);
        userObj.put("history", history);
        
        saveUserDataFile(userData);
    }
}
