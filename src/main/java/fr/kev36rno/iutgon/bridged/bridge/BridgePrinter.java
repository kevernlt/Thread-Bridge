package fr.kev36rno.iutgon.bridged.bridge;

import fr.kev36rno.iutgon.bridged.Segment;
import fr.kev36rno.iutgon.bridged.Vehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

import java.util.Map;
import java.util.PriorityQueue;

public class BridgePrinter {

    private final Bridge bridge;

    public BridgePrinter(Bridge bridge) {
        this.bridge = bridge;
    }

    public void printCurrentState() {
        clearOutput();
        System.out.println("Current bridge state: " + bridge.getCurrentState());

        System.out.println("Cars segments:");

        int totalSegments = bridge.getSegmentsForWay(Way.CAR_LEFT_TO_RIGHT).size();
        for (Segment segment : bridge.getSegmentsForWay(Way.CAR_LEFT_TO_RIGHT)) {
            Segment oppositeSegment = bridge.getSegmentsForWay(segment.getWay().getOppositeWay()).get(totalSegments - segment.getSegmentId());
            System.out.println("  -> " + segment.getSegmentId() + " [" + segment.getStatusMessage() + "]        " + oppositeSegment.getSegmentId() + "  [" + oppositeSegment.getStatusMessage() + "]");
        }
        System.out.println("Vehicles at bridge entry:");
        for (Map.Entry<Way, PriorityQueue<Vehicle>> entry : bridge.getVehicleAtBridgeEntry().entrySet()) {
            System.out.println("  -> " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("-----------------------------------");
    }

    public void clearOutput() {
        System.out.println("\033[2J");
        System.out.flush();
    }
}
