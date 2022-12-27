package com.hyun.betterspawner;

import com.hyun.betterspawner.commands.BSCommand;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public final class BetterSpawner extends JavaPlugin {
    private FileConfiguration spawnerData;
    private File spawnerDataFile;

    public final NamespacedKey keyDurability = new NamespacedKey(this, "durability");
    public final NamespacedKey keyMaxDurability = new NamespacedKey(this, "maxDurability");
    public final NamespacedKey keySpawnerLoc = new NamespacedKey(this, "SpawnerLoc");
    public final NamespacedKey keyFlags = new NamespacedKey(this, "flags");
    public final NamespacedKey keyNBT = new NamespacedKey(this, "nbt");
    public final I18n i18n = new I18n( this);

    public FileConfiguration getSpawnerData() {
        return spawnerData;
    }

    public String getSpawnerDataKey(Location loc) {
        return Objects.requireNonNull(loc.getWorld()).getName() + "." + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public void saveSpawnerData() {
        try {
            spawnerData.save(spawnerDataFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + spawnerDataFile, ex);
        }
    }
    public String i18n(String key, Map<String, String> replace) {
        return i18n.message(key, replace);
    }

    public String i18n(String key) {
        return i18n(key, Map.of());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onLoad() {
        saveDefaultConfig();

        spawnerDataFile = new File(getDataFolder(), "spawnerData.yml");
        if (!spawnerDataFile.exists()) {
            try {
                spawnerDataFile.createNewFile();
            } catch (IOException e) {
                getLogger().log(java.util.logging.Level.SEVERE, "Failed to create spawnerData.yml");
                getPluginLoader().disablePlugin(this);
                return;
            }
        }
        spawnerData = YamlConfiguration.loadConfiguration(spawnerDataFile);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new BSListener(this), this);

        final var command = getCommand("betterspawner");
        assert command != null;

        final var bsCommand = new BSCommand(this);
        command.setExecutor(bsCommand);
        command.setTabCompleter(bsCommand);
    }

    @Override
    public void onDisable() {
        saveSpawnerData();
    }
}
