package net.rubyhorizon.campfires.listener.campfire;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.SneakyThrows;
import net.rubyhorizon.campfires.Bundle;
import net.rubyhorizon.campfires.configuration.campfire.CampfireBurningItemConfiguration;
import net.rubyhorizon.campfires.listener.BaseListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class CampfireListener extends BaseListener {
    public CampfireListener(Bundle bundle) {
        super(bundle);

        (new CampfireCleaner()).runTaskTimer(bundle.getJavaPlugin(), 20, bundle.getCampfireConfiguration().getCampfireCleanerRate());
        (new CampfireFuelLevelRemover()).runTaskTimer(bundle.getJavaPlugin(), 20, 20);
        (new CampfireTextUpdater()).runTaskTimer(bundle.getJavaPlugin(), 20, bundle.getCampfireConfiguration().getCampfireProgressBarDrawRate());
    }

    private final LinkedBlockingQueue<Campfire> campfires = new LinkedBlockingQueue<>();

    private Campfire findCampfire(Block block) {
        for(Campfire campfire: campfires) {
            if(campfire.equalsByBlock(block)) {
                return campfire;
            }
        }

        return null;
    }

    private boolean campfireContains(Block block) {
        return findCampfire(block) != null;
    }

    private void addCampfireIfNotExists(Block block) {
        if(!campfireContains(block)) {
            campfires.add(new Campfire(block));
        }
    }

    private CampfireBurningItemConfiguration.BurningItem findBurningItem(ItemStack itemStack, List<CampfireBurningItemConfiguration.BurningItem> burningItems) {
        for(CampfireBurningItemConfiguration.BurningItem burningItem: burningItems) {
            if(burningItem.getMaterial() == itemStack.getType()) {
                return burningItem;
            }
        }

        return null;
    }

    private CampfireBurningItemConfiguration.BurningItem findSoulBurningItem(ItemStack itemStack) {
        return findBurningItem(itemStack, bundle.getCampfireConfiguration().getSoulCampfire().getBurningItems());
    }

    private CampfireBurningItemConfiguration.BurningItem findCommonBurningItem(ItemStack itemStack) {
        return findBurningItem(itemStack, bundle.getCampfireConfiguration().getCommonCampfire().getBurningItems());
    }

    private void extinguishCampfire(Block block, boolean extinguish) {
        if(block.getType() != Material.CAMPFIRE && block.getType() != Material.SOUL_CAMPFIRE) {
            return;
        }

        org.bukkit.block.data.type.Campfire campfireBlockData = (org.bukkit.block.data.type.Campfire) block.getBlockData();
        campfireBlockData.setLit(!extinguish);
        block.setBlockData(campfireBlockData);
    }

    private boolean campfireIsFire(Block block) {
        org.bukkit.block.data.type.Campfire campfireBlockData = (org.bukkit.block.data.type.Campfire) block.getBlockData();
        return campfireBlockData.isLit();
    }

    @EventHandler
    public void onCampfirePlace(BlockPlaceEvent event) {
        if(event.getBlockPlaced().getType() == Material.CAMPFIRE || event.getBlockPlaced().getType() == Material.SOUL_CAMPFIRE) {
            addCampfireIfNotExists(event.getBlockPlaced());
            extinguishCampfire(event.getBlockPlaced(), true);
        }
    }

    @EventHandler
    public void onCampfireBurningItemAdd(PlayerInteractEvent event) {
        if(!event.getAction().isRightClick()) {
            return;
        }

        if(event.getClickedBlock() == null || event.getItem() == null) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.SOUL_CAMPFIRE && event.getClickedBlock().getType() != Material.CAMPFIRE) {
            return;
        }

        CampfireBurningItemConfiguration.BurningItem burningItem = null;
        long campfireMaxBurningTime = 0;

        switch (event.getClickedBlock().getType()) {
            case SOUL_CAMPFIRE -> {
                burningItem = findSoulBurningItem(event.getItem());
                campfireMaxBurningTime = bundle.getCampfireConfiguration().getSoulCampfire().getMaxBurningTime();
            }
            case CAMPFIRE -> {
                burningItem = findCommonBurningItem(event.getItem());
                campfireMaxBurningTime = bundle.getCampfireConfiguration().getCommonCampfire().getMaxBurningTime();
            }
        }

        if(burningItem == null || campfireMaxBurningTime == 0) {
            return;
        }

        Campfire campfire = findCampfire(event.getClickedBlock());

        if(campfire == null) {
            campfires.add(new Campfire(event.getClickedBlock()));
            onCampfireBurningItemAdd(event);
            return;
        }

        if(campfire.addBurningTime(burningItem.getTicks(), campfireMaxBurningTime)) {
            event.getItem().setAmount(event.getItem().getAmount() - 1);
        }
    }

    @EventHandler
    public void onCampfireFire(PlayerInteractEvent event) {
        if(!event.getAction().isRightClick()) {
            return;
        }

        if(event.getClickedBlock() == null || event.getItem() == null) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.SOUL_CAMPFIRE && event.getClickedBlock().getType() != Material.CAMPFIRE) {
            return;
        }

        if(event.getItem().getType() != Material.FLINT_AND_STEEL) {
            return;
        }

        Campfire campfire = findCampfire(event.getClickedBlock());

        if(campfire == null) {
            campfires.add(new Campfire(event.getClickedBlock()));
            onCampfireFire(event);
            return;
        }

        if(campfire.getBurningTimeTicks() == 0L) {
            event.setCancelled(true);
            return;
        }

        extinguishCampfire(event.getClickedBlock(), false);
    }

    private void removeCampfireIfExists(Block block) {
        if(block.getType() != Material.SOUL_CAMPFIRE && block.getType() != Material.CAMPFIRE) {
            return;
        }

        Campfire campfire = findCampfire(block);

        if(campfire != null) {
            campfires.remove(campfire);

            for(Player player: campfire.getWorld().getPlayers()) {
                hideText(campfire.getId(), player);
            }
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        removeCampfireIfExists(event.getBlock());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        removeCampfireIfExists(event.getBlock());
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        removeCampfireIfExists(event.getBlock());
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        event.blockList().forEach(this::removeCampfireIfExists);
    }

    private class CampfireCleaner extends BukkitRunnable {
        @Override
        public void run() {
            for(Campfire campfire: campfires) {
                Block block = campfire.getLocation().getBlock();

                if(block.getType() != Material.CAMPFIRE && block.getType() != Material.SOUL_CAMPFIRE) {
                    campfires.remove(campfire);

                    for(Player player: campfire.getWorld().getPlayers()) {
                        hideText(campfire.getId(), player);
                    }
                }
            }
        }
    }

    private class CampfireFuelLevelRemover extends BukkitRunnable {
        @Override
        public void run() {
            for(Campfire campfire: campfires) {
                Block campfireBlock = campfire.getLocation().getBlock();

                if(campfireBlock.getType() != Material.CAMPFIRE && campfireBlock.getType() != Material.SOUL_CAMPFIRE) {
                    continue;
                }

                org.bukkit.block.data.type.Campfire blockData = (org.bukkit.block.data.type.Campfire) campfireBlock.getBlockData();

                if(blockData.isLit()) {
                    campfire.decrementBurningTime(20L);

                    if(campfire.getBurningTimeTicks() <= 0) {
                        blockData.setLit(false);
                        campfireBlock.setBlockData(blockData);
                    }
                }
            }
        }
    }

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    @SneakyThrows
    private void showText(Player player, Location location, int entityId, String text) {
        final PacketContainer entityPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        entityPacket.getIntegers().write(0, entityId);
        entityPacket.getUUIDs().write(0, UUID.randomUUID());
        entityPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
        entityPacket.getDoubles().write(0, location.getX() + 0.50);
        entityPacket.getDoubles().write(1, location.getY() - 0.35);
        entityPacket.getDoubles().write(2, location.getZ() + 0.50);
        protocolManager.sendServerPacket(player, entityPacket);

        final PacketContainer metadata = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metadata.getIntegers().write(0, entityId);

        List<WrappedDataValue> wrappedDataValues = new ArrayList<>();

        wrappedDataValues.add(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20)); // invisible
        wrappedDataValues.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean.class), true)); // show custom name

        // setting custom name
        Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(text)[0].getHandle());
        wrappedDataValues.add(new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), opt));

        wrappedDataValues.add(new WrappedDataValue(15, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x01)); // small

        metadata.getDataValueCollectionModifier().write(0, wrappedDataValues);
        protocolManager.sendServerPacket(player, metadata);
    }

    private void showText(Player player, Campfire campfire, String text) {
        showText(player, campfire.getLocation(), campfire.getId(), text);
    }

    @SneakyThrows
    private void hideText(int entityId, Player player) {
        final PacketContainer entityDestroy = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        entityDestroy.getModifier().write(0, new IntArrayList(new int[]{ entityId }));
        protocolManager.sendServerPacket(player, entityDestroy);
    }

    private class CampfireTextUpdater extends BukkitRunnable implements Listener {
        public CampfireTextUpdater() {
            bundle.getJavaPlugin().getServer().getPluginManager().registerEvents(this, bundle.getJavaPlugin());
        }

        private long getCampfireMaxBurningTimeByCampfire(Campfire campfire) {
            return switch(campfire.getCampfireType()) {
                case COMMON -> bundle.getCampfireConfiguration().getCommonCampfire().getMaxBurningTime();
                case SOUL -> bundle.getCampfireConfiguration().getSoulCampfire().getMaxBurningTime();
            };
        }

        private long calculateFuelProgressTiles(Campfire campfire) {
            final long maxBurningTime = getCampfireMaxBurningTimeByCampfire(campfire);
            return (long) (((float) campfire.getBurningTimeTicks() / (float) maxBurningTime) * 100) / bundle.getCampfireConfiguration().getCampfireProgressBarSize();
        }

        private String generateProgressBar(Campfire campfire, String back, String front) {
            final long progressTiles = calculateFuelProgressTiles(campfire);

            StringBuilder progressBar = new StringBuilder();
            progressBar.append(ChatColor.translateAlternateColorCodes('&', front));

            for(int i = 1; i < bundle.getCampfireConfiguration().getCampfireProgressBarSize(); i++) {
                if(i < progressTiles) {
                    progressBar.append(ChatColor.translateAlternateColorCodes('&', front));
                    continue;
                }
                progressBar.append(ChatColor.translateAlternateColorCodes('&', back));
            }

            return progressBar.toString();
        }

        private String generateProgressBar(Campfire campfire) {
            return generateProgressBar(campfire, bundle.getCampfireConfiguration().getCampfireProgressBarSymbolBack(), bundle.getCampfireConfiguration().getCampfireProgressBarSymbolFront());
        }

        private final ArrayList<Player> playersLookedToCampfire = new ArrayList<>();

        private void removeLookedPlayerAndHideText(Player player) {
            if(playersLookedToCampfire.contains(player)) {
                playersLookedToCampfire.remove(player);
                hideText(player.getUniqueId().hashCode(), player);
            }
        }

        @Override
        public void run() {
            for(Campfire campfire: campfires) {
                for(Player player: campfire.getWorld().getPlayers()) {

                    switch(player.getGameMode()) {
                        case ADVENTURE, SURVIVAL -> {
                            if(!bundle.getCampfireConfiguration().isCampfireProgressBarDrawForSurvival()) {
                                continue;
                            }
                        }
                    }

                    if(bundle.getCampfireConfiguration().getCampfireDrawDistance() < campfire.getLocation().distance(player.getLocation())) {
                        hideText(campfire.getId(), player);
                        continue;
                    }

                    showText(player, campfire, generateProgressBar(campfire));
                }
            }

            if(bundle.getCampfireConfiguration().isCampfireProgressBarDrawForSurvival()) {
                return;
            }

            for(World world: bundle.getJavaPlugin().getServer().getWorlds()) {
                for(Player player: world.getPlayers()) {

                    switch(player.getGameMode()) {
                        case CREATIVE, SPECTATOR -> {
                            continue;
                        }
                    }

                    RayTraceResult rayTraceResult = player.rayTraceBlocks(4d);

                    if(rayTraceResult == null || rayTraceResult.getHitBlock() == null) {
                        removeLookedPlayerAndHideText(player);
                        continue;
                    }

                    if(rayTraceResult.getHitBlock().getType() != Material.CAMPFIRE && rayTraceResult.getHitBlock().getType() != Material.SOUL_CAMPFIRE) {
                        removeLookedPlayerAndHideText(player);
                        continue;
                    }

                    Campfire campfire = findCampfire(rayTraceResult.getHitBlock());

                    if(campfire == null) {
                        campfire = new Campfire(rayTraceResult.getHitBlock());
                        campfires.add(campfire);

                        if(campfireIsFire(rayTraceResult.getHitBlock())) {
                            extinguishCampfire(rayTraceResult.getHitBlock(), true);
                        }
                    }

                    playersLookedToCampfire.add(player);
                    showText(player, campfire.getLocation(), player.getUniqueId().hashCode(), generateProgressBar(campfire));
                }
            }
        }

        @EventHandler
        public void onPlayerGameModeChanged(PlayerGameModeChangeEvent event) {
            switch(event.getNewGameMode()) {
                case SURVIVAL, ADVENTURE -> {
                    if(!bundle.getCampfireConfiguration().isCampfireProgressBarDrawForSurvival()) {
                        for(Campfire campfire: campfires) {
                            hideText(campfire.getId(), event.getPlayer());
                        }
                    }
                }

                case CREATIVE, SPECTATOR -> {
                    removeLookedPlayerAndHideText(event.getPlayer());
                }
            }
        }
    }

    private boolean burningItemOfCampfireContains(Campfire.Type type, Material material) {
        List<CampfireBurningItemConfiguration.BurningItem> burningItems = null;

        switch(type) {
            case COMMON -> burningItems = bundle.getCampfireConfiguration().getCommonCampfire().getBurningItems();
            case SOUL -> burningItems = bundle.getCampfireConfiguration().getSoulCampfire().getBurningItems();
        }

        if(burningItems == null) {
            return false;
        }

        for(CampfireBurningItemConfiguration.BurningItem burningItem: burningItems) {
            if(burningItem.getMaterial() == material) {
                return true;
            }
        }

        return false;
    }

    private boolean burningItemOfGeneralCampfireContains(Material material) {
        return burningItemOfCampfireContains(Campfire.Type.COMMON, material) || burningItemOfCampfireContains(Campfire.Type.SOUL, material);
    }

    @EventHandler
    public void onPlayerClickCombustibleItem(PlayerInteractEvent event) {
        if(!event.getAction().isRightClick() || event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CAMPFIRE && event.getClickedBlock().getType() != Material.SOUL_CAMPFIRE || event.getItem() == null) {
            return;
        }

        switch(event.getPlayer().getGameMode()) {
            case CREATIVE, SPECTATOR -> {
                return;
            }
        }

        if(!campfireIsFire(event.getClickedBlock())) {
            return;
        }

        if(burningItemOfGeneralCampfireContains(event.getItem().getType())) {
            return;
        }

        switch(event.getItem().getType()) {
            case GUNPOWDER -> {
                event.getPlayer().damage(2);
            }

            case TNT -> {
                event.setCancelled(true);
                event.getPlayer().getWorld().createExplosion(event.getPlayer().getLocation(), 5f);
            }

            default -> {
                return;
            }
        }

        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
}
