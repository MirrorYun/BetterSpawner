package com.hyun.betterspawner;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
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
        getServer().getPluginManager().registerEvents(new Listener(this), this);
    }

    @Override
    public void onDisable() {
        saveSpawnerData();
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("give");
        }
        if (args.length == 2 && args[0].equals("give")) {
            return null;
        }
        if (args.length == 3 && args[0].equals("give")) {
            args[2] = args[2].toUpperCase();
            completions.addAll(Arrays.stream(EntityType.values()).map(Enum::name).filter(s -> s.contains(args[2])).toList());
        }
        if (args.length == 4 && args[0].equals("give")) {
            completions.add("1");
        }
        if (args.length == 5 && args[0].equals("give")) {
            completions.add(String.valueOf(getConfig().getInt("durability")));
        }
        if (args.length == 6 && args[0].equals("give")) {
            completions.add(String.valueOf(getConfig().getInt("drop-flags")));
        }
        return completions;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 1 && args[0].equals("reload")) {
            reloadConfig();
            i18n.reload();
            sender.sendMessage(i18n("message-reload"));
            return true;
        }

        if (args.length >= 2 && args[0].equals("give")) {
            Player p = getServer().getPlayer(args[1]);
            if(p == null) {
                sender.sendMessage(i18n("message-player-not-found", Map.of("player", args[1])));
                return true;
            }

            EntityType type = EntityType.PIG;
            if(args.length >= 3) {
                try {
                    type = EntityType.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(i18n("message-entity-not-found", Map.of("entity", args[2].toUpperCase())));
                    return true;
                }
            }

            int amount = 1;
            if(args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(i18n("message-must-be-int", Map.of("value", "Amount")));
                    return true;
                }
            }

            int durability = getConfig().getInt("durability");
            if(args.length >= 5) {
                try {
                    durability = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(i18n("message-must-be-int", Map.of("value", "Durability")));
                    return true;
                }
            }

            int flags = 0;
            if(args.length >= 6) {
                try {
                    flags = Integer.parseInt(args[5]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(i18n("message-must-be-int", Map.of("value", "Flags")));
                    return true;
                }
            }
            ConfigurationSection spawnerData = new MemoryConfiguration();
            spawnerData.set("durability", durability);
            spawnerData.set("flags", flags);
            p.getInventory().addItem(Utils.getSpawnerDropItem(this, type, spawnerData, amount));
            sender.sendMessage(i18n("message-give-success", Map.of("player", p.getDisplayName(),
                    "amount", String.valueOf(amount),
                    "item",  i18n((flags & 1) == 0 ? "display-name": "display-name-deny-break"))));
            return true;
        }
        return false;
    }
}
