package net.rubyhorizon.campfires.configuration.campfire;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@Getter
@Builder(access = AccessLevel.PACKAGE)
@ToString
public class BurningItemConfiguration {

    @EqualsAndHashCode
    @Getter
    @ToString
    @AllArgsConstructor
    public static class BurningItem {
        private Material material;
        private int ticks;
    }

    private List<BurningItem> burningItems;
    private int maxBurningTime;

    public static BurningItemConfiguration getInstance(FileConfiguration fileConfiguration, String campfireConfigPath) {
        BurningItemConfigurationBuilder builder = BurningItemConfiguration.builder();

        ConfigurationSection burningItemsSection = fileConfiguration.getConfigurationSection(campfireConfigPath + ".burningItems");

        if(burningItemsSection == null) {
            throw new RuntimeException("Section with name: \"" + campfireConfigPath + "\" not found!");
        }

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
                burningItemsList.add(new BurningItem(materialOfItem, Integer.parseInt(entry.getValue().toString())));
            }
        }

        builder.burningItems(burningItemsList);
        builder.maxBurningTime(fileConfiguration.getInt(campfireConfigPath + ".maxBurningTime"));

        if(builder.maxBurningTime <= 0) {
            throw new RuntimeException("Max burning time has not possible lower of 0!");
        }

        for(BurningItem burningItem: builder.burningItems) {
            if(burningItem.getTicks() > builder.maxBurningTime) {
                throw new RuntimeException("Burning item ticks value be can`t bigger of max burning time (%s > %s)".formatted(burningItem.getTicks(), builder.maxBurningTime));
            }
        }

        return builder.build();
    }
}
