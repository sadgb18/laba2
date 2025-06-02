package com.example.metro;

import java.sql.ResultSet; // Для хранения результатов SQL-запросов
import java.sql.SQLException; // Для обработки ошибок БД

// Класс SubwayData работает с базой данных метро
// Наследуется от DataLinkManager (управляет подключением к БД)
public class SubwayData extends DataLinkManager {

    // Конструктор: подключается к БД с указанными параметрами
    SubwayData(String dbName, String dbUser, String dbPassword) throws ClassNotFoundException, SQLException {
        // Вызываем конструктор родительского класса
        super(dbName, dbUser, dbPassword);
    }

    // Получает координаты линий метро для отрисовки
    public ResultSet fetchConnections() throws SQLException {
        // SQL-запрос: выбираем координаты станций и цвет линии
        // Соединяем таблицы connections, stations (S1 и S2) и lines
        String query = "SELECT S1.XCoordinate, S1.YCoordinate, S2.XCoordinate, S2.YCoordinate, L.LineColor\n" +
                "FROM connections AS C\n" +
                "JOIN stations AS S1 ON C.Station1ID = S1.StationID \n" +
                "JOIN stations AS S2 ON C.Station2ID = S2.StationID \n" +
                "JOIN lines AS L ON C.LineID = L.LineID;";
        return executeQuery(query); // Выполняем запрос
    }

    // Получает все станции метро
    public ResultSet fetchStations() throws SQLException {
        // SQL-запрос: выбираем все данные из таблицы stations
        String query = "SELECT * FROM stations;";
        return executeQuery(query);
    }

    public ResultSet fetchTravelTimes() throws SQLException {
        // SQL-запрос: выбираем ID станций и время в пути
        String query = "SELECT Station1ID, Station2ID, TravelTime FROM connections;";
        return executeQuery(query);
    }

    // Считает количество станций
    public ResultSet countStations() throws SQLException {
        String query = "SELECT COUNT(StationID) FROM stations;";
        return executeQuery(query);
    }

    // Получает ID станции по её названию
    public ResultSet findStationId(String stationName) throws SQLException {
        // SQL-запрос: ищем StationID по StationName
        String query = "SELECT StationID FROM stations\n" +
                "WHERE StationName = '" + stationName + "';";
        return executeQuery(query);
    }

    public ResultSet findStationName(int stationId) throws SQLException {
        // SQL-запрос: ищем StationName по StationId
        String query = "SELECT StationName FROM stations\n" +
                "WHERE StationId = " + stationId + ";";
        return executeQuery(query);
    }

    public ResultSet findCoordinates(int stationId) throws SQLException {
        // SQL-запрос: ищем X и Y координаты по StationId
        String query = "SELECT XCoordinate, YCoordinate FROM stations\n" +
                "WHERE StationId = " + stationId + ";";
        return executeQuery(query);
    }

    public ResultSet findConnection(int station1Id, int station2Id) throws SQLException {
        // SQL-запрос: ищем соединение между Station1Id и Station2Id
        // Соединяем таблицы с проверкой обоих направлений
        String query = "SELECT S1.XCoordinate, S1.YCoordinate, S2.XCoordinate, S2.YCoordinate, L.LineColor\n" +
                "FROM connections AS C\n" +
                "INNER JOIN stations AS S1 ON C.Station1ID = S1.StationID OR C.Station2ID = S1.StationID\n" +
                "INNER JOIN stations AS S2 ON C.Station2ID = S2.StationID OR C.Station1ID = S2.StationID\n" +
                "INNER JOIN lines AS L ON C.LineID = L.LineID\n" +
                "WHERE S1.StationID = " + station1Id + " AND S2.StationID = " + station2Id + ";";
        return executeQuery(query);
    }
}