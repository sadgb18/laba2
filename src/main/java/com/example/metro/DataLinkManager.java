package com.example.metro;

import java.sql.*;

public class DataLinkManager {
    private Connection activeLink = null;
    private String repositoryIdentifier;
    private String accessKey;
    private String secretKey;

    public String getAccessKey() {
        return accessKey;
    }

    public String getRepositoryIdentifier() {
        return repositoryIdentifier;
    }

    public Connection getActiveLink() {
        return activeLink;
    }

    public void setRepositoryIdentifier(String identifier) {
        this.repositoryIdentifier = identifier;
    }

    public void setAccessKey(String key) {
        this.accessKey = key;
    }

    public void setSecretKey(String key) {
        this.secretKey = key;
    }

    DataLinkManager(String identifier, String key, String secret) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver"); // Загрузка драйвера PostgreSQL
        this.repositoryIdentifier = identifier;
        this.accessKey = key;
        this.secretKey = secret;
        establishConnection();
    }

    // Метод установки соединения с БД
    public Connection establishConnection() throws SQLException {
        // Формирование строки подключения
        String connectionString = "jdbc:postgresql://localhost:5432/" + this.repositoryIdentifier;
        // Установка соединения через DriverManager
        this.activeLink = DriverManager.getConnection(connectionString, this.accessKey, this.secretKey);

        if (this.activeLink != null) {
            System.out.println("Connection established successfully with " + this.repositoryIdentifier);
        } else {
            System.out.println("Failed to establish connection with " + this.repositoryIdentifier);
        }

        return this.activeLink;
    }

    // Метод выполнения SQL-запроса
    public ResultSet executeQuery(String command) throws SQLException {
        Statement executor = this.activeLink.createStatement(); // Создание SQL-экзекутора
        return executor.executeQuery(command); // Выполнение запроса и возврат результата
    }
}
