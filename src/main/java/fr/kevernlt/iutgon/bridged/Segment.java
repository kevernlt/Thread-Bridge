package fr.kevernlt.iutgon.bridged;

import fr.kevernlt.iutgon.bridged.enums.Way;

public class Segment {
    private final int segmentId;
    private final Way way;
    private WheeledVehicle vehicleOccupyingSegment;
    private WheeledVehicle emergencyLaneVehicle;

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

    public WheeledVehicle getEmergencyLaneVehicle() {
        return emergencyLaneVehicle;
    }

    public void setEmergencyLaneVehicle(WheeledVehicle emergencyLaneVehicle) {
        this.emergencyLaneVehicle = emergencyLaneVehicle;
    }

    public boolean hasEmergencyLaneVehicle() {
        return emergencyLaneVehicle != null;
    }

    public String getStatusMessage() {
        return this.vehicleOccupyingSegment == null ? "     FREE     " :
                this.vehicleOccupyingSegment.isEmergencyVehicle() ? "OCCUPIED [EM] " :
                        this.vehicleOccupyingSegment.isOnEmergencyLane() ? "OCCUPIED [BAU] " :
                "   OCCUPIED   ";
    }

    public String getEmergencyLaneStatusMessage() {
        return hasEmergencyLaneVehicle() ? emergencyLaneVehicle.getClass().getSimpleName() : " FREE ";
    }

    @Override
    public int hashCode() {
        return segmentId + way.ordinal();
    }

    @Override
    public String toString() {
        return "S" + segmentId + "(" + way + ")";
    }
}
