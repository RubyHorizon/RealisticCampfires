package net.rubyhorizon.campfires.configuration.campfire;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ProgressBarSection {
    private final int drawDistance;
    private final int drawDistancePersonally;
    private final boolean drawForSurvival;
    private final double drawYOffset;
    private final int size;
    private final String symbolFront;
    private final String symbolBack;

    public ProgressBarSection(FileConfiguration fileConfiguration, String sectionName) {
        ConfigurationSection progressBarSection = fileConfiguration.getConfigurationSection(sectionName);

        if(progressBarSection == null) {
            throw new RuntimeException("Section with name: \"" + sectionName + "\" not found!");
        }

        drawDistance = progressBarSection.getInt("drawDistance");
        drawDistancePersonally = progressBarSection.getInt("drawDistancePersonally");
        drawForSurvival = progressBarSection.getBoolean("drawForSurvival");
        drawYOffset = progressBarSection.getDouble("drawYOffset");
        size = progressBarSection.getInt("size");
        symbolBack = progressBarSection.getString("symbol.back");
        symbolFront = progressBarSection.getString("symbol.front");

        if(drawDistance <= 0 || drawDistancePersonally <= 0) {
            throw new RuntimeException("Campfire draw distance value has not possible lower of 0!");
        }

        if(size <= 0) {
            throw new RuntimeException("Campfire progress bar size value has not possible lower of 0!");
        }
    }
}
