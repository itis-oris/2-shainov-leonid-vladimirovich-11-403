package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.unit.Unit;

public class TurnRightSpell extends TurnActionSpell {

    public TurnRightSpell() {
        super("Turn Right");
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
        caster.setDirection(caster.getDirection().right());
        TurnActionSpell.lockAllTurnActions(caster);
    }
}
