package net.rubyhorizon.campfires.configuration.campfire;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@Getter
@Builder
@ToString
public class CampfireBurningItemConfiguration {

    @EqualsAndHashCode
    @Getter
    @ToString
    @AllArgsConstructor
    public static class BurningItem {
        private Material material;
        private long ticks;
    }

    private List<BurningItem> burningItems;
    private long maxBurningTime;

    public static CampfireBurningItemConfiguration getInstance(FileConfiguration fileConfiguration, String campfireConfigPath) {
        CampfireBurningItemConfigurationBuilder builder = CampfireBurningItemConfiguration.builder();

        ConfigurationSection burningItemsSection = fileConfiguration.getConfigurationSection(campfireConfigPath + ".burningItems");
        if(burningItemsSection == null) throw new RuntimeException("Section with name: \"" + campfireConfigPath + "\" not found!");

        Map<String, Object> burningItemsMap = burningItemsSection.getValues(true);
        List<BurningItem> burningItemsList = new ArrayList<>();

        for(Map.Entry<String, Object> entry: burningItemsMap.entrySet()) {
            Material materialOfItem = null;

            for(Material material: Material.values()) {
                if(material.name().equals(entry.getKey())) {
                    materialOfItem = material;
                    break;
                }
            }

            if(materialOfItem != null) {
                burningItemsList.add(new BurningItem(materialOfItem, Long.parseLong(entry.getValue().toString())));
            }
        }

        builder.burningItems(burningItemsList);
        builder.maxBurningTime(fileConfiguration.getLong(campfireConfigPath + ".maxBurningTime"));

        return builder.build();
    }
}
