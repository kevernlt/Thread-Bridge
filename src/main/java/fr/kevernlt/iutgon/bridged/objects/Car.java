package fr.kevernlt.iutgon.bridged.objects;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;

/**
 * Standard car occupying 1 road segment.
 */
public class Car extends WheeledVehicle {
    /**
     * Creates a new car with direction and bridge reference.
     *
     * @param way    direction of travel
     * @param bridge bridge reference
     */
    public Car(Way way, Bridge bridge) {
        super(way, bridge);
    }

    @Override
    public int getSize() {
        return 1;
    }
}
