package net.rubyhorizon.campfires.configuration.campfire;

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

    public CampfireConfiguration(FileConfiguration fileConfiguration) {
        commonCampfire = new BaseCampfireSection(fileConfiguration, "campfire.common");
        soulCampfire = new BaseCampfireSection(fileConfiguration, "campfire.soul");
        progressBar = new ProgressBarSection(fileConfiguration, "campfire.progressBar");
        torch = new TorchSection(fileConfiguration, "campfire.allowFireCampfireByTorch");

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
    }

    public List<BaseCampfireSection.BurningItem> getBurningItemsOfCampfire(Material material) {
        return switch(material) {
            case CAMPFIRE -> getCommonCampfire().getBurningItems();
            case SOUL_CAMPFIRE -> getSoulCampfire().getBurningItems();
            default -> throw new IllegalArgumentException("%s is not campfire!".formatted(material.name()));
        };
    }
    
    public long getMaxBurningTimeOfCampfire(Material material) {
        return switch(material) {
            case CAMPFIRE -> getCommonCampfire().getMaxBurningTimeMillis();
            case SOUL_CAMPFIRE -> getSoulCampfire().getMaxBurningTimeMillis();
            default -> throw new IllegalArgumentException("%s is not campfire!".formatted(material.name()));
        };
    }

    public ExplosiveReactionSection getExplosiveReactionOfCampfire(Material material) {
        return switch(material) {
            case CAMPFIRE -> getCommonCampfire().getExplosiveReaction();
            case SOUL_CAMPFIRE -> getSoulCampfire().getExplosiveReaction();
            default -> throw new IllegalArgumentException("%s is not campfire!".formatted(material.name()));
        };
    }
}
