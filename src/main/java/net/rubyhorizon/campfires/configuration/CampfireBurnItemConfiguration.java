package net.rubyhorizon.campfires.configuration;

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
public class CampfireBurnItemConfiguration {

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

    public static CampfireBurnItemConfiguration getInstance(FileConfiguration fileConfiguration, String campfireConfigPath) {
        CampfireBurnItemConfigurationBuilder builder = CampfireBurnItemConfiguration.builder();

        ConfigurationSection burningItemsSection = fileConfiguration.getConfigurationSection(campfireConfigPath + "burningItems");
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
        builder.maxBurningTime(fileConfiguration.getLong(campfireConfigPath + "maxBurningTime"));

        return builder.build();
    }
}
