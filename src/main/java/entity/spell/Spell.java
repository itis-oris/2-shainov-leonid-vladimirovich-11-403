package entity.spell;

import entity.map.GameContext;
import entity.map.GameMap;
import entity.map.Target;
import entity.map.TargetType;
import entity.map.Cell;
import entity.unit.Unit;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Spell {

    protected final String name;
    protected final int maxCooldown;
    protected int cooldown;
    protected String picturePath;

    public Spell(String name, int maxCooldown) {
        this.name = name;
        this.maxCooldown = maxCooldown;
        this.cooldown = 0;
    }

    public String getName() {
        return name;
    }

    public boolean isReady() {
        return cooldown == 0;
    }

    public void tickCooldown() {
        if (cooldown > 0) {
            cooldown--;
        }
    }

    public void startCooldown() {
        cooldown = maxCooldown;
    }

    public List<Target> getValidTargets(Unit caster, GameContext context) {
        List<Target> validTargets = new ArrayList<>();
        GameMap map = context.getMap();

        switch (getTargetType()) {

            case NONE -> {
                if (canApply(caster, null, context)) {
                    validTargets.add(null);
                }
            }

            case SELF -> {
                if (canApply(caster, Target.SelfTarget.INSTANCE, context)) {
                    validTargets.add(Target.SelfTarget.INSTANCE);
                }
            }

            case CELL -> {
                for (Cell cell : map.getAllCells()) {
                    Target.CellTarget t = new Target.CellTarget(cell.x(), cell.y());
                    if (canApply(caster, t, context)) {
                        validTargets.add(t);
                    }
                }
            }

            case CELL_IN_RADIUS -> {
                int radius = 1;
                for (Cell cell : map.getCellsInRadius(caster.getX(), caster.getY(), radius)) {
                    Target.CellTarget t = new Target.CellTarget(cell.x(), cell.y());
                    if (canApply(caster, t, context)) {
                        validTargets.add(t);
                    }
                }
            }

            case UNIT -> {
                for (Unit u : context.getUnits()) {
                    Target.UnitTarget t = new Target.UnitTarget(u);
                    if (canApply(caster, t, context)) {
                        validTargets.add(t);
                    }
                }
            }
        }

        return validTargets;
    }


    public abstract TargetType getTargetType();

    public abstract boolean canApply(Unit caster, Target target, GameContext context);

    public abstract void apply(Unit caster, Target target, GameContext context);
}
