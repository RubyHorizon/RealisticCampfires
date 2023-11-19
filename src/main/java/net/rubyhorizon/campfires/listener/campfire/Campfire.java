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
    private long burningTimeTicks;

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

    public Campfire(Block block, long burningTimeTicks) {
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
        this(block, 0L);
    }

    public void addBurningTime(long burningTimeTicks) {
        this.burningTimeTicks += burningTimeTicks;
    }

    public boolean addBurningTime(long burningTimeTicks, long max) {
        if(burningTimeTicks >= max) {
            return false;
        }

        if(this.burningTimeTicks >= max) {
            return false;
        }

        if(burningTimeTicks + this.burningTimeTicks > max) {
            long extra = burningTimeTicks + this.burningTimeTicks - max;
            this.burningTimeTicks = burningTimeTicks + this.burningTimeTicks - extra;
            return true;
        }

        this.burningTimeTicks += burningTimeTicks;
        return true;
    }

    public void decrementBurningTime(Long burningTimeTicks) {
        this.burningTimeTicks -= burningTimeTicks;

        if(this.burningTimeTicks <= 0L) {
            this.burningTimeTicks = 0L;
        }
    }

    public boolean equalsByBlock(Block block) {
        return block.getType() == campfireType.material && block.getLocation().equals(location) && block.getWorld().equals(world);
    }
}
