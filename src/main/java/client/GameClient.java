package client;

import protocol.ActionType;
import entity.unit.Direction;
import entity.map.Cell;
import protocol.MessageType;
import protocol.UnitType;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class GameClient {

    private DataOutputStream out;

    private LobbyWindow lobbyWindow;
    private GameWindow gameWindow;

    private int myPlayerId = -1;
    private boolean myTurn = false;

    public static void main(String[] args) {
        new GameClient().start();
    }

    public void start() {
        try {
            Socket socket = new Socket("localhost", 5000);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            SwingUtilities.invokeLater(() -> {
                lobbyWindow = new LobbyWindow(this);
            });

            new Thread(() -> {
                try {
                    while (true) {
                        MessageType type = MessageType.fromByte(in.readByte());
                        int length = in.readInt();
                        byte[] payload = new byte[length];
                        in.readFully(payload);
                        DataInputStream data = new DataInputStream(
                                new ByteArrayInputStream(payload));

                        switch (type) {
                            case ASSIGN_PLAYER -> {
                                myPlayerId = data.readInt();
                            }

                            case LOBBY_STATE -> {
                                Map<Integer, UnitType> selected = new HashMap<>();
                                int selCount = data.readInt();
                                for (int i = 0; i < selCount; i++) {
                                    int pid = data.readInt();
                                    UnitType ut = UnitType.values()[data.readInt()];
                                    selected.put(pid, ut);
                                }

                                Set<Integer> ready = new HashSet<>();
                                int readyCount = data.readInt();
                                for (int i = 0; i < readyCount; i++) {
                                    ready.add(data.readInt());
                                }

                                SwingUtilities.invokeLater(() ->
                                        lobbyWindow.updateLobbyState(selected, ready)
                                );
                            }

                            case START_GAME -> {
                                SwingUtilities.invokeLater(() -> {
                                    lobbyWindow.dispose();
                                    gameWindow = new GameWindow(this);
                                });
                            }


                            case PLAYER_TURN -> {
                                int p = data.readInt();
                                myTurn = (p == myPlayerId);

                                SwingUtilities.invokeLater(() -> {
                                    gameWindow.setControlsEnabled(myTurn);
                                    gameWindow.setInfoText(
                                            myTurn ? "YOUR TURN" : "ENEMY TURN"
                                    );
                                });
                            }


                            case GAME_STATE -> {
                                int count = data.readInt();
                                List<UnitSnapshot> units = new ArrayList<>();
                                for (int i = 0; i < count; i++) {
                                    int pid = data.readInt();
                                    String name = data.readUTF();
                                    int x = data.readInt();
                                    int y = data.readInt();
                                    int hp = data.readInt();
                                    Direction dir = Direction.values()[data.readByte()];
                                    units.add(new UnitSnapshot(pid, name, x, y, hp, dir));
                                }
                                SwingUtilities.invokeLater(() ->
                                        gameWindow.updateBoard(units)
                                );
                            }

                            case UNIT_INFO -> {
                                String unitName = data.readUTF();
                                int hp = data.readInt();

                                int spellCount = data.readInt();
                                StringBuilder sb = new StringBuilder();
                                sb.append(unitName).append("\nHP: ").append(hp).append("\n\nSpells:\n");

                                for (int i = 0; i < spellCount; i++) {
                                    String name = data.readUTF();
                                    int cd = data.readInt();
                                    int max = data.readInt();

                                    sb.append("- ")
                                            .append(name)
                                            .append(" (")
                                            .append(cd)
                                            .append("/")
                                            .append(max)
                                            .append(")\n");
                                }

                                SwingUtilities.invokeLater(() ->
                                        JOptionPane.showMessageDialog(null, sb.toString(), "Unit Info",
                                                JOptionPane.INFORMATION_MESSAGE)
                                );
                            }

                            case SPELL_TARGETS -> {
                                int count = data.readInt();
                                List<Cell> targets = new ArrayList<>();
                                for (int i = 0; i < count; i++) {
                                    data.readByte();
                                    targets.add(new Cell(
                                            data.readInt(),
                                            data.readInt()));
                                }
                                SwingUtilities.invokeLater(() ->
                                        gameWindow.showSpellTargets(targets)
                                );
                            }

                            case SPELL_LIST -> {
                                int count = data.readInt();

                                List<String> spells = new ArrayList<>();
                                List<Integer> cooldowns = new ArrayList<>();

                                for (int i = 0; i < count; i++) {
                                    spells.add(data.readUTF());
                                    cooldowns.add(data.readInt());
                                    data.readInt();
                                }

                                SwingUtilities.invokeLater(() ->
                                        gameWindow.updateSpells(spells, cooldowns)
                                );
                            }

                            case GAME_OVER -> {
                                int winnerId = data.readInt();

                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(
                                            null,
                                            winnerId == myPlayerId
                                                    ? "YOU WIN!"
                                                    : "YOU LOSE!"
                                    );

                                    gameWindow.dispose();
                                    gameWindow = null;

                                    lobbyWindow = new LobbyWindow(this);
                                });
                            }

                            case ERROR -> {
                                String msg = new String(payload);
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(null, msg);
                                    if (gameWindow != null) gameWindow.onError();
                                });
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAction(ActionType type, ActionWriter writer) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeByte(type.code);
            System.out.println("CLIENT SEND ACTION: " + type);
            writer.write(daos);

            System.out.println("CLIENT PAYLOAD BYTES: " + Arrays.toString(baos.toByteArray()));

            byte[] payload = baos.toByteArray();
            out.writeByte(MessageType.ACTION.code);
            out.writeInt(payload.length);
            out.write(payload);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public record UnitSnapshot(
            int playerId, String unitName, int x, int y, int hp, Direction direction) {}

    @FunctionalInterface
    public interface ActionWriter {
        void write(DataOutputStream out) throws IOException;
    }
}
