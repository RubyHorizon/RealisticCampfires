package net.rubyhorizon.campfires;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

@Getter
@Builder
public class CampfireConfiguration {

    @EqualsAndHashCode
    @Getter
    @Builder
    public static class BurningItem {
        private Material material;
        private Long ticks;
    }

    private List<BurningItem> burningItems;
    private Long maxBurningTime;

    public static CampfireConfiguration getInstance(FileConfiguration fileConfiguration) {
        CampfireConfigurationBuilder builder = CampfireConfiguration.builder();

        Object burningItemsObject = fileConfiguration.get("campfire.burningItems");
        if(burningItemsObject == null) throw new RuntimeException("Campfire configuration path not found!");

        Map<String, Long> burningItemsMap = (Map<String, Long>) burningItemsObject;
        List<Material> materials = Arrays.stream(Material.values()).toList();

        List<BurningItem> burningItems = new ArrayList<>();

        for(Map.Entry<String, Long> burningItem: burningItemsMap.entrySet()) {
            for(Material material: materials) {
                if(material.name().equals(burningItem.getKey())) {
                    burningItems.add(
                            BurningItem.builder()
                                    .material(material)
                                    .ticks(burningItem.getValue())
                                    .build()
                    );
                    break;
                }
            }
        }

        builder.burningItems(burningItems);
        builder.maxBurningTime(fileConfiguration.getLong("maxBurningTime"));

        return builder.build();
    }
}
