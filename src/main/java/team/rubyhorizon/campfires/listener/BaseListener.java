package team.rubyhorizon.campfires.listener;

import lombok.AllArgsConstructor;
import team.rubyhorizon.campfires.configuration.Bundle;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class BaseListener implements Listener, PluginDisableListener {
    protected Bundle bundle;
}
