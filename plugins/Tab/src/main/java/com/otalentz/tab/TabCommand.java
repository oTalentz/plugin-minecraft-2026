package com.otalentz.tab;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TabCommand implements CommandExecutor {

    private final TabPlugin plugin;

    public TabCommand(TabPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("otalentztab.admin")) {
            sender.sendMessage("§cVoce nao tem permissao.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("reload")) {
            plugin.refreshAll();
            sender.sendMessage("§aTab e resource pack recarregados para todos os jogadores.");
            return true;
        }

        if (args[0].equalsIgnoreCase("pack")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.sendPack(player);
            }
            sender.sendMessage("§aResource pack reenviado para todos os jogadores.");
            return true;
        }

        if (args[0].equalsIgnoreCase("update")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.updateTab(player);
            }
            sender.sendMessage("§aTab atualizado para todos os jogadores.");
            return true;
        }

        if (args[0].equalsIgnoreCase("test")) {
            if (sender instanceof Player) {
                plugin.sendTest((Player) sender);
                sender.sendMessage("§aMensagem de teste enviada. Verifique o chat e o tab.");
            } else {
                sender.sendMessage("§cApenas jogadores podem usar /otab test");
            }
            return true;
        }

        sender.sendMessage("§cUso: /otab [reload|pack|update|test]");
        return true;
    }
}
