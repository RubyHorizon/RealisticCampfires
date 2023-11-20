package net.rubyhorizon.campfires.configuration.campfire;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CampfireConfiguration {
    private BurningItemConfiguration commonCampfire;
    private BurningItemConfiguration soulCampfire;

    private int campfireCleanerRate;
    private int campfireDrawDistance;
    private int campfireProgressBarSize;
    private int campfireProgressBarDrawRate;

    private String campfireProgressBarSymbolFront;
    private String campfireProgressBarSymbolBack;

    private boolean campfireProgressBarDrawForSurvival;

    private TorchConfiguration torchConfiguration;

    public static CampfireConfiguration getInstance(FileConfiguration fileConfiguration) {
        CampfireConfiguration.CampfireConfigurationBuilder builder = CampfireConfiguration.builder();

        builder.commonCampfire(BurningItemConfiguration.getInstance(fileConfiguration, "campfire.common"));
        builder.soulCampfire(BurningItemConfiguration.getInstance(fileConfiguration, "campfire.soul"));

        builder.campfireCleanerRate(fileConfiguration.getInt("campfire.cleanerRate"));

        builder.campfireDrawDistance(fileConfiguration.getInt("campfire.progressBar.drawDistance"));
        builder.campfireProgressBarSize(fileConfiguration.getInt("campfire.progressBar.size"));
        builder.campfireProgressBarDrawRate(fileConfiguration.getInt("campfire.progressBar.drawRate"));

        builder.campfireProgressBarSymbolBack(fileConfiguration.getString("campfire.progressBar.symbol.back"));
        builder.campfireProgressBarSymbolFront(fileConfiguration.getString("campfire.progressBar.symbol.front"));
        builder.campfireProgressBarDrawForSurvival(fileConfiguration.getBoolean("campfire.progressBar.drawForSurvival"));

        if(builder.campfireCleanerRate <= 0) {
            throw new RuntimeException("Campfire cleaner rate has not possible lower of 0!");
        }

        if(builder.campfireDrawDistance <= 0) {
            throw new RuntimeException("Campfire draw instance has not possible lower of 0!");
        }

        if(builder.campfireProgressBarSize <= 0) {
            throw new RuntimeException("Campfire progress bar size has not possible lower of 0!");
        }

        if(builder.campfireProgressBarDrawRate <= 0) {
            throw new RuntimeException("Campfire progress bar draw rate has not possible lower of 0!");
        }

        TorchConfiguration.TorchConfigurationBuilder torchConfigBuilder = TorchConfiguration.builder();
        torchConfigBuilder.torch(fileConfiguration.getBoolean("campfire.allowFireCampfireByTorch.torch"));
        torchConfigBuilder.soulTorch(fileConfiguration.getBoolean("campfire.allowFireCampfireByTorch.soulTorch"));
        torchConfigBuilder.redStoneTorch(fileConfiguration.getBoolean("campfire.allowFireCampfireByTorch.redStoneTorch"));

        TorchConfiguration torchConfiguration = torchConfigBuilder.build();
        builder.torchConfiguration(torchConfiguration);

        List<BurningItemConfiguration.BurningItem> allBurningItems = new ArrayList<>();
        allBurningItems.addAll(builder.commonCampfire.getBurningItems());
        allBurningItems.addAll(builder.soulCampfire.getBurningItems());

        for(BurningItemConfiguration.BurningItem burningItem: allBurningItems) {
            switch(burningItem.getMaterial()) {
                case TORCH -> {
                    if(torchConfiguration.isTorch()) {
                        throw new RuntimeException("Conflict: torch is burning and allowed for fire.");
                    }
                }

                case SOUL_TORCH -> {
                    if(torchConfiguration.isSoulTorch()) {
                        throw new RuntimeException("Conflict: soul torch is burning and allowed for fire.");
                    }
                }

                case REDSTONE_TORCH -> {
                    if(torchConfiguration.isRedStoneTorch()) {
                        throw new RuntimeException("Conflict: red stone torch is burning and allowed for fire.");
                    }
                }
            }
        }

        return builder.build();
    }
}
