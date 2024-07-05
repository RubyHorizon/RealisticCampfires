package team.rubyhorizon.campfires.configuration.campfire;

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

        drawDistance = progressBarSection.getInt("draw-distance");
        drawDistancePersonally = progressBarSection.getInt("draw-distance-personally");
        drawForSurvival = progressBarSection.getBoolean("draw-for-survival");
        drawYOffset = progressBarSection.getDouble("draw-Y-offset");
        size = progressBarSection.getInt("size");
        symbolBack = progressBarSection.getString("symbol.back");
        symbolFront = progressBarSection.getString("symbol.front");

        if(drawDistance <= 0 || drawDistancePersonally <= 0) {
            throw new RuntimeException("drawDistance or drawDistancePersonally value cannot be lower of 0!");
        }

        if(size <= 0) {
            throw new RuntimeException("size value cannot be lower of 0!");
        }
    }
}
