package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.unit.Direction;
import entity.unit.Unit;

import java.util.ArrayList;
import java.util.List;

public class TideSpell extends Spell {

    public TideSpell() {
        super("Tide", 2);
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.CELL;
    }

    @Override
    public boolean canApply(Unit caster, Target target, GameContext context) {
        if (!isReady()) return false;
        if (!(target instanceof Target.CellTarget cell)) return false;

        Unit enemy = context.getMap().getUnitAt(cell.x(), cell.y());
        if (enemy == null) return false;
        if (enemy.getPlayerId() == caster.getPlayerId()) return false;

        Direction dir = caster.getDirection();
        int newX = enemy.getX() - dir.dx;
        int newY = enemy.getY() - dir.dy;

        return context.getMap().isWalkable(newX, newY);
    }



    @Override
    public void apply(Unit caster, Target target, GameContext context) {
        Target.CellTarget cell = (Target.CellTarget) target;
        Unit enemy = context.getMap().getUnitAt(cell.x(), cell.y());

        if (enemy == null) return;

        Direction dir = caster.getDirection();
        int newX = enemy.getX() - dir.dx;
        int newY = enemy.getY() - dir.dy;

        context.getMap().moveUnit(enemy, newX, newY);
        startCooldown();
    }


    @Override
    public List<Target> getValidTargets(Unit caster, GameContext context) {
        List<Target> result = new ArrayList<>();

        int x = caster.getX();
        int y = caster.getY();
        Direction dir = caster.getDirection();

        int cx = x + dir.dx;
        int cy = y + dir.dy;

        while (context.getMap().isInside(cx, cy)) {
            Unit u = context.getMap().getUnitAt(cx, cy);
            if (u != null) {
                if (u.getPlayerId() != caster.getPlayerId()) {
                    result.add(new Target.CellTarget(cx, cy));
                }
                break;
            }
            cx += dir.dx;
            cy += dir.dy;
        }

        return result;
    }

}
