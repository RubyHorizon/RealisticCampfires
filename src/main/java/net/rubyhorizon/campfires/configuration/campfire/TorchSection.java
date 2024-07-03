package net.rubyhorizon.campfires.configuration.campfire;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class TorchSection {
    private final boolean torch;
    private final boolean soulTorch;
    private final boolean redstoneTorch;

    public TorchSection(FileConfiguration fileConfiguration, String sectionName) {
        ConfigurationSection torchSection = fileConfiguration.getConfigurationSection(sectionName);

        if(torchSection == null) {
            throw new RuntimeException("Section with name: \"" + sectionName + "\" not found!");
        }

        torch = fileConfiguration.getBoolean("torch");
        soulTorch = fileConfiguration.getBoolean("soulTorch");
        redstoneTorch = fileConfiguration.getBoolean("redstoneTorch");
    }

    public boolean isTorchAllowed(Material material) {
        return switch(material) {
            case TORCH -> isTorch();
            case SOUL_TORCH -> isSoulTorch();
            case REDSTONE_TORCH -> isRedstoneTorch();
            default -> false;
        };
    }
}
