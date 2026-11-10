package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.enums.BridgeState;
import fr.kev36rno.iutgon.bridged.enums.Way;

import java.util.*;

public class Bridge {
    private BridgeState currentState;

    private final List<Segment> segments;

    private final HashMap<Way, PriorityQueue<Vehicle>> vehicleAtBridgeEntry = new HashMap<>();

    public Bridge() {
        this.segments = new ArrayList<>();
        this.currentState = BridgeState.CLOSED;
    }

    public void printCurrentState() {
        clearOutput();
        System.out.println("Current bridge state: " + currentState);
        System.out.println("Segments:");
        for (Segment segment : segments) {
            System.out.println(segment);
        }
        System.out.println("-----------------------------------");
    }

    public void clearOutput() {
        System.out.println("\033[2J");
        System.out.flush();
    }

    public void setCurrentState(BridgeState currentState) {
        this.currentState = currentState;
    }

    public BridgeState getCurrentState() {
        return currentState;
    }

    public void addSegment(Segment... segments) {
        this.segments.addAll(Arrays.asList(segments));
    }

    public void addVehicle(Vehicle vehicle) {
        vehicleAtBridgeEntry.computeIfAbsent(vehicle.getWay(), k -> new PriorityQueue<>()).add(vehicle);
    }

    public void processSegments() {

    }
}
