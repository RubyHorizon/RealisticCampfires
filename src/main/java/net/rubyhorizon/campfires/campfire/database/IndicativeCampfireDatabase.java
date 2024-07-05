package net.rubyhorizon.campfires.campfire.database;

import net.rubyhorizon.campfires.campfire.IndicativeCampfire;

import java.util.Collection;

public interface IndicativeCampfireDatabase {
    void save(Collection<IndicativeCampfire> indicativeCampfires);
    Collection<IndicativeCampfire> load();
    void clear();
}
