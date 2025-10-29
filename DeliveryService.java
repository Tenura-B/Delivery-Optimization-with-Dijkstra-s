package com.dijkstras.demo;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DeliveryService {


    private final Map<String, Map<String, Double>> graph = createPredefinedGraph();
    private final DijkstraRouter router = new DijkstraRouter(graph);
    private final PriorityQueue<DeliveryOrder> orderQueue;
    private int currentMinutes = 0;
    private String currentVehicleLocation = "DEPOT";


    public static final Map<String, double[]> GPS_COORDINATES = Map.of(
            "DEPOT", new double[]{40.7306, -73.9866},
            "A", new double[]{40.7580, -73.9855},
            "B", new double[]{40.7813, -73.9660},
            "C", new double[]{40.7128, -74.0060},
            "D", new double[]{40.7410, -73.9989}
    );

    public DeliveryService() {
        // Initialize the Priority Queue with the DPS comparator logic
        this.orderQueue = new PriorityQueue<>((o1, o2) -> {
            double dps1 = o1.calculateDPS(currentMinutes);
            double dps2 = o2.calculateDPS(currentMinutes);
            return Double.compare(dps2, dps1);
        });
        System.out.println("[Service] Delivery Service Initialized.");
    }


    private Map<String, Map<String, Double>> createPredefinedGraph() {
        Map<String, Map<String, Double>> map = new HashMap<>();
        map.put("DEPOT", Map.of("A", 10.0, "B", 15.0));
        map.put("A", Map.of("DEPOT", 10.0, "B", 5.0, "C", 20.0));
        map.put("B", Map.of("DEPOT", 15.0, "A", 5.0, "D", 12.0));
        map.put("C", Map.of("A", 20.0, "D", 8.0));
        map.put("D", Map.of("B", 12.0, "C", 8.0));
        return map;
    }


    public void addOrder(DeliveryOrder order) throws IllegalArgumentException {
        String dest = order.getDestination().toUpperCase();
        if (!graph.containsKey(dest) || !GPS_COORDINATES.containsKey(dest)) {
            throw new IllegalArgumentException("Destination " + dest + " not found.");
        }
        orderQueue.add(order);
        System.out.printf("[LOG] Added Order %s. Queue size: %d\n", order.getOrderId(), orderQueue.size());
    }

    public List<RouteResult> runSimulation(String startLocation, double congestionFactor) throws IllegalStateException {
        if (orderQueue.isEmpty()) {
            throw new IllegalStateException("Order queue is empty.");
        }

        // Reset state for a fresh run
        this.currentVehicleLocation = startLocation.toUpperCase();
        this.currentMinutes = 0;
        List<RouteResult> results = new ArrayList<>();

        System.out.println("\n--- SIMULATION STARTED ---");

        // The core simulation loop (transferred from the old handleStartSimulation)
        while (!orderQueue.isEmpty()) {
            // Re-sort logic
            List<DeliveryOrder> tempOrders = new ArrayList<>(orderQueue);
            orderQueue.clear();
            orderQueue.addAll(tempOrders);

            DeliveryOrder nextOrder = orderQueue.poll();
            String previousLocation = currentVehicleLocation;

            List<String> route = router.findOptimalRoute(previousLocation, nextOrder.getDestination(), congestionFactor);

            if (route.isEmpty()) {
                System.out.printf("[ERROR] No route found to %s. Skipping order %s\n", nextOrder.getDestination(), nextOrder.getOrderId());
                continue;
            }

            double routeTimeBase = calculateRouteBaseTime(route);
            int travelTime = (int) Math.ceil(routeTimeBase * (1.0 + congestionFactor));

            // Collect results
            results.add(new RouteResult(
                    nextOrder.getOrderId(),
                    route,
                    travelTime,
                    currentMinutes,
                    nextOrder.calculateDPS(currentMinutes)
            ));

            // Update state
            currentMinutes += travelTime;
            currentVehicleLocation = nextOrder.getDestination();
            orderQueue.forEach(o -> o.setCurrentDelayMinutes(o.currentDelayMinutes + travelTime));
        }

        System.out.printf("--- SIMULATION ENDED --- Total Time: %d min\n", currentMinutes);
        return results;
    }

    private double calculateRouteBaseTime(List<String> route) {
        // ... (Logic as before) ...
        if (route.size() < 2) return 0.0;
        double totalTime = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            totalTime += graph.get(route.get(i)).getOrDefault(route.get(i + 1), 0.0);
        }
        return totalTime;
    }

    // Helper class for JSON response
    public static class RouteResult {
        public String orderId;
        public List<String> route;
        public List<double[]> gpsRoute;
        public int travelTime;
        public int startTime;
        public double dps;

        public RouteResult(String orderId, List<String> route, int travelTime, int startTime, double dps) {
            this.orderId = orderId;
            this.route = route;
            this.travelTime = travelTime;
            this.startTime = startTime;
            this.dps = dps;
            // Use the service's static map for conversion
            this.gpsRoute = route.stream()
                    .map(GPS_COORDINATES::get)
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
        }
    }
}