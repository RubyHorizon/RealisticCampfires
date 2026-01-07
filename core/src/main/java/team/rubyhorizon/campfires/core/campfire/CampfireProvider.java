package team.rubyhorizon.campfires.core.campfire;

import org.jetbrains.annotations.Nullable;
import team.rubyhorizon.campfires.core.world.Location;

import java.util.List;
import java.util.function.Consumer;

public interface CampfireProvider {
    List<Campfire> getAll();

    @Nullable
    Campfire getAt(Location location);

    void addCampfireListener(Consumer<Campfire> consumer);
}
