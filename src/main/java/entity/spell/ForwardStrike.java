package entity.spell;

import entity.map.AreaUtils;
import entity.map.GameContext;
import entity.map.Target;
import entity.map.Cell;
import entity.unit.Unit;

import java.util.List;

public class ForwardStrike extends AttackSpell {

    public ForwardStrike() {
        super("Forward Strike");
    }

    @Override
    protected List<Cell> getAttackArea(Unit caster) {
        return AreaUtils.forwardLine(
                caster.getX(),
                caster.getY(),
                caster.getDirection(),
                3
        );
    }

    @Override
    public boolean canApply(Unit caster, Target target, GameContext context) {
        if (!isReady()) return false;
        if (!(target instanceof Target.CellTarget cell)) return false;

        Unit victim = context.getMap().getUnitAt(cell.x(), cell.y());
        return victim != null && victim.getPlayerId() != caster.getPlayerId();
    }

    @Override
    public void apply(Unit caster, Target target, GameContext context) {
        Target.CellTarget cell = (Target.CellTarget) target;
        Unit victim = context.getMap().getUnitAt(cell.x(), cell.y());

        if (victim != null) {
            victim.getDamage(1);
            startCooldown();
        }
    }
}
