package protocol;

public enum MessageType {
    CONNECT (1),
    ASSIGN_PLAYER (2),
    START_GAME (3),
    PLAYER_TURN (4),
    ACTION (5),
    GAME_STATE (6),
    ERROR (7),
    SPELL_TARGETS (8),
    SPELL_LIST (9),
    LOBBY_STATE (10),
    GAME_OVER (11),
    UNIT_INFO(12);

    public final byte code;

    MessageType(int code) {
        this.code = (byte) code;
    }

    public static MessageType fromByte(byte b) {
        for (MessageType t : values())
            if (t.code == b) return t;
        throw new IllegalArgumentException("Unknown type: " + b);
    }
}
/*
[byte]   protocol.MessageType.ACTION
[int]    payloadLength

[int]    unitId
[byte]   actionType
...      actionPayload

 */