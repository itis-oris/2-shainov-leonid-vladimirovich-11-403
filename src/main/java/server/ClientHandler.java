package server;

import entity.map.GameContext;
import entity.map.Target;
import entity.spell.Spell;
import entity.unit.Unit;
import lombok.Getter;
import protocol.ActionType;
import protocol.ClientState;
import protocol.MessageType;
import protocol.UnitType;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientHandler implements Runnable {

    private final GameServer server;
    @Getter
    private final int playerId;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Socket socket;

    @Getter
    private ClientState state = ClientState.LOBBY;

    public ClientHandler(GameServer server, Socket socket, int playerId) throws IOException {
        this.server = server;
        this.socket = socket;
        this.playerId = playerId;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                byte typeByte = in.readByte();
                MessageType type = MessageType.fromByte(typeByte);
                int length = in.readInt();

                byte[] payload = new byte[length];
                in.readFully(payload);

                DataInputStream data =
                        new DataInputStream(new ByteArrayInputStream(payload));

                if (type == MessageType.ACTION) {
                    ActionType action = ActionType.fromByte(data.readByte());
                    server.handleAction(this, action, data);
                }
            }
        } catch (EOFException e) {
            System.out.println("Player " + playerId + " disconnected");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void startGame(GameContext context) throws IOException {
        state = ClientState.IN_GAME;
        sendStartGame();

        Unit myUnit = context.getUnits()
                .stream()
                .filter(u -> u.getPlayerId() == playerId)
                .findFirst()
                .orElseThrow();

        sendSpellList(myUnit);
    }

    public void sendAssignPlayer() throws IOException {
        out.writeByte(MessageType.ASSIGN_PLAYER.code);
        out.writeInt(4);
        out.writeInt(playerId);
        out.flush();
    }

    public void sendStartGame() throws IOException {
        out.writeByte(MessageType.START_GAME.code);
        out.writeInt(0);
        out.flush();
    }

    public void sendPlayerTurn(int activePlayerId) throws IOException {
        out.writeByte(MessageType.PLAYER_TURN.code);
        out.writeInt(4);
        out.writeInt(activePlayerId);
        out.flush();
    }

    public void sendGameState(GameContext context) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        List<Unit> units = context.getUnits();
        dos.writeInt(units.size());

        for (Unit u : units) {
            dos.writeInt(u.getPlayerId());
            dos.writeUTF(u.getName());
            dos.writeInt(u.getX());
            dos.writeInt(u.getY());
            dos.writeInt(u.getHp());
            dos.writeByte(u.getDirection().ordinal());

        }

        dos.flush();
        byte[] payload = baos.toByteArray();

        out.writeByte(MessageType.GAME_STATE.code);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    public void sendSpellTargets(List<Target> targets) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(baos);

        data.writeInt(targets.size());
        for (Target t : targets) {
            if (t instanceof Target.CellTarget cell) {
                data.writeByte(1);
                data.writeInt(cell.x());
                data.writeInt(cell.y());
            }
        }

        byte[] payload = baos.toByteArray();
        out.writeByte(MessageType.SPELL_TARGETS.code);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    public void sendSpellList(Unit unit) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(baos);

        List<Spell> spells = unit.getSpells();

        data.writeInt(spells.size());
        for (Spell spell : spells) {
            data.writeUTF(spell.getName());
            data.writeInt(spell.getCooldown());
            data.writeInt(spell.getMaxCooldown());
        }

        data.flush();
        byte[] payload = baos.toByteArray();

        out.writeByte(MessageType.SPELL_LIST.code);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }


    public void sendLobbyState(
            Map<Integer, UnitType> selected,
            Set<Integer> ready
    ) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(baos);

        data.writeInt(selected.size());
        for (var e : selected.entrySet()) {
            data.writeInt(e.getKey());
            data.writeInt(e.getValue().ordinal());
        }

        data.writeInt(ready.size());
        for (int pid : ready) {
            data.writeInt(pid);
        }

        byte[] payload = baos.toByteArray();
        out.writeByte(MessageType.LOBBY_STATE.code);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    public void sendGameOver(int winnerId) throws IOException {
        out.writeByte(MessageType.GAME_OVER.code);
        out.writeInt(4);
        out.writeInt(winnerId);
        out.flush();
    }

    public void resetToLobby() {
        state = ClientState.LOBBY;
    }

    public void sendUnitInfo(Unit unit) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(baos);

        data.writeUTF(unit.getName());
        data.writeInt(unit.getHp());

        List<Spell> spells = unit.getSpells();
        data.writeInt(spells.size());

        for (Spell s : spells) {
            data.writeUTF(s.getName());
            data.writeInt(s.getCooldown());
            data.writeInt(s.getMaxCooldown());
        }

        byte[] payload = baos.toByteArray();
        out.writeByte(MessageType.UNIT_INFO.code);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }



    public void sendError(String msg) throws IOException {
        byte[] payload = msg.getBytes();
        out.writeByte(MessageType.ERROR.code);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    private void closeConnection() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}
