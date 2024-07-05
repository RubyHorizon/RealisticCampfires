package team.rubyhorizon.campfires.campfire;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IndicativeCampfireProtocolManager {
    void spawnOrUpdate(@NotNull List<? extends Player> packetReceivers, @NotNull IndicativeCampfire indicativeCampfire);
    void update(@NotNull List<? extends Player> packetReceivers, @NotNull IndicativeCampfire indicativeCampfire);
    void destroy(@NotNull List<? extends Player> packetReceivers, @NotNull IndicativeCampfire indicativeCampfire);

    default void spawnOrUpdate(@NotNull Player player, @NotNull IndicativeCampfire indicativeCampfire) {
        spawnOrUpdate(List.of(player), indicativeCampfire);
    }

    default void update(@NotNull Player player, @NotNull IndicativeCampfire indicativeCampfire) {
        update(List.of(player), indicativeCampfire);
    }

    default void destroy(@NotNull Player player, @NotNull IndicativeCampfire indicativeCampfire) {
        destroy(List.of(player), indicativeCampfire);
    }
}
