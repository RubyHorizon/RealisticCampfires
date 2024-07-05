package net.rubyhorizon.campfires.campfire.database;

import lombok.SneakyThrows;
import net.rubyhorizon.campfires.campfire.IndicativeCampfire;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IndicativeCampfireDatabaseImpl implements IndicativeCampfireDatabase {

    private final SQLiteDataSource dataSource;

    @SneakyThrows
    public IndicativeCampfireDatabaseImpl(File databaseFile) {
        if(!databaseFile.exists()) {
            databaseFile.createNewFile();
        }

        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl("jdbc:sqlite:%s".formatted(databaseFile.getAbsolutePath()));
        dataSource = sqLiteDataSource;

        try(Connection connection = sqLiteDataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS campfires (location TEXT NOT NULL, burning_time_millis BIGINT NOT NULL)")) {
                preparedStatement.executeUpdate();
            }
        }
    }

    private String convertToStringLocation(Location location) {
        return "%s;%s;%s;%s".formatted(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    @Nullable
    private Location convertFromStringLocation(String stringLocation) {
        String[] piecesOfLocation = stringLocation.split(";");
        World world = Bukkit.getWorld(piecesOfLocation[0]);

        if(world == null) {
            return null;
        }

        try {
            return new Location(world, Double.parseDouble(piecesOfLocation[1]), Double.parseDouble(piecesOfLocation[2]), Double.parseDouble(piecesOfLocation[3]));
        } catch(NumberFormatException exception) {
            return null;
        }
    }

    @Override
    @SneakyThrows
    public synchronized void save(Collection<IndicativeCampfire> indicativeCampfires) {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO campfires (location, burning_time_millis) VALUES(?, ?)")) {

                connection.setAutoCommit(false);

                for(IndicativeCampfire indicativeCampfire: indicativeCampfires) {
                    preparedStatement.setString(1, convertToStringLocation(indicativeCampfire.getLocation()));
                    preparedStatement.setLong(2, indicativeCampfire.getBurningTimeMillis());
                    preparedStatement.addBatch();
                }

                preparedStatement.executeBatch();
                connection.commit();
            }
        }
    }

    @Override
    @SneakyThrows
    public synchronized Collection<IndicativeCampfire> load() {
        List<IndicativeCampfire> indicativeCampfires = new ArrayList<>();

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM campfires")) {
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while(resultSet.next()) {
                        Location location = convertFromStringLocation(resultSet.getString("location"));

                        if(location != null) {
                            indicativeCampfires.add(new IndicativeCampfire(location.getBlock(), resultSet.getLong("burning_time_millis")));
                        }
                    }
                }
            }
        }

        return indicativeCampfires;
    }

    @Override
    @SneakyThrows
    public void clear() {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM campfires")) {
                preparedStatement.executeUpdate();
            }
        }
    }
}
