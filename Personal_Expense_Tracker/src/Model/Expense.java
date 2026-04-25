package Model;
import java.time.LocalDate;
import java.util.*;
import Exception.InvalidAmountException;
import Exception.EmptyFieldException;

public class Expense {
    private int expenseid;
    private int amount;
    private String title;
    private Category category;
    private LocalDate date;
    private List<String> history;

    public Expense(int expenseid,int amount,String title,Category category,LocalDate d1) throws InvalidAmountException, EmptyFieldException {
        this.expenseid=expenseid;
        if(amount <= 0){
            throw new InvalidAmountException("Amount must be a positive integer");
        }
        this.amount=amount;
        if(title == null || title.trim().isEmpty()){
            throw new EmptyFieldException("Title cannot be empty");
        }
        this.title=title;
        this.category=category;
        this.date=d1;
        this.history=new ArrayList<>();
    }

    public Expense(int id, String title, Category category, int amount) throws InvalidAmountException, EmptyFieldException {
        this.expenseid = id;
        if(amount <= 0){
            throw new InvalidAmountException("Amount must be a positive integer");
        }
        this.amount = amount;
        if(title == null || title.trim().isEmpty()){
            throw new EmptyFieldException("Title cannot be empty");
        }
        this.title = title;
        this.category = category;
        this.date = LocalDate.now();
    }

    public int getamount(){
        return this.amount;
    }

    public int getexpenseid(){
        return this.expenseid;
    }

    public String getTitle() {
        return this.title;
    }

    //setter
    public void setamount(int am){
         this.amount=am;
    }
    public void setCategory(Category ca){
        this.category=ca;
    }
    public void settitle(String title){
        this.title=title;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public java.time.LocalDate getDate(){
        return this.date;
    }
    
    public Category getCategory() {
        return category;
    }

    public void recordHistory(String event) {
        history.add(event);
    }

    @Override
public String toString() {
    return "Expense ID: " + expenseid +
           " | Title: " + title +
           " | Amount: " + amount +
           " | Category: " + category
            +" | Date:"+date;
}
}
