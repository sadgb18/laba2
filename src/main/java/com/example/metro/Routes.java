package com.example.metro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Routes {
    private double[][] distances;
    private double[][] steps;
    private boolean[] visited;
    private double[] minDistances;

    Routes(int size, double[][] edges) {
        this.distances = new double[size][size];
        this.steps = new double[size][size];

        for (double[] edge : edges) {
            try {
                if (edge.length != 3) {
                    throw new RuntimeException("Неверный формат данных ребра графа");
                }
                int from = (int) edge[0];
                int to = (int) edge[1];
                double weight = edge[2];
                distances[from][to] = weight;
                distances[to][from] = weight;
                steps[from][to] = 1;
                steps[to][from] = 1;
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при создании графа", e);
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (distances[i][j] == 0 && i != j) {
                    distances[i][j] = Double.POSITIVE_INFINITY;
                    steps[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    public List<Integer> ShortestWay(int start, int end) {
        int size = distances.length;
        visited = new boolean[size];
        minDistances = new double[size];
        int[] prev = new int[size];

        Arrays.fill(minDistances, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        minDistances[start] = 0;

        for (int i = 0; i < size - 1; i++) {
            int current = findMinDistanceVertex();
            if (current == -1) break;

            visited[current] = true;

            for (int v = 0; v < size; v++) {
                if (!visited[v] && distances[current][v] != Double.POSITIVE_INFINITY) {
                    double newDist = minDistances[current] + distances[current][v];
                    if (newDist < minDistances[v]) {
                        minDistances[v] = newDist;
                        prev[v] = current;
                    }
                }
            }
        }

        return reconstructPath(start, end, prev);
    }

    private int findMinDistanceVertex() {
        double min = Double.POSITIVE_INFINITY;
        int minVertex = -1;

        for (int v = 0; v < distances.length; v++) {
            if (!visited[v] && minDistances[v] < min) {
                min = minDistances[v];
                minVertex = v;
            }
        }
        return minVertex;
    }

    private List<Integer> reconstructPath(int start, int end, int[] prev) {
        List<Integer> path = new ArrayList<>();
        for (int at = end; at != -1; at = prev[at]) {
            path.add(0, at);
        }
        return path;
    }

    public double ShortestDistance(int start, int end) {
        ShortestWay(start, end);
        return minDistances[end];
    }

    public List<Integer> MinVertexWay(int start, int end) {
        int size = steps.length;
        visited = new boolean[size];
        minDistances = new double[size];
        int[] prev = new int[size];

        Arrays.fill(minDistances, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        minDistances[start] = 0;

        for (int i = 0; i < size - 1; i++) {
            int current = findMinDistanceVertex();
            if (current == -1) break;

            visited[current] = true;

            for (int v = 0; v < size; v++) {
                if (!visited[v] && steps[current][v] != Double.POSITIVE_INFINITY) {
                    double newDist = minDistances[current] + steps[current][v];
                    if (newDist < minDistances[v]) {
                        minDistances[v] = newDist;
                        prev[v] = current;
                    }
                }
            }
        }

        return reconstructPath(start, end, prev);
    }

    public double MinVertex(int start, int end) {
        MinVertexWay(start, end);
        return minDistances[end];
    }

    @Override
    public String toString() {
        String str = "";
        for (double[] row : distances) {
            for (double val : row) {
                str += val + " ";
            }
            str += "\n";
        }
        str += "\n";
        for (double val : minDistances) {
            str += val + " ";
        }
        str += "\n";
        return str;
    }
}