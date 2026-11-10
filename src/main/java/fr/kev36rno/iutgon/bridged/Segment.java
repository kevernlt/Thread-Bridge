package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.enums.Way;

public class Segment {
    private final int segmentId;
    private final Way way;
    private WheeledVehicle vehicleOccupyingSegment;

    public Segment(int segmentId, Way way) {
        this.segmentId = segmentId;
        this.way = way;
    }

    public int getSegmentId() {
        return segmentId;
    }

    public Way getWay() {
        return way;
    }

    public WheeledVehicle getVehicleOccupyingSegment() {
        return vehicleOccupyingSegment;
    }

    public void setVehicleOccupyingSegment(WheeledVehicle vehicleOccupyingSegment) {
        this.vehicleOccupyingSegment = vehicleOccupyingSegment;
    }

    public String getStatusMessage() {
        return this.vehicleOccupyingSegment == null ? "  FREE  " : "OCCUPIED";
    }
}
