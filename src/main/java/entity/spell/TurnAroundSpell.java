package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.unit.Unit;

public class TurnAroundSpell extends Spell {

    public TurnAroundSpell() {
        super("Turn Around", 2);
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.SELF;
    }

    @Override
    public boolean canApply(Unit caster, Target target, GameContext context) {
        return isReady();
    }

    @Override
    public void apply(Unit caster, Target target, GameContext context) {
        caster.setDirection(caster.getDirection().back());
        TurnActionSpell.lockAllTurnActions(caster);
    }
}
