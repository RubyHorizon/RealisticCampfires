package net.rubyhorizon.campfires.configuration.campfire;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@Getter
public class BaseCampfireSection {

    @EqualsAndHashCode
    @Getter
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class BurningItem {
        private final Material material;
        private final long timeMillis;
    }

    private final List<BurningItem> burningItems;
    private final int maxBurningTimeMillis;

    private final ExplosiveReactionSection explosiveReaction;

    public BaseCampfireSection(FileConfiguration fileConfiguration, String sectionName) {
        ConfigurationSection baseCampfireSection = fileConfiguration.getConfigurationSection(sectionName);

        if(baseCampfireSection == null) {
            throw new RuntimeException("Section with name: \"" + sectionName + "\" not found!");
        }

        ConfigurationSection burningItemsSection = baseCampfireSection.getConfigurationSection("burning-items");

        if(burningItemsSection == null) {
            throw new RuntimeException("Missing burningItems section!");
        }

        Map<String, Object> burningItemsMap = burningItemsSection.getValues(true);
        burningItems = new ArrayList<>();

        for(Map.Entry<String, Object> entry: burningItemsMap.entrySet()) {
            for(Material material: Material.values()) {
                if(material.name().equals(entry.getKey())) {
                    try {
                        burningItems.add(new BurningItem(material, Long.parseLong(entry.getValue().toString())));
                    } catch(NumberFormatException numberFormatException) {
                        throw new RuntimeException("Needed a number, not \"%s\"!".formatted(entry.getValue().toString()));
                    }
                    break;
                }
            }
        }

        maxBurningTimeMillis = baseCampfireSection.getInt("max-burning-time-millis");

        if(maxBurningTimeMillis <= 0) {
            throw new RuntimeException("maxBurningTimeMillis value cannot be lower of 0!");
        }

        for(BurningItem burningItem: burningItems) {
            if(burningItem.getTimeMillis() > maxBurningTimeMillis) {
                throw new RuntimeException("maxBurningTimeMillis value be can`t bigger of max burning time (%s > %s)".formatted(burningItem.getTimeMillis(), maxBurningTimeMillis));
            }
        }

        explosiveReaction = new ExplosiveReactionSection(fileConfiguration, "%s.explosive-reaction".formatted(sectionName));
    }
}
