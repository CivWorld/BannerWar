package io.github.townyadvanced.flagwar.database;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.BattleStatus;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.DamageOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.KillOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.PlayerSnapshot;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.BattleSnapshot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class TrackerDatabase {
    /** Holds the name of the battle table. */
    private static final String TRACKED_BATTLE_TABLE = "TrackedBattle";

    /** Holds the name of the banner placer table. */
    private static final String TRACKED_PLAYER_TABLE = "TrackedPlayer";

    /** Holds the {@link DatabaseManager} instance. */
    private final DatabaseManager MANAGER;

    /** Holds the {@link Logger} of this class. */
    private final Logger LOGGER = FlagWar.getInstance().getLogger();

    public TrackerDatabase(DatabaseManager manager) {
        this.MANAGER = manager;
    }

    public CompletableFuture<Collection<BattleSnapshot>> getTrackedBattles() {
        return CompletableFuture.supplyAsync(() -> {
            Collection<BattleSnapshot> battles = new ArrayList<>();
            String query = "SELECT * FROM " + TRACKED_BATTLE_TABLE;
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long unixStart = rs.getLong(4);
                        String townName = rs.getString(1);
                        var tbr = new BattleSnapshot(
                            townName,
                            BattleStatus.ONGOING,
                            rs.getString(2),
                            rs.getString(3),
                            unixStart,
                            Duration.ofMillis(unixStart - System.currentTimeMillis()),
                            getTrackedPlayersSync(townName),
                            DamageOccurrence.deserialize(rs.getString(5))
                        );
                        battles.add(tbr);
                    }
                    return battles;
                }
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Void> insertOrUpdateBattle(BattleSnapshot r) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT OR REPLACE INTO " + TRACKED_BATTLE_TABLE +
                " VALUES(?,?,?,?,?)";
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                ps.setString(1, r.townName());
                ps.setString(2, r.attackerNationName());
                ps.setString(3, r.defenderNationName());
                ps.setLong(4, r.unixStartTime());
                ps.setString(5, DamageOccurrence.serialize(r.damageOccurrences()));

                if (ps.executeUpdate() > 0)
                    LOGGER.info("Successfully added battle " + r.townName() + " to database!");
                else
                    LOGGER.warning("Failed to add battle " + r.townName() + " to database!");

            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }

        });
    }

    public CompletableFuture<Void> deleteBattle(String contestedTown) {
        return CompletableFuture.runAsync(() -> {
            deleteTrackedPlayers(contestedTown);
            String query = "DELETE FROM " + TRACKED_BATTLE_TABLE + " WHERE ContestedTown = ?";
            try(PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                ps.setString(1, contestedTown);
                ps.executeUpdate();
            }
            catch(SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }

    public void insertOrUpdatePlayersSync(Collection<PlayerSnapshot> players, String battleTown) throws SQLException {

        Connection conn = MANAGER.getConnection();
        conn.setAutoCommit(false);

        String query = "INSERT OR REPLACE INTO " + TRACKED_PLAYER_TABLE + " VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            for (var trackedPlayer : players) {
                ps.setString(1, trackedPlayer.playerName());
                ps.setString(2, battleTown);
                ps.setString(3, trackedPlayer.affiliation().name());
                ps.setDouble(4, trackedPlayer.damageDealt());
                ps.setDouble(5, trackedPlayer.damageTaken());
                ps.setString(6, KillOccurrence.serialize(trackedPlayer.kills()));
                ps.setString(7, KillOccurrence.serialize(trackedPlayer.deaths()));
                ps.setInt(8, trackedPlayer.gapsUsed());
                ps.setInt(9, trackedPlayer.potsUsed());
                ps.setString(10, FlagOccurrence.serialize(trackedPlayer.flags()));
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {conn.rollback();
            LOGGER.warning("Failed to add players to database! " + e.getMessage());}
        finally {
            conn.setAutoCommit(true);
        }
    }

    private Map<String, PlayerSnapshot> getTrackedPlayersSync(String battleTown) {
        Map<String, PlayerSnapshot> results = new HashMap<>();
        String query = "SELECT * FROM " + TRACKED_PLAYER_TABLE + " WHERE BattleTown = ?";
        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
            ps.setString(1, battleTown);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    results.put(name, new PlayerSnapshot(
                            name,
                            Affiliation.valueOf(rs.getString(3)),
                            KillOccurrence.deserialize(rs.getString(6)),
                            KillOccurrence.deserialize(rs.getString(7)),
                            rs.getDouble(4),
                            rs.getDouble(5),
                            rs.getInt(9),
                            rs.getInt(8),
                            FlagOccurrence.deserialize(rs.getString(10))
                        )
                    );
                }
                return results;
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            return new HashMap<>();
        }
    }

    private void deleteTrackedPlayers(String battleTown) {
        String query = "DELETE FROM " + TRACKED_PLAYER_TABLE + " WHERE BattleTown = ?";
        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
            ps.setString(1, battleTown);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }
    }
}
