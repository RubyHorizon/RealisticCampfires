package team.rubyhorizon.campfires.campfire;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

@EqualsAndHashCode
@Getter
@ToString
public class CampfireIndicator {
    private static int ids = 0;

    private final int id;
    private final Type campfireType;
    private final Location location;
    private long burningTimeMillis;

    @Setter
    private boolean show = true;

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

        public static boolean containsByMaterial(Material material) {
            return getByMaterial(material) != null;
        }
    }

    public CampfireIndicator(Block block, long burningTimeMillis) {
        this.location = block.getLocation().toCenterLocation();
        this.burningTimeMillis = burningTimeMillis;
        this.campfireType = Type.getByMaterial(block.getType());

        if(campfireType == null) {
            throw new IllegalArgumentException("Block %s is not campfire!".formatted(block.getType().name()));
        }

        this.id = ids++;
    }

    public CampfireIndicator(Block block) {
        this(block, 0);
    }

    public void addBurningTime(long burningTimeMillis) {
        this.burningTimeMillis += burningTimeMillis;
    }

    public boolean addBurningTime(long burningTimeMillis, long max) {
        if(burningTimeMillis <= 0 || max <= 0) {
            throw new IllegalArgumentException("burningTimeMillis or max values cannot be equal and lower 0!");
        }

        if(burningTimeMillis >= max || this.burningTimeMillis >= max) {
            return false;
        }

        if(burningTimeMillis + this.burningTimeMillis > max) {
            final long extra = burningTimeMillis + this.burningTimeMillis - max;
            this.burningTimeMillis = burningTimeMillis + this.burningTimeMillis - extra;
            return true;
        }

        this.burningTimeMillis += burningTimeMillis;
        return true;
    }

    public void decrementBurningTime(long burningTimeTicks) {
        this.burningTimeMillis -= burningTimeTicks;

        if(this.burningTimeMillis <= 0) {
            this.burningTimeMillis = 0;
        }
    }

    public boolean equalsByBlock(Block block) {
        return block.getType() == campfireType.material && block.getLocation().toCenterLocation().equals(location) && block.getWorld().equals(location.getWorld());
    }
}
