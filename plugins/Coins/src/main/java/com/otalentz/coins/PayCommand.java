package com.otalentz.coins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final CoinsPlugin plugin;

    public PayCommand(CoinsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cApenas jogadores."));
            return true;
        }

        if (!sender.hasPermission("coins.pay")) {
            sender.sendMessage(color("&cSem permissao."));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(color("&6--- Ajuda Pay ---"));
            sender.sendMessage(color("&e/pay <jogador> <quantia> &7- transfere coins para outro jogador"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(color("&cUso: /pay <jogador> <quantia> | /pay help"));
            return true;
        }

        Player player = (Player) sender;
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(color("&cQuantia invalida."));
            return true;
        }

        if (!plugin.hasBalance(player.getUniqueId(), amount)) {
            player.sendMessage(color("&cSaldo insuficiente."));
            return true;
        }

        plugin.withdraw(player.getUniqueId(), amount);
        plugin.deposit(target.getUniqueId(), amount);

        player.sendMessage(color("&aVoce pagou &f" + plugin.format(amount) + "&a para &f" + target.getName() + "&a."));
        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(color("&aVoce recebeu &f" + plugin.format(amount) + "&a de &f" + player.getName() + "&a."));
        }
        return true;
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
