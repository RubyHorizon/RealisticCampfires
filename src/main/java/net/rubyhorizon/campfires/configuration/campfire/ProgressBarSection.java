package net.rubyhorizon.campfires.configuration.campfire;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ProgressBarSection {
    private final int drawDistance;
    private final int size;
    private final String symbolFront;
    private final String symbolBack;
    private final boolean drawForSurvival;

    public ProgressBarSection(FileConfiguration fileConfiguration, String sectionName) {
        ConfigurationSection progressBarSection = fileConfiguration.getConfigurationSection(sectionName);

        if(progressBarSection == null) {
            throw new RuntimeException("Section with name: \"" + sectionName + "\" not found!");
        }

        drawDistance = progressBarSection.getInt("drawDistance");
        size = progressBarSection.getInt("size");
        symbolBack = progressBarSection.getString("symbol.back");
        symbolFront = progressBarSection.getString("symbol.front");
        drawForSurvival = progressBarSection.getBoolean("drawForSurvival");

        if(drawDistance <= 0) {
            throw new RuntimeException("Campfire draw instance value has not possible lower of 0!");
        }

        if(size <= 0) {
            throw new RuntimeException("Campfire progress bar size value has not possible lower of 0!");
        }
    }
}
