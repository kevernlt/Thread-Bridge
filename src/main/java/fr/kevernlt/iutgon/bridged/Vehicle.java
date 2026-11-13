package fr.kevernlt.iutgon.bridged;

import fr.kevernlt.iutgon.bridged.enums.Way;

/**
 * Base abstract class for all vehicles (wheeled vehicles and floating boats).
 * Extends Thread so each vehicle can move autonomously.
 */
public class Vehicle extends Thread implements Comparable<Vehicle> {

    private Way way;

    /**
     * Creates a new vehicle moving in a given direction.
     *
     * @param way the direction of travel
     */
    public Vehicle(Way way) {
        this.way = way;
    }

    /**
     * Returns the direction of travel for this vehicle.
     *
     * @return the way
     */
    public Way getWay() {
        return this.way;
    }

    /**
     * Compares two vehicles for priority queues.
     *
     * @param o the other vehicle
     * @return negative if this vehicle has higher priority, positive if lower, 0 if equal
     */
    @Override
    public int compareTo(Vehicle o) {
        if (this instanceof WheeledVehicle wv1 && o instanceof WheeledVehicle wv2) {
            return wv1.compareTo(wv2);
        }
        if (this instanceof FloatingVehicle fv1 && o instanceof FloatingVehicle fv2) {
            return fv1.compareTo(fv2);
        }
        return 0;
    }
}
