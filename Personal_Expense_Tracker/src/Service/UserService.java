package Service;

import Model.UserAuth;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private Map<String, String> users;
    private String currentUser;
    private FileService fileService;

    public UserService() {
        fileService = new FileService();
        this.users = fileService.loadUsers();
        
        // Add default test user if file is empty
        if (users.isEmpty()) {
            users.put("user1", "pass123");
            fileService.saveUsers(users);
        }
    }

    /**
     * Register a new user
     * @return true if successful, false if username already exists
     */
    public boolean signup(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        if (username.equals(password)) {
            return false;
        }
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, password);
        fileService.saveUsers(users);
        return true;
    }

    /**
     * Login user
     * @return true if credentials match, false otherwise
     */
    public boolean login(String username, String password) {
        if (!users.containsKey(username)) {
            return false;
        }
        if (users.get(username).equals(password)) {
            currentUser = username;
            return true;
        }
        return false;
    }

    /**
     * Check if username already exists
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * Get current logged-in user
     */
    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * Logout current user
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}