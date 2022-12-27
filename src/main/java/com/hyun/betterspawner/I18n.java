package com.hyun.betterspawner;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class I18n {
    private static String f(String raw, Map<String, String> replace) {
        for (Map.Entry<String, String> entry : replace.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', raw);
    }

    private static List<String> f(List<String> raw, Map<String, String> replace) {
        return raw.stream().map(i->f(i, replace)).toList();
    }
    private FileConfiguration messageData;
    Plugin plugin;
    private String lang;

    public I18n(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.lang = "messages_" + plugin.getConfig().getString("language", "cn") + ".yml";

        try {
            plugin.saveResource(lang, false);
        } catch (Exception e) {
            this.lang = "messages_cn.yml";
            plugin.saveResource(lang, false);
        }

        File messageFile = new File(plugin.getDataFolder(), lang);
        if (!messageFile.exists()) {
            throw new RuntimeException("Failed to create messages.yml");
        }
        messageData = YamlConfiguration.loadConfiguration(messageFile);

        final var defaults = plugin.getResource(lang);
        if(defaults != null) messageData.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaults, Charsets.UTF_8)));
    }

    public String message(String key, Map<String, String> replace) {
        Object raw = messageData.get(key);
        if(raw instanceof String message) {
            return f(message, replace);
        }

        if(raw instanceof List<?> messages) {
            if(messages.stream().allMatch(i->i instanceof String))
                return String.join("\n", f((List<String>) messages, replace).stream().toList());
        }

        return "Invalid message type: " + (raw == null? "null": raw.getClass().getSimpleName());
    }
}
