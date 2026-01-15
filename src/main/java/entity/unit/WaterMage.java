package entity.unit;

import entity.spell.*;

public class WaterMage extends Unit {
    public WaterMage() {
        hp = 3;
        maxHp = 3;
        spells.add(new MoveSpell());
        spells.add(new TurnLeftSpell());
        spells.add(new TurnRightSpell());
        spells.add(new WaterAttackSpell());
        spells.add(new HealSpell());
        spells.add(new TideSpell());
        name = "WaterMage";
        direction = Direction.NORTH;
    }
}
