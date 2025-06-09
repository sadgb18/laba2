package com.example.metro;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
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

public class MetroMapApplication extends Application {
    private DataBase database;
    private List<List<Double>> routeData;
    private double[][] routeMatrix;
    private Routes routeGraph;
    private List<Integer> fastestRoute = new ArrayList<>();
    private double totalTime;
    private List<Integer> shortestRoute = new ArrayList<>();
    private double stationCount;

    private static final double STATION_RADIUS = 8;
    private static final double HIGHLIGHT_RADIUS = 12;
    private static final double LINE_WIDTH = 4;
    private static final double HIGHLIGHT_WIDTH = 8;
    private static final Font STATION_FONT = Font.font("Segoe UI", FontWeight.MEDIUM, 14);
    private static final Font ROUTE_INFO_FONT = Font.font("Segoe UI", FontWeight.BOLD, 16);

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
            connection.setStrokeWidth(LINE_WIDTH);
            connection.setOpacity(0.8);
            root.getChildren().add(connection);
        }
    }

    private void renderStations(Group root) throws SQLException {
        ResultSet stations = this.database.getAllStations();
        while (stations.next()) {
            // Создаем внешний круг (обводка)
            Circle stationBorder = new Circle(
                stations.getInt("XCoordinate"),
                stations.getInt("YCoordinate"), 
                STATION_RADIUS + 2
            );
            stationBorder.setFill(Color.WHITE);
            stationBorder.setEffect(new DropShadow(4, Color.GRAY));

            // Создаем внутренний круг (станция)
            Circle stationPoint = new Circle(
                stations.getInt("XCoordinate"),
                stations.getInt("YCoordinate"), 
                STATION_RADIUS
            );
            stationPoint.setFill(Color.BLACK);

            Text stationName = new Text(
                stations.getInt("XCoordinate") - 25,
                stations.getInt("YCoordinate") - 20,
                stations.getString("StationName")
            );
            stationName.setFont(STATION_FONT);
            stationName.setWrappingWidth(100);
            stationName.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            root.getChildren().addAll(stationBorder, stationPoint, stationName);
        }
    }

    private void setupInterface(VBox controls, Group mapRoot) throws SQLException {
        controls.setSpacing(20);
        controls.setPadding(new Insets(20));
        controls.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        controls.setAlignment(Pos.CENTER);

        ResultSet stations = this.database.getAllStations();
        ObservableList<String> stationNames = FXCollections.observableArrayList();
        while (stations.next()) {
            stationNames.add(stations.getString("StationName"));
        }

        Label startLabel = new Label("Начальная станция:");
        startLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        ChoiceBox<String> startStation = new ChoiceBox<>(stationNames);
        startStation.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");

        Label endLabel = new Label("Конечная станция:");
        endLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        ChoiceBox<String> endStation = new ChoiceBox<>(stationNames);
        endStation.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");

        Button findRoute = new Button("Найти маршрут");
        findRoute.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 5;"
        );
        findRoute.setOnMouseEntered(e -> findRoute.setStyle(
            "-fx-background-color: #1976D2;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 5;"
        ));
        findRoute.setOnMouseExited(e -> findRoute.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 5;"
        ));

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

        controls.getChildren().addAll(
            startLabel, startStation,
            endLabel, endStation,
            findRoute
        );
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
                    highlight.setStroke(Color.valueOf("#2196F3"));
                    highlight.setStrokeWidth(HIGHLIGHT_WIDTH);
                    highlight.setOpacity(0.6);
                    root.getChildren().add(highlight);
                }
            }
            ResultSet station = this.database.getStationCoords(fastRoute.get(i));
            if (station.next()) {
                Circle highlight = new Circle(station.getInt("XCoordinate"), 
                                           station.getInt("YCoordinate"), HIGHLIGHT_RADIUS);
                highlight.setFill(Color.valueOf("#2196F3"));
                highlight.setOpacity(0.6);
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
                    highlight.setStroke(Color.valueOf("#4CAF50"));
                    highlight.setStrokeWidth(HIGHLIGHT_WIDTH - 2);
                    highlight.setOpacity(0.6);
                    root.getChildren().add(highlight);
                }
            }
            ResultSet station = this.database.getStationCoords(shortRoute.get(i));
            if (station.next()) {
                Circle highlight = new Circle(station.getInt("XCoordinate"), 
                                           station.getInt("YCoordinate"), HIGHLIGHT_RADIUS - 2);
                highlight.setFill(Color.valueOf("#4CAF50"));
                highlight.setOpacity(0.6);
                root.getChildren().add(highlight);
            }
        }
    }

    private void displayRouteInfo(VBox controls, List<Integer> fastRoute, double time, 
                                List<Integer> shortRoute, double stations) throws SQLException {
        VBox routeInfo = new VBox(15);
        routeInfo.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 15;" +
            "-fx-background-radius: 5;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);"
        );

        // Информация о быстром маршруте
        VBox fastRouteInfo = new VBox(8);
        Label fastRouteLabel = new Label("Самый быстрый маршрут:");
        fastRouteLabel.setFont(ROUTE_INFO_FONT);
        fastRouteLabel.setTextFill(Color.valueOf("#2196F3"));
        
        StringBuilder fastPath = new StringBuilder();
        for (Integer station : fastRoute) {
            ResultSet rs = this.database.getStationById(station);
            if (rs.next()) {
                fastPath.append(rs.getString("StationName")).append(" → ");
            }
        }
        Label fastPathLabel = new Label(fastPath.substring(0, fastPath.length() - 3));
        fastPathLabel.setWrapText(true);
        fastPathLabel.setPrefWidth(280);
        fastPathLabel.setStyle("-fx-font-size: 13px;");
        
        Label timeLabel = new Label(String.format("Время в пути: %.1f мин.", time));
        timeLabel.setStyle("-fx-font-weight: bold;");
        fastRouteInfo.getChildren().addAll(fastRouteLabel, fastPathLabel, timeLabel);

        // Информация о коротком маршруте
        VBox shortRouteInfo = new VBox(8);
        Label shortRouteLabel = new Label("Маршрут с минимальным\nколичеством станций:");
        shortRouteLabel.setFont(ROUTE_INFO_FONT);
        shortRouteLabel.setTextFill(Color.valueOf("#4CAF50"));
        
        StringBuilder shortPath = new StringBuilder();
        for (Integer station : shortRoute) {
            ResultSet rs = this.database.getStationById(station);
            if (rs.next()) {
                shortPath.append(rs.getString("StationName")).append(" → ");
            }
        }
        Label shortPathLabel = new Label(shortPath.substring(0, shortPath.length() - 3));
        shortPathLabel.setWrapText(true);
        shortPathLabel.setPrefWidth(280);
        shortPathLabel.setStyle("-fx-font-size: 13px;");
        
        Label stationsLabel = new Label(String.format("Количество станций: %.0f", stations));
        stationsLabel.setStyle("-fx-font-weight: bold;");
        shortRouteInfo.getChildren().addAll(shortRouteLabel, shortPathLabel, stationsLabel);

        routeInfo.getChildren().addAll(fastRouteInfo, shortRouteInfo);

        // Обновляем информацию о маршруте
        controls.getChildren().removeIf(node -> node instanceof VBox && node != controls);
        controls.getChildren().add(routeInfo);
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
            routeGraph = new Routes(count.getInt("count") + 1, routeMatrix);
        }
    }

    @Override
    public void start(Stage stage) throws IOException, SQLException, ClassNotFoundException {
        this.database = new DataBase("Metro", "postgres", "62426");
        initializeRouteGraph();

        // Создаем корневой контейнер как HBox для размещения карты и контролов по горизонтали
        HBox root = new HBox(20); // 20 пикселей отступ между картой и контролами
        root.setStyle("-fx-background-color: white;");
        root.setPadding(new Insets(20));

        // Контейнер для карты
        Group map = new Group();
        // Создаем контейнер для карты с отступом слева
        HBox mapContainer = new HBox(map);
        mapContainer.setPadding(new Insets(0, 0, 0, 20));
        mapContainer.setMinWidth(900);
        mapContainer.setPrefWidth(900);

        // Контейнер для элементов управления
        VBox controls = new VBox(20);
        controls.setPrefWidth(350);
        controls.setMinWidth(350);
        controls.setMaxWidth(350);
        controls.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 20;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);"
        );

        renderConnections(map);
        renderStations(map);
        setupInterface(controls, map);

        // Добавляем карту и контролы в корневой контейнер
        root.getChildren().addAll(mapContainer, controls);
        
        // Создаем сцену с белым фоном
        Scene scene = new Scene(root, 1300, 800, Color.WHITE);
        
        stage.setScene(scene);
        stage.setTitle("Московский Метрополитен");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}