package net.rubyhorizon.campfires.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rubyhorizon.campfires.configuration.campfire.CampfireConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@AllArgsConstructor
public class Bundle {
    private final CampfireConfiguration campfireConfiguration;
}
