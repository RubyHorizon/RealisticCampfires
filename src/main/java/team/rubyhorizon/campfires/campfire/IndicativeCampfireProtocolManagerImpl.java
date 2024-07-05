package team.rubyhorizon.campfires.campfire;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import team.rubyhorizon.campfires.configuration.Bundle;
import team.rubyhorizon.campfires.configuration.campfire.CampfireConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IndicativeCampfireProtocolManagerImpl implements IndicativeCampfireProtocolManager {
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final CampfireConfiguration campfireConfiguration;

    public IndicativeCampfireProtocolManagerImpl(Bundle bundle) {
        this.campfireConfiguration = bundle.getCampfireConfiguration();
    }

    private int calculateCampfireProgress(IndicativeCampfire indicativeCampfire) {
        final int maxBurningTime = switch(indicativeCampfire.getCampfireType()) {
            case COMMON -> campfireConfiguration.getCommonCampfire().getMaxBurningTimeMillis();
            case SOUL -> campfireConfiguration.getSoulCampfire().getMaxBurningTimeMillis();
        };

        final int progressBarSize = campfireConfiguration.getProgressBar().getSize();
        final long burningTimeMillis = indicativeCampfire.getBurningTimeMillis();
        return (int) (((float) burningTimeMillis / (float) maxBurningTime) * progressBarSize) + 1;
    }

    private String buildCampfireProgressBar(IndicativeCampfire indicativeCampfire) {
        final int progressTiles = calculateCampfireProgress(indicativeCampfire);
        StringBuilder progressBar = new StringBuilder();

        for(int i = 0; i < campfireConfiguration.getProgressBar().getSize(); i++) {
            if(i < progressTiles) {
                progressBar.append(ChatColor.translateAlternateColorCodes('&', campfireConfiguration.getProgressBar().getSymbolFront()));
                continue;
            }
            progressBar.append(ChatColor.translateAlternateColorCodes('&', campfireConfiguration.getProgressBar().getSymbolBack()));
        }

        return progressBar.toString();
    }

    @Override
    public synchronized void spawnOrUpdate(@NotNull List<? extends Player> packetReceivers, @NotNull IndicativeCampfire indicativeCampfire) {
        final PacketContainer entityPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        entityPacket.getIntegers().write(0, indicativeCampfire.getId());
        entityPacket.getUUIDs().write(0, UUID.randomUUID());
        entityPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
        entityPacket.getDoubles().write(0, indicativeCampfire.getLocation().getX());
        entityPacket.getDoubles().write(1, indicativeCampfire.getLocation().getY() + campfireConfiguration.getProgressBar().getDrawYOffset());
        entityPacket.getDoubles().write(2, indicativeCampfire.getLocation().getZ());

        sendPacketsToReceivers(packetReceivers, entityPacket);
        update(packetReceivers, indicativeCampfire);
    }

    @Override
    public void spawnOrUpdate(@NotNull Player player, @NotNull IndicativeCampfire indicativeCampfire) {
        spawnOrUpdate(List.of(player), indicativeCampfire);
    }

    @Override
    public synchronized void update(@NotNull List<? extends Player> packetReceivers, @NotNull IndicativeCampfire indicativeCampfire) {
        final PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, indicativeCampfire.getId());
        List<WrappedDataValue> wrappedDataValues = new ArrayList<>();
        wrappedDataValues.add(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20)); // invisible
        wrappedDataValues.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean.class), true)); // show custom name
        wrappedDataValues.add(new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromChatMessage(buildCampfireProgressBar(indicativeCampfire))[0].getHandle()))); // setting custom name
        wrappedDataValues.add(new WrappedDataValue(15, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x01)); // small
        metadataPacket.getDataValueCollectionModifier().write(0, wrappedDataValues);

        sendPacketsToReceivers(packetReceivers, metadataPacket);
    }

    @Override
    public void update(@NotNull Player player, @NotNull IndicativeCampfire indicativeCampfire) {
        update(List.of(player), indicativeCampfire);
    }

    @Override
    public synchronized void destroy(@NotNull List<? extends Player> packetReceivers, @NotNull IndicativeCampfire indicativeCampfire) {
        final PacketContainer entityDestroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        entityDestroyPacket.getModifier().write(0, new IntArrayList(new int[]{ indicativeCampfire.getId() }));
        sendPacketsToReceivers(packetReceivers, entityDestroyPacket);
    }

    @Override
    public void destroy(@NotNull Player player, @NotNull IndicativeCampfire indicativeCampfire) {
        destroy(List.of(player), indicativeCampfire);
    }

    private void sendPacketsToReceivers(List<? extends Player> players, PacketContainer... packetContainers) {
        players.forEach(player -> {
            for(PacketContainer packetContainer: packetContainers) {
                protocolManager.sendServerPacket(player, packetContainer, false);
            }
        });
    }
}
