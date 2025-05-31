package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.data.Shop;
import de.syscall.util.ColorUtil;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityItem;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final SlownVectur plugin;
    private final Map<String, Set<Integer>> shopHolograms;
    private final Map<String, Set<Integer>> shopItems;
    private final Map<Integer, Float> itemRotations;
    private int nextEntityId;

    public HologramManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.shopHolograms = new ConcurrentHashMap<>();
        this.shopItems = new ConcurrentHashMap<>();
        this.itemRotations = new ConcurrentHashMap<>();
        this.nextEntityId = 100000;
        startRotationTask();
    }

    private void startRotationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateItemRotations();
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void updateItemRotations() {
        for (Map.Entry<Integer, Float> entry : itemRotations.entrySet()) {
            int entityId = entry.getKey();
            float newRotation = entry.getValue() + 3.0f;
            if (newRotation >= 360.0f) {
                newRotation = 0.0f;
            }
            itemRotations.put(entityId, newRotation);
        }
    }

    public void createShopHologram(Shop shop) {
        String shopId = shop.getUniqueId();
        Location loc = shop.getHologramLocation();

        Set<Integer> hologramIds = new HashSet<>();
        Set<Integer> itemIds = new HashSet<>();

        int priceHologramId = nextEntityId++;
        int amountHologramId = nextEntityId++;
        int itemEntityId = nextEntityId++;

        hologramIds.add(priceHologramId);
        hologramIds.add(amountHologramId);
        itemIds.add(itemEntityId);

        shopHolograms.put(shopId, hologramIds);
        shopItems.put(shopId, itemIds);
        itemRotations.put(itemEntityId, 0.0f);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getLocation().distanceSquared(loc) < 10000) {
                sendHologramPackets(player, shop, priceHologramId, amountHologramId, itemEntityId);
            }
        }
    }

    private void sendHologramPackets(Player player, Shop shop, int priceId, int amountId, int itemId) {
        Location loc = shop.getHologramLocation();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        sendArmorStandPacket(entityPlayer, priceId, loc.clone().add(0, 0.3, 0),
                "§6" + shop.getAmount() + "x §7für §a" + String.format("%.2f", shop.getTotalPrice()) + " Coins");

        sendArmorStandPacket(entityPlayer, amountId, loc.clone().add(0, 0.6, 0),
                "§7Shop von §6" + plugin.getServer().getOfflinePlayer(shop.getOwner()).getName());

        sendItemEntityPacket(entityPlayer, itemId, loc.clone().add(0, 1.2, 0), shop.getItem());
    }

    private void sendArmorStandPacket(EntityPlayer player, int entityId, Location location, String text) {
        try {
            EntityArmorStand armorStand = new EntityArmorStand(EntityTypes.f, ((CraftWorld) location.getWorld()).getHandle());
            armorStand.a_(location.getX(), location.getY(), location.getZ());

            setEntityId(armorStand, entityId);
            armorStand.k(true);
            armorStand.b(IChatBaseComponent.a(text));
            armorStand.n(true);
            armorStand.v(true);

            PacketPlayOutSpawnEntity spawnPacket = createSpawnPacket(armorStand);
            PacketPlayOutEntityMetadata metadataPacket = createMetadataPacket(entityId, armorStand);

            player.f.b(spawnPacket);
            player.f.b(metadataPacket);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send hologram packet: " + e.getMessage());
        }
    }

    private void sendItemEntityPacket(EntityPlayer player, int entityId, Location location, org.bukkit.inventory.ItemStack item) {
        try {
            EntityItem entityItem = new EntityItem(EntityTypes.T, ((CraftWorld) location.getWorld()).getHandle());
            entityItem.a_(location.getX(), location.getY(), location.getZ());
            entityItem.e(CraftItemStack.asNMSCopy(item));

            setEntityId(entityItem, entityId);
            setNoPickup(entityItem);

            PacketPlayOutSpawnEntity spawnPacket = createSpawnPacket(entityItem);
            PacketPlayOutEntityMetadata metadataPacket = createMetadataPacket(entityId, entityItem);

            player.f.b(spawnPacket);
            player.f.b(metadataPacket);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send item entity packet: " + e.getMessage());
        }
    }

    private void setEntityId(net.minecraft.world.entity.Entity entity, int id) {
        try {
            Field idField = net.minecraft.world.entity.Entity.class.getDeclaredField("ap");
            idField.setAccessible(true);
            idField.setInt(entity, id);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set entity ID: " + e.getMessage());
        }
    }

    private void setNoPickup(EntityItem entityItem) {
        try {
            Method setNeverPickUp = EntityItem.class.getDeclaredMethod("z", boolean.class);
            setNeverPickUp.setAccessible(true);
            setNeverPickUp.invoke(entityItem, true);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set no pickup: " + e.getMessage());
        }
    }

    private PacketPlayOutSpawnEntity createSpawnPacket(net.minecraft.world.entity.Entity entity) {
        try {
            Constructor<PacketPlayOutSpawnEntity> constructor = PacketPlayOutSpawnEntity.class.getDeclaredConstructor(net.minecraft.world.entity.Entity.class);
            constructor.setAccessible(true);
            return constructor.newInstance(entity);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create spawn packet: " + e.getMessage());
            return null;
        }
    }

    private PacketPlayOutEntityMetadata createMetadataPacket(int entityId, net.minecraft.world.entity.Entity entity) {
        try {
            Method getEntityData = net.minecraft.world.entity.Entity.class.getDeclaredMethod("ai");
            getEntityData.setAccessible(true);
            DataWatcher dataWatcher = (DataWatcher) getEntityData.invoke(entity);

            Method packDirty = DataWatcher.class.getDeclaredMethod("i");
            packDirty.setAccessible(true);
            Object packedData = packDirty.invoke(dataWatcher);

            Constructor<PacketPlayOutEntityMetadata> constructor = PacketPlayOutEntityMetadata.class.getDeclaredConstructor(int.class, packedData.getClass());
            constructor.setAccessible(true);
            return constructor.newInstance(entityId, packedData);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create metadata packet: " + e.getMessage());
            return null;
        }
    }

    public void removeShopHologram(String shopId) {
        Set<Integer> hologramIds = shopHolograms.remove(shopId);
        Set<Integer> itemIds = shopItems.remove(shopId);

        if (hologramIds != null) {
            for (int entityId : hologramIds) {
                removeEntityForAllPlayers(entityId);
            }
        }

        if (itemIds != null) {
            for (int entityId : itemIds) {
                removeEntityForAllPlayers(entityId);
                itemRotations.remove(entityId);
            }
        }
    }

    private void removeEntityForAllPlayers(int entityId) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);
                entityPlayer.f.b(destroyPacket);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to remove entity: " + e.getMessage());
            }
        }
    }

    public void updatePlayerHolograms(Player player) {
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (player.getLocation().distanceSquared(shop.getHologramLocation()) < 10000) {
                String shopId = shop.getUniqueId();
                Set<Integer> hologramIds = shopHolograms.get(shopId);
                Set<Integer> itemIds = shopItems.get(shopId);

                if (hologramIds != null && itemIds != null &&
                        hologramIds.size() >= 2 && itemIds.size() >= 1) {

                    Integer[] hIds = hologramIds.toArray(new Integer[0]);
                    Integer[] iIds = itemIds.toArray(new Integer[0]);

                    sendHologramPackets(player, shop, hIds[0], hIds[1], iIds[0]);
                }
            }
        }
    }

    public void cleanup() {
        for (Set<Integer> entityIds : shopHolograms.values()) {
            for (int entityId : entityIds) {
                removeEntityForAllPlayers(entityId);
            }
        }
        for (Set<Integer> entityIds : shopItems.values()) {
            for (int entityId : entityIds) {
                removeEntityForAllPlayers(entityId);
            }
        }
        shopHolograms.clear();
        shopItems.clear();
        itemRotations.clear();
    }
}