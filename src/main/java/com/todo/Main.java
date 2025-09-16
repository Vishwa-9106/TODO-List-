package com.todo;

import java.sql.Connection;
import java.sql.SQLException;

import com.todo.util.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection db_connection = new DatabaseConnection();
        try {
            Connection cn = db_connection.getConnection();
            System.err.println("Connection Successful!");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
        }
    }
}