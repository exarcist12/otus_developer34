package ru.otus;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    public static void main(String[] args) {
        try {
            String url = "jdbc:postgresql://localhost:5432/demoDB";
            String user = "usr";
            String password = "pwd";

            System.out.println("Testing connection to: " + url);
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection SUCCESS!");
            conn.close();
        } catch (Exception e) {
            System.err.println("Connection FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
