package com.otalentz.coins;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoinsPlugin extends JavaPlugin {

    private static CoinsPlugin instance;
    private final ConcurrentHashMap<UUID, Double> balances = new ConcurrentHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;
    private double startBalance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        startBalance = getConfig().getDouble("start-balance", 0.0);

        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Nao foi possivel criar data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadBalances();

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            Bukkit.getServicesManager().register(Economy.class, new CoinsEconomy(this), this, ServicePriority.Normal);
            getLogger().info("Vault Economy registrada com sucesso.");
        } else {
            getLogger().warning("Vault nao encontrado. A API de economia nao estara disponivel para outros plugins.");
        }

        getCommand("coins").setExecutor(new CoinsCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
    }

    @Override
    public void onDisable() {
        saveBalances();
    }

    public static CoinsPlugin getInstance() {
        return instance;
    }

    public double getStartBalance() {
        return startBalance;
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, round(amount)));
    }

    public boolean hasBalance(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount < 0) return false;
        double current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    public void deposit(UUID uuid, double amount) {
        if (amount < 0) return;
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public void createAccount(UUID uuid) {
        balances.putIfAbsent(uuid, startBalance);
    }

    public String format(UUID uuid) {
        double amount = getBalance(uuid);
        return format(amount);
    }

    public String format(double amount) {
        return String.format("%,.2f %s", amount, amount == 1.0 ? "Coin" : "Coins");
    }

    private void loadBalances() {
        if (dataConfig == null) return;
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double value = dataConfig.getDouble(key, 0.0);
                balances.put(uuid, round(value));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveBalances() {
        if (dataConfig == null || dataFile == null) return;
        dataConfig.getKeys(false).forEach(k -> dataConfig.set(k, null));
        for (java.util.Map.Entry<UUID, Double> entry : balances.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().warning("Erro ao salvar data.yml: " + e.getMessage());
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
