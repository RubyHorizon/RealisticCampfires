package net.rubyhorizon.campfires.configuration.campfire;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CampfireConfiguration {
    private CampfireBurningItemConfiguration commonCampfire;
    private CampfireBurningItemConfiguration soulCampfire;

    private int campfireCleanerRate;

    private int campfireDrawDistance;
    private int campfireProgressBarSize;
    private int campfireProgressBarDrawRate;

    private String campfireProgressBarSymbolFront;
    private String campfireProgressBarSymbolBack;

    private boolean campfireProgressBarDrawForSurvival;

    public static CampfireConfiguration getInstance(FileConfiguration fileConfiguration) {
        CampfireConfiguration.CampfireConfigurationBuilder builder = CampfireConfiguration.builder();

        builder.commonCampfire(CampfireBurningItemConfiguration.getInstance(fileConfiguration, "campfire.common"));
        builder.soulCampfire(CampfireBurningItemConfiguration.getInstance(fileConfiguration, "campfire.soul"));

        builder.campfireCleanerRate(fileConfiguration.getInt("campfire.cleanerRate"));

        builder.campfireDrawDistance(fileConfiguration.getInt("campfire.progressBar.drawDistance"));
        builder.campfireProgressBarSize(fileConfiguration.getInt("campfire.progressBar.size"));
        builder.campfireProgressBarDrawRate(fileConfiguration.getInt("campfire.progressbar.drawRate"));

        builder.campfireProgressBarSymbolBack(fileConfiguration.getString("campfire.progressBar.symbol.back"));
        builder.campfireProgressBarSymbolFront(fileConfiguration.getString("campfire.progressBar.symbol.front"));
        builder.campfireProgressBarDrawForSurvival(fileConfiguration.getBoolean("campfire.progressBar.drawForSurvival"));
        return builder.build();
    }
}
