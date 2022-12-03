package com.hyun.betterspawner;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Listener implements org.bukkit.event.Listener {
    private final BetterSpawner plugin;

    public Listener(BetterSpawner plugin) {
        this.plugin = plugin;
    }

    public @Nullable ArmorStand getSpawnerHologram(Location loc) {
        final List<Entity> armorStands = (List<Entity>) Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, 1, 1, 1, entity -> {
            if(!(entity instanceof ArmorStand)) return false;
            final var container = entity.getPersistentDataContainer();
            if(!container.has(plugin.keySpawnerLoc, PersistentDataType.STRING)) return false;
            return Objects.equals(container.get(plugin.keySpawnerLoc, PersistentDataType.STRING), plugin.getSpawnerDataKey(loc));
        });
        if(armorStands.size() == 0) return null;
        return (ArmorStand) armorStands.get(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        if(e.getRightClicked().getPersistentDataContainer().has(plugin.keySpawnerLoc, PersistentDataType.STRING)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        final String key = plugin.getSpawnerDataKey(e.getSpawner().getBlock().getLocation());
        final ConfigurationSection spawnerData = plugin.getSpawnerData().getConfigurationSection(key);
        if(spawnerData == null) return;

        Location loc = e.getSpawner().getBlock().getLocation();

        int durability = spawnerData.getInt("durability", 0);
        final int ratio = plugin.getConfig().getInt("entity-ratio." + e.getEntityType(), 1);

        durability -= ratio;

        if(durability <= 0) {
            e.getSpawner().getBlock().setType(Material.AIR);
            ArmorStand armorStand = getSpawnerHologram(loc);
            if(armorStand != null) armorStand.remove();
            plugin.getSpawnerData().set(key, null);
            plugin.saveSpawnerData();

            plugin.getServer().getOnlinePlayers().forEach(p -> {
                if(p.getLocation().distance(loc) <= 16) {
                    p.playSound(loc, Sound.ENTITY_ITEM_BREAK, 1, 1);
                }
            });
            return;
        }

        e.getEntity().setMetadata("no-punya", new org.bukkit.metadata.FixedMetadataValue(plugin, true));

        spawnerData.set("durability", durability);
        // 为了增加效率不再每刷出实体就保存数据，改为插件被卸载后保存
        // plugin.saveSpawnerData();

        ArmorStand armorStand = getSpawnerHologram(loc);
        if(armorStand == null) {
            armorStand = (ArmorStand) Objects.requireNonNull(loc.getWorld()).spawnEntity(loc.add(0.5, 0, 0.5), EntityType.ARMOR_STAND);
            armorStand.setCanPickupItems(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setCollidable(false);
            armorStand.setAI(false);
            armorStand.setSmall(true);
            armorStand.getPersistentDataContainer().set(plugin.keySpawnerLoc, PersistentDataType.STRING, key);
        }

        armorStand.setCustomName(plugin.i18n("spawner-hologram", Map.of(
                "durability", String.valueOf(durability),
                "maxDurability", String.valueOf(spawnerData.getInt("maxDurability", durability)),
                "displayName", plugin.i18n((spawnerData.getInt("flags") & 1) == 0 ? "display-name": "display-name-deny-break"),
                "entity", e.getEntityType().toString()
        )));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if(e.getBlockPlaced().getBlockData().getMaterial() != Material.SPAWNER) return;
        if(!e.getItemInHand().hasItemMeta()) return;
        if(!e.canBuild()) return;

        BlockStateMeta meta = (BlockStateMeta) e.getItemInHand().getItemMeta();
        assert meta != null;
        final var container = meta.getPersistentDataContainer();

        if(!container.has(plugin.keyDurability, PersistentDataType.INTEGER)) {
            if(!e.getPlayer().hasPermission("betterspawner.bypass")) {
                e.getPlayer().sendMessage(plugin.i18n("message-denied-place"));
                e.setBuild(false);
            }
            return;
        }

        EntityType entityType = ((CreatureSpawner) meta.getBlockState()).getSpawnedType();
        final ConfigurationSection spawnerData = new MemoryConfiguration();
        spawnerData.set("type", entityType.toString());
        spawnerData.set("durability", container.get(plugin.keyDurability, PersistentDataType.INTEGER));
        spawnerData.set("maxDurability", container.get(plugin.keyMaxDurability, PersistentDataType.INTEGER));
        if(container.has(plugin.keyFlags, PersistentDataType.INTEGER))
            spawnerData.set("flags", container.get(plugin.keyFlags, PersistentDataType.INTEGER));

        CreatureSpawner state = (CreatureSpawner) e.getBlockPlaced().getState();
        state.setSpawnedType(entityType);
        state.update();

        plugin.getSpawnerData().set(plugin.getSpawnerDataKey(e.getBlockPlaced().getLocation()), spawnerData);
        plugin.saveSpawnerData();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.getBlock().getBlockData().getMaterial() != Material.SPAWNER) return;

        e.setDropItems(false);
        final String key = plugin.getSpawnerDataKey(e.getBlock().getLocation());
        final ConfigurationSection spawnerData = plugin.getSpawnerData().getConfigurationSection(key);


        final ItemStack itemInHand = e.getPlayer().getInventory().getItemInMainHand();
        if(spawnerData != null && itemInHand.getType().toString().endsWith("_PICKAXE") && itemInHand.containsEnchantment(Enchantment.SILK_TOUCH)) {
            final int flags = spawnerData.getInt("flags");
            final boolean denyBreak = (flags & 1) != 0;

            if(!denyBreak) {
                e.setExpToDrop(0);
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), Utils.getSpawnerDropItem(plugin, ((CreatureSpawner) e.getBlock().getState()).getSpawnedType(), spawnerData));
            }
        }

        ArmorStand armorStand = getSpawnerHologram(e.getBlock().getLocation());
        if(armorStand != null) armorStand.remove();
        plugin.getSpawnerData().set(key, null);
        plugin.saveSpawnerData();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if(e.getHand() == null ||
                e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                e.getItem() == null ||
                e.getClickedBlock() == null ||
                e.getClickedBlock().getBlockData().getMaterial() != Material.SPAWNER) return;

        final String spawnerEggName = e.getItem().getType().toString();
        if(!spawnerEggName.endsWith("_SPAWN_EGG")) return;

        final EntityType type = EntityType.valueOf(spawnerEggName.substring(0, spawnerEggName.length() - 10));
        if(type == EntityType.UNKNOWN) return;

        final boolean allowBypass = e.getPlayer().hasPermission("betterspawner.bypass");
        final ConfigurationSection spawnerData = plugin.getSpawnerData().getConfigurationSection(plugin.getSpawnerDataKey(e.getClickedBlock().getLocation()));

        boolean denyChange = false;
        if(spawnerData != null) {
            final int flags = spawnerData.getInt("flags");
            denyChange = (flags & 1 << 1) != 0;
        }

        if(spawnerData == null || denyChange) {
            if(!allowBypass) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(plugin.i18n("message-denied-change-spawner"));
            }
            return;
        }

        e.setCancelled(true);
        if(!plugin.getConfig().getStringList("allow-change").contains(type.toString()) && !allowBypass) {
            e.getPlayer().sendMessage(plugin.i18n("message-denied-change", Map.of("entity", type.toString())));
            return;
        }

        Block block = e.getClickedBlock();
        CreatureSpawner state = (CreatureSpawner) block.getState();
        if(state.getSpawnedType().equals(type)) return;

        state.setSpawnedType(type);
        state.update();

        e.getItem().setAmount(e.getItem().getAmount() - 1);
        spawnerData.set("type", type.toString());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        ArrayList<Block> spawnerBlocks = new ArrayList<>();
        e.blockList().removeIf(block -> {
            if(block.getBlockData().getMaterial() == Material.SPAWNER) {
                return spawnerBlocks.add(block);
            } else {
                return false;
            }
        });

        spawnerBlocks.forEach(block -> {
            EntityType spawnerType = ((CreatureSpawner) block.getState()).getSpawnedType();

            final String key = plugin.getSpawnerDataKey(block.getLocation());
            final ConfigurationSection spawnerData = plugin.getSpawnerData().getConfigurationSection(key);

            // 随机掉落自然生成的刷怪笼
            if(spawnerData == null) {
                if(plugin.getConfig().getDouble("drop-chance") > Math.random()) {
                    block.setType(Material.AIR);
                    ConfigurationSection newSpawnerData = new MemoryConfiguration();

                    final int defaultDurability = plugin.getConfig().getInt("durability");
                    newSpawnerData.set("durability", defaultDurability);
                    block.getWorld().dropItemNaturally(block.getLocation(), Utils.getSpawnerDropItem(plugin, spawnerType, newSpawnerData));
                }
            } else {
                final int flags = spawnerData.getInt("flags");
                final boolean denyBreak = (flags & 1) != 0;
                if(denyBreak) return;

                block.setType(Material.AIR);
                ArmorStand armorStand = getSpawnerHologram(block.getLocation());
                if(armorStand != null) armorStand.remove();

                plugin.getSpawnerData().set(key, null);
                block.getWorld().dropItemNaturally(block.getLocation(), Utils.getSpawnerDropItem(plugin, spawnerType, spawnerData));
            }
        });
        plugin.saveSpawnerData();
    }
}
