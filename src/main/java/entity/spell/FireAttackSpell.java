package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.Cell;
import entity.unit.Unit;

import java.util.List;

public class FireAttackSpell extends AttackSpell {

    public FireAttackSpell() {
        super("Fire Attack");
    }

    @Override
    protected List<Cell> getAttackArea(Unit caster) {
        int x = caster.getX();
        int y = caster.getY();

        return switch (caster.getDirection()) {
            case NORTH -> List.of(
                    new Cell(x, y - 1),
                    new Cell(x - 1, y - 1),
                    new Cell(x + 1, y - 1)
            );
            case SOUTH -> List.of(
                    new Cell(x, y + 1),
                    new Cell(x - 1, y + 1),
                    new Cell(x + 1, y + 1)
            );
            case WEST -> List.of(
                    new Cell(x - 1, y),
                    new Cell(x - 1, y - 1),
                    new Cell(x - 1, y + 1)
            );
            case EAST -> List.of(
                    new Cell(x + 1, y),
                    new Cell(x + 1, y - 1),
                    new Cell(x + 1, y + 1)
            );
        };
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
