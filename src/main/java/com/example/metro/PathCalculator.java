package com.example.metro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathCalculator {
    // Таблица связей между точками (вершинами), где числа — это стоимости путей
    private double[][] weightMatrix;
    // Такая же таблица, но вместо стоимости стоит 1 (считаем шаги между точками)
    private double[][] stepMatrix;
    // Список, где отмечаем, какие точки уже проверили в алгоритме Дейкстры
    private boolean[] visited;
    // Массив минимальных расстояний от начальной точки до всех остальных
    private double[] distances;

    // Конструктор: создаём граф из данных
    public PathCalculator(int vertexCount, double[][] data) {
        // Создаём таблицы связей нужного размера
        this.weightMatrix = new double[vertexCount][vertexCount];
        this.stepMatrix = new double[vertexCount][vertexCount];

        // Заполняем таблицы данными из входного массива
        for (double[] edge : data) {
            try {
                // Проверяем, что строка содержит [от, до, вес] (3 числа)
                if (edge.length != 3) {
                    throw new RuntimeException("Invalid data format");
                }
                // Добавляем связь в обе таблицы (граф неориентированный)
                int from = (int) edge[0];
                int to = (int) edge[1];
                double weight = edge[2];
                weightMatrix[from][to] = weight;
                weightMatrix[to][from] = weight;
                stepMatrix[from][to] = 1;
                stepMatrix[to][from] = 1;
            } catch (Exception e) {
                throw new RuntimeException("Error in graph data");
            }
        }

        // Заменяем нули на "бесконечность" (нет связи), диагонали на 0
        for (int i = 0; i < vertexCount; i++) {
            for (int j = 0; j < vertexCount; j++) {
                if (weightMatrix[i][j] == 0) {
                    weightMatrix[i][j] = Double.POSITIVE_INFINITY;
                    stepMatrix[i][j] = Double.POSITIVE_INFINITY;
                }
                if (i == j) { // Себя в себя не идём (нулевые диагонали)
                    weightMatrix[i][j] = 0;
                    stepMatrix[i][j] = 0;
                }
            }
        }

        // Инициализируем вспомогательные массивы
        visited = new boolean[vertexCount];
        Arrays.fill(visited, false); // Все вершины не посещены
        distances = new double[vertexCount];
        Arrays.fill(distances, Double.POSITIVE_INFINITY); // Расстояния = бесконечность
    }

    // Алгоритм Дейкстры: кратчайший путь по стоимости
    public double calculateTotalTime(int start, int end) {
        // Сбрасываем пометки и расстояния
        Arrays.fill(visited, false);
        Arrays.fill(distances, Double.POSITIVE_INFINITY);
        distances[start] = 0; // Расстояние до старта = 0
        int current = start;

        // Основной цикл алгоритма
        for (int i = 0; i < distances.length; i++) {
            visited[current] = true; // Помечаем текущую вершину как посещённую
            double min = Double.POSITIVE_INFINITY;
            int minIdx = current;

            // Обновляем расстояния до соседей
            for (int j = 0; j < visited.length; j++) {
                if (!visited[j] &&
                        (distances[current] + weightMatrix[current][j] < distances[j])) {
                    distances[j] = distances[current] + weightMatrix[current][j];
                }
                // Ищем следующую вершину с минимальным расстоянием
                if (!visited[j] && distances[j] <= min) {
                    min = distances[j];
                    minIdx = j;
                }
            }
            current = minIdx;
        }
        return distances[end]; // Возвращаем расстояние до конца
    }

    // Восстанавливаем путь по стоимости
    public List<Integer> findFastestRoute(int start, int end) {
        calculateTotalTime(start, end); // Сначала вычисляем расстояния
        Arrays.fill(visited, false);
        int current = end;
        List<Integer> route = new ArrayList<>();
        route.addFirst(current); // Начинаем с конца

        // Идём обратно от конца к началу
        for (int i = 0; i < distances.length; i++) {
            visited[current] = true;
            for (int j = 0; j < visited.length; j++) {
                // Ищем предыдущую вершину в пути
                if (!visited[j] && distances[current] - distances[j] == weightMatrix[current][j]) {
                    current = j;
                    route.addFirst(current); // Добавляем в начало списка
                    break;
                }
            }
            if (current == end) break; // Выход, если путь не найден
        }
        return route; // Возвращаем путь
    }

    // Алгоритм Дейкстры: минимальное количество вершин
    public double calculateTotalStations(int start, int end) {
        Arrays.fill(visited, false);
        Arrays.fill(distances, Double.POSITIVE_INFINITY);
        distances[start] = 0;
        int current = start;

        for (int i = 0; i < distances.length; i++) {
            visited[current] = true;
            double min = Double.POSITIVE_INFINITY;
            int minIdx = current;

            // Обновляем счётчик шагов
            for (int j = 0; j < visited.length; j++) {
                if (!visited[j] &&
                        distances[current] + stepMatrix[current][j] < distances[j]) {
                    distances[j] = distances[current] + stepMatrix[current][j];
                }
                if (!visited[j] && distances[j] <= min) {
                    min = distances[j];
                    minIdx = j;
                }
            }
            current = minIdx;
        }
        return distances[end]; // Возвращаем количество шагов
    }

    // Восстанавливаем путь по количеству вершин
    public List<Integer> findShortestRoute(int start, int end) {
        calculateTotalStations(start, end); // Сначала вычисляем шаги
        Arrays.fill(visited, false);
        int current = end;
        List<Integer> route = new ArrayList<>();
        route.addFirst(current);

        for (int i = 0; i < distances.length; i++) {
            visited[current] = true;
            for (int j = 0; j < visited.length; j++) {
                // Ищем предыдущую вершину по шагам
                if (!visited[j] && distances[current] - distances[j] == stepMatrix[current][j]) {
                    current = j;
                    route.addFirst(current);
                    break;
                }
            }
            if (current == end) break;
        }
        return route;
    }

    // Отладочный вывод матрицы и расстояний
    @Override
    public String toString() {
        String str = "";
        // Выводим основную матрицу связей
        for (double[] row : weightMatrix) {
            for (double val : row) {
                str += val + " ";
            }
            str += "\n";
        }
        str += "\n";
        // Выводим текущие расстояния
        for (double val : distances) {
            str += val + " ";
        }
        str += "\n";
        return str;
    }
}