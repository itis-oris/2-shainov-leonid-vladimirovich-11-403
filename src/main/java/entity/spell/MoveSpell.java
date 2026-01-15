package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.unit.Unit;
public class MoveSpell extends TurnActionSpell {

    public MoveSpell() {
        super("Move");
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.CELL_IN_RADIUS;
    }

    @Override
    public boolean canApply(Unit caster, Target target, GameContext context) {
        if (!isReady()) return false;
        if (!(target instanceof Target.CellTarget cell)) return false;

        int dx = Math.abs(cell.x() - caster.getX());
        int dy = Math.abs(cell.y() - caster.getY());

        return dx + dy == 1 && context.getMap().isWalkable(cell.x(), cell.y());
    }

    @Override
    public void apply(Unit caster, Target target, GameContext context) {
        Target.CellTarget cell = (Target.CellTarget) target;
        context.getMap().moveUnit(caster, cell.x(), cell.y());
        TurnActionSpell.lockAllTurnActions(caster);
    }
}
