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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
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
    private boolean tabPluginAvailable = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadData();

        tabPluginAvailable = Bukkit.getPluginManager().getPlugin("TAB") != null;
        if (!tabPluginAvailable) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            for (Tag tag : Tag.values()) {
                Team team = scoreboard.getTeam(tag.getGroupName());
                if (team == null) {
                    team = scoreboard.registerNewTeam(tag.getGroupName());
                }
                team.setPrefix(tag.getPrefix());
            }
        } else {
            getLogger().info("TAB detectado. Tags nao criara scoreboard teams.");
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

    public void openTagsMenu(Player viewer, Player target) {
        Inventory inv = Bukkit.createInventory(null, 27, color("&8Tags &7| &6Selecione"));

        // Bordas decorativas
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        int[] borderSlots = {0,1,2,3,4,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26};
        for (int slot : borderSlots) {
            inv.setItem(slot, border);
        }

        // Titulo do menu
        ItemStack title = new ItemStack(Material.NAME_TAG);
        ItemMeta titleMeta = title.getItemMeta();
        if (titleMeta != null) {
            titleMeta.setDisplayName(color("&6&lMenu de Tags"));
            titleMeta.setLore(Arrays.asList(
                color("&7Clique na tag desejada."),
                color("&7Tag atual sera marcada com brilho.")
            ));
            title.setItemMeta(titleMeta);
        }
        inv.setItem(4, title);

        // Indicador de tag atual
        Tag current = getTag(target);
        ItemStack currentItem = new ItemStack(Material.BOOK);
        ItemMeta currentMeta = currentItem.getItemMeta();
        if (currentMeta != null) {
            currentMeta.setDisplayName(color("&aTag Atual"));
            currentMeta.setLore(Collections.singletonList(color("&7" + target.getName() + " esta usando: " + current.getPrefix() + current.getDisplay())));
            currentItem.setItemMeta(currentMeta);
        }
        inv.setItem(13, currentItem);

        // Slots das tags
        int[] tagSlots = {10, 12, 14, 16};
        for (int i = 0; i < Tag.values().length; i++) {
            Tag tag = Tag.values()[i];
            ItemStack item = new ItemStack(tag.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            boolean isCurrent = current == tag;
            String name = (isCurrent ? color("&a✔ ") : "") + tag.getColor() + "&l" + tag.getDisplay();
            meta.setDisplayName(color(name));

            List<String> lore = new ArrayList<>();
            lore.add(color(tag.getDescription()));
            lore.add(color("&7Peso: &f" + tag.getWeight()));
            lore.add(color("&7Permissoes:"));
            for (String perm : tag.getPermissions()) {
                lore.add(color(" &8- &f" + perm));
            }
            if (isCurrent) {
                lore.add(color(""));
                lore.add(color("&aTag atual do jogador"));
                meta.setEnchantmentGlintOverride(true);
            } else {
                lore.add(color(""));
                if (viewer.equals(target)) {
                    lore.add(color("&eClique para selecionar"));
                } else {
                    lore.add(color("&eClique para definir para &f" + target.getName()));
                }
            }
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(menuKey, PersistentDataType.STRING, tag.name());
            item.setItemMeta(meta);
            inv.setItem(tagSlots[i], item);
        }

        // Botao fechar
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(color("&cFechar"));
            closeMeta.setLore(Collections.singletonList(color("&7Clique para fechar o menu")));
            close.setItemMeta(closeMeta);
        }
        inv.setItem(26, close);

        setMenuTarget(viewer.getUniqueId(), target.getUniqueId());
        viewer.playSound(viewer.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f);
        viewer.openInventory(inv);
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private boolean canSelectTag(Player player, Tag tag) {
        if (player.hasPermission("tags.admin.set")) return true;
        if (tag == Tag.RECRUTA) return true;
        return player.hasPermission("tags.tag." + tag.getGroupName());
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
        if (tabPluginAvailable || scoreboard == null) {
            return;
        }
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

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        // Botao fechar
        if (item.getType() == Material.BARRIER) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 0.8f);
            player.closeInventory();
            menuTargets.remove(player.getUniqueId());
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(menuKey, PersistentDataType.STRING)) return;

        String tagName = meta.getPersistentDataContainer().get(menuKey, PersistentDataType.STRING);
        Tag tag = Tag.fromString(tagName);
        if (tag == null) return;

        Player target = Bukkit.getPlayer(targetId);

        if (!canSelectTag(player, tag)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cVoce nao tem permissao para selecionar a tag " + tag.getPrefix() + tag.getDisplay()));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.closeInventory();
            menuTargets.remove(player.getUniqueId());
            return;
        }

        setTag(targetId, tag);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTag definida para " + tag.getPrefix() + tag.getDisplay()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.2f);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1.5, 0), 15, 0.3, 0.5, 0.3, 0);

        if (target != null && !target.getUniqueId().equals(player.getUniqueId())) {
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSua tag foi atualizada para " + tag.getPrefix() + tag.getDisplay()));
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.2f);
        }

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
