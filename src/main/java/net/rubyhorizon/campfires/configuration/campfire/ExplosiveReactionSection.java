package net.rubyhorizon.campfires.configuration.campfire;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class ExplosiveReactionSection {
    private final boolean enable;
    private final double damageOfGunpowder;
    private final double powerOfTNT;
    private final boolean setFireAfterExplode;
    private final boolean breakBlocksAfterExplode;

    public ExplosiveReactionSection(FileConfiguration fileConfiguration, String sectionName) {
        ConfigurationSection explosiveReactionSection = fileConfiguration.getConfigurationSection(sectionName);

        if(explosiveReactionSection == null) {
            throw new RuntimeException("Section with name: \"" + sectionName + "\" not found!");
        }

        enable = explosiveReactionSection.getBoolean("enable");
        damageOfGunpowder = explosiveReactionSection.getDouble("damageOfGunpowder");
        powerOfTNT = explosiveReactionSection.getDouble("powerOfTNT");
        setFireAfterExplode = explosiveReactionSection.getBoolean("setFireAfterExplode");
        breakBlocksAfterExplode = explosiveReactionSection.getBoolean("breakBlocksAfterExplode");

        if(damageOfGunpowder <= 0) {
            throw new RuntimeException("damageOfGunpowder value cannot be lower of 0!");
        }

        if(powerOfTNT <= 0) {
            throw new RuntimeException("powerOfTNT value cannot be lower of 0!");
        }
    }
}