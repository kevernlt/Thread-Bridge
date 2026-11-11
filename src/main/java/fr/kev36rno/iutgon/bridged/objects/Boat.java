package fr.kev36rno.iutgon.bridged.objects;

import fr.kev36rno.iutgon.bridged.FloatingVehicle;
import fr.kev36rno.iutgon.bridged.bridge.Bridge;
import fr.kev36rno.iutgon.bridged.enums.Way;

public class Boat extends FloatingVehicle {
    public Boat(Way way, Bridge bridge) {
        super(way, bridge);
    }

    public Boat(Way way) {
        super(way, null);
    }
}
