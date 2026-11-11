package fr.kevernlt.iutgon.bridged.objects;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;

public class Car extends WheeledVehicle {
    public Car(Way way, Bridge bridge) {
        super(way, bridge);
    }

    @Override
    public int getSize() {
        return 1;
    }
}
