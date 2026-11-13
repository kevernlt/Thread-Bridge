package fr.kevernlt.iutgon.bridged.objects;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;

/**
 * Exceptional convoy load occupying 4 segments and blocking traffic on the opposite direction.
 */
public class ExceptionalLoad extends WheeledVehicle {
    /**
     * Creates a new exceptional load vehicle.
     *
     * @param way    direction of travel
     * @param bridge bridge reference
     */
    public ExceptionalLoad(Way way, Bridge bridge) {
        super(way, bridge);
    }

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public boolean isBlockingOtherWay() {
        return true;
    }
}
