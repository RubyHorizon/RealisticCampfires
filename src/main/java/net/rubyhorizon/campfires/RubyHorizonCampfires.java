package net.rubyhorizon.campfires;

import org.bukkit.plugin.java.JavaPlugin;

public final class RubyHorizonCampfires extends JavaPlugin {

    private CampfireConfiguration campfireConfiguration;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        campfireConfiguration = CampfireConfiguration.getInstance(getConfig());
        campfireConfiguration.getBurningItems().forEach(System.out::println);
    }
}
