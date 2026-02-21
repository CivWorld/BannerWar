package io.github.townyadvanced.flagwar;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleStage;

public final class BannerWarAPI {
    private BannerWarAPI() {}

    /**
     * Returns whether the {@link Town} is already under a battle by the same or a different {@link Nation}.
     * @param town the specified {@link Town}
     */
    public static boolean isInBattle(Town town) {
        return BattleManager.getBattle(town.getName()) != null;
    }

     /**
     * Returns whether the {@link Town} is already under a battle by the same or a different {@link Nation}.
     * @param townName the specified {@link Town}'s name
     */
    public static boolean isInBattle(String townName) {
        return BattleManager.getBattle(townName) != null;
    }

    /**
     * Returns the {@link Battle} object associated with this {@link Town}.
     * @param town the specified {@link Town}
     */
    public static Battle getBattle(Town town) {
        return BattleManager.getBattle(town.getName());
    }

    /**
     * Returns the {@link Battle} object associated with the {@link Town} of this name.
     * @param townName the specified {@link Town}'s name
     */
    public static Battle getBattle(String townName) {
        return BattleManager.getBattle(townName);
    }

    /**
     * Returns whether the {@link Town}'s {@link Battle} is in its {@link io.github.townyadvanced.flagwar.objects.BattleStage#FLAG} state.
     * @param town the specified {@link Town}
     */
    public static boolean canFlag(Town town) {
        Battle battle = BattleManager.getBattle(town.getName());
        return (battle != null && battle.getStage() == BattleStage.FLAG);
    }
}
