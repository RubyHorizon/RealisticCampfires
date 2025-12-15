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

public class CampfireIndicatorProtocolManagerImpl implements CampfireIndicatorProtocolManager {
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final CampfireConfiguration campfireConfiguration;

    public CampfireIndicatorProtocolManagerImpl(Bundle bundle) {
        this.campfireConfiguration = bundle.getCampfireConfiguration();
    }

    private int calculateCampfireProgress(CampfireIndicator campfireIndicator) {
        final int maxBurningTime = switch(campfireIndicator.getCampfireType()) {
            case COMMON -> campfireConfiguration.getCommonCampfire().getMaxBurningTimeMillis();
            case SOUL -> campfireConfiguration.getSoulCampfire().getMaxBurningTimeMillis();
        };

        final int progressBarSize = campfireConfiguration.getProgressBar().getSize();
        final long burningTimeMillis = campfireIndicator.getBurningTimeMillis();
        return (int) (((float) burningTimeMillis / (float) maxBurningTime) * progressBarSize) + 1;
    }

    private String buildCampfireProgressBar(CampfireIndicator campfireIndicator) {
        final int progressTiles = calculateCampfireProgress(campfireIndicator);
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
    public synchronized void spawnOrUpdate(@NotNull List<? extends Player> packetReceivers, @NotNull CampfireIndicator campfireIndicator) {
        final PacketContainer entityPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        entityPacket.getIntegers().write(0, campfireIndicator.getId());
        entityPacket.getUUIDs().write(0, UUID.randomUUID());
        entityPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
        entityPacket.getDoubles().write(0, campfireIndicator.getLocation().getX());
        entityPacket.getDoubles().write(1, campfireIndicator.getLocation().getY() + campfireConfiguration.getProgressBar().getDrawYOffset());
        entityPacket.getDoubles().write(2, campfireIndicator.getLocation().getZ());

        sendPacketsToReceivers(packetReceivers, entityPacket);
        update(packetReceivers, campfireIndicator);
    }

    @Override
    public void spawnOrUpdate(@NotNull Player player, @NotNull CampfireIndicator campfireIndicator) {
        spawnOrUpdate(List.of(player), campfireIndicator);
    }

    @Override
    public synchronized void update(@NotNull List<? extends Player> packetReceivers, @NotNull CampfireIndicator campfireIndicator) {
        final PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, campfireIndicator.getId());
        List<WrappedDataValue> wrappedDataValues = new ArrayList<>();
        wrappedDataValues.add(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20)); // invisible
        wrappedDataValues.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean.class), campfireIndicator.isShow())); // show custom name
        wrappedDataValues.add(new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromChatMessage(buildCampfireProgressBar(campfireIndicator))[0].getHandle()))); // setting custom name
        wrappedDataValues.add(new WrappedDataValue(15, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x01)); // small
        metadataPacket.getDataValueCollectionModifier().write(0, wrappedDataValues);

        sendPacketsToReceivers(packetReceivers, metadataPacket);
    }

    @Override
    public void update(@NotNull Player player, @NotNull CampfireIndicator campfireIndicator) {
        update(List.of(player), campfireIndicator);
    }

    @Override
    public synchronized void destroy(@NotNull List<? extends Player> packetReceivers, @NotNull CampfireIndicator campfireIndicator) {
        final PacketContainer entityDestroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        entityDestroyPacket.getModifier().write(0, new IntArrayList(new int[]{ campfireIndicator.getId() }));
        sendPacketsToReceivers(packetReceivers, entityDestroyPacket);
    }

    @Override
    public void destroy(@NotNull Player player, @NotNull CampfireIndicator campfireIndicator) {
        destroy(List.of(player), campfireIndicator);
    }

    private void sendPacketsToReceivers(List<? extends Player> players, PacketContainer... packetContainers) {
        players.forEach(player -> {
            for(PacketContainer packetContainer: packetContainers) {
                protocolManager.sendServerPacket(player, packetContainer, false);
            }
        });
    }
}
