package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.unit.Unit;

public class HopSpell extends Spell {

    public HopSpell() {
        super("Hop", 2);
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.SELF;
    }

    @Override
    public boolean canApply(Unit caster, Target target, GameContext context) {
        if (!isReady()) return false;

        int nx = caster.getX() + caster.getDirection().dx * 2;
        int ny = caster.getY() + caster.getDirection().dy * 2;

        return context.getMap().isWalkable(nx, ny);
    }

    @Override
    public void apply(Unit caster, Target target, GameContext context) {
        int nx = caster.getX() + caster.getDirection().dx * 2;
        int ny = caster.getY() + caster.getDirection().dy * 2;

        context.getMap().moveUnit(caster, nx, ny);
        TurnActionSpell.lockAllTurnActions(caster);
    }
}
