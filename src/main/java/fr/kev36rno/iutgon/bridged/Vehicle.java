package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.enums.Way;

public class Vehicle extends Thread implements Comparable<Vehicle> {

    private Way way;

    public Vehicle(Way way) {
        this.way = way;
    }

    public Way getWay() {
        return this.way;
    }

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
