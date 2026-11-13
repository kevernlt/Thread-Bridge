package fr.kevernlt.iutgon.bridged;

import fr.kevernlt.iutgon.bridged.enums.Way;

/**
 * Represents a single road segment on the bridge.
 * A segment can be occupied by a wheeled vehicle on the main lane
 * or on the emergency lane (BAU).
 */
public class Segment {
    private final int segmentId;
    private final Way way;
    private WheeledVehicle vehicleOccupyingSegment;
    private WheeledVehicle emergencyLaneVehicle;

    /**
     * Creates a new segment with an id and a traffic direction.
     *
     * @param segmentId segment number
     * @param way       traffic direction
     */
    public Segment(int segmentId, Way way) {
        this.segmentId = segmentId;
        this.way = way;
    }

    /**
     * Returns the segment ID.
     *
     * @return segment ID
     */
    public int getSegmentId() {
        return segmentId;
    }

    /**
     * Returns the traffic direction of this segment.
     *
     * @return the way
     */
    public Way getWay() {
        return way;
    }

    /**
     * Returns the vehicle currently on the main lane of this segment.
     *
     * @return occupying vehicle, or null if empty
     */
    public WheeledVehicle getVehicleOccupyingSegment() {
        return vehicleOccupyingSegment;
    }

    /**
     * Sets the vehicle occupying the main lane of this segment.
     *
     * @param vehicleOccupyingSegment vehicle to place on this segment
     */
    public void setVehicleOccupyingSegment(WheeledVehicle vehicleOccupyingSegment) {
        this.vehicleOccupyingSegment = vehicleOccupyingSegment;
    }

    /**
     * Returns the vehicle parked on the emergency lane of this segment.
     *
     * @return emergency lane vehicle, or null if none
     */
    public WheeledVehicle getEmergencyLaneVehicle() {
        return emergencyLaneVehicle;
    }

    /**
     * Sets the vehicle parked on the emergency lane of this segment.
     *
     * @param emergencyLaneVehicle vehicle on emergency lane
     */
    public void setEmergencyLaneVehicle(WheeledVehicle emergencyLaneVehicle) {
        this.emergencyLaneVehicle = emergencyLaneVehicle;
    }

    /**
     * Checks if there is a vehicle on the emergency lane of this segment.
     *
     * @return true if emergency lane is occupied
     */
    public boolean hasEmergencyLaneVehicle() {
        return emergencyLaneVehicle != null;
    }

    /**
     * Returns a string representing the status of the main lane.
     *
     * @return status message
     */
    public String getStatusMessage() {
        return this.vehicleOccupyingSegment == null ? "     FREE     " :
                this.vehicleOccupyingSegment.isEmergencyVehicle() ? "OCCUPIED [EM] " :
                        this.vehicleOccupyingSegment.isOnEmergencyLane() ? "OCCUPIED [BAU] " :
                "   OCCUPIED   ";
    }

    /**
     * Returns a string representing the status of the emergency lane.
     *
     * @return emergency lane status message
     */
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
