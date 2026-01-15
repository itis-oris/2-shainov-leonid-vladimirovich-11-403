package entity.unit;

import entity.spell.Spell;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter@Setter
public abstract class Unit {
    protected int playerId;
    protected boolean alive;
    protected int x;
    protected int y;
    protected int hp;
    protected int maxHp;
    protected Direction direction = Direction.NORTH;
    protected String name;
    List<Spell> spells = new ArrayList<>();
    private int movementLockedTurns;

    public boolean isAlive() {
        return hp > 0;
    }

    public void endTurn() {
    }

    public void getDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    public boolean canMove() {
        return movementLockedTurns == 0;
    }

    public void lockMovementForTurns(int turns) {
        movementLockedTurns = Math.max(movementLockedTurns, turns);
    }

    public void tickTurn() {
        if (movementLockedTurns > 0) {
            movementLockedTurns--;
        }
    }

}
