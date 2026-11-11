package fr.kevernlt.iutgon.bridged.enums;

import fr.kevernlt.iutgon.bridged.FloatingVehicle;
import fr.kevernlt.iutgon.bridged.Vehicle;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;

/**
 * Traffic directions for both road vehicles and boats.
 */
public enum Way {
    CAR_LEFT_TO_RIGHT,
    CAR_RIGHT_TO_LEFT,
    BOAT_NORTH,
    BOAT_SOUTH;

    /**
     * Returns the opposite direction.
     *
     * @return opposite way
     */
    public Way getOppositeWay() {
        return switch (this) {
            case CAR_LEFT_TO_RIGHT -> CAR_RIGHT_TO_LEFT;
            case CAR_RIGHT_TO_LEFT -> CAR_LEFT_TO_RIGHT;
            case BOAT_NORTH -> BOAT_SOUTH;
            case BOAT_SOUTH -> BOAT_NORTH;
        };
    }

    /**
     * Verifies if this direction is compatible with the given vehicle type.
     *
     * @param vehicle vehicle to check
     * @return true if compatible
     */
    public boolean isLogic(Vehicle vehicle) {
        if (vehicle instanceof FloatingVehicle && (this == Way.BOAT_NORTH || this == Way.BOAT_SOUTH)) {
            return true;
        }
        if (vehicle instanceof WheeledVehicle && (this == Way.CAR_LEFT_TO_RIGHT || this == Way.CAR_RIGHT_TO_LEFT)) {
            return true;
        }
        return false;
    }
}
