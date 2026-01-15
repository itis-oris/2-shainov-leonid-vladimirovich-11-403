package protocol;

public enum ActionType {
    MOVE(1),
    TURN(2),
    CAST_SPELL_REQUEST(3),
    CAST_SPELL_APPLY(4),
    END_TURN(5),
    SELECT_CHARACTER(6),
    PLAYER_READY(7),
    SET_PLAYER_NAME(8),
    INSPECT_UNIT(9);


    public final byte code;

    ActionType(int code) {
        this.code = (byte) code;
    }
    public static ActionType fromByte(byte b) {
        for (ActionType a : values()) {
            if (a.code == b) return a;
        }
        throw new IllegalArgumentException("Unknown action type: " + b);
    }
}
