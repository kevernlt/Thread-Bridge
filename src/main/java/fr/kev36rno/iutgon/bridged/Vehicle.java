package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.enums.Way;

public class Vehicle extends Thread {

    private Way way;

    public Vehicle(Way way) {
        this.way = way;
    }

    public Way getWay() {
        return this.way;
    }
}
