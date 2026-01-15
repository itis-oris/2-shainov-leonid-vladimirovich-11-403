package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.unit.Unit;

public class FlySpell extends TurnActionSpell {

    public FlySpell() {
        super("Fly");
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.CELL;
    }

    @Override
    public boolean canApply(Unit caster, Target target, GameContext context) {
        if (!isReady()) return false;
        if (!(target instanceof Target.CellTarget cell)) return false;

        boolean sameRow = caster.getY() == cell.y();
        boolean sameCol = caster.getX() == cell.x();

        return (sameRow || sameCol)
                && context.getMap().isWalkable(cell.x(), cell.y());
    }

    @Override
    public void apply(Unit caster, Target target, GameContext context) {
        Target.CellTarget cell = (Target.CellTarget) target;
        context.getMap().moveUnit(caster, cell.x(), cell.y());
        TurnActionSpell.lockAllTurnActions(caster);
    }
}
