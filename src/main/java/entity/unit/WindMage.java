package entity.unit;

import entity.spell.*;

public class WindMage extends Unit {
    public WindMage() {
        hp = 3;
        maxHp = 3;
        spells.add(new MoveSpell());
        spells.add(new TurnLeftSpell());
        spells.add(new TurnRightSpell());
        spells.add(new ForwardStrike());
        spells.add(new FlySpell());
        spells.add(new HopSpell());
        name = "WindMage";
        direction = Direction.NORTH;
    }
}
