package team.rubyhorizon.campfires.campfire;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CampfireIndicatorProtocolManager {
    void spawnOrUpdate(@NotNull List<? extends Player> packetReceivers, @NotNull CampfireIndicator campfireIndicator);
    void update(@NotNull List<? extends Player> packetReceivers, @NotNull CampfireIndicator campfireIndicator);
    void destroy(@NotNull List<? extends Player> packetReceivers, @NotNull CampfireIndicator campfireIndicator);

    default void spawnOrUpdate(@NotNull Player player, @NotNull CampfireIndicator campfireIndicator) {
        spawnOrUpdate(List.of(player), campfireIndicator);
    }

    default void update(@NotNull Player player, @NotNull CampfireIndicator campfireIndicator) {
        update(List.of(player), campfireIndicator);
    }

    default void destroy(@NotNull Player player, @NotNull CampfireIndicator campfireIndicator) {
        destroy(List.of(player), campfireIndicator);
    }
}
