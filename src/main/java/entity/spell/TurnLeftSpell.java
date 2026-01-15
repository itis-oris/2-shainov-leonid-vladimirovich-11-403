package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.unit.Unit;

public class TurnLeftSpell extends TurnActionSpell {

    public TurnLeftSpell() {
        super("Turn Left");
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
        caster.setDirection(caster.getDirection().left());
        TurnActionSpell.lockAllTurnActions(caster);
    }
}
