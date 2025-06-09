package com.example.metro;

import java.sql.ResultSet; // Для хранения результатов SQL-запросов
import java.sql.SQLException; // Для обработки ошибок БД

// Класс MetroDB работает с базой данных метро
// Наследуется от DataLinkManager (управляет подключением к БД)
public class MetroDB extends DBConnector {

    // Конструктор: подключается к БД с указанными параметрами
    MetroDB(String dbName, String user, String pwd) throws ClassNotFoundException, SQLException {
        // Вызываем конструктор родительского класса
        super(dbName, user, pwd);
    }

    // Получает координаты линий метро для отрисовки
    public ResultSet getRoutes() throws SQLException {
        // SQL-запрос: выбираем координаты станций и цвет линии
        // Соединяем таблицы connections, stations (S1 и S2) и lines
        String sql = "SELECT S1.XCoordinate, S1.YCoordinate, S2.XCoordinate, S2.YCoordinate, L.LineColor\n" +
                "FROM connections AS C\n" +
                "JOIN stations AS S1 ON C.Station1ID = S1.StationID \n" +
                "JOIN stations AS S2 ON C.Station2ID = S2.StationID \n" +
                "JOIN lines AS L ON C.LineID = L.LineID;";
        return executeQuery(sql); // Выполняем запрос
    }

    // Получает все станции метро
    public ResultSet getAllStations() throws SQLException {
        // SQL-запрос: выбираем все данные из таблицы stations
        return executeQuery("SELECT * FROM stations;");
    }

    public ResultSet getTravelTimes() throws SQLException {
        // SQL-запрос: выбираем ID станций и время в пути
        return executeQuery("SELECT Station1ID, Station2ID, TravelTime FROM connections;");
    }

    // Считает количество станций
    public ResultSet getStationCount() throws SQLException {
        return executeQuery("SELECT COUNT(StationID) as count FROM stations;");
    }

    // Получает ID станции по её названию
    public ResultSet getStationByName(String name) throws SQLException {
        // SQL-запрос: ищем StationID по StationName
        return executeQuery("SELECT StationID FROM stations WHERE StationName = '" + name + "';");
    }

    public ResultSet getStationById(int id) throws SQLException {
        // SQL-запрос: ищем StationName по StationId
        return executeQuery("SELECT * FROM stations WHERE StationID = " + id + ";");
    }

    public ResultSet getStationCoords(int id) throws SQLException {
        // SQL-запрос: ищем X и Y координаты по StationId
        return executeQuery("SELECT XCoordinate, YCoordinate FROM stations WHERE StationID = " + id + ";");
    }

    public ResultSet getConnectionsBetween(int station1, int station2) throws SQLException {
        // SQL-запрос: ищем соединение между Station1Id и Station2Id
        // Соединяем таблицы с проверкой обоих направлений
        String sql = "SELECT S1.XCoordinate, S1.YCoordinate, S2.XCoordinate, S2.YCoordinate, L.LineColor\n" +
                "FROM connections AS C\n" +
                "INNER JOIN stations AS S1 ON C.Station1ID = S1.StationID OR C.Station2ID = S1.StationID\n" +
                "INNER JOIN stations AS S2 ON C.Station2ID = S2.StationID OR C.Station1ID = S2.StationID\n" +
                "INNER JOIN lines AS L ON C.LineID = L.LineID\n" +
                "WHERE S1.StationID = " + station1 + " AND S2.StationID = " + station2 + ";";
        return executeQuery(sql);
    }
}