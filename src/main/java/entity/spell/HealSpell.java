package entity.spell;

import entity.map.GameContext;
import entity.map.Target;
import entity.map.TargetType;
import entity.unit.Unit;

public class HealSpell extends Spell {

    public HealSpell() {
        super("Heal", 4);
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.SELF;
    }

    @Override
    public boolean canApply(Unit caster, Target target, GameContext context) {
        return isReady() && caster.getHp() < caster.getMaxHp();
    }

    @Override
    public void apply(Unit caster, Target target, GameContext context) {
        caster.setHp(Math.min(
                caster.getHp() + 1,
                caster.getMaxHp()
        ));
        startCooldown();
    }
}
