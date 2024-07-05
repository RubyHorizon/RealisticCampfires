package team.rubyhorizon.campfires.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team.rubyhorizon.campfires.configuration.campfire.CampfireConfiguration;

@Getter
@AllArgsConstructor
public class Bundle {
    private final CampfireConfiguration campfireConfiguration;
}
