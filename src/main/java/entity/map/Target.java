package entity.map;

import entity.unit.Unit;

public sealed interface Target permits Target.CellTarget, Target.UnitTarget, Target.SelfTarget {

    record CellTarget(int x, int y) implements Target {}

    record UnitTarget(Unit unit) implements Target {}

    record SelfTarget() implements Target {
        public static final SelfTarget INSTANCE = new SelfTarget();
    }
}