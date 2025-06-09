package com.example.metro;

import java.sql.*;

public class DBConnector {
    private Connection dbConnection = null;
    private String dbName;
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getDBName() {
        return dbName;
    }

    public Connection getConnection() {
        return dbConnection;
    }

    public void setDBName(String name) {
        this.dbName = name;
    }

    public void setUsername(String user) {
        this.username = user;
    }

    public void setPassword(String pwd) {
        this.password = pwd;
    }

    DBConnector(String name, String user, String pwd) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        this.dbName = name;
        this.username = user;
        this.password = pwd;
        connect();
    }

    public Connection connect() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/" + this.dbName;
        this.dbConnection = DriverManager.getConnection(url, this.username, this.password);

        if (this.dbConnection != null) {
            System.out.println("Подключение к БД " + this.dbName + " успешно");
        } else {
            System.out.println("Не удалось подключиться к БД " + this.dbName);
        }

        return this.dbConnection;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Statement stmt = this.dbConnection.createStatement();
        return stmt.executeQuery(sql);
    }
}
