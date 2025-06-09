package com.example.metro;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import java.sql.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GUI extends Application {
    private MetroDB database;
    private List<List<Double>> routeData;
    private double[][] routeMatrix;
    private Graph routeGraph;
    private List<Integer> fastestRoute = new ArrayList<>();
    private double totalTime;
    private List<Integer> shortestRoute = new ArrayList<>();
    private double stationCount;

    private void renderConnections(Group root) throws SQLException {
        ResultSet connections = this.database.getRoutes();
        while (connections.next()) {
            Line connection = new Line(
                connections.getInt(1), 
                connections.getInt(2), 
                connections.getInt(3), 
                connections.getInt(4)
            );
            connection.setStroke(Color.valueOf(connections.getString(5)));
            connection.setStrokeWidth(5);
            root.getChildren().add(connection);
        }
    }

    private void renderStations(Group root) throws SQLException {
        ResultSet stations = this.database.getAllStations();
        while (stations.next()) {
            Circle stationPoint = new Circle(
                stations.getInt("XCoordinate"),
                stations.getInt("YCoordinate"), 
                10
            );
            stationPoint.setFill(Color.BLACK);
            stationPoint.setStroke(Color.WHITE);
            stationPoint.setStrokeWidth(5);

            Text stationName = new Text(
                stations.getInt("XCoordinate") - 25,
                stations.getInt("YCoordinate") - 30,
                stations.getString("StationName")
            );
            stationName.setFont(Font.font("Helvetica", FontWeight.BOLD, 20));

            root.getChildren().addAll(stationPoint, stationName);
        }
    }

    private void setupInterface(HBox controls, Group mapRoot) throws SQLException {
        ResultSet stations = this.database.getAllStations();
        ObservableList<String> stationNames = FXCollections.observableArrayList();
        while (stations.next()) {
            stationNames.add(stations.getString("StationName"));
        }

        ChoiceBox<String> startStation = new ChoiceBox<>(stationNames);
        ChoiceBox<String> endStation = new ChoiceBox<>(stationNames);
        Button findRoute = new Button("Найти маршрут");

        findRoute.setOnMouseClicked(e -> {
            try {
                ResultSet start = this.database.getStationByName(startStation.getValue());
                ResultSet end = this.database.getStationByName(endStation.getValue());

                if (start.next() && end.next()) {
                    fastestRoute = routeGraph.ShortestWay(start.getInt("StationId"), end.getInt("StationId"));
                    totalTime = routeGraph.ShortestDistance(start.getInt("StationId"), end.getInt("StationId"));
                    shortestRoute = routeGraph.MinVertexWay(start.getInt("StationId"), end.getInt("StationId"));
                    stationCount = routeGraph.MinVertex(start.getInt("StationId"), end.getInt("StationId"));
                }
                displayRouteInfo(controls, fastestRoute, totalTime, shortestRoute, stationCount);
                highlightRoutes(mapRoot, fastestRoute, shortestRoute);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        controls.getChildren().addAll(startStation, endStation, findRoute);
    }

    private void highlightRoutes(Group root, List<Integer> fastRoute, List<Integer> shortRoute) throws SQLException {
        root.getChildren().removeIf(item -> item instanceof Circle || item instanceof Line);

        renderConnections(root);
        renderStations(root);

        // Подсветка быстрого маршрута
        for (int i = 0; i < fastRoute.size(); i++) {
            if (i + 1 < fastRoute.size()) {
                ResultSet connections = this.database.getConnectionsBetween(fastRoute.get(i), fastRoute.get(i + 1));
                if (connections.next()) {
                    Line highlight = new Line(connections.getInt(1), connections.getInt(2), 
                                           connections.getInt(3), connections.getInt(4));
                    highlight.setStroke(Color.valueOf(connections.getString(5)).brighter());
                    highlight.setStrokeWidth(20);
                    root.getChildren().add(highlight);
                }
            }
            ResultSet station = this.database.getStationCoords(fastRoute.get(i));
            if (station.next()) {
                Circle highlight = new Circle(station.getInt("XCoordinate"), 
                                           station.getInt("YCoordinate"), 25);
                highlight.setFill(Color.BLACK);
                root.getChildren().add(highlight);
            }
        }

        // Подсветка короткого маршрута
        for (int i = 0; i < shortRoute.size(); i++) {
            if (i + 1 < shortRoute.size()) {
                ResultSet connections = this.database.getConnectionsBetween(shortRoute.get(i), shortRoute.get(i + 1));
                if (connections.next()) {
                    Line highlight = new Line(connections.getInt(1), connections.getInt(2), 
                                           connections.getInt(3), connections.getInt(4));
                    highlight.setStroke(Color.valueOf(connections.getString(5)).darker());
                    highlight.setStrokeWidth(10);
                    root.getChildren().add(highlight);
                }
            }
            ResultSet station = this.database.getStationCoords(shortRoute.get(i));
            if (station.next()) {
                Circle highlight = new Circle(station.getInt("XCoordinate"), 
                                           station.getInt("YCoordinate"), 15);
                highlight.setFill(Color.GOLD);
                root.getChildren().add(highlight);
            }
        }
    }

    private void displayRouteInfo(HBox controls, List<Integer> fastRoute, double time, 
                                List<Integer> shortRoute, double stations) throws SQLException {
        Label info = new Label();
        info.setFont(Font.font("Helvetica", FontWeight.BOLD, 14));
        info.setTranslateY(info.getTranslateY() + 40);
        info.setTranslateX(info.getTranslateX() - 850);

        StringBuilder text = new StringBuilder("Быстрый маршрут: ");
        for (Integer station : fastRoute) {
            ResultSet rs = this.database.getStationById(station);
            if (rs.next()) {
                text.append(rs.getString("StationName")).append("->");
            }
        }
        text.append("Время: ").append(time).append(" мин.\n");

        text.append("Короткий маршрут: ");
        for (Integer station : shortRoute) {
            ResultSet rs = this.database.getStationById(station);
            if (rs.next()) {
                text.append(rs.getString("StationName")).append("->");
            }
        }
        text.append("Станций: ").append(stations).append("\n");

        info.setText(text.toString());
        controls.getChildren().removeIf(item -> item instanceof Label);
        controls.getChildren().add(info);
    }

    private void initializeRouteGraph() throws SQLException {
        ResultSet connections = this.database.getTravelTimes();
        this.routeData = new ArrayList<>();
        while (connections.next()) {
            List<Double> edge = new ArrayList<>();
            edge.add(connections.getDouble("Station1ID"));
            edge.add(connections.getDouble("Station2ID"));
            edge.add(connections.getDouble("TravelTime"));
            this.routeData.add(edge);
        }

        routeMatrix = new double[routeData.size()][routeData.get(0).size()];
        for (int i = 0; i < routeMatrix.length; i++) {
            for (int j = 0; j < routeMatrix[0].length; j++) {
                routeMatrix[i][j] = routeData.get(i).get(j);
            }
        }

        ResultSet count = this.database.getStationCount();
        if (count.next()) {
            routeGraph = new Graph(count.getInt("count") + 1, routeMatrix);
        }
    }

    @Override
    public void start(Stage stage) throws IOException, SQLException, ClassNotFoundException {
        this.database = new MetroDB("Metro", "postgres", "62426");
        initializeRouteGraph();

        Group root = new Group();
        Group map = new Group();
        HBox controls = new HBox();

        controls.setSpacing(70);
        controls.setTranslateX(320);
        controls.setTranslateY(565);

        renderConnections(map);
        renderStations(map);
        setupInterface(controls, map);

        root.getChildren().addAll(map, controls);
        Scene scene = new Scene(root, 1024, 700);

        stage.setScene(scene);
        stage.setTitle("Metro");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}