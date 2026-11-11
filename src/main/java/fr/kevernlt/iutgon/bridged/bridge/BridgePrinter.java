package fr.kevernlt.iutgon.bridged.bridge;

import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.Vehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;

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
        boolean isLeftToRightLocked = bridge.getSegmentsForWay(Way.CAR_LEFT_TO_RIGHT).stream().anyMatch(segment -> segment.getVehicleOccupyingSegment() != null && segment.getVehicleOccupyingSegment().isBlockingOtherWay());
        boolean isRightToLeftLocked = bridge.getSegmentsForWay(Way.CAR_RIGHT_TO_LEFT).stream().anyMatch(segment -> segment.getVehicleOccupyingSegment() != null && segment.getVehicleOccupyingSegment().isBlockingOtherWay());
        System.out.println("  ->      " + (isRightToLeftLocked ? "LOCKED" : "OPEN") + "                 " + (isLeftToRightLocked ? "LOCKED" : "OPEN"));

        for (Segment segment : bridge.getSegmentsForWay(Way.CAR_LEFT_TO_RIGHT)) {
            Segment oppositeSegment = bridge.getSegmentsForWay(segment.getWay().getOppositeWay()).get(totalSegments - segment.getSegmentId());

            //pa bo
            String firstColor = resetColor();
            String secondColor = resetColor();
            if(segment.getVehicleOccupyingSegment() != null)
                firstColor = getRandomAnsiColorCodeFromStrings(String.valueOf(segment.getVehicleOccupyingSegment().hashCode()));
            if(oppositeSegment.getVehicleOccupyingSegment() != null)
                secondColor = getRandomAnsiColorCodeFromStrings(String.valueOf(oppositeSegment.getVehicleOccupyingSegment().hashCode()));

            System.out.println("  -> " +
                    segment.getSegmentId() +
                    firstColor +
                    " ["
                    + segment.getStatusMessage() +
                    "]        " +

                    resetColor() +
                    oppositeSegment.getSegmentId() +
                    secondColor +
                    "  ["
                    + oppositeSegment.getStatusMessage() +
                    "]" +
                    resetColor()
            );
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


    String resetColor() {
        return "\u001B[0m";
    }

    private String getRandomAnsiColorCodeFromStrings(String seed) {
        int hash = Math.abs(seed.hashCode());
        int colorCode = 31 + (hash % 6);
        return "\u001B[" + colorCode + "m";
    }
}
