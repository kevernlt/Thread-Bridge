package fr.kev36rno.iutgon.bridged.objects;

import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

public class EmergencyCar extends WheeledVehicle {
    public EmergencyCar(Way way) {
        super(way);
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
