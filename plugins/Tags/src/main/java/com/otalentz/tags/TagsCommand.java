package com.otalentz.tags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            StringBuilder sb = new StringBuilder(color("&aTags disponiveis: "));
            for (Tag tag : Tag.values()) {
                sb.append(tag.getPrefix()).append(tag.getDisplay()).append(ChatColor.RESET).append(", ");
            }
            sender.sendMessage(sb.substring(0, sb.length() - 2));
            return true;
        }

        if (command.getName().equalsIgnoreCase("tag")) {
            if (!sender.hasPermission("tags.admin.set")) {
                sender.sendMessage(color("&cSem permissao para definir tags."));
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(color("&cUso: /tag <jogador> <tag>"));
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

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
