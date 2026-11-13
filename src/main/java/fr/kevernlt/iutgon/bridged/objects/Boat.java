package fr.kevernlt.iutgon.bridged.objects;

import fr.kevernlt.iutgon.bridged.FloatingVehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.bridge.Bridge;

/**
 * Concrete boat navigating on the river under the bridge.
 */
public class Boat extends FloatingVehicle {
    /**
     * Creates a new boat with navigation way and bridge reference.
     *
     * @param way    navigation way (BOAT_NORTH or BOAT_SOUTH)
     * @param bridge bridge reference
     */
    public Boat(Way way, Bridge bridge) {
        super(way, bridge);
    }
}
