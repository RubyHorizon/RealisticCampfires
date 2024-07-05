package net.rubyhorizon.campfires.listener.campfire;

import net.rubyhorizon.campfires.campfire.IndicativeCampfire;
import net.rubyhorizon.campfires.campfire.IndicativeCampfireProtocolManager;
import net.rubyhorizon.campfires.configuration.Bundle;
import net.rubyhorizon.campfires.configuration.campfire.ExplosiveReactionSection;
import net.rubyhorizon.campfires.listener.BaseListener;
import net.rubyhorizon.campfires.util.Synchronizer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.RayTraceResult;

import java.util.*;
import java.util.concurrent.*;

public class CampfireListener extends BaseListener {
    private final IndicativeCampfireProtocolManager indicativeCampfireProtocolManager;
    private final Synchronizer synchronizer;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(6);

    private final LinkedBlockingQueue<IndicativeCampfire> indicativeCampfires = new LinkedBlockingQueue<>();

    public CampfireListener(Bundle bundle, IndicativeCampfireProtocolManager indicativeCampfireProtocolManager, Synchronizer synchronizer) {
        super(bundle);
        this.indicativeCampfireProtocolManager = indicativeCampfireProtocolManager;
        this.synchronizer = synchronizer;

        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresFuel, 1, 1, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresBurningState, 1, 200, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresIndicationsVisibility, 1, 200, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresIndicationsVisibilityPersonally, 1, 100, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresIndications, 1, 50, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::checkCampfiresIndicationsForRemove, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onPluginDisable() {
        scheduledExecutorService.shutdown();
    }

    private void extinguishCampfire(Block block, boolean extinguish) {
        if(IndicativeCampfire.Type.containsByMaterial(block.getType())) {
            org.bukkit.block.data.type.Campfire campfireBlockData = (org.bukkit.block.data.type.Campfire) block.getBlockData();
            campfireBlockData.setLit(!extinguish);
            block.setBlockData(campfireBlockData);
        }
    }

    private boolean isCampfireFire(Block block) {
        if(!IndicativeCampfire.Type.containsByMaterial(block.getType())) {
            return false;
        }
        return ((org.bukkit.block.data.type.Campfire) block.getBlockData()).isLit();
    }

    private boolean isCampfireInteract(PlayerInteractEvent event) {
        return event.getAction().isRightClick() && event.getClickedBlock() != null && event.getItem() != null && IndicativeCampfire.Type.containsByMaterial(event.getClickedBlock().getType());
    }

    private IndicativeCampfire findOrCreateCampfireIndicator(Block campfireBlock) {
        IndicativeCampfire indicativeCampfire = indicativeCampfires.stream().filter(indicator -> indicator.equalsByBlock(campfireBlock)).findFirst().orElse(null);

        if(indicativeCampfire == null) {
            indicativeCampfire = new IndicativeCampfire(campfireBlock);
            indicativeCampfires.add(indicativeCampfire);
        }

        return indicativeCampfire;
    }

    private void removeAndDestroyCampfireIfExists(Block campfireBlock) {
        if(!IndicativeCampfire.Type.containsByMaterial(campfireBlock.getType())) {
            return;
        }

        indicativeCampfires.stream().filter(indicativeCampfire -> indicativeCampfire.equalsByBlock(campfireBlock)).forEach(indicativeCampfire -> {
            playersWhoViewedCampfires.forEach((uuid, ids) -> {
                if(!ids.isEmpty() && ids.contains(indicativeCampfire.getId())) {
                    Player playerOfUuid = Bukkit.getPlayer(uuid);

                    if(playerOfUuid != null) {
                        indicativeCampfireProtocolManager.destroy(playerOfUuid, indicativeCampfire);
                    }
                }
            });
            indicativeCampfires.remove(indicativeCampfire);
        });
    }

    @EventHandler
    private void onCampfirePlace(BlockPlaceEvent event) {
        if(IndicativeCampfire.Type.containsByMaterial(event.getBlockPlaced().getType())) {
            if(indicativeCampfires.stream().noneMatch(indicativeCampfire -> indicativeCampfire.equalsByBlock(event.getBlockPlaced()))) {
                indicativeCampfires.add(new IndicativeCampfire(event.getBlockPlaced()));
            }
            extinguishCampfire(event.getBlockPlaced(), true);
        }
    }

    @EventHandler
    private void onCampfireFire(PlayerInteractEvent event) {
        if(!isCampfireInteract(event)) {
            return;
        }

        IndicativeCampfire indicativeCampfire = findOrCreateCampfireIndicator(event.getClickedBlock());
        boolean canFire = false;

        switch(event.getItem().getType()) {
            case FLINT_AND_STEEL -> {
                if(indicativeCampfire.getBurningTimeMillis() > 0) {
                    canFire = true;
                }
            }

            case TORCH, SOUL_TORCH, REDSTONE_TORCH -> {
                event.setCancelled(true);

                if(bundle.getCampfireConfiguration().getTorch().isTorchAllowed(event.getItem().getType())) {
                    canFire = true;

                    bundle.getCampfireConfiguration().getBurningItemsOfCampfire(event.getClickedBlock().getType()).stream().filter(burningItem -> burningItem.getMaterial() == Material.STICK).findFirst().ifPresent(burningItem -> {
                        if(indicativeCampfire.addBurningTime(burningItem.getTimeMillis(), bundle.getCampfireConfiguration().getMaxBurningTimeOfCampfire(event.getClickedBlock().getType()))) {
                            event.getItem().setAmount(event.getItem().getAmount() - 1);
                        }
                    });
                }
            }
        }

        if(canFire) {
            extinguishCampfire(event.getClickedBlock(), false);
        }
    }

    @EventHandler
    private void onCampfireBurningItemAdd(PlayerInteractEvent event) {
        if(!isCampfireInteract(event)) {
            return;
        }

        IndicativeCampfire indicativeCampfire = findOrCreateCampfireIndicator(event.getClickedBlock());

        bundle.getCampfireConfiguration().getBurningItemsOfCampfire(event.getClickedBlock().getType()).stream().filter(burningItem -> burningItem.getMaterial() == event.getItem().getType()).findFirst().ifPresent((burningItem -> {
            if(indicativeCampfire.addBurningTime(burningItem.getTimeMillis(), bundle.getCampfireConfiguration().getMaxBurningTimeOfCampfire(event.getClickedBlock().getType()))) {
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            }
        }));
    }

    private synchronized void updateCampfiresFuel() {
        for(IndicativeCampfire indicativeCampfire: indicativeCampfires) {
            synchronizer.synchronize(() -> {
                if(isCampfireFire(indicativeCampfire.getLocation().getBlock())) {
                    indicativeCampfire.decrementBurningTime(1);
                }
            });
        }
    }

    private void updateCampfiresBurningState() {
        for(IndicativeCampfire indicativeCampfire: indicativeCampfires) {
            if(indicativeCampfire.getBurningTimeMillis() <= 0) {
                synchronizer.synchronize(() -> extinguishCampfire(indicativeCampfire.getLocation().getBlock(), true));
            }
        }
    }

    private final ConcurrentMap<UUID, ConcurrentSkipListSet<Integer>> playersWhoViewedCampfires = new ConcurrentHashMap<>();

    @EventHandler
    private void onPlayerQuitForRemoveFromViewersList(PlayerQuitEvent event) {
        playersWhoViewedCampfires.remove(event.getPlayer().getUniqueId());
    }

    private void updateCampfiresIndicationsVisibility() {
        for(IndicativeCampfire indicativeCampfire: indicativeCampfires) {
            for(Player player: indicativeCampfire.getLocation().getWorld().getPlayers()) {

                if(!player.getWorld().equals(indicativeCampfire.getLocation().getWorld())) {
                    continue;
                }

                if((bundle.getCampfireConfiguration().getProgressBar().isDrawForSurvival() && (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL))
                        || (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) {

                    ConcurrentSkipListSet<Integer> viewedCampfiresIds = playersWhoViewedCampfires.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentSkipListSet<>());
                    boolean isPlayerViewedCampfire = viewedCampfiresIds.contains(indicativeCampfire.getId());
                    boolean isPlayerCanViewCampfire = player.getLocation().distance(indicativeCampfire.getLocation()) <= bundle.getCampfireConfiguration().getProgressBar().getDrawDistance();

                    if(isPlayerCanViewCampfire && !isPlayerViewedCampfire) {
                        indicativeCampfireProtocolManager.spawnOrUpdate(player, indicativeCampfire);
                        viewedCampfiresIds.add(indicativeCampfire.getId());

                    } else if(!isPlayerCanViewCampfire && isPlayerViewedCampfire) {
                        indicativeCampfireProtocolManager.destroy(player, indicativeCampfire);
                        viewedCampfiresIds.remove(indicativeCampfire.getId());

                    }
                }
            }
        }
    }

    private void updateCampfiresIndicationsVisibilityPersonally() {
        for(IndicativeCampfire indicativeCampfire: indicativeCampfires) {
            for(Player player: indicativeCampfire.getLocation().getWorld().getPlayers()) {

                if(!player.getWorld().equals(indicativeCampfire.getLocation().getWorld())) {
                    continue;
                }

                if(!bundle.getCampfireConfiguration().getProgressBar().isDrawForSurvival() && (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL)) {
                    RayTraceResult rayTraceResult = player.rayTraceBlocks(bundle.getCampfireConfiguration().getProgressBar().getDrawDistancePersonally());

                    // Ray trace result is can null when real result nothing
                    Block hitBlock = rayTraceResult != null ? rayTraceResult.getHitBlock() : null;

                    ConcurrentSkipListSet<Integer> viewedCampfiresIds = playersWhoViewedCampfires.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentSkipListSet<>());
                    boolean isPlayerViewedCampfire = viewedCampfiresIds.contains(indicativeCampfire.getId());

                    if(hitBlock != null && indicativeCampfire.equalsByBlock(hitBlock) && !isPlayerViewedCampfire) {
                        indicativeCampfireProtocolManager.spawnOrUpdate(player, indicativeCampfire);
                        viewedCampfiresIds.add(indicativeCampfire.getId());

                    } else if((hitBlock == null || !IndicativeCampfire.Type.containsByMaterial(hitBlock.getType())) && isPlayerViewedCampfire) {
                        indicativeCampfireProtocolManager.destroy(player, indicativeCampfire);
                        viewedCampfiresIds.remove(indicativeCampfire.getId());

                    }
                }
            }
        }
    }

    private void updateCampfiresIndications() {
        playersWhoViewedCampfires.forEach((uuid, campfiresIds) -> {
            if(campfiresIds.isEmpty()) {
                return;
            }

            Player playerOfUuid = Bukkit.getPlayer(uuid);

            if(playerOfUuid != null) {
                for(IndicativeCampfire indicativeCampfire: indicativeCampfires) {
                    if(campfiresIds.contains(indicativeCampfire.getId())) {
                        indicativeCampfireProtocolManager.update(playerOfUuid, indicativeCampfire);
                    }
                }
            }
        });
    }

    private void checkCampfiresIndicationsForRemove() {
        for(IndicativeCampfire indicativeCampfire: indicativeCampfires) {
            if(!IndicativeCampfire.Type.containsByMaterial(indicativeCampfire.getLocation().getBlock().getType())) {
                playersWhoViewedCampfires.forEach((uuid, ids) -> {
                    if(ids.contains(indicativeCampfire.getId())) {
                        Player playerOfUuid = Bukkit.getPlayer(uuid);

                        if(playerOfUuid != null) {
                            indicativeCampfireProtocolManager.destroy(playerOfUuid, indicativeCampfire);
                        }
                        ids.remove(indicativeCampfire.getId());
                    }
                });
                indicativeCampfires.remove(indicativeCampfire);
            }
        }
    }

    @EventHandler
    private void onCampfireBreak(BlockBreakEvent event) {
        removeAndDestroyCampfireIfExists(event.getBlock());
    }

    @EventHandler
    private void onCampfireExplode(BlockExplodeEvent event) {
        removeAndDestroyCampfireIfExists(event.getBlock());
    }

    @EventHandler
    private void onCampfireExplodeByEntity(EntityExplodeEvent event) {
        event.blockList().forEach(this::removeAndDestroyCampfireIfExists);
    }

    @EventHandler
    private void onCampfireFade(BlockFadeEvent event) {
        removeAndDestroyCampfireIfExists(event.getBlock());
    }

    @EventHandler
    private void onCampfireExplosiveReaction(PlayerInteractEvent event) {
        if(!isCampfireInteract(event) || !isCampfireFire(event.getClickedBlock())) {
            return;
        }

        switch(event.getItem().getType()) {
            case GUNPOWDER -> event.getPlayer().damage(bundle.getCampfireConfiguration().getExplosiveReactionOfCampfire(event.getClickedBlock().getType()).getDamageOfGunpowder());
            case TNT -> {
                ExplosiveReactionSection explosiveReaction = bundle.getCampfireConfiguration().getExplosiveReactionOfCampfire(event.getClickedBlock().getType());
                event.getPlayer().getWorld().createExplosion(event.getClickedBlock().getLocation(), (float) explosiveReaction.getPowerOfTNT(), explosiveReaction.isSetFireAfterExplode(), explosiveReaction.isBreakBlocksAfterExplode());
            }
            default -> {
                return;
            }
        }

        event.setCancelled(true);
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
}
