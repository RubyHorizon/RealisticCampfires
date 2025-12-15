package team.rubyhorizon.campfires.listener.campfire;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.RayTraceResult;
import team.rubyhorizon.campfires.campfire.CampfireIndicator;
import team.rubyhorizon.campfires.campfire.CampfireIndicatorProtocolManager;
import team.rubyhorizon.campfires.campfire.database.CampfireIndicatorDatabase;
import team.rubyhorizon.campfires.configuration.Bundle;
import team.rubyhorizon.campfires.configuration.campfire.ExplosiveReactionSection;
import team.rubyhorizon.campfires.listener.BaseListener;
import team.rubyhorizon.campfires.util.Synchronizer;

import java.util.UUID;
import java.util.concurrent.*;

public class CampfireListener extends BaseListener {

    private final CampfireIndicatorProtocolManager campfireIndicatorProtocolManager;
    private final CampfireIndicatorDatabase campfireIndicatorDatabase;
    private final Synchronizer synchronizer;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(7);
    private final LinkedBlockingQueue<CampfireIndicator> campfireIndicators = new LinkedBlockingQueue<>();

    public CampfireListener(Bundle bundle, CampfireIndicatorProtocolManager campfireIndicatorProtocolManager,
                            CampfireIndicatorDatabase campfireIndicatorDatabase, Synchronizer synchronizer) {
        super(bundle);
        this.campfireIndicatorProtocolManager = campfireIndicatorProtocolManager;
        this.campfireIndicatorDatabase = campfireIndicatorDatabase;
        this.synchronizer = synchronizer;

        campfireIndicators.addAll(campfireIndicatorDatabase.load()
                .stream().filter(cmp -> bundle.getCampfireConfiguration().getEnableStatusOfCampfire(cmp.getLocation().getBlock().getType())).toList());
        campfireIndicatorDatabase.clear();

        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresFuel, 1, 1, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresBurningState, 1, 200, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresIndicationsVisibility, 1, 100, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresIndicationsVisibilityPersonally, 1, 100, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresIndications, 1, 50, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::checkCampfiresIndicationsForRemove, 1, 1, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::checkPlayersGlancesForAddCampfire, 1, 200, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(this::updateCampfiresShowStatus, 1, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onPluginDisable() {
        scheduledExecutorService.shutdown();
        campfireIndicatorDatabase.save(campfireIndicators);
    }

    private void extinguishCampfire(Block block, boolean extinguish) {
        if (CampfireIndicator.Type.containsByMaterial(block.getType())) {
            org.bukkit.block.data.type.Campfire campfireBlockData = (org.bukkit.block.data.type.Campfire) block.getBlockData();
            campfireBlockData.setLit(!extinguish);
            block.setBlockData(campfireBlockData);
        }
    }

    private boolean isCampfireFire(Block block) {
        if (!CampfireIndicator.Type.containsByMaterial(block.getType())) {
            return false;
        }
        return ((org.bukkit.block.data.type.Campfire) block.getBlockData()).isLit();
    }

    private boolean isCampfireInteract(PlayerInteractEvent event) {
        return event.getAction().isRightClick() && event.getClickedBlock() != null && event.getItem() != null && CampfireIndicator.Type.containsByMaterial(event.getClickedBlock().getType());
    }

    private CampfireIndicator findOrCreateCampfireIndicator(Block campfireBlock) {
        CampfireIndicator campfireIndicator = campfireIndicators.stream().filter(indicator -> indicator.equalsByBlock(campfireBlock)).findFirst().orElse(null);

        if (campfireIndicator == null) {
            campfireIndicator = new CampfireIndicator(campfireBlock);
            campfireIndicators.add(campfireIndicator);
        }

        return campfireIndicator;
    }

    private void removeAndDestroyCampfireIfExists(Block campfireBlock) {
        if (!CampfireIndicator.Type.containsByMaterial(campfireBlock.getType())) {
            return;
        }

        campfireIndicators.stream().filter(indicativeCampfire -> indicativeCampfire.equalsByBlock(campfireBlock)).forEach(indicativeCampfire -> {
            playersWhoViewedCampfires.forEach((uuid, ids) -> {
                if (!ids.isEmpty() && ids.contains(indicativeCampfire.getId())) {
                    Player playerOfUuid = Bukkit.getPlayer(uuid);

                    if (playerOfUuid != null) {
                        campfireIndicatorProtocolManager.destroy(playerOfUuid, indicativeCampfire);
                    }
                }
            });
            campfireIndicators.remove(indicativeCampfire);
        });
    }

    private boolean isPlayerInCreative(Player player) {
        return switch (player.getGameMode()) {
            case CREATIVE, SPECTATOR -> true;
            default -> false;
        };
    }

    private boolean isPlayerInSurvival(Player player) {
        return switch (player.getGameMode()) {
            case SURVIVAL, ADVENTURE -> true;
            default -> false;
        };
    }

    @EventHandler
    private void onCampfirePlace(BlockPlaceEvent event) {
        Material placedBlockMaterial = event.getBlockPlaced().getType();

        if (CampfireIndicator.Type.containsByMaterial(placedBlockMaterial)
                && bundle.getCampfireConfiguration().getEnableStatusOfCampfire(placedBlockMaterial)) {

            findOrCreateCampfireIndicator(event.getBlockPlaced());
            extinguishCampfire(event.getBlockPlaced(), true);
        }
    }

    @EventHandler
    private void onCampfireFire(PlayerInteractEvent event) {
        if (!isCampfireInteract(event)) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!bundle.getCampfireConfiguration().getEnableStatusOfCampfire(clickedBlock.getType())) {
            return;
        }

        CampfireIndicator campfireIndicator = findOrCreateCampfireIndicator(clickedBlock);
        boolean canFire = false;

        switch (event.getItem().getType()) {
            case FLINT_AND_STEEL -> {
                if (campfireIndicator.getBurningTimeMillis() > 0) {
                    canFire = true;
                }
            }

            case TORCH, SOUL_TORCH, REDSTONE_TORCH -> {
                event.setCancelled(true);

                if (bundle.getCampfireConfiguration().getTorch().isTorchAllowed(event.getItem().getType())) {
                    canFire = true;

                    bundle.getCampfireConfiguration().getBurningItemsOfCampfire(clickedBlock.getType()).stream().filter(burningItem -> burningItem.getMaterial() == Material.STICK).findFirst().ifPresent(burningItem -> {
                        if (campfireIndicator.addBurningTime(burningItem.getTimeMillis(), bundle.getCampfireConfiguration().getMaxBurningTimeOfCampfire(clickedBlock.getType()))) {
                            event.getItem().setAmount(event.getItem().getAmount() - 1);
                        }
                    });
                }
            }
        }

        if (canFire) {
            extinguishCampfire(event.getClickedBlock(), false);
        }
    }

    @EventHandler
    private void onCampfireBurningItemAdd(PlayerInteractEvent event) {
        if (isCampfireInteract(event) && bundle.getCampfireConfiguration().getEnableStatusOfCampfire(event.getClickedBlock().getType())) {
            CampfireIndicator campfireIndicator = findOrCreateCampfireIndicator(event.getClickedBlock());

            bundle.getCampfireConfiguration().getBurningItemsOfCampfire(event.getClickedBlock().getType()).stream().filter(burningItem -> burningItem.getMaterial() == event.getItem().getType()).findFirst().ifPresent((burningItem -> {
                if (campfireIndicator.addBurningTime(burningItem.getTimeMillis(), bundle.getCampfireConfiguration().getMaxBurningTimeOfCampfire(event.getClickedBlock().getType()))) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                }
            }));
        }
    }

    private synchronized void updateCampfiresFuel() {
        for (CampfireIndicator campfireIndicator : campfireIndicators) {
            if (!campfireIndicator.getLocation().isChunkLoaded()) {
                continue;
            }

            if (isCampfireFire(campfireIndicator.getLocation().getBlock())) {
                campfireIndicator.decrementBurningTime(1);
            }
        }
    }

    private void updateCampfiresBurningState() {
        for (CampfireIndicator campfireIndicator : campfireIndicators) {
            if (!campfireIndicator.getLocation().isChunkLoaded()) {
                continue;
            }

            if (campfireIndicator.getBurningTimeMillis() <= 0) {
                synchronizer.synchronize(() -> extinguishCampfire(campfireIndicator.getLocation().getBlock(), true)).join();
            }
        }
    }

    private final ConcurrentMap<UUID, ConcurrentSkipListSet<Integer>> playersWhoViewedCampfires = new ConcurrentHashMap<>();

    @EventHandler
    private void onPlayerQuitForRemoveFromViewersList(PlayerQuitEvent event) {
        playersWhoViewedCampfires.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPlayerChangeWorldForRemoveFromViewersList(PlayerChangedWorldEvent event) {
        playersWhoViewedCampfires.remove(event.getPlayer().getUniqueId());
    }

    private void updateCampfiresIndicationsVisibility() {
        for (CampfireIndicator campfireIndicator : campfireIndicators) {
            for (Player player : campfireIndicator.getLocation().getWorld().getPlayers()) {

                if (!player.getWorld().equals(campfireIndicator.getLocation().getWorld())) {
                    continue;
                }

                if ((bundle.getCampfireConfiguration().getProgressBar().isDrawForSurvival() && isPlayerInSurvival(player)) || isPlayerInCreative(player)) {

                    ConcurrentSkipListSet<Integer> viewedCampfiresIds = playersWhoViewedCampfires.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentSkipListSet<>());
                    boolean isPlayerViewedCampfire = viewedCampfiresIds.contains(campfireIndicator.getId());
                    boolean isPlayerCanViewCampfire = player.getLocation().distance(campfireIndicator.getLocation()) <= bundle.getCampfireConfiguration().getProgressBar().getDrawDistance();

                    if (isPlayerCanViewCampfire && !isPlayerViewedCampfire) {
                        campfireIndicatorProtocolManager.spawnOrUpdate(player, campfireIndicator);
                        viewedCampfiresIds.add(campfireIndicator.getId());

                    } else if (!isPlayerCanViewCampfire && isPlayerViewedCampfire) {
                        campfireIndicatorProtocolManager.destroy(player, campfireIndicator);
                        viewedCampfiresIds.remove(campfireIndicator.getId());
                    }
                }
            }
        }
    }

    private void updateCampfiresIndicationsVisibilityPersonally() {
        for (CampfireIndicator campfireIndicator : campfireIndicators) {
            for (Player player : campfireIndicator.getLocation().getWorld().getPlayers()) {

                if (!player.getWorld().equals(campfireIndicator.getLocation().getWorld())) {
                    continue;
                }

                if (!bundle.getCampfireConfiguration().getProgressBar().isDrawForSurvival() && isPlayerInSurvival(player)) {
                    RayTraceResult rayTraceResult = player.rayTraceBlocks(bundle.getCampfireConfiguration().getProgressBar().getDrawDistancePersonally());

                    // Ray trace result is can null when real result nothing
                    Block hitBlock = rayTraceResult != null ? rayTraceResult.getHitBlock() : null;

                    ConcurrentSkipListSet<Integer> viewedCampfiresIds = playersWhoViewedCampfires.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentSkipListSet<>());
                    boolean isPlayerViewedCampfire = viewedCampfiresIds.contains(campfireIndicator.getId());

                    if (hitBlock != null && campfireIndicator.equalsByBlock(hitBlock) && !isPlayerViewedCampfire) {
                        campfireIndicatorProtocolManager.spawnOrUpdate(player, campfireIndicator);
                        viewedCampfiresIds.add(campfireIndicator.getId());

                    } else if ((hitBlock == null || !CampfireIndicator.Type.containsByMaterial(hitBlock.getType())) && isPlayerViewedCampfire) {
                        campfireIndicatorProtocolManager.destroy(player, campfireIndicator);
                        viewedCampfiresIds.remove(campfireIndicator.getId());

                    }
                }
            }
        }
    }

    private void updateCampfiresIndications() {
        playersWhoViewedCampfires.forEach((uuid, campfiresIds) -> {
            if (campfiresIds.isEmpty()) {
                return;
            }

            Player playerOfUuid = Bukkit.getPlayer(uuid);

            if (playerOfUuid != null) {
                for (CampfireIndicator campfireIndicator : campfireIndicators) {
                    if (campfiresIds.contains(campfireIndicator.getId())) {
                        campfireIndicatorProtocolManager.update(playerOfUuid, campfireIndicator);
                    }
                }
            }
        });
    }

    private void updateCampfiresShowStatus() {
        if (bundle.getCampfireConfiguration().getProgressBar().isHideWhenInterferes()) {
            campfireIndicators.forEach(campfire -> {

                // I use small version of armor stand, so i can calculate a hologram location if i'm just add small armor stand height
                Location locationToCheck = campfire.getLocation().clone()
                        .add(0, bundle.getCampfireConfiguration().getProgressBar().getDrawYOffset() + 0.9875, 0);

                campfire.setShow(locationToCheck.getBlock().getType() == Material.AIR);
            });
        }
    }

    private void checkCampfiresIndicationsForRemove() {
        for (CampfireIndicator campfireIndicator : campfireIndicators) {
            if (!CampfireIndicator.Type.containsByMaterial(campfireIndicator.getLocation().getBlock().getType())) {
                playersWhoViewedCampfires.forEach((uuid, ids) -> {
                    if (ids.contains(campfireIndicator.getId())) {
                        Player playerOfUuid = Bukkit.getPlayer(uuid);

                        if (playerOfUuid != null) {
                            campfireIndicatorProtocolManager.destroy(playerOfUuid, campfireIndicator);
                        }
                        ids.remove(campfireIndicator.getId());
                    }
                });
                campfireIndicators.remove(campfireIndicator);
            }
        }
    }

    private void checkPlayersGlancesForAddCampfire() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            RayTraceResult rayTraceResult = player.rayTraceBlocks(20);
            Block hitBlock = rayTraceResult != null ? rayTraceResult.getHitBlock() : null;

            if (hitBlock != null && CampfireIndicator.Type.containsByMaterial(hitBlock.getType())) {
                if (bundle.getCampfireConfiguration().getEnableStatusOfCampfire(hitBlock.getType())) {
                    findOrCreateCampfireIndicator(hitBlock);
                }
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
        if (!isCampfireInteract(event) || !isCampfireFire(event.getClickedBlock())) {
            return;
        }

        switch (event.getItem().getType()) {
            case GUNPOWDER -> {
                if (isPlayerInSurvival(event.getPlayer())) {
                    event.getPlayer().damage(bundle.getCampfireConfiguration().getExplosiveReactionOfCampfire(event.getClickedBlock().getType()).getDamageOfGunpowder());
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                }
            }
            case TNT -> {
                ExplosiveReactionSection explosiveReaction = bundle.getCampfireConfiguration().getExplosiveReactionOfCampfire(event.getClickedBlock().getType());
                event.getPlayer().getWorld().createExplosion(event.getClickedBlock().getLocation(), (float) explosiveReaction.getPowerOfTNT(), explosiveReaction.isSetFireAfterExplode(), explosiveReaction.isBreakBlocksAfterExplode());
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            }
            default -> {
                return;
            }
        }

        event.setCancelled(true);
    }
}
