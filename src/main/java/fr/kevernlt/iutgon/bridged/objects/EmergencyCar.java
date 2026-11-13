package fr.kevernlt.iutgon.bridged.objects;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;

/**
 * Emergency vehicle (SAMU/Ambulance) with highest priority.
 * Occupies 1 segment and causes other vehicles to divert to the emergency lane (when gates are open).
 */
public class EmergencyCar extends WheeledVehicle {
    /**
     * Creates a new emergency vehicle with direction and bridge reference.
     *
     * @param way    direction of travel
     * @param bridge bridge reference
     */
    public EmergencyCar(Way way, Bridge bridge) {
        super(way, bridge);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean isEmergencyVehicle() {
        return true;
    }
}
