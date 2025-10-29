package com.dijkstras.demo;

import java.util.*;

public class DijkstraRouter {

    static class Location {
        String name;
        double accumulatedTime;
        Location parent;

        public Location(String name, double time, Location p) {
            this.name = name;
            this.accumulatedTime = time;
            this.parent = p;
        }
    }

    private final Map<String, Map<String, Double>> graph;

    public DijkstraRouter(Map<String, Map<String, Double>> graph) {
        this.graph = graph;
    }

    public List<String> findOptimalRoute(String start, String goal, double congestionFactor) {
        PriorityQueue<Location> openSet = new PriorityQueue<>(Comparator.comparingDouble(l -> l.accumulatedTime));
        Map<String, Double> minTimes = new HashMap<>();

        Location startLoc = new Location(start, 0.0, null);
        openSet.add(startLoc);
        minTimes.put(start, 0.0);
// 3. Main Loop: Explore the graph
        while (!openSet.isEmpty()) {
            Location current = openSet.poll();

            if (current.name.equals(goal)) {
                return reconstructPath(current);
            }
// Iterate through all neighbors
            for (Map.Entry<String, Double> edge : graph.getOrDefault(current.name, Collections.emptyMap()).entrySet()) {
                String neighborName = edge.getKey();
                double baseTravelTime = edge.getValue();
                // : Apply Cost Logic (Weight + Congestion)
                double edgeTravelTime = baseTravelTime * (1.0 + congestionFactor);
                double newAccumulatedTime = current.accumulatedTime + edgeTravelTime;

                if (newAccumulatedTime < minTimes.getOrDefault(neighborName, Double.MAX_VALUE)) {
                    Location neighbor = new Location(neighborName, newAccumulatedTime, current);
                    minTimes.put(neighborName, newAccumulatedTime);
                    openSet.removeIf(l -> l.name.equals(neighborName));
                    openSet.add(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<String> reconstructPath(Location goalLoc) {
        List<String> path = new LinkedList<>();
        Location current = goalLoc;
        while (current != null) {
            path.add(0, current.name);
            current = current.parent;
        }
        return path;
    }
}