package io.github.townyadvanced.flagwar.objects;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.bukkit.towny.utils.TownRuinUtil;
import io.github.townyadvanced.flagwar.BattleManager;
import io.github.townyadvanced.flagwar.events.BattleEndEvent;
import io.github.townyadvanced.flagwar.events.BattleFlagEvent;
import io.github.townyadvanced.flagwar.util.BannerWarUtil;
import org.bukkit.Bukkit;

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

    /** Holds a {@link List} of {@link CellUnderAttack}s relevant to this battle. */
    private List<CellUnderAttack> flags;

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

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * This battle constructor is mainly for resuming an existing battle.
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
        STAGE_DURATIONS = BannerWarUtil.computeStageTimes(this);
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
        this.ATTACKER = attacker;
        this.DEFENDER = defender;
        this.CONTESTED_TOWN = contestedTown;
        this.flags = new ArrayList<>();
        this.INITIAL_TOWN_BLOCKS = new ArrayList<>(contestedTown.getTownBlocks());
        this.stageStartTimeMillis = System.currentTimeMillis();
        this.isCityState = isCityState;
        this.stage = BattleStage.PRE_FLAG;
        this.INITIAL_MAYOR = contestedTown.getMayor();

        // shouldn't be null as an existing home block is a requirement for a war to be initiated.
        this.HOME_BLOCK = contestedTown.getHomeBlockOrNull();
        STAGE_DURATIONS = BannerWarUtil.computeStageTimes(this);
    }

    /**
     * Sets up a battle between an attacking nation and a defending nation. <br>
     * This battle constructor is mainly for resuming an existing battle.
     * @param br the {@link BattleRecord} from a database or another persistent storage
     */
    public Battle(BattleRecord br) {
        this(TownyAPI.getInstance().getNation(br.attacker()),
            TownyAPI.getInstance().getNation(br.defender()),
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
    public Resident getInitialMayor() {
        return INITIAL_MAYOR;
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
        long elapsedMillis = System.currentTimeMillis() - stageStartTimeMillis;
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

    /** Adds a new {@link CellUnderAttack} to the list of flags. */
    public void addFlag(CellUnderAttack attackData) {
        flags.add(attackData);
    }

    /**
     * Advances the stage of this battle to the next one.
     * @param win whether the battle is to be won by the {@link #DEFENDER} if the next stage ends it.
     */
    public void advanceStage(boolean win) {
        switch (stage) {
            case PRE_FLAG -> beginFlag();
            case FLAG -> { if (win) winDefense(); else loseDefense(); }
            case RUINED -> unRuin();
            case DORMANT -> BattleManager.removeBattle(this);
        }
    }

    /**
     *  The function to be called when the battle is to allow flags.
     */
    public void beginFlag() {
        stage = BattleStage.FLAG;
        Bukkit.getPluginManager().callEvent(new BattleFlagEvent(this));
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
     * The function to be called when a defense is won (time runs out before defense is won).
     */
    public void winDefense() {
        endWarProcedures();
        setStage(BattleStage.DORMANT);
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, true));
    }

    /** Puts the {@link #CONTESTED_TOWN} into a ruined state. */
    private void ruin() {
        setStage(BattleStage.RUINED);
        TownRuinUtil.putTownIntoRuinedState(getContestedTown());
    }

    /** Puts a town out of its ruined state and turns it {@link BattleStage#DORMANT}. */
    private void unRuin() {
        setStage(BattleStage.DORMANT);
        TownRuinUtil.reclaimTown(getInitialMayor(), getContestedTown());
    }

    /** Procedures to be performed at the end of a war, such as transferring ownership of {@link TownBlock}s back. */
    private void endWarProcedures() {
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
}
