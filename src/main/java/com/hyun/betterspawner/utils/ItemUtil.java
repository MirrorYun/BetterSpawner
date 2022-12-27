package com.hyun.betterspawner.utils;

import com.hyun.betterspawner.BetterSpawner;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class ItemUtil {
    public static String getSpawnerDisplayName(BetterSpawner plugin, @Nonnull ConfigurationSection spawnerData) {
        return spawnerData.getString("displayName", plugin.i18n((spawnerData.getInt("flags") & 1) == 0 ? "display-name": "display-name-deny-break"));
    }

    public static ItemStack getSpawnerDropItem(BetterSpawner plugin, EntityType type, @Nonnull ConfigurationSection spawnerData) {
        return getSpawnerDropItem(plugin, type, spawnerData, 1);
    }

    public static ItemStack getSpawnerDropItem(BetterSpawner plugin, EntityType type, @Nonnull ConfigurationSection spawnerData, int amount) {
        ItemStack item = new ItemStack(Material.SPAWNER, amount);
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        assert meta != null;

        CreatureSpawner spawner = (CreatureSpawner) meta.getBlockState();
        spawner.setSpawnedType(type);
        spawner.update(true);
        meta.setBlockState(spawner);
        item.setItemMeta(meta);

        final int durability = spawnerData.getInt("durability");
        final int maxDurability = spawnerData.getInt("maxDurability", durability);
        final int flags = spawnerData.getInt("flags", plugin.getConfig().getInt("drop-flags"));

        var container = meta.getPersistentDataContainer();
        container.set(plugin.keyDurability, PersistentDataType.INTEGER, durability);
        container.set(plugin.keyMaxDurability, PersistentDataType.INTEGER, maxDurability);
        container.set(plugin.keyFlags, PersistentDataType.INTEGER, flags);
        if(spawnerData.contains("nbt"))
            container.set(plugin.keyNBT, PersistentDataType.STRING, Objects.requireNonNull(spawnerData.getString("nbt")));

        final int percent = (int) (16 * (double) durability / (double)maxDurability);
        final boolean denyBreak = (flags & 1) != 0;
        final boolean denyChange = (flags & (1 << 1)) != 0;

        var lore = new ArrayList<String>();
        var attrs = new ArrayList<String>();

        lore.add(plugin.i18n("lore-type", Map.of("type", spawnerData.contains("nbt") ? "CUSTOM" : type.toString())));
        lore.add("");

        if(!denyBreak)
            attrs.add(plugin.i18n("lore-attribute-breakable"));

        if(!denyChange)
            attrs.add(plugin.i18n("lore-attribute-changeable"));

        if(attrs.size() > 0) {
            lore.addAll(attrs);
            lore.add("");
        }

        lore.addAll(Arrays.stream(plugin.i18n(maxDurability < 0 ? "lore-durability-unlimited": "lore-durability", Map.of("durability", String.valueOf(durability),
                "maxDurability", String.valueOf(maxDurability),
                "progress-bar", "§a" + "■".repeat(percent) + "§7" + "□".repeat(16 - percent))).split("\n")).toList());

        meta.setLore(lore);
        meta.setDisplayName(getSpawnerDisplayName(plugin, spawnerData));
        item.setItemMeta(meta);
        return item;
    }
}
