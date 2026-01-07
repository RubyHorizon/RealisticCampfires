package team.rubyhorizon.campfires.core.world;

public interface Location {
    double getX();

    void setX(double x);

    double getY();

    void setY(double y);

    double getZ();

    void setZ(double z);

    void addX(double x);

    void addY(double y);

    void addZ(double z);

    World getWorld();

    default String getWorldIdentifier() {
        return getWorld().getIdentifier();
    }
}
