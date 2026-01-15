package server;

import entity.unit.FireMage;
import entity.unit.Unit;
import entity.unit.WaterMage;
import entity.unit.WindMage;
import protocol.UnitType;

public class UnitFactory {

    public static Unit create(UnitType type, int playerId) {
        Unit unit = switch (type) {
            case WATER_MAGE -> new WaterMage();
            case FIRE_MAGE -> new FireMage();
            case WIND_MAGE -> new WindMage();
        };
        unit.setPlayerId(playerId);
        return unit;
    }
}
