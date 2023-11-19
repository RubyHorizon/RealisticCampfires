package net.rubyhorizon.campfires;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.rubyhorizon.campfires.configuration.campfire.CampfireConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@AllArgsConstructor
public class Bundle {
    private JavaPlugin javaPlugin;
    private CampfireConfiguration campfireConfiguration;
}
