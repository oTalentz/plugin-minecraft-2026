package com.otalentz.tags;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.WeightNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class TagsPlugin extends JavaPlugin implements Listener {

    private static TagsPlugin instance;
    private Scoreboard scoreboard;
    private final Map<UUID, Tag> activeTags = new HashMap<>();
    private LuckPerms luckPerms;
    private File dataFile;
    private FileConfiguration dataConfig;
    private NamespacedKey menuKey;
    private final Map<UUID, UUID> menuTargets = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadData();

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        for (Tag tag : Tag.values()) {
            Team team = scoreboard.getTeam(tag.getGroupName());
            if (team == null) {
                team = scoreboard.registerNewTeam(tag.getGroupName());
            }
            team.setPrefix(tag.getPrefix());
        }

        setupLuckPerms();

        getCommand("tag").setExecutor(new TagsCommand(this));
        getCommand("tags").setExecutor(new TagsCommand(this));

        menuKey = new NamespacedKey(this, "tag-menu");

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveData();
    }

    public static TagsPlugin getInstance() {
        return instance;
    }

    public NamespacedKey getMenuKey() {
        return menuKey;
    }

    public void setMenuTarget(UUID viewer, UUID target) {
        menuTargets.put(viewer, target);
    }

    private void setupLuckPerms() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().info("LuckPerms nao encontrado. Tags funcionara apenas com scoreboard.");
            return;
        }
        try {
            luckPerms = LuckPermsProvider.get();
            for (Tag tag : Tag.values()) {
                createLuckPermsGroup(tag);
            }
            getLogger().info("LuckPerms integrado. Grupos de tags criados/atualizados.");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Nao foi possivel integrar com LuckPerms: " + e.getMessage(), e);
        }
    }

    private void createLuckPermsGroup(Tag tag) {
        if (luckPerms == null) return;
        try {
            luckPerms.getGroupManager().modifyGroup(tag.getGroupName(), group -> {
                group.data().clear(node -> node instanceof InheritanceNode);
                group.data().add(PrefixNode.builder().prefix(tag.getPrefix().replace("§", "&")).priority(tag.getWeight()).build());
                group.data().add(WeightNode.builder(tag.getWeight()).build());
                for (String perm : tag.getPermissions()) {
                    group.data().add(PermissionNode.builder(perm).build());
                }
            }).join();
        } catch (Exception e) {
            getLogger().warning("Erro ao criar grupo " + tag.getGroupName() + ": " + e.getMessage());
        }
    }

    public void setTag(UUID uuid, Tag tag) {
        activeTags.put(uuid, tag);
        saveData();

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            updateScoreboard(player, tag);
        }

        if (luckPerms != null) {
            try {
                luckPerms.getUserManager().modifyUser(uuid, user -> {
                    for (Tag t : Tag.values()) {
                        user.data().remove(InheritanceNode.builder(t.getGroupName()).build());
                    }
                    user.data().add(InheritanceNode.builder(tag.getGroupName()).build());
                    user.setPrimaryGroup(tag.getGroupName());
                }).join();
            } catch (Exception e) {
                getLogger().warning("Erro ao atualizar tag no LuckPerms: " + e.getMessage());
            }
        }
    }

    public void setTag(Player player, Tag tag) {
        setTag(player.getUniqueId(), tag);
    }

    private void updateScoreboard(Player player, Tag tag) {
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
        Team team = scoreboard.getTeam(tag.getGroupName());
        if (team != null) {
            team.addEntry(player.getName());
        }
        player.setScoreboard(scoreboard);
    }

    public Tag getTag(UUID uuid) {
        if (activeTags.containsKey(uuid)) {
            return activeTags.get(uuid);
        }
        if (luckPerms != null) {
            try {
                User user = luckPerms.getUserManager().getUser(uuid);
                if (user != null) {
                    String primary = user.getPrimaryGroup();
                    Tag fromGroup = Tag.fromString(primary);
                    if (fromGroup != null) {
                        activeTags.put(uuid, fromGroup);
                        return fromGroup;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public Tag getTag(Player player) {
        Tag tag = getTag(player.getUniqueId());
        if (tag == null) {
            tag = Tag.RECRUTA;
            setTag(player, tag);
        }
        return tag;
    }

    public Tag getTag(OfflinePlayer player) {
        return getTag(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID targetId = menuTargets.get(player.getUniqueId());
        if (targetId == null) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(menuKey, PersistentDataType.STRING)) {
            event.setCancelled(true);
            return;
        }

        String tagName = meta.getPersistentDataContainer().get(menuKey, PersistentDataType.STRING);
        Tag tag = Tag.fromString(tagName);
        if (tag != null) {
            setTag(targetId, tag);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTag definida para " + tag.getPrefix() + tag.getDisplay()));
            Player target = Bukkit.getPlayer(targetId);
            if (target != null) {
                target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSua tag foi atualizada para " + tag.getPrefix() + tag.getDisplay()));
            }
        }

        event.setCancelled(true);
        player.closeInventory();
        menuTargets.remove(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Tag tag = getTag(player);
        updateScoreboard(player, tag);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Tag tag = getTag(player.getUniqueId());
        if (tag == null) tag = Tag.RECRUTA;
        String prefix = ChatColor.translateAlternateColorCodes('&', tag.getPrefix().replace("§", "&"));
        event.setFormat(prefix + "%1$s" + ChatColor.RESET + ": %2$s");
    }

    private void loadData() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Nao foi possivel criar data.yml");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                Tag tag = Tag.fromString(dataConfig.getString(key));
                if (tag != null) activeTags.put(uuid, tag);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveData() {
        if (dataConfig == null || dataFile == null) return;
        for (String key : dataConfig.getKeys(false)) dataConfig.set(key, null);
        for (Map.Entry<UUID, Tag> entry : activeTags.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue().name());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().warning("Erro ao salvar data.yml");
        }
    }
}
