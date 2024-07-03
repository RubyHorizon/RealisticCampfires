package net.rubyhorizon.campfires;

import net.rubyhorizon.campfires.campfire.IndicativeCampfireProtocolManagerImpl;
import net.rubyhorizon.campfires.configuration.Bundle;
import net.rubyhorizon.campfires.configuration.campfire.CampfireConfiguration;
import net.rubyhorizon.campfires.listener.BaseListener;
import net.rubyhorizon.campfires.listener.campfire.CampfireListener;
import net.rubyhorizon.campfires.util.Synchronizer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class RubyHorizonCampfires extends JavaPlugin {
    private final List<BaseListener> listeners = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bundle bundle = new Bundle(new CampfireConfiguration(getConfig()));
        registerListeners(new CampfireListener(bundle, new IndicativeCampfireProtocolManagerImpl(bundle), new Synchronizer(this)));
    }

    private void registerListeners(BaseListener... listeners) {
        for(BaseListener listener: listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
            this.listeners.add(listener);
        }
    }

    private void unregisterListeners() {
        listeners.forEach(BaseListener::onPluginDisable);
    }

    @Override
    public void onDisable() {
        unregisterListeners();
    }
}
