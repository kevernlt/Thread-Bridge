package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.enums.Way;

public class WheeledVehicle extends Vehicle {

    // size in platforms: in consignee it say "un camion fait 2 platforme" and "une voiture fait une platforme"
    public WheeledVehicle(Way way) {
        super(way);
    }

    public int getSize() {
        return 0;
    }

    public boolean isBlockingOtherWay() {
        return false;
    }

    public boolean isEmergencyVehicle() {
        return false;
    }
}
