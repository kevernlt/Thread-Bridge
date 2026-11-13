package fr.kevernlt.iutgon.bridged.objects;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;

/**
 * Truck occupying 2 road segments.
 */
public class Truck extends WheeledVehicle {
    /**
     * Creates a new truck with direction and bridge reference.
     *
     * @param way    direction of travel
     * @param bridge bridge reference
     */
    public Truck(Way way, Bridge bridge) {
        super(way, bridge);
    }

    @Override
    public int getSize() {
        return 2;
    }
}
