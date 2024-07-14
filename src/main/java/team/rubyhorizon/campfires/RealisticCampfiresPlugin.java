package team.rubyhorizon.campfires;

import team.rubyhorizon.campfires.campfire.IndicativeCampfireProtocolManagerImpl;
import team.rubyhorizon.campfires.campfire.database.IndicativeCampfireDatabaseImpl;
import team.rubyhorizon.campfires.configuration.Bundle;
import team.rubyhorizon.campfires.configuration.campfire.CampfireConfiguration;
import team.rubyhorizon.campfires.listener.BaseListener;
import team.rubyhorizon.campfires.listener.campfire.CampfireListener;
import team.rubyhorizon.campfires.util.Metrics;
import team.rubyhorizon.campfires.util.Synchronizer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class RealisticCampfiresPlugin extends JavaPlugin {
    private final List<BaseListener> listeners = new ArrayList<>();

    private static final int METRICS_ID = 22599;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Metrics metrics = new Metrics(this, METRICS_ID);

        Bundle bundle = new Bundle(new CampfireConfiguration(getConfig()));
        registerListeners(new CampfireListener(bundle, new IndicativeCampfireProtocolManagerImpl(bundle),
                new IndicativeCampfireDatabaseImpl(new File(getDataFolder(), bundle.getCampfireConfiguration().getDatabaseFilename())),
                new Synchronizer(this)));
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
