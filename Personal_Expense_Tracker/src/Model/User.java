package Model;

public class User {
    private String name;
    private Budget budget;

    public User(String name, Budget budget) {
        this.name = name;
        this.budget = budget;
    }

    public String getName() {
        return name;
    }

    public Budget getBudget() {
        return budget;
    }
}
