package Model;

public class Budget {
    private int limit;

    public Budget(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "Budget: " + limit;
    }
}
