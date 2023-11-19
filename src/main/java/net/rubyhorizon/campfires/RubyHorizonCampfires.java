package net.rubyhorizon.campfires;

import net.rubyhorizon.campfires.configuration.campfire.CampfireConfiguration;
import net.rubyhorizon.campfires.listener.campfire.CampfireListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class RubyHorizonCampfires extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bundle bundle = new Bundle(this, CampfireConfiguration.getInstance(getConfig()));

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new CampfireListener(bundle), this);
    }
}
