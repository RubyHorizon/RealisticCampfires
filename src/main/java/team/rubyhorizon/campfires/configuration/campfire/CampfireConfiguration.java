package team.rubyhorizon.campfires.configuration.campfire;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CampfireConfiguration {
    private final BaseCampfireSection commonCampfire;
    private final BaseCampfireSection soulCampfire;
    private final ProgressBarSection progressBar;
    private final TorchSection torch;

    private final String databaseFilename;

    public CampfireConfiguration(FileConfiguration fileConfiguration) {
        commonCampfire = new BaseCampfireSection(fileConfiguration, "campfire.common");
        soulCampfire = new BaseCampfireSection(fileConfiguration, "campfire.soul");
        progressBar = new ProgressBarSection(fileConfiguration, "campfire.progress-bar");
        torch = new TorchSection(fileConfiguration, "campfire.allow-fire-campfire-by-torch");

        List<BaseCampfireSection.BurningItem> allBurningItems = new ArrayList<>();
        allBurningItems.addAll(commonCampfire.getBurningItems());
        allBurningItems.addAll(soulCampfire.getBurningItems());

        for(BaseCampfireSection.BurningItem burningItem: allBurningItems) {
            switch(burningItem.getMaterial()) {
                case TORCH -> {
                    if(torch.isTorch()) {
                        throw new RuntimeException("Conflict: torch is burning and allowed for fire.");
                    }
                }

                case SOUL_TORCH -> {
                    if(torch.isSoulTorch()) {
                        throw new RuntimeException("Conflict: soul torch is burning and allowed for fire.");
                    }
                }

                case REDSTONE_TORCH -> {
                    if(torch.isRedstoneTorch()) {
                        throw new RuntimeException("Conflict: redstone torch is burning and allowed for fire.");
                    }
                }
            }
        }

        databaseFilename = fileConfiguration.getString("campfire.database-filename", "database.db");
    }

    private BaseCampfireSection getCampfireSectionBy(Material material) {
        return switch (material) {
            case CAMPFIRE -> getCommonCampfire();
            case SOUL_CAMPFIRE -> getSoulCampfire();
            default -> throw new IllegalArgumentException("%s is not campfire!".formatted(material.name()));
        };
    }

    public List<BaseCampfireSection.BurningItem> getBurningItemsOfCampfire(Material material) {
        return getCampfireSectionBy(material).getBurningItems();
    }
    
    public long getMaxBurningTimeOfCampfire(Material material) {
        return getCampfireSectionBy(material).getMaxBurningTimeMillis();
    }

    public ExplosiveReactionSection getExplosiveReactionOfCampfire(Material material) {
        return getCampfireSectionBy(material).getExplosiveReaction();
    }

    public boolean getEnableStatusOfCampfire(Material material) {
        return getCampfireSectionBy(material).isEnable();
    }
}
