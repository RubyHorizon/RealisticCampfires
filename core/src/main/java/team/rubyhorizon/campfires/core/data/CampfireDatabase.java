package team.rubyhorizon.campfires.core.data;

import team.rubyhorizon.campfires.core.campfire.Campfire;

import java.util.List;

public interface CampfireDatabase {
    void save(Campfire campfire);

    List<Campfire> getAll();
}
