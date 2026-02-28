package io.github.townyadvanced.flagwar.objects;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.utils.TownRuinUtil;
import io.github.townyadvanced.flagwar.BattleManager;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.chunk.ChunkCopy;
import io.github.townyadvanced.flagwar.chunk.ChunkPaste;
import io.github.townyadvanced.flagwar.events.BattleEndEvent;
import io.github.townyadvanced.flagwar.events.BattleFlaggableEvent;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

/** Contains all the information required to host a BannerWar battle. */
public class Battle {
    /** Holds the {@link Nation} that initiated the battle. */
    private final Nation ATTACKER;

    /** Holds the {@link Nation} that accommodates the town at which the battle is held. */
    private final Nation DEFENDER;

    /** Holds the {@link Town} at which the battle is held. */
    private final Town CONTESTED_TOWN;

    /** Holds a {@link List} of {@link String}s of every player that has placed a {@link CellUnderAttack} relevant to this battle. */
    private final List<String> flags;

    /** Holds the critical {@link TownBlock} that, when won, ends the battle. */
    private final TownBlock HOME_BLOCK;

    /** Holds the {@link Duration} of every stage of this war. */
    private Map<BattleStage, Duration> STAGE_DURATIONS = new EnumMap<>(BattleStage.class);

    /** Holds the {@link BattleStage} of this battle. */
    private BattleStage stage;

    /** Holds a {@link Collection} {@link TownBlock}s that belong to the town before the battle, for restoration. */
    private final Collection<TownBlock> INITIAL_TOWN_BLOCKS;

    /** Holds the {@link Resident} who was mayor of the {@link #CONTESTED_TOWN} at the time of the attack. */
    private final Resident INITIAL_MAYOR;

    /** Holds the Unix Epoch time in milliseconds at which the current {@link #stage} started. */
    private long stageStartTimeMillis;

    /** Holds whether this battle's {@link #CONTESTED_TOWN} is a City State or not. */
    private final boolean isCityState;

    /** Holds the {@link BossBar} of this battle. */
    private BossBar bossBar;

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * @param attacker the attacking nation
     * @param defender the defending nation
     * @param contestedTown the town at which the battle is held
     * @param stm the system time in milliseconds (Unix Epoch) at which the battle started
     * @param preWarBlocks the {@link List} of {@link TownBlock}s that belonged to the town before the battle
     * @param homeBlock the homeblock of the contested town
     * @param isCityState whether this battle's town is a City State or not
     * @param stage the {@link BattleStage} of the battle
     * @param initialMayor the {@link Resident} who was mayor of the {@link #CONTESTED_TOWN} at the time of the attack
     */
    private Battle(Nation attacker, Nation defender, Town contestedTown, Collection<TownBlock> preWarBlocks, long stm, TownBlock homeBlock, boolean isCityState, BattleStage stage, Resident initialMayor) {
        this.ATTACKER = attacker;
        this.DEFENDER = defender;
        this.CONTESTED_TOWN = contestedTown;
        this.flags = new ArrayList<>(); // every flag is lost after a resume.
        this.INITIAL_TOWN_BLOCKS = preWarBlocks;
        this.stageStartTimeMillis = stm;
        this.isCityState = isCityState;
        this.stage = stage;
        this.HOME_BLOCK = homeBlock;
        this.INITIAL_MAYOR = initialMayor;
        STAGE_DURATIONS = BattleUtil.computeStageTimes(this);

        createBossBar();

        var chunks = BattleUtil.toChunks(contestedTown.getTownBlocks(), contestedTown.getWorld());
        ChunkCopy.getInstance().copy(BattleUtil.toChunkSnapshot(chunks));

    }

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * This battle constructor is mainly for starting a new battle.
     * @param attacker the attacking nation
     * @param defender the defending nation
     * @param contestedTown the town at which the battle is held
     * @param isCityState whether this battle's town is a City State or not.
     */
    public Battle(Nation attacker, Nation defender, Town contestedTown, boolean isCityState) {
        this(attacker,
            defender,
            contestedTown,
            new ArrayList<>(contestedTown.getTownBlocks()),
            System.currentTimeMillis(),
            contestedTown.getHomeBlockOrNull(),
            isCityState,
            BattleStage.PRE_FLAG,
            contestedTown.getMayor()
        );
    }

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * This battle constructor is mainly for resuming an existing battle.
     * @param br the {@link BattleRecord} from a database or another persistent storage
     */
    public Battle(BattleRecord br) {
        this(!br.attacker().equals("_") ? TownyAPI.getInstance().getNation(br.attacker()) : null,
            !br.defender().equals("_") ? TownyAPI.getInstance().getNation(br.defender()) : null,
            TownyAPI.getInstance().getTown(br.contestedTown()),
            br.townBlocks(),
            br.stageStartTime(),
            TownyAPI.getInstance().getTownBlock(
            new WorldCoord(Bukkit.getWorld(br.worldID()), br.homeX(), br.homeZ())),
            br.isCityState(),
            br.stage(),
            TownyAPI.getInstance().getResident(br.initialMayorID())
        );
    }

    /** Returns the attacking nation. */
    public Nation getAttacker() {
        return ATTACKER;
    }

    /** Returns the defending nation. */
    public Nation getDefender() {
        return DEFENDER;
    }

    /** Returns the home block of the town where the battle is held. */
    public TownBlock getHomeBlock() {
        return HOME_BLOCK;
    }

    /** Returns the town where the battle is held. */
    public Town getContestedTown() {
        return CONTESTED_TOWN;
    }

    /** Returns the {@link Resident} who was mayor of the {@link #CONTESTED_TOWN} at the time of the attack. */
    public @NotNull Resident getInitialMayor() {
        return INITIAL_MAYOR;
    }

    /**
     * Returns the {@link Duration} of the given {@link BattleStage} for this battle, or {@code null}.
     * @param s the given {@link BattleStage}
     */
    public Duration getDuration(BattleStage s) {
        return STAGE_DURATIONS.getOrDefault(s, null);
    }

    /** Returns the {@link Collection} of {@link TownBlock}s that belonged to this {@link #CONTESTED_TOWN} before the battle. */
    public Collection<TownBlock> getInitialTownBlocks() {
        return INITIAL_TOWN_BLOCKS;
    }

    /** Returns the {@link Collection} of {@link TownBlock}s that have been captured by the {@link #ATTACKER} during the battle. */
    public Collection<TownBlock> getCapturedTownBlocks() {
        Collection<TownBlock> out = getInitialTownBlocks();
        out.removeAll(getContestedTown().getTownBlocks());

        return out;
    }

    /** Returns the {@link Duration} left for the current {@link BattleStage}.
     * If this time is negative, it returns a {@link Duration} of zero seconds.
     */
    public Duration getTimeRemainingForCurrentStage() {
        long elapsedMillis = System.currentTimeMillis() - stageStartTimeMillis;
        Duration remainingTime = STAGE_DURATIONS.get(getCurrentStage()).minusMillis(elapsedMillis);
        return remainingTime.isNegative() ? Duration.ZERO : remainingTime;
    }

    /** Returns whether more time has elapsed than the duration of a {@link BattleStage} of this battle. */
    public boolean isPendingStageAdvance() {
        return getTimeRemainingForCurrentStage().isZero();
    }

    /** Returns the current {@link BattleStage} for this battle. */
    public BattleStage getCurrentStage() {
        return stage;
    }

    /** Returns the Unix Epoch start time, in milliseconds, of the current {@link #stage} of this battle. */
    public long getStageStartTime() {
        return stageStartTimeMillis;
    }

    /**
     * Sets the {@link BattleStage} for this battle and resets the {@link Battle#stageStartTimeMillis}
     * @param stage the {@link BattleStage} to be set
     */
    public void setStage(BattleStage stage) {
        stageStartTimeMillis = System.currentTimeMillis();
        this.stage = stage;
    }

    /** Returns whether this battle's {@link #CONTESTED_TOWN} is a City State or not. */
    public boolean isCityState() {
        return isCityState;
    }

    /** Adds a new flag to the list of flags.
     * @param name the name of the flag owner
     */
    public void addFlag(String name) {
        flags.add(name);
    }

    /** Removes an existing flag from the list of flags.
     * @param name the name of the flag owner
     */
    public void removeFlag(String name) {
        flags.remove(name);
    }

    /**
     * Advances the stage of this battle to the next one.
     * @param win whether the battle is to be won by the {@link #DEFENDER} if the next stage ends it.
     */
    public void advanceStage(boolean win) {
        switch (stage) {
            case PRE_FLAG -> makeFlaggable();
            case FLAG -> { if (win) winDefense(); else loseDefense(); }
            case RUINED -> unRuin();
            case DORMANT -> BattleManager.removeBattle(this);
        }
    }

    /**
     *  The function to be called when the battle is to allow flags.
     */
    public void makeFlaggable() {
        setStage(BattleStage.FLAG);
        Bukkit.getPluginManager().callEvent(new BattleFlaggableEvent(this));
    }

    /**
     * The function to be called when a defense is lost (homeblock is won).
     */
    public void loseDefense() {
        endWarProcedures();
        ruin();
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, false));
    }

    /**
     * Returns whether this {@link Battle} is in its {@link BattleStage#PRE_FLAG} or {@link BattleStage#FLAG} states.
     */
    public boolean isActive() {
        return getCurrentStage() == BattleStage.PRE_FLAG || getCurrentStage() == BattleStage.FLAG;
    }

    /**
     * The function to be called when a defense is won (time runs out before defense is won).
     */
    public void winDefense() {
        endWarProcedures();
        makeDormant();
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, true));
    }

    /**
     * Deletes the {@link Battle#bossBar} and sets the {@link Battle#stage} to {@link BattleStage#DORMANT}.
     */
    private void makeDormant() {
        setStage(BattleStage.DORMANT);
        deleteBossBar();

        ChunkPaste.getInstance()
            .paste(BattleUtil.toChunks(getInitialTownBlocks(), getContestedTown().getWorld()), getContestedTown().getWorld());
    }

    /** Puts the {@link #CONTESTED_TOWN} into a ruined state. */
    private void ruin() {
        setStage(BattleStage.RUINED);
        if (getContestedTown() != null)
            TownRuinUtil.putTownIntoRuinedState(getContestedTown());
    }

    /** Puts the {@link #CONTESTED_TOWN} out of its ruined state and turns it {@link BattleStage#DORMANT}. */
    private void unRuin() {
        makeDormant();
        if (getContestedTown().isRuined())
            TownRuinUtil.reclaimTown(getInitialMayor(), getContestedTown());
    }

    /** Procedures to be performed at the end of a war, regardless of the result, such as transferring ownership of {@link TownBlock}s back and cancelling ongoing flags. */
    private void endWarProcedures() {

        for (String n : flags) FlagWar.removeAttackerFlags(n);

        transferBlockOwnership(getContestedTown(), getInitialTownBlocks(), getHomeBlock());
    }

    /**
     * Reclaims the ownership of the specified {@link TownBlock}s
     * @param town the {@link Town}
     * @param townBlocks the {@link TownBlock}s to be reclaimed by this {@link Town}
     * @param homeBlock the new homeblock of this {@link Town}
     */
    private void transferBlockOwnership(final Town town, final Collection<TownBlock> townBlocks, final TownBlock homeBlock) {
        try {
            for (var tb : townBlocks) {
                tb.setTown(town);
                tb.save();
            }

            town.setHomeBlock(homeBlock);

        } catch (Exception E) {
            // Couldn't claim it.
            TownyMessaging.sendErrorMsg(E.getMessage());
            E.printStackTrace();
        }
    }

    /**
     * Creates the {@link Battle#bossBar}.
     */
    private void createBossBar() {

        String bossBarMessage = "[BATTLE] %s - %s"; // probably going to make this configurable?

        bossBar = Bukkit.createBossBar(
            String.format(bossBarMessage, getContestedTown().getName(), getCurrentStage().name().toUpperCase()),
            BarColor.RED,
            BarStyle.SOLID
        );
    }

    /**
     * Updates the {@link Battle#bossBar}.
     */
    public void updateBossBar() {

        if (getCurrentStage() == BattleStage.DORMANT) return;

        String bossBarMessage = "[BATTLE] %s - %s"; // probably going to make this configurable?

        if (bossBar == null) return;

        bossBar.setProgress(1.0 - (
            (double) getTimeRemainingForCurrentStage().toSeconds() / getDuration(getCurrentStage()).toSeconds())
        );

        bossBar.setTitle(
            String.format(bossBarMessage, getContestedTown().getName(), getCurrentStage().name().toUpperCase())
        );

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Resident r = TownyAPI.getInstance().getResident(p);
            if (isParticipant(r)) bossBar.addPlayer(p);
            else bossBar.removePlayer(p);
        }
    }

    /**
     * Deletes the {@link Battle#bossBar}.
     */
    public void deleteBossBar() {
        if (bossBar == null) return;
        bossBar.removeAll();
        bossBar = null;
    }

    /**
     * Returns whether a {@link Resident} is part of either the attacking {@link Nation}, defending {@link Nation} or one of their allies.
     * @param r the {@link Resident} in question.
     */
    public boolean isParticipant(Resident r) {
        Set<Nation> relevantNations = new HashSet<>();

        if (r == null || r.getTownOrNull() == null || r.getNationOrNull() == null) return false;

        relevantNations.add(getDefender());
        relevantNations.add(getAttacker());
        if (getDefender() != null) relevantNations.addAll(getDefender().getAllies());
        if (getAttacker() != null) relevantNations.addAll(getAttacker().getAllies());

        return relevantNations.contains(r.getTownOrNull().getNationOrNull()) || getContestedTown().getResidents().contains(r);
    }

    /**
     * Returns the {@link CellUnderAttack} with the specified X and Z chunk coordinates.
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    public CellUnderAttack getCellUnderAttack(int x, int z) {

        if (flags.isEmpty()) return null;

        for (var name : flags) {

            var cuas = FlagWar.getCellsUnderAttackByPlayer(name);

            if (!cuas.isEmpty()) {
                CellUnderAttack cua = cuas.get(0); // there is only one flag per player.

                if (cua.getX() ==  x && cua.getZ() == z)
                    return cua;
            }
        }

        return null;
    }

    /**
     * Gets every flag's flag owner associated with this {@link Battle}.
     */
    public Collection<String> getCellsUnderAttack() {
        return flags;
    }
}
