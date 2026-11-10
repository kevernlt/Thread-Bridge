package fr.kev36rno.iutgon.bridged.objects;

import fr.kev36rno.iutgon.bridged.bridge.Bridge;
import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

public class Truck extends WheeledVehicle {
    public Truck(Way way, Bridge bridge) {
        super(way, bridge);
    }

    @Override
    public int getSize() {
        return 2;
    }
}
