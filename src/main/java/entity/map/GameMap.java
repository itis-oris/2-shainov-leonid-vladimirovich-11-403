package entity.map;

import entity.unit.Direction;
import entity.unit.Unit;
import exception.CannotMoveToTheCellException;
import exception.CellIsNotWalkableException;

import java.util.ArrayList;
import java.util.List;

public class GameMap {
    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private final Unit[][] units;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new TileType[width][height];
        this.units = new Unit[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TileType.FLOOR;
            }
        }
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isWalkable(int x, int y) {
        return isInside(x, y)
                && tiles[x][y] == TileType.FLOOR
                && units[x][y] == null;
    }

    public Unit getUnitAt(int x, int y) {
        if (!isInside(x, y)) return null;
        return units[x][y];
    }

    public void placeUnit(Unit unit, int x, int y, Direction direction) {
        if (!isWalkable(x, y)) {
            throw new CellIsNotWalkableException("Cell is not walkable");
        }
        units[x][y] = unit;
        unit.setX(x);
        unit.setY(y);
        unit.setDirection(direction);
    }

    public void moveUnit(Unit unit, int newX, int newY) {
        if (!isWalkable(newX, newY)) {
            throw new CannotMoveToTheCellException("can't move to the cell");
        }
        units[unit.getX()][unit.getY()] = null;
        units[newX][newY] = unit;
        unit.setX(newX);
        unit.setY(newY);
    }

    public List<Cell> getCellsInRadius(int cx, int cy, int radius) {
        List<Cell> result = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int x = cx + dx;
                int y = cy + dy;

                if (isInside(x, y) && Math.abs(dx) + Math.abs(dy) <= radius) {
                    result.add(new Cell(x, y));
                }
            }
        }
        return result;
    }

    public List<Cell> getAllCells() {
        List<Cell> result = new ArrayList<>();
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                result.add(new Cell(x, y));
        return result;
    }
}
