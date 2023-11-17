package net.rubyhorizon.campfires.listener.campfire;

import net.rubyhorizon.campfires.Bundle;
import net.rubyhorizon.campfires.configuration.CampfireBurnItemConfiguration;
import net.rubyhorizon.campfires.listener.BaseListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

// TODO AMOGUS
// TODO I`m has not tested this listener but i will test this listener tomorrow
public class CampfireListener extends BaseListener {
    public CampfireListener(Bundle bundle) {
        super(bundle);
    }

    private final List<Campfire> campfires = new ArrayList<>();

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

    private CampfireBurnItemConfiguration.BurningItem findBurningItem(ItemStack itemStack, List<CampfireBurnItemConfiguration.BurningItem> burningItems) {
        for(CampfireBurnItemConfiguration.BurningItem burningItem: burningItems) {
            if(burningItem.getMaterial() == itemStack.getType()) {
                return burningItem;
            }
        }

        return null;
    }

    private CampfireBurnItemConfiguration.BurningItem findSoulBurningItem(ItemStack itemStack) {
        return findBurningItem(itemStack, bundle.getCampfireConfiguration().getSoulCampfire().getBurningItems());
    }

    private CampfireBurnItemConfiguration.BurningItem findCommonBurningItem(ItemStack itemStack) {
        return findBurningItem(itemStack, bundle.getCampfireConfiguration().getCommonCampfire().getBurningItems());
    }

    @EventHandler
    public void onCampfirePlace(BlockPlaceEvent event) {
        if(event.getBlockPlaced().getType() == Material.CAMPFIRE || event.getBlockPlaced().getType() == Material.SOUL_CAMPFIRE) {
            addCampfireIfNotExists(event.getBlockPlaced());
        }
    }

    @EventHandler
    public void onCampfireBurningItemAdd(PlayerInteractEvent event) {
        if(!event.getAction().isRightClick() || event.getItem() == null || event.getItem().getType() != Material.FLINT || event.getClickedBlock() == null) {
            return;
        }

        CampfireBurnItemConfiguration.BurningItem burningItem = null;
        Campfire campfire = findCampfire(event.getClickedBlock());

        switch (event.getClickedBlock().getType()) {
            case SOUL_CAMPFIRE -> burningItem = findSoulBurningItem(event.getItem());
            case CAMPFIRE -> burningItem = findCommonBurningItem(event.getItem());
        }

        if(burningItem == null || campfire == null) {
            return;
        }

        final long maxBurningTime = bundle.getCampfireConfiguration().getSoulCampfire().getMaxBurningTime();
        long burningTime = burningItem.getTicks() + campfire.getBurningTimeTicks();

        if(burningTime > maxBurningTime) {
            burningTime -= burningTime - maxBurningTime;
        }

        campfire.addBurningTime(burningTime);
    }

    @EventHandler
    public void onCampfireFire(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null || event.getItem() == null || event.getItem().getType() != Material.FLINT) {
            return;
        }

        Campfire campfire = findCampfire(event.getClickedBlock());

        if(campfire == null || campfire.getBurningTimeTicks() == 0L) {
            event.setCancelled(true);
        }
    }

    private void removeCampfireIfExists(Block block) {
        Campfire campfire = findCampfire(block);

        if(campfire != null) {
            campfires.remove(campfire);
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
}
