package fr.kev36rno.iutgon.bridged.enums;

import fr.kev36rno.iutgon.bridged.FloatingVehicle;
import fr.kev36rno.iutgon.bridged.Vehicle;
import fr.kev36rno.iutgon.bridged.WheeledVehicle;

public enum Way {
    CAR_LEFT_TO_RIGHT,
    CAR_RIGHT_TO_LEFT,
    BOAT_NORTH,
    BOAT_SOUTH;

    public Way getOppositeWay() {
        return switch (this) {
            case CAR_LEFT_TO_RIGHT -> CAR_RIGHT_TO_LEFT;
            case CAR_RIGHT_TO_LEFT -> CAR_LEFT_TO_RIGHT;
            case BOAT_NORTH -> BOAT_SOUTH;
            case BOAT_SOUTH -> BOAT_NORTH;
        };
    }

    public boolean isLogic(Vehicle vehicle) {
         if(vehicle instanceof FloatingVehicle && (this == Way.BOAT_NORTH || this == Way.BOAT_SOUTH)) {
             return true;
         }
         if(vehicle instanceof WheeledVehicle && (this == Way.CAR_LEFT_TO_RIGHT || this == Way.CAR_RIGHT_TO_LEFT)) {
             return true;
         }
         return false;
    }
}
