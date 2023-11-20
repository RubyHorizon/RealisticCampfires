package net.rubyhorizon.campfires.listener.campfire;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Arrays;

@EqualsAndHashCode
@Getter
@ToString
public class Campfire {
    private static int ids = 0;

    private final Location location;
    private int burningTimeTicks;

    @Getter
    @AllArgsConstructor
    @ToString
    public enum Type {
        COMMON(Material.CAMPFIRE), SOUL(Material.SOUL_CAMPFIRE);

        private final Material material;

        public static Type getByMaterial(Material material) {
            for(Type type: Type.values()) {
                if(type.material == material) {
                    return type;
                }
            }

            return null;
        }
    }

    private final Type campfireType;
    private final World world;

    private final int id;

    public Campfire(Block block, int burningTimeTicks) {
        this.location = block.getLocation();
        this.burningTimeTicks = burningTimeTicks;
        this.world = block.getWorld();

        if(Arrays.stream(Type.values()).anyMatch((type) -> type.material == block.getType())) {
            this.campfireType = Type.getByMaterial(block.getType());
        } else {
            throw new RuntimeException();
        }

        this.id = ids++;
    }

    public Campfire(Block block) {
        this(block, 0);
    }

    public void addBurningTime(long burningTimeTicks) {
        this.burningTimeTicks += burningTimeTicks;
    }

    public boolean addBurningTime(int burningTimeTicks, int max) {
        if(burningTimeTicks >= max) {
            return false;
        }

        if(this.burningTimeTicks >= max) {
            return false;
        }

        if(burningTimeTicks + this.burningTimeTicks > max) {
            int extra = burningTimeTicks + this.burningTimeTicks - max;
            this.burningTimeTicks = burningTimeTicks + this.burningTimeTicks - extra;
            return true;
        }

        this.burningTimeTicks += burningTimeTicks;
        return true;
    }

    public void decrementBurningTime(int burningTimeTicks) {
        this.burningTimeTicks -= burningTimeTicks;

        if(this.burningTimeTicks <= 0) {
            this.burningTimeTicks = 0;
        }
    }

    public boolean equalsByBlock(Block block) {
        return block.getType() == campfireType.material && block.getLocation().equals(location) && block.getWorld().equals(world);
    }
}
