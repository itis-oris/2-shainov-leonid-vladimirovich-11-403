package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.map.Cell;
import entity.unit.Unit;

import java.util.List;

public abstract class AttackSpell extends Spell {

    protected AttackSpell(String name) {
        super(name, 1);
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.CELL;
    }

    protected abstract List<Cell> getAttackArea(Unit caster);

    @Override
    public List<Target> getValidTargets(Unit caster, GameContext context) {
        return getAttackArea(caster).stream()
                .filter(c -> context.getMap().isInside(c.x(), c.y()))
                .map(c -> (Target) new Target.CellTarget(c.x(), c.y()))
                .toList();
    }
}
