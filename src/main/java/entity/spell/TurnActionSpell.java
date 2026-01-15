package entity.spell;

import entity.unit.Unit;

public abstract class TurnActionSpell extends Spell {

    public TurnActionSpell(String name) {
        super(name, 1); // cooldown = 1 всегда
    }

    @Override
    public final void startCooldown() {
        super.startCooldown();
    }

    public static void lockAllTurnActions(Unit unit) {
        for (Spell spell : unit.getSpells()) {
            if (spell instanceof TurnActionSpell s) {
                s.startCooldown();
            }
        }
    }
}
