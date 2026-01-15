package entity.map;

import entity.unit.Direction;

import java.util.ArrayList;
import java.util.List;

public class AreaUtils {

    public static Cell step(int x, int y, Direction dir, int distance) {
        return new Cell(
                x + dir.dx * distance,
                y + dir.dy * distance
        );
    }

    public static List<Cell> forwardLine(int x, int y, Direction dir, int length) {
        List<Cell> result = new ArrayList<>();
        for (int i = 1; i <= length; i++) {
            result.add(step(x, y, dir, i));
        }
        return result;
    }

    public static List<Cell> fullLine(int x, int y, Direction dir, int length) {
        List<Cell> result = new ArrayList<>();
        for (int i = 1; i <= length; i++) {
            result.add(step(x, y, dir, i));
            result.add(step(x, y, dir.back(), i));
        }
        return result;
    }

    public static List<Cell> frontCone(int x, int y, Direction dir) {
        List<Cell> result = new ArrayList<>();

        Cell front = step(x, y, dir, 1);
        result.add(front);

        Direction left = dir.left();
        Direction right = dir.right();

        result.add(new Cell(front.x() + left.dx, front.y() + left.dy));
        result.add(new Cell(front.x() + right.dx, front.y() + right.dy));

        return result;
    }

    public static List<Cell> around(int x, int y) {
        List<Cell> result = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                result.add(new Cell(x + dx, y + dy));
            }
        }
        return result;
    }

    public static List<Cell> aroundExceptBack(int x, int y, Direction dir) {
        List<Cell> result = new ArrayList<>();
        Cell back = step(x, y, dir.back(), 1);

        for (Cell c : around(x, y)) {
            if (c.equals(back)) continue;
            result.add(c);
        }
        return result;
    }

    public static List<Cell> rookLines(int x, int y, int range) {
        List<Cell> result = new ArrayList<>();

        for (Direction d : Direction.values()) {
            for (int i = 1; i <= range; i++) {
                result.add(step(x, y, d, i));
            }
        }
        return result;
    }
}
