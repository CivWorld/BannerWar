package io.github.townyadvanced.flagwar.objects;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.util.BannerWarUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

/** Contains all the information required to host a BannerWar battle. */
public class Battle {
    /** Holds the {@link Nation} that initiated the battle. */
    private final Nation ATTACKER;

    /** Holds the {@link Nation} that accommodates the town at which the battle is held. */
    private final Nation DEFENDER;

    /** Holds the {@link Town} at which the battle is held. */
    private final Town CONTESTED_TOWN;

    /** Holds a {@link List} of {@link CellUnderAttack}s relevant to this battle. */
    private List<CellUnderAttack> flags;

    /** Holds the critical {@link TownBlock} that, when won, ends the battle. */
    private final TownBlock HOME_BLOCK;

    /** Holds the {@link Duration} of every stage of this war. */
    private EnumMap<BattleStage, Duration> STAGE_DURATIONS = new EnumMap<>(BattleStage.class);

    /** Holds the {@link BattleStage} of this battle. */
    private BattleStage stage;

    /** Holds a {@link Collection} {@link TownBlock}s that belong to the town before the battle, for restoration. */
    private final Collection<TownBlock> INITIAL_TOWN_BLOCKS;

    /** Holds the Unix Epoch time in milliseconds at which the current {@link #stage} started. */
    private final long STAGE_START_TIME_MILLIS;

    /** Holds whether this battle's {@link #CONTESTED_TOWN} is a City State or not. */
    private final boolean isCityState;

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * This battle constructor is mainly for resuming an existing battle.
     * @param attacker the attacking nation
     * @param defender the defending nation
     * @param contestedTown the town at which the battle is held
     * @param bstm the system time in miliseconds (Unix Epoch) at which the battle started
     * @param preWarBlocks the {@link List} of {@link TownBlock}s that belonged to the town before the battle
     * @param homeBlock the homeblock of the contested town
     * @param isCityState whether this battle's town is a City State or not
     * @param stage the {@link BattleStage} of the battle
     */
    public Battle(Nation attacker, Nation defender, Town contestedTown, Collection<TownBlock> preWarBlocks, long bstm, TownBlock homeBlock, boolean isCityState, BattleStage stage) {
        STAGE_DURATIONS = BannerWarUtil.computeStageTimes(this);
        this.ATTACKER = attacker;
        this.DEFENDER = defender;
        this.CONTESTED_TOWN = contestedTown;
        this.flags = new ArrayList<>(); // every flag is lost after a resume.
        this.INITIAL_TOWN_BLOCKS = preWarBlocks;
        this.STAGE_START_TIME_MILLIS = bstm;
        this.isCityState = isCityState;
        this.stage = stage;
        this.HOME_BLOCK = homeBlock;
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
        STAGE_DURATIONS = BannerWarUtil.computeStageTimes(this);
        this.ATTACKER = attacker;
        this.DEFENDER = defender;
        this.CONTESTED_TOWN = contestedTown;
        this.flags = new ArrayList<>();
        this.INITIAL_TOWN_BLOCKS = new ArrayList<>(contestedTown.getTownBlocks());
        this.STAGE_START_TIME_MILLIS = System.currentTimeMillis();
        this.isCityState = isCityState;
        this.stage = BattleStage.PRE_FLAG;

        // shouldn't be null as an existing home block is a requirement for a war to be initiated.
        this.HOME_BLOCK = contestedTown.getHomeBlockOrNull();
    }

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * This battle constructor is mainly for resuming an existing battle.
     * @param record the {@link BattleRecord} from a database or another persistent storage
     */
    public Battle(BattleRecord record) {
        this(
            TownyAPI.getInstance().getNation(record.attacker()),
            TownyAPI.getInstance().getNation(record.defender()),
            TownyAPI.getInstance().getTown(record.contestedTown()),
            record.townBlocks(),
            record.stageStartTime(),
            TownyAPI.getInstance().getTownBlock(new WorldCoord(TownyAPI.getInstance().getNation(record.attacker()).getWorld(), record.homeX(), record.homeZ())),
            record.isCityState(),
            record.stage()
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

    /**
     * Returns the {@link Duration} of the given {@link BattleStage} for this battle, or {@code null}.
     * @param s the given {@link BattleStage}
     */
    public Duration getDuration(BattleStage s) {
        return STAGE_DURATIONS.getOrDefault(s, null);
    }

    /** Returns the list of {@link TownBlock}s that belonged to this {@link #CONTESTED_TOWN} before the battle. */
    public Collection<TownBlock> getInitialTownBlocks() {
        return INITIAL_TOWN_BLOCKS;
    }

    /** Returns the {@link Duration} left for the current {@link BattleStage}.
     * If this time is negative, it returns a {@link Duration} of zero seconds.
     */
    public Duration getTimeRemainingForCurrentStage() {
        long elapsedMillis = System.currentTimeMillis() - STAGE_START_TIME_MILLIS;
        Duration remainingTime = STAGE_DURATIONS.get(this.getStage()).minusMillis(elapsedMillis);
        return remainingTime.isNegative() ? Duration.ZERO : remainingTime;
    }

    /** Returns whether more time has elapsed than the duration of a {@link BattleStage} of this battle. */
    public boolean isPendingStageAdvance() {
        return getTimeRemainingForCurrentStage().isZero();
    }

    /** Returns the current {@link BattleStage} for this battle. */
    public BattleStage getStage() {
        return stage;
    }

    /** Returns the Unix Epoch start time, in millis, of the current {@link #stage} of this battle. */
    public long getStageStartTime() {
        return STAGE_START_TIME_MILLIS;
    }

    /**
     * Sets the {@link BattleStage} for this battle.
     * @param stage the {@link BattleStage} to be set
     */
    public void setStage(BattleStage stage) {
        this.stage = stage;
    }

    /**
     * Returns whether this battle's {@link #CONTESTED_TOWN} is a City State or not.
     */
    public boolean isCityState() {
        return isCityState;
    }

    /** Adds a new {@link CellUnderAttack} to the list of flags. */
    public void addFlag(CellUnderAttack attackData) {
        flags.add(attackData);
    }

    /** Advances the stage of this battle to the next one. <br>
     * @param win whether the battle is to be won if the next stage ends it.
     */
    public void advanceStage(boolean win) {
        switch (stage) {
            case PRE_FLAG -> stage = BattleStage.FLAG;
            case FLAG -> stage = win ? BattleStage.DORMANT : BattleStage.RUINED;
            case RUINED -> stage = BattleStage.DORMANT;
            case DORMANT -> System.out.println("debug sout: battle should be out of cooldown due to a phase advance.");
        }
    }
}
