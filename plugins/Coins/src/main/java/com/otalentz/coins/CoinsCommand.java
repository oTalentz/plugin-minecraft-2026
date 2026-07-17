package com.otalentz.coins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoinsCommand implements CommandExecutor {

    private final CoinsPlugin plugin;

    public CoinsCommand(CoinsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("coins.use")) {
            sender.sendMessage(color("&cVoce nao tem permissao."));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(color("&cUse: /coins <jogador>"));
                return true;
            }
            Player player = (Player) sender;
            sender.sendMessage(color("&aSeu saldo: &f" + plugin.format(player.getUniqueId())));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("coins.admin")) {
                sender.sendMessage(color("&cSem permissao para ver saldo de outros."));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            sender.sendMessage(color("&aSaldo de &f" + target.getName() + "&a: &f" + plugin.format(target.getUniqueId())));
            return true;
        }

        if (args.length >= 3) {
            if (!sender.hasPermission("coins.admin")) {
                sender.sendMessage(color("&cSem permissao."));
                return true;
            }
            String action = args[0].toLowerCase();
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            double amount;
            try {
                amount = Double.parseDouble(args[2]);
                if (amount < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(color("&cQuantia invalida."));
                return true;
            }

            switch (action) {
                case "set":
                    plugin.setBalance(target.getUniqueId(), amount);
                    sender.sendMessage(color("&aSaldo de &f" + target.getName() + "&a definido para &f" + plugin.format(target.getUniqueId())));
                    break;
                case "add":
                    plugin.deposit(target.getUniqueId(), amount);
                    sender.sendMessage(color("&aAdicionado &f" + plugin.format(amount) + "&a para &f" + target.getName() + "&a. Novo saldo: &f" + plugin.format(target.getUniqueId())));
                    break;
                case "remove":
                    if (plugin.withdraw(target.getUniqueId(), amount)) {
                        sender.sendMessage(color("&aRemovido &f" + plugin.format(amount) + "&a de &f" + target.getName() + "&a. Novo saldo: &f" + plugin.format(target.getUniqueId())));
                    } else {
                        sender.sendMessage(color("&cSaldo insuficiente."));
                    }
                    break;
                default:
                    sender.sendMessage(color("&cUso: /coins [set/add/remove] [jogador] [quantia]"));
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&6--- Ajuda Coins ---"));
        sender.sendMessage(color("&e/coins &7- ve seu saldo"));
        sender.sendMessage(color("&e/coins help &7- mostra esta ajuda"));
        if (sender.hasPermission("coins.admin")) {
            sender.sendMessage(color("&e/coins <jogador> &7- ve saldo de outro jogador"));
            sender.sendMessage(color("&e/coins set <jogador> <quantia> &7- define o saldo"));
            sender.sendMessage(color("&e/coins add <jogador> <quantia> &7- adiciona coins"));
            sender.sendMessage(color("&e/coins remove <jogador> <quantia> &7- remove coins"));
        }
        if (sender.hasPermission("coins.pay")) {
            sender.sendMessage(color("&e/pay <jogador> <quantia> &7- transfere coins"));
        }
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
