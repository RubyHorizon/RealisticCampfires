package team.rubyhorizon.campfires.core.campfire;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface CampfireHologram {
    void hideFor(UUID uuid);

    void showFor(UUID uuid);

    boolean isHidedFor(UUID uuid);

    void setText(Component component);

    Component getText();
}
