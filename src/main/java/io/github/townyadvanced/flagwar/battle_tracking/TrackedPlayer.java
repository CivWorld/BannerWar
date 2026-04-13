package io.github.townyadvanced.flagwar.battle_tracking;

import org.bukkit.entity.Player;

import java.util.UUID;

class TrackedPlayer {
    private final UUID PLAYER_UUID;
    private final String PLAYER_NAME;

    public TrackedPlayer(Player player) {
        this.PLAYER_UUID = player.getUniqueId();
        this.PLAYER_NAME = player.getName();
    }
}
