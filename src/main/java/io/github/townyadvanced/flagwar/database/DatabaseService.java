package io.github.townyadvanced.flagwar.database;

import io.github.townyadvanced.flagwar.objects.BattleRecord;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import io.github.townyadvanced.flagwar.util.BannerWarUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class DatabaseService {

    /** Holds the name of the battle table. */
    private static final String BATTLE_TABLE = "Battle";

    /** Holds the {@link DatabaseManager} instance. */
    private final DatabaseManager MANAGER;

    /** Holds the {@link Logger} of this class. */
    private final Logger LOGGER;

    public DatabaseService(Logger logger, DatabaseManager manager) {
        this.MANAGER = manager;
        this.LOGGER = logger;
    }

    public CompletableFuture<Collection<BattleRecord>> getBattles() {

        return CompletableFuture.supplyAsync(() -> {
            Collection<BattleRecord> battles = new ArrayList<>();
            String query = "SELECT * FROM " + BATTLE_TABLE;
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                    {
                            battles.add(new BattleRecord(
                             rs.getString(1),
                             rs.getString(2),
                             rs.getString(3),
                             rs.getInt(4),
                             rs.getInt(5),
                             rs.getLong(6),
                             rs.getBoolean(7),
                             BattleStage.valueOf(rs.getString(8)),
                             UUID.fromString(rs.getString(10)),
                             BannerWarUtil.toBlockList(rs.getString(9), rs.getString(10))
                         ));
                    }
                    return battles;
                }
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Void> insertBattle(BattleRecord r) {

        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO " + BATTLE_TABLE + "VALUES(?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {

                ps.setString(1, r.attacker());
                ps.setString(2, r.defender());
                ps.setString(3, r.contestedTown());
                ps.setInt(4, r.homeX());
                ps.setInt(5, r.homeZ());
                ps.setLong(6, r.stageStartTime());
                ps.setBoolean(7, r.isCityState());
                ps.setString(8, r.stage().name());
                ps.setString(9, r.worldID().toString());
                ps.setString(10, BannerWarUtil.fromBlockList(r.townBlocks()));

                if (ps.executeUpdate() > 0)
                    LOGGER.info("Successfully added battle " + r.contestedTown() + " to database!");
                else
                    LOGGER.warning("Failed to add battle " + r.contestedTown() + " to database!");

            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }

    public void updateBattle(BattleRecord r) {
            deleteBattle(r.contestedTown())
                .thenRun(() -> insertBattle(r));
    }

    public CompletableFuture<Void> deleteBattle(String contestedTown) {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM " + BATTLE_TABLE + " WHERE id = ?";
            try(PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                ps.setString(1, contestedTown);
                ps.executeUpdate();
            }
            catch(SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }
}
