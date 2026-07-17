package com.otalentz.tags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class TagsCommand implements CommandExecutor {

    private final TagsPlugin plugin;

    public TagsCommand(TagsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tags")) {
            if (!sender.hasPermission("tags.use")) {
                sender.sendMessage(color("&cSem permissao."));
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                sendHelp(sender);
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(color("&cApenas jogadores podem usar o menu de tags."));
                return true;
            }

            Player player = (Player) sender;
            openTagsMenu(player, player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("tag")) {
            if (!sender.hasPermission("tags.admin.set")) {
                sender.sendMessage(color("&cSem permissao para definir tags."));
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                sendTagHelp(sender);
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(color("&cUso: /tag <jogador> <tag> | /tag help"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(color("&cJogador nao encontrado."));
                return true;
            }

            Tag tag = Tag.fromString(args[1]);
            if (tag == null) {
                sender.sendMessage(color("&cTag invalida. Use: Recruta, Moderador, Admin ou Dono."));
                return true;
            }

            plugin.setTag(target, tag);
            sender.sendMessage(color("&aTag de &f" + target.getName() + "&a definida para " + tag.getPrefix() + tag.getDisplay()));
            target.sendMessage(color("&aSua tag foi atualizada para " + tag.getPrefix() + tag.getDisplay()));
            return true;
        }

        return false;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&6--- Ajuda Tags ---"));
        sender.sendMessage(color("&e/tags &7- abre o menu de tags"));
        sender.sendMessage(color("&e/tags help &7- mostra esta ajuda"));
        sender.sendMessage(color("&7Tags disponiveis: &fRecruta, Moderador, Admin, Dono"));
        if (sender.hasPermission("tags.admin.set")) {
            sender.sendMessage(color("&e/tag <jogador> <tag> &7- define a tag de outro jogador"));
        }
    }

    private void sendTagHelp(CommandSender sender) {
        sender.sendMessage(color("&6--- Ajuda Tag (admin) ---"));
        sender.sendMessage(color("&e/tag <jogador> <tag> &7- define a tag de um jogador"));
        sender.sendMessage(color("&7Tags: Recruta, Moderador, Admin, Dono"));
    }

    public void openTagsMenu(Player viewer, Player target) {
        Inventory inv = Bukkit.createInventory(null, 9, color("&aTags"));

        for (int i = 0; i < Tag.values().length; i++) {
            Tag tag = Tag.values()[i];
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(color(tag.getPrefix() + tag.getDisplay()));
            meta.setLore(Arrays.asList(
                color("&7Clique para selecionar esta tag."),
                color("&7Permissoes: &f" + String.join(", ", tag.getPermissions()))
            ));
            meta.getPersistentDataContainer().set(plugin.getMenuKey(), PersistentDataType.STRING, tag.name());
            item.setItemMeta(meta);
            inv.setItem(i + 2, item);
        }

        plugin.setMenuTarget(viewer.getUniqueId(), target.getUniqueId());
        viewer.openInventory(inv);
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
