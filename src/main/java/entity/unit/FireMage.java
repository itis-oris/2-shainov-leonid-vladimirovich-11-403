package entity.unit;

import entity.spell.*;

public class FireMage extends Unit {
    public FireMage() {
        hp = 3;
        maxHp = 3;
        spells.add(new MoveSpell());
        spells.add(new TurnLeftSpell());
        spells.add(new TurnRightSpell());
        spells.add(new FireAttackSpell());
        spells.add(new HopSpell());
        spells.add(new TurnAroundSpell());
        name = "FireMage";
        direction = Direction.NORTH;
    }
}
