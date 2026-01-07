package team.rubyhorizon.campfires.core.campfire;

import team.rubyhorizon.campfires.core.world.Location;

public interface Campfire {
    Location getLocation();

    boolean isBurning();

    void setBurning(boolean state);

    boolean isDestroyed();

    CampfireHologram getHologram();
}
