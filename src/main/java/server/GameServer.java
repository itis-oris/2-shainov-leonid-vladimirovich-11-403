package server;

import db.GameRepository;
import entity.map.GameContext;
import entity.map.GameMap;
import entity.map.Target;
import entity.map.TargetType;
import entity.spell.Spell;
import entity.unit.*;
import exception.CannotMoveToTheCellException;
import exception.CellIsNotWalkableException;
import protocol.ActionType;
import protocol.ClientState;
import protocol.UnitType;

import java.io.DataInput;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;

public class GameServer {

    private static final int PORT = 5000;

    private final List<ClientHandler> clients = new ArrayList<>();

    private final Map<Integer, UnitType> selectedUnits = new HashMap<>();
    private final Set<Integer> readyPlayers = new HashSet<>();
    private final Map<Integer, String> playerNames = new HashMap<>();
    private LocalDateTime gameStartedAt;
    private boolean gameFinished = false;


    private GameContext context;
    private int currentPlayer = 1;

    public static void main(String[] args) throws Exception {
        new GameServer().start();
    }

    public void start() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (clients.size() < 2) {
            Socket socket = serverSocket.accept();
            int playerId = clients.size() + 1;

            ClientHandler handler = new ClientHandler(this, socket, playerId);
            clients.add(handler);

            new Thread(handler).start();
            handler.sendAssignPlayer();

            System.out.println("Player " + playerId + " connected");
        }

        sendLobbyState();
    }

    public synchronized void handleAction(ClientHandler from, ActionType type, DataInput data) {

        try {
            if (from.getState() == ClientState.LOBBY) {
                handleLobbyAction(from, type, data);
                return;
            }

            if (type == ActionType.INSPECT_UNIT) {
                int x = data.readInt();
                int y = data.readInt();

                Unit target = context.getMap().getUnitAt(x, y);
                if (target != null) {
                    from.sendUnitInfo(target);
                }
                return;
            }

            if (from.getPlayerId() != currentPlayer) {
                from.sendError("Not your turn");
                return;
            }

            Unit unit = context.getUnits().get(from.getPlayerId() - 1);

            switch (type) {
                case CAST_SPELL_REQUEST -> {
                    int spellIndex = data.readInt();
                    Spell spell = unit.getSpells().get(spellIndex);

                    List<Target> targets = spell.getValidTargets(unit, context);

                    if (targets.isEmpty()) {
                        from.sendError("Spell cannot be used now");
                        return;
                    }

                    if (spell.getTargetType() == TargetType.SELF) {
                        spell.apply(unit, null, context);
                        from.sendSpellList(unit);
                        broadcastGameState();
                        return;
                    }

                    from.sendSpellTargets(targets);

                }

                case CAST_SPELL_APPLY -> {
                    int spellIndex = data.readInt();
                    Spell spell = unit.getSpells().get(spellIndex);

                    byte targetType = data.readByte();
                    Target target = null;

                    if (targetType == 1) {
                        target = new Target.CellTarget(data.readInt(), data.readInt());
                    }

                    if (spell.canApply(unit, target, context)) {
                        spell.apply(unit, target, context);
                        from.sendSpellList(unit);
                        broadcastGameState();
                        checkWinCondition();
                    }
                }
                case END_TURN -> processEndTurn();
            }

        } catch (CellIsNotWalkableException | CannotMoveToTheCellException e) {
            try { from.sendError(e.getMessage()); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLobbyAction(ClientHandler from, ActionType type, DataInput data) throws Exception {

        switch (type) {

            case SELECT_CHARACTER -> {
                int ordinal = data.readInt();
                selectedUnits.put(from.getPlayerId(), UnitType.values()[ordinal]);
                sendLobbyState();
            }

            case SET_PLAYER_NAME -> {
                String name = data.readUTF();
                playerNames.put(from.getPlayerId(), name);
            }

            case PLAYER_READY -> {
                readyPlayers.add(from.getPlayerId());

                if (readyPlayers.size() == 2) {
                    startGame();
                } else {
                    sendLobbyState();
                }
            }

            default -> from.sendError("Invalid lobby action");
        }
    }

    private void startGame() throws Exception {
        GameMap map = new GameMap(5, 5);
        List<Unit> units = new ArrayList<>();
        gameFinished = false;

        Unit u1 = UnitFactory.create(selectedUnits.get(1), 1);
        Unit u2 = UnitFactory.create(selectedUnits.get(2), 2);

        map.placeUnit(u1, 0, 0, Direction.SOUTH);
        map.placeUnit(u2, 4, 4, Direction.NORTH);

        units.add(u1);
        units.add(u2);

        context = new GameContext(map, units);
        currentPlayer = 1;

        for (ClientHandler c : clients) {
            c.startGame(context);
        }
        gameStartedAt = LocalDateTime.now();
        broadcastGameState();
        broadcastPlayerTurn();
    }

    private void sendLobbyState() throws Exception {
        for (ClientHandler c : clients) {
            c.sendLobbyState(selectedUnits, readyPlayers);
        }
    }

    private void processEndTurn() throws Exception {
        context.getPlayerUnits(currentPlayer).forEach(u -> {
            u.endTurn();
            u.getSpells().forEach(Spell::tickCooldown);
        });

        for (ClientHandler c : clients) {
            Unit unit = context.getUnits()
                    .stream()
                    .filter(u -> u.getPlayerId() == c.getPlayerId())
                    .findFirst()
                    .orElseThrow();

            c.sendSpellList(unit);
        }

        currentPlayer = (currentPlayer == 1) ? 2 : 1;

        broadcastPlayerTurn();
        broadcastGameState();
    }


    private void broadcastPlayerTurn() throws Exception {
        for (ClientHandler c : clients) {
            c.sendPlayerTurn(currentPlayer);
        }
    }

    private void checkWinCondition() throws Exception {

        if (gameFinished) return;

        List<Unit> alive = context.getUnits().stream()
                .filter(Unit::isAlive)
                .toList();

        if (alive.size() == 1) {
            gameFinished = true;
            int winnerId = alive.get(0).getPlayerId();
            onGameWon(winnerId);
        }
    }


    private void onGameWon(int winnerId) throws Exception {
        LocalDateTime endedAt = LocalDateTime.now();

        Unit u1 = context.getUnits().stream()
                .filter(u -> u.getPlayerId() == 1)
                .findFirst()
                .orElseThrow();

        Unit u2 = context.getUnits().stream()
                .filter(u -> u.getPlayerId() == 2)
                .findFirst()
                .orElseThrow();

        GameRepository.saveGame(
                gameStartedAt,
                endedAt,
                playerNames.get(1),
                u1.getName(),
                playerNames.get(2),
                u2.getName(),
                winnerId
        );
        for (ClientHandler c : clients) {
            c.sendGameOver(winnerId);
            c.resetToLobby();
        }

        context = null;
        selectedUnits.clear();
        readyPlayers.clear();
        currentPlayer = 1;

        sendLobbyState();
    }

    private void broadcastGameState() throws Exception {
        for (ClientHandler c : clients) {
            c.sendGameState(context);
        }
    }
}
