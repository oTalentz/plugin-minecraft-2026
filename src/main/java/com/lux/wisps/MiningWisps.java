package com.lux.wisps;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiningWisps extends JavaPlugin implements Listener, CommandExecutor {

    private Map<UUID, ActiveWisp> activeWisps = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("wisp") != null) {
            getCommand("wisp").setExecutor(this);
        }
        getLogger().info("MiningWisps foi ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        for (ActiveWisp wisp : activeWisps.values()) {
            wisp.remove();
        }
        activeWisps.clear();
        getLogger().info("MiningWisps foi desativado.");
    }

    public ItemStack getWispItem() {
        ItemStack item = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "✦ Espírito de Luz ✦");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Um pequeno espírito mágico engarrafado.",
                    ChatColor.GRAY + "Clique direito para invocá-lo.",
                    ChatColor.GRAY + "Agache (Shift) + Clique para guardá-lo.",
                    "",
                    ChatColor.AQUA + "Duração: 3 Minutos"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("wisp")) {
            if (sender instanceof Player && sender.hasPermission("miningwisps.admin")) {
                Player p = (Player) sender;
                p.getInventory().addItem(getWispItem());
                p.sendMessage(ChatColor.GREEN + "Você recebeu um Espírito de Luz!");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Apenas jogadores com permissão podem usar este comando.");
            }
        }
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.GLOWSTONE_DUST && item.hasItemMeta()) {
                String displayName = item.getItemMeta().getDisplayName();
                if (displayName != null && displayName.contains("Espírito de Luz")) {
                    event.setCancelled(true);

                    if (activeWisps.containsKey(p.getUniqueId())) {
                        if (p.isSneaking()) {
                            // Player is sneaking, let's deactivate/store the wisp
                            activeWisps.get(p.getUniqueId()).remove();
                            activeWisps.remove(p.getUniqueId());
                            p.sendMessage(ChatColor.YELLOW + "Você guardou o seu Espírito de Luz!");
                            return;
                        } else {
                            p.sendMessage(ChatColor.RED + "Você já tem um espírito de luz acompanhando você! Agache (Shift) e clique para guardá-lo.");
                            return;
                        }
                    }

                    // Consume the item (1 unit)
                    item.setAmount(item.getAmount() - 1);

                    // Spawn the wisp
                    spawnWisp(p);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (activeWisps.containsKey(p.getUniqueId())) {
            activeWisps.get(p.getUniqueId()).remove();
            activeWisps.remove(p.getUniqueId());
        }
    }

    private void spawnWisp(Player player) {
        Location loc = player.getLocation().add(0, 2, 0);
        ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setGravity(false);
            as.setSmall(true);
            as.setMarker(true); // Prevents player interaction
            as.setInvulnerable(true);
        });

        ActiveWisp wisp = new ActiveWisp(player, stand);
        activeWisps.put(player.getUniqueId(), wisp);
        
        player.sendMessage(ChatColor.YELLOW + "Um Espírito de Luz começou a te seguir!");
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1.0f, 2.0f);
    }

    // Inner class representing an active wisp
    class ActiveWisp extends BukkitRunnable {
        private final Player player;
        private final ArmorStand stand;
        private int ticksLived = 0;
        private final int MAX_TICKS = 20 * 60 * 3; // 3 minutes duration
        private double angle = 0;

        public ActiveWisp(Player player, ArmorStand stand) {
            this.player = player;
            this.stand = stand;
            // Run every 1 tick
            this.runTaskTimer(MiningWisps.this, 0L, 1L);
        }

        @Override
        public void run() {
            if (!player.isOnline() || player.isDead() || ticksLived >= MAX_TICKS) {
                remove();
                activeWisps.remove(player.getUniqueId());
                return;
            }

            // Calculate new position around the player
            angle += 0.15;
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;
            // Add a little floating bob effect using sine
            double yOffset = 1.6 + Math.sin(angle * 0.5) * 0.3;
            
            Location target = player.getLocation().add(x, yOffset, z);
            
            // Move the armor stand smoothly
            stand.teleport(target);

            // Emit particles
            player.getWorld().spawnParticle(Particle.END_ROD, target.add(0, 0.5, 0), 1, 0, 0, 0, 0.01);
            player.getWorld().spawnParticle(Particle.WAX_ON, target, 1, 0.1, 0.1, 0.1, 0.05);

            // Keep Night Vision effect applied without particles so the player can see clearly
            // Duration is 220 ticks (11 seconds) so it never flickers.
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 220, 0, false, false, false));

            ticksLived++;
        }

        public void remove() {
            this.cancel();
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
            if (player.isOnline()) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
                player.sendMessage(ChatColor.GRAY + "O Espírito de Luz desapareceu...");
            }
        }
    }
}
