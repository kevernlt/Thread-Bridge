package fr.kevernlt.iutgon.bridged.objects;

import fr.kevernlt.iutgon.bridged.FloatingVehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.bridge.Bridge;

public class Boat extends FloatingVehicle {
    public Boat(Way way,Bridge bridge) {
        super(way,bridge);
    }
}
