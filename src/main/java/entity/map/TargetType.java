package entity.map;

public enum TargetType {
    NONE,// без цели
    CELL,// клетка (x, y)
    CELL_IN_RADIUS,// клетка (x, y) и радиус от неё r
    UNIT,// юнит
    ALLY_UNIT,// союзный юнит
    SELF, // кастер
    DIRECTION
}