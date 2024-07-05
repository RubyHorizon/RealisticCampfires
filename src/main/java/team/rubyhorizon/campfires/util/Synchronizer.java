package team.rubyhorizon.campfires.util;

import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class Synchronizer {
    private final Plugin plugin;

    public CompletableFuture<Void> synchronize(Runnable runnable) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                    completableFuture.complete(null);
                } catch(Exception exception) {
                    completableFuture.completeExceptionally(exception);
                }
            }
        }.runTask(plugin);

        return completableFuture;
    }
}
