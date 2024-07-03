package net.rubyhorizon.campfires.util;

import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class Synchronizer {
    private final Plugin plugin;

    public void synchronize(Runnable runnable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTask(plugin);
    }
}
