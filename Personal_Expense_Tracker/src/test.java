import java.util.*;
public class test
{
    public static void main(String[] args) {
        System.out.println("Test running");
    }
}

    class student{
    private String name;
    private int age;

    public student(String name,int age){
        this.name=name;
        this.age=age;
    }

    void get(){
        System.out.println(name+"\n"+age);
    }
    @Override
    public String toString() {
        return "Name: " + name + ", Age: " + age;
    }
}