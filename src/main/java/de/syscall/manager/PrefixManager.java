package de.syscall.manager;

import de.syscall.SlownVectur;
import de.syscall.util.ColorUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrefixManager {

    private final SlownVectur plugin;
    private LuckPerms luckPerms;
    private final Map<UUID, String> prefixCache;
    private final Map<UUID, Integer> weightCache;
    private final Map<UUID, String> groupCache;

    public PrefixManager(SlownVectur plugin) {
        this.plugin = plugin;
        this.prefixCache = new HashMap<>();
        this.weightCache = new HashMap<>();
        this.groupCache = new HashMap<>();

        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            plugin.getLogger().warning("LuckPerms nicht gefunden!");
        }
    }

    public void updatePlayer(Player player) {
        if (luckPerms == null) return;

        UUID uuid = player.getUniqueId();
        User user = luckPerms.getUserManager().getUser(uuid);

        if (user != null) {
            CachedMetaData metaData = user.getCachedData().getMetaData();
            String prefix = metaData.getPrefix();

            if (prefix != null) {
                prefix = ColorUtil.colorize(prefix);
                prefixCache.put(uuid, prefix);
            }

            String primaryGroup = metaData.getPrimaryGroup();
            if (primaryGroup != null) {
                groupCache.put(uuid, primaryGroup);

                Group group = luckPerms.getGroupManager().getGroup(primaryGroup);
                if (group != null) {
                    int weight = group.getWeight().orElse(0);
                    weightCache.put(uuid, weight);
                } else {
                    weightCache.put(uuid, 0);
                }
            } else {
                groupCache.put(uuid, "default");
                weightCache.put(uuid, 0);
            }
        } else {
            groupCache.put(uuid, "default");
            weightCache.put(uuid, 0);
        }
    }

    public String getPrefix(Player player) {
        return getPrefix(player.getUniqueId());
    }

    public String getPrefix(UUID uuid) {
        return prefixCache.getOrDefault(uuid, "");
    }

    public String getGroup(Player player) {
        return getGroup(player.getUniqueId());
    }

    public String getGroup(UUID uuid) {
        return groupCache.getOrDefault(uuid, "default");
    }

    public String getGroupDisplayName(Player player) {
        return getGroupDisplayName(player.getUniqueId());
    }

    public String getGroupDisplayName(UUID uuid) {
        String groupName = getGroup(uuid);
        if (luckPerms != null) {
            Group group = luckPerms.getGroupManager().getGroup(groupName);
            if (group != null) {
                String displayName = group.getDisplayName();
                if (displayName != null) {
                    return ColorUtil.colorize(displayName);
                }
            }
        }
        return groupName;
    }

    public int getWeight(Player player) {
        return getWeight(player.getUniqueId());
    }

    public int getWeight(UUID uuid) {
        return weightCache.getOrDefault(uuid, 0);
    }

    public String getDisplayName(Player player) {
        String prefix = getPrefix(player);
        if (prefix.isEmpty()) {
            return player.getName();
        }
        return prefix + " " + player.getName();
    }

    public void removePlayer(UUID uuid) {
        prefixCache.remove(uuid);
        weightCache.remove(uuid);
        groupCache.remove(uuid);
    }

    public void reload() {
        prefixCache.clear();
        weightCache.clear();
        groupCache.clear();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }
}