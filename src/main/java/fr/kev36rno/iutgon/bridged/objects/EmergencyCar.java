package fr.kev36rno.iutgon.bridged.objects;

import fr.kev36rno.iutgon.bridged.Bridge;
import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

public class EmergencyCar extends WheeledVehicle {
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
