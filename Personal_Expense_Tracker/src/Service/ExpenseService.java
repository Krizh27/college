package Service;

import Model.Category;
import Model.Expense;
import Model.Budget;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.time.temporal.WeekFields;

public class ExpenseService {
    private List<Expense> expenses = new ArrayList<>();
    private List<String> history = new ArrayList<>();

    public void addexpense(Expense e) {
        expenses.add(e);
        history.add("Expense added: " + e.toString());
    }

    public Expense remove(int id) {
        for (Expense e : expenses) {
            if (e.getexpenseid() == id) {
                expenses.remove(e);
                history.add("Expense removed: " + e.toString());
                return e;
            }
        }
        return null;
    }

    public List<Expense> getExpenses() {
        return new ArrayList<>(expenses);
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    /** record an arbitrary text entry (in‑/outflow, budget change etc.) */
    public void recordHistory(String entry) {
        history.add(entry);
    }

    public void budgetupdater(int amount, Budget b) {
        b.setLimit(b.getLimit() - amount);
    }

    public Map<String, Map<Category, Integer>> getCategoryTotalsByInterval(String interval) {
        Map<String, Map<Category, Integer>> result = new TreeMap<>();
        if (expenses.isEmpty()) return result;

        for (Expense e : expenses) {
            java.time.LocalDate d = e.getDate();
            String key = "";
            switch (interval.toLowerCase()) {
                case "daily":
                    key = d.toString();
                    break;
                case "weekly":
                    WeekFields wf = WeekFields.of(Locale.getDefault());
                    int week = d.get(wf.weekOfWeekBasedYear());
                    key = d.getYear() + "-W" + String.format("%02d", week);
                    break;
                case "monthly":
                    key = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
                    break;
                case "yearly":
                    key = String.valueOf(d.getYear());
                    break;
                default:
                    key = d.toString();
            }

            Map<Category, Integer> catMap = result.get(key);
            if (catMap == null) {
                catMap = new EnumMap<>(Category.class);
                // initialize categories to zero
                for (Category c : Category.values()) {
                    catMap.put(c, 0);
                }
                result.put(key, catMap);
            }

            Category cat = e.getCategory();
            int prev = catMap.getOrDefault(cat, 0);
            catMap.put(cat, prev + e.getamount());
        }

        return result;
    }
}
