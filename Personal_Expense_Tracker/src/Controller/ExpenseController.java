package Controller;

import Model.Expense;
import Service.ExpenseService;
import Model.Budget;

public class ExpenseController {
    private ExpenseService service = new ExpenseService();
    private Budget budget;

    public ExpenseController(Budget budget) {
        this.budget = budget;
    }

    public void addExpense(Expense e) {
        service.addexpense(e);
        service.budgetupdater(e.getamount(), budget);
    }

    public Expense removeExpense(int id) {
        return service.remove(id);
    }
}
