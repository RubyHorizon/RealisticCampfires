package team.rubyhorizon.campfires.campfire.database;

import team.rubyhorizon.campfires.campfire.CampfireIndicator;

import java.util.Collection;

public interface CampfireIndicatorDatabase {
    void save(Collection<CampfireIndicator> campfireIndicators);
    Collection<CampfireIndicator> load();
    void clear();
}
