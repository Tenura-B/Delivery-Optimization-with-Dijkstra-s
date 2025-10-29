package com.dijkstras.demo;


public class DeliveryOrder {
    private String orderId;
    private String destination;
    private int initialDeadlineMinutes;
    private int importanceLevel;
    public int currentDelayMinutes;


    public DeliveryOrder() {}

    public DeliveryOrder(String orderId, String destination, int initialDeadlineMinutes, int importanceLevel) {
        this.orderId = orderId;
        this.destination = destination;
        this.initialDeadlineMinutes = initialDeadlineMinutes;
        this.importanceLevel = importanceLevel;
        this.currentDelayMinutes = 0;
    }


    public double calculateDPS(int currentMinutes) {

        final double W_URGENCY = 5.0;
        final double W_IMPORTANCE = 10.0;
        int timeRemaining = initialDeadlineMinutes - currentMinutes;
        double urgency = (timeRemaining > 0) ? (100.0 / timeRemaining) : 1000.0;
        double importance = (double) importanceLevel;
        return (W_URGENCY * urgency) + (W_IMPORTANCE * importance) + currentDelayMinutes;
    }

    public String getDestination() { return destination; }
    public String getOrderId() { return orderId; }
    public void setCurrentDelayMinutes(int delay) { this.currentDelayMinutes = delay; }

}