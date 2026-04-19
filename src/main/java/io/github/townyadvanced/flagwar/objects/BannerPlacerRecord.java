package io.github.townyadvanced.flagwar.objects;

import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.config.BannerWarConfig;

/**
 * A record of a banner placement.
 * @param town the town whose resident placed the banner
 * @param dayOfAttack the day since the plugin's installation that the attack has occurred, assuming the plugin is available during new Towny day events.
 */
public record BannerPlacerRecord(
    Town town,
    long dayOfAttack
)
{
    /**
     * Returns a {@link BannerPlacerRecord} with this town and the current towny day, by calling {@link BannerWarConfig#getCurrentTownyDay()}.
     * @param town the town in question
     */
    public static BannerPlacerRecord of(Town town) {
        return new BannerPlacerRecord(town, BannerWarConfig.getCurrentTownyDay());
    }
}
