package fr.kev36rno.iutgon.bridged.bridge;

import fr.kev36rno.iutgon.bridged.Segment;
import fr.kev36rno.iutgon.bridged.Vehicle;
import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class BridgePrinter {

    private final Bridge bridge;

    public BridgePrinter(Bridge bridge) {
        this.bridge = bridge;
    }

    public void printCurrentState() {
        clearOutput();
        boolean urgL2R = bridge.isEmergencyVehicleActive(Way.CAR_LEFT_TO_RIGHT);
        boolean urgR2L = bridge.isEmergencyVehicleActive(Way.CAR_RIGHT_TO_LEFT);
        String urgBanner = "";
        if (urgL2R || urgR2L) {
            urgBanner = " | \u001B[31mEmergency vehicle passing" + (urgL2R ? " [L->R]" : "") + (urgR2L ? " [R->L]" : "") + "\u001B[0m";
        }

        System.out.println("Current bridge state: " + bridge.getCurrentState() + " | Roads gate: " + (bridge.isRoadsGateClosed() ? "CLOSED" : "OPEN") + urgBanner);

        System.out.println("Cars segments (Main Road & BAU):");

        List<Segment> leftToRightSegments = bridge.getSegmentsForWay(Way.CAR_LEFT_TO_RIGHT);
        List<Segment> rightToLeftSegments = bridge.getSegmentsForWay(Way.CAR_RIGHT_TO_LEFT);
        int totalSegments = leftToRightSegments.size();

        boolean isLeftToRightLocked = leftToRightSegments.stream().anyMatch(segment -> segment.getVehicleOccupyingSegment() != null && segment.getVehicleOccupyingSegment().isBlockingOtherWay());
        boolean isRightToLeftLocked = rightToLeftSegments.stream().anyMatch(segment -> segment.getVehicleOccupyingSegment() != null && segment.getVehicleOccupyingSegment().isBlockingOtherWay());
        System.out.println("  ->      " + (isRightToLeftLocked ? "LOCKED" : "OPEN") + "                 " + (isLeftToRightLocked ? "LOCKED" : "OPEN"));

        for (Segment segment : leftToRightSegments) {
            int oppositeIndex = totalSegments - segment.getSegmentId();
            Segment oppositeSegment = (oppositeIndex >= 0 && oppositeIndex < rightToLeftSegments.size())
                    ? rightToLeftSegments.get(oppositeIndex)
                    : null;

            String firstColor = resetColor();
            String secondColor = resetColor();
            if (segment.getVehicleOccupyingSegment() != null)
                firstColor = getRandomAnsiColorCodeFromStrings(String.valueOf(segment.getVehicleOccupyingSegment().hashCode()));
            if (oppositeSegment != null && oppositeSegment.getVehicleOccupyingSegment() != null)
                secondColor = getRandomAnsiColorCodeFromStrings(String.valueOf(oppositeSegment.getVehicleOccupyingSegment().hashCode()));

            String firstUrg = segment.hasEmergencyLaneVehicle()
                    ? "\u001B[33m(BAU: " + segment.getEmergencyLaneVehicle().getClass().getSimpleName() + ")\u001B[0m"
                    : "              ";
            String secondUrg = (oppositeSegment != null && oppositeSegment.hasEmergencyLaneVehicle())
                    ? "\u001B[33m(BAU: " + oppositeSegment.getEmergencyLaneVehicle().getClass().getSimpleName() + ")\u001B[0m"
                    : "";

            String oppMsg = oppositeSegment != null
                    ? resetColor() + oppositeSegment.getSegmentId() + secondColor + "  [" + oppositeSegment.getStatusMessage() + "] " + secondUrg + resetColor()
                    : "";

            System.out.println("  -> " +
                    segment.getSegmentId() +
                    firstColor +
                    " ["
                    + segment.getStatusMessage() +
                    "] "
                    + firstUrg +
                    "    " +
                    oppMsg
            );
        }

        System.out.println("Vehicles at bridge entry:");
        for (Map.Entry<Way, PriorityQueue<Vehicle>> entry : bridge.getVehicleAtBridgeEntry().entrySet()) {
            System.out.println("  -> " + entry.getKey() + ": " + entry.getValue());
        }

        List<WheeledVehicle> urgL2RVehicles = bridge.getVehiclesOnEmergencyLane(Way.CAR_LEFT_TO_RIGHT);
        List<WheeledVehicle> urgR2LVehicles = bridge.getVehiclesOnEmergencyLane(Way.CAR_RIGHT_TO_LEFT);
        if (!urgL2RVehicles.isEmpty() || !urgR2LVehicles.isEmpty()) {
            System.out.println("Vehicles on emergency lane:");
            if (!urgL2RVehicles.isEmpty()) {
                System.out.println("  -> CAR_LEFT_TO_RIGHT: " + urgL2RVehicles);
            }
            if (!urgR2LVehicles.isEmpty()) {
                System.out.println("  -> CAR_RIGHT_TO_LEFT: " + urgR2LVehicles);
            }
        }

        System.out.println("-----------------------------------");
    }

    public void clearOutput() {
        System.out.print("\033[H\033[2J");
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
