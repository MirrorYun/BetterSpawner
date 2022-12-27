package com.hyun.betterspawner.commands;

import com.hyun.betterspawner.BetterSpawner;
import com.hyun.betterspawner.utils.ItemUtil;
import com.hyun.betterspawner.utils.nbt.MojangsonParser;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BSCommand implements CommandExecutor, TabCompleter {
    private final BetterSpawner plugin;

    public BSCommand(BetterSpawner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull org.bukkit.command.Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 1 && args[0].equals("reload")) {
            plugin.reloadConfig();
            plugin.i18n.reload();
            sender.sendMessage(plugin.i18n("message-reload"));
            return true;
        }

        if (args.length >= 2 && args[0].equals("give")) {
            Player p = plugin.getServer().getPlayer(args[1]);
            if (p == null) {
                sender.sendMessage(plugin.i18n("message-player-not-found", Map.of("player", args[1])));
                return true;
            }

            ConfigurationSection config = new MemoryConfiguration();

            EntityType type = EntityType.PIG;
            int amount = 1;
            int durability = plugin.getConfig().getInt("durability");
            int flags = 0;

            for (var i = 2; i < args.length - 1; i++) {
                final var arg = args[++i];
                switch (args[i - 1]) {
                    case "-t" -> {
                        // 如果这部分填的是 NBT
                        if (arg.startsWith("{")) {
                            try {
                                MojangsonParser.parse(arg);
                            } catch (MojangsonParser.MojangsonParseException e) {
                                sender.sendMessage(plugin.i18n("message-invalid-nbt", Map.of("error", e.getMessage())));
                                return true;
                            }
                            config.set("nbt", arg);
                        } else {
                            try {
                                type = EntityType.valueOf(arg.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(plugin.i18n("message-entity-not-found", Map.of("entity", arg.toUpperCase())));
                                return true;
                            }
                        }
                    }
                    case "-a" -> {
                        try {
                            amount = Integer.parseInt(arg);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(plugin.i18n("message-must-be-int", Map.of("value", "Amount")));
                            return true;
                        }
                    }
                    case "-d" -> {
                        try {
                            durability = Integer.parseInt(arg);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(plugin.i18n("message-must-be-int", Map.of("value", "Durability")));
                            return true;
                        }
                    }
                    case "-f" -> {
                        try {
                            flags = Integer.parseInt(arg);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(plugin.i18n("message-must-be-int", Map.of("value", "Flags")));
                            return true;
                        }
                    }
                    case "-n" -> config.set("displayName", org.bukkit.ChatColor.translateAlternateColorCodes('&', arg));
                    default -> i -= 1;
                }
            }

            config.set("durability", durability);
            config.set("flags", flags);
            p.getInventory().addItem(ItemUtil.getSpawnerDropItem(plugin, type, config, amount));
            sender.sendMessage(plugin.i18n("message-give-success", Map.of("player", p.getDisplayName(),
                    "amount", String.valueOf(amount),
                    "item", ItemUtil.getSpawnerDisplayName(plugin, config))));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull org.bukkit.command.Command command, @Nonnull String label, @Nonnull String[] args) {
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("give");
        }
        if (args.length == 2 && args[0].equals("give")) {
            return null;
        }
        if (args.length >= 3 && args[0].equals("give")) {
            switch (args[args.length - 2]) {
                case "-t" -> {
                    final var arg = args[args.length - 1].toUpperCase();
                    completions.addAll(Arrays.stream(EntityType.values()).map(Enum::name).filter(s -> s.contains(arg)).toList());
                }
                case "-a" -> completions.add("1");
                case "-d" -> {
                    completions.add("-1");
                    completions.add(String.valueOf(plugin.getConfig().getInt("durability")));
                }
                case "-f" -> completions.add(String.valueOf(plugin.getConfig().getInt("drop-flags")));
                case "-n" -> {}
                default -> {
                    final ArrayList<String> options = new ArrayList<>();
                    options.add("-n");
                    options.add("-f");
                    options.add("-d");
                    options.add("-a");
                    options.add("-t");
                    completions.addAll(options.stream().filter(s -> !Arrays.asList(args).contains(s)).toList());
                }
            }
        }
        return completions;
    }
}
