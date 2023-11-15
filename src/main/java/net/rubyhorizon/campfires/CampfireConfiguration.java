package net.rubyhorizon.campfires;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@Getter
@Builder
public class CampfireConfiguration {

    @EqualsAndHashCode
    @Getter
    @Builder
    @ToString
    public static class BurningItem {
        private Material material;
        private Long ticks;
    }

    private List<BurningItem> burningItems;
    private Long maxBurningTime;

    public static CampfireConfiguration getInstance(FileConfiguration fileConfiguration) {
        CampfireConfigurationBuilder builder = CampfireConfiguration.builder();

        ConfigurationSection burningItemsSection = fileConfiguration.getConfigurationSection("campfire.burningItems");
        if(burningItemsSection == null) throw new RuntimeException();

        Map<String, Object> burningItemsMap = burningItemsSection.getValues(true);
        List<BurningItem> burningItemsList = new ArrayList<>();

        for(Map.Entry<String, Object> entry: burningItemsMap.entrySet()) {
            burningItemsList.add(
                    BurningItem.builder()
                            .material(Material.valueOf(entry.getKey()))
                            .ticks(Long.parseLong(entry.getValue().toString()))
                            .build()
            );
        }

        builder.burningItems(burningItemsList);
        builder.maxBurningTime(fileConfiguration.getLong("campfire.maxBurningTime"));

        return builder.build();
    }
}
