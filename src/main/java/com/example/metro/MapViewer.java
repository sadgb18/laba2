package com.example.metro;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapViewer extends Application {
    private SubwayData subwayData;
    private List<List<Double>> routeGraph;
    private double[][] adjacencyMatrix;
    private PathCalculator pathFinder;
    private List<Integer> timeOptimalPath = new ArrayList<>();
    private double totalTime;
    private List<Integer> stationOptimalPath = new ArrayList<>();
    private double totalStations;

    private void renderConnections(Group root) throws SQLException {
        ResultSet connections = this.subwayData.fetchConnections();
        while (connections.next()) {
            Line link = new Line(connections.getInt(1), connections.getInt(2), 
                               connections.getInt(3), connections.getInt(4));
            link.setStroke(Color.valueOf(connections.getString(5)));
            link.setStrokeWidth(3);
            link.setOpacity(0.7);
            root.getChildren().add(link);
        }
    }

    private void renderStations(Group root) throws SQLException {
        ResultSet stations = this.subwayData.fetchStations();
        while (stations.next()) {
            Circle point = new Circle(stations.getInt("XCoordinate"),
                    stations.getInt("YCoordinate"), 8);
            point.setFill(Color.WHITE);
            point.setStroke(Color.BLACK);
            point.setStrokeWidth(2);

            Text label = new Text(stations.getInt("XCoordinate") - 20,
                    stations.getInt("YCoordinate") - 20,
                    stations.getString("StationName"));
            label.setFont(Font.font("Arial", FontWeight.MEDIUM, 14));
            label.setFill(Color.DARKBLUE);

            root.getChildren().addAll(point, label);
        }
    }

    private void setupInterface(VBox controls, Group root) throws SQLException {
        ResultSet stations = this.subwayData.fetchStations();
        ObservableList<String> stationNames = FXCollections.observableArrayList();
        while (stations.next()) {
            stationNames.add(stations.getString("StationName"));
        }

        ComboBox<String> startStation = new ComboBox<>(stationNames);
        ComboBox<String> endStation = new ComboBox<>(stationNames);
        startStation.setPromptText("Откуда");
        endStation.setPromptText("Куда");

        Button findRoute = new Button("Рассчитать");
        findRoute.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        findRoute.setOnAction(e -> {
            try {
                ResultSet id1 = this.subwayData.findStationId(startStation.getValue());
                ResultSet id2 = this.subwayData.findStationId(endStation.getValue());

                if (id1.next() && id2.next()) {
                    timeOptimalPath = pathFinder.findFastestRoute(id1.getInt("StationID"), id2.getInt("StationID"));
                    totalTime = pathFinder.calculateTotalTime(id1.getInt("StationID"), id2.getInt("StationID"));
                    stationOptimalPath = pathFinder.findShortestRoute(id1.getInt("StationID"), id2.getInt("StationID"));
                    totalStations = pathFinder.calculateTotalStations(id1.getInt("StationID"), id2.getInt("StationID"));
                }
                displayRouteInfo(controls);
                highlightRoutes(root);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        controls.getChildren().addAll(
            new Label("Выберите маршрут:"),
            startStation,
            endStation,
            findRoute
        );
    }

    private void highlightRoutes(Group root) throws SQLException {
        root.getChildren().removeIf(item -> item instanceof Circle || item instanceof Line);

        renderConnections(root);
        renderStations(root);

        for (int i = 0; i < timeOptimalPath.size(); i++) {
            if (i + 1 < timeOptimalPath.size()) {
                ResultSet connections = this.subwayData.findConnection(timeOptimalPath.get(i), timeOptimalPath.get(i + 1));
                if (connections.next()) {
                    Line link = new Line(connections.getInt(1), connections.getInt(2),
                            connections.getInt(3), connections.getInt(4));
                    link.setStroke(Color.valueOf(connections.getString(5)).brighter());
                    link.setStrokeWidth(6);
                    root.getChildren().add(link);
                }
            }
            ResultSet station = this.subwayData.findCoordinates(timeOptimalPath.get(i));
            if (station.next()) {
                Circle point = new Circle(station.getInt("XCoordinate"),
                        station.getInt("YCoordinate"), 12);
                point.setFill(Color.GOLD);
                root.getChildren().add(point);
            }
        }

        for (int i = 0; i < stationOptimalPath.size(); i++) {
            if (i + 1 < stationOptimalPath.size()) {
                ResultSet connections = this.subwayData.findConnection(stationOptimalPath.get(i), stationOptimalPath.get(i + 1));
                if (connections.next()) {
                    Line link = new Line(connections.getInt(1), connections.getInt(2),
                            connections.getInt(3), connections.getInt(4));
                    link.setStroke(Color.valueOf(connections.getString(5)));
                    link.setStrokeWidth(4);
                    link.setOpacity(0.9);
                    root.getChildren().add(link);
                }
            }
        }
    }

    private void displayRouteInfo(VBox controls) throws SQLException {
        Label info = new Label();
        info.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        info.setStyle("-fx-text-fill: #2196F3;");
        
        StringBuilder route = new StringBuilder("⚡ Быстрый маршрут:\n");
        for (Integer i : timeOptimalPath) {
            ResultSet station = this.subwayData.findStationName(i);
            if (station.next()) {
                route.append("➜ ").append(station.getString("StationName")).append("\n");
            }
        }
        route.append(String.format("⏱ Время в пути: %.1f мин\n\n", totalTime));

        route.append("📍 Минимум станций:\n");
        for (Integer i : stationOptimalPath) {
            ResultSet station = this.subwayData.findStationName(i);
            if (station.next()) {
                route.append("➜ ").append(station.getString("StationName")).append("\n");
            }
        }
        route.append(String.format("🚇 Станций: %.0f", totalStations));

        info.setText(route.toString());
        controls.getChildren().removeIf(node -> node instanceof Label && 
            !node.equals(controls.getChildren().get(0)));
        controls.getChildren().add(info);
    }

    private void initializeGraph() throws SQLException {
        ResultSet connections = this.subwayData.fetchTravelTimes();
        this.routeGraph = new ArrayList<>();
        while (connections.next()) {
            List<Double> edge = new ArrayList<>();
            edge.add(connections.getDouble("Station1ID"));
            edge.add(connections.getDouble("Station2ID"));
            edge.add(connections.getDouble("TravelTime"));
            this.routeGraph.add(edge);
        }

        adjacencyMatrix = new double[routeGraph.size()][routeGraph.get(0).size()];
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix[0].length; j++) {
                adjacencyMatrix[i][j] = routeGraph.get(i).get(j);
            }
        }

        ResultSet stationCount = this.subwayData.countStations();
        if (stationCount.next()) {
            pathFinder = new PathCalculator(stationCount.getInt("count") + 1, adjacencyMatrix);
        }
    }

    @Override
    public void start(Stage stage) throws IOException, SQLException, ClassNotFoundException {
        this.subwayData = new SubwayData("Metro", "postgres", "62426");
        initializeGraph();

        Group root = new Group();
        Group mapLayer = new Group();
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(10));
        controls.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        renderConnections(mapLayer);
        renderStations(mapLayer);
        setupInterface(controls, mapLayer);

        root.getChildren().addAll(mapLayer, controls);
        Scene scene = new Scene(root, 1024, 700, Color.WHITE);

        stage.setScene(scene);
        stage.setTitle("Карта метро");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}