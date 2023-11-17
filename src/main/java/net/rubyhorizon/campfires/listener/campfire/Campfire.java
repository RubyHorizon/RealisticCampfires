package net.rubyhorizon.campfires.listener.campfire;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;

@EqualsAndHashCode
@Getter
@Setter
public class Campfire {
    private Location location;
    private Long burningTimeTicks;

    @Getter
    @AllArgsConstructor
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

    private Type campfireType;

    public Campfire(Block block, long burningTimeTicks) {
        this.location = block.getLocation();
        this.burningTimeTicks = burningTimeTicks;

        if(Arrays.stream(Type.values()).anyMatch((type) -> type.material == block.getType())) {
            this.campfireType = Type.getByMaterial(block.getType());
        } else {
            throw new RuntimeException();
        }
    }

    public Campfire(Block block) {
        this(block, 0L);
    }

    public void addBurningTime(Long burningTimeTicks) {
        this.burningTimeTicks += burningTimeTicks;
    }

    public void addBurningTimeSeconds(Long burningTimeSeconds) {
        this.burningTimeTicks += burningTimeSeconds / 20;
    }

    public void addBurningTimeMillis(Long burningTimeMillis) {
        addBurningTimeSeconds(burningTimeMillis / 100);
    }

    public boolean equalsByBlock(Block block) {
        return block.getType() == campfireType.material && block.getLocation().equals(location);
    }
}
