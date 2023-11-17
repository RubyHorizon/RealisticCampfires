package net.rubyhorizon.campfires.configuration;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
@Builder
public class CampfireConfiguration {
    private CampfireBurnItemConfiguration commonCampfire;
    private CampfireBurnItemConfiguration soulCampfire;
    private Long campfireCleanerRate;

    public static CampfireConfiguration getInstance(FileConfiguration fileConfiguration) {
        ConfigurationBuilder builder = CampfireConfiguration.builder();
        builder.commonCampfire(CampfireBurnItemConfiguration.getInstance(fileConfiguration, "campfire.common"));
        builder.soulCampfire(CampfireBurnItemConfiguration.getInstance(fileConfiguration, "campfire.soul"));
        builder.campfireCleanerRate(fileConfiguration.getLong("campfire.campfireCleanerRate"));
        return builder.build();
    }
}
