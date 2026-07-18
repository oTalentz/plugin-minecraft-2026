package com.otalentz.tab;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TabPlugin extends JavaPlugin implements Listener {

    private static TabPlugin instance;

    private File packFile;
    private byte[] packHash;
    private String packUrl;

    private Object tagsPluginInstance;
    private Method tagsGetTagMethod;
    private Method tagGetDisplayMethod;
    private Method tagGetPrefixMethod;
    private boolean tagsAvailable = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        packUrl = getConfig().getString("resource-pack-url", "https://otalentz-mc-console.bore.digital/tab-resourcepack.zip");
        getDataFolder().mkdirs();
        generateResourcePack();
        setupTagsIntegration();

        getCommand("tab").setExecutor(new TabCommand(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            sendPack(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        updateTab(player);
                    }
                }
            }.runTaskLater(this, 40L);
        }
    }

    public static TabPlugin getInstance() {
        return instance;
    }

    public File getPackFile() {
        return packFile;
    }

    public byte[] getPackHash() {
        return packHash;
    }

    public String getPackUrl() {
        return packUrl;
    }

    public boolean isTagsAvailable() {
        return tagsAvailable;
    }

    private void generateResourcePack() {
        File tempDir = new File(getDataFolder(), "pack-tmp");
        deleteDir(tempDir);
        tempDir.mkdirs();

        try {
            File meta = new File(tempDir, "pack.mcmeta");
            Files.write(meta.toPath(), "{\"pack\":{\"pack_format\":64,\"description\":\"oTalentz Tab Rank Pack\"}}".getBytes());

            File fontDir = new File(tempDir, "assets/minecraft/font");
            fontDir.mkdirs();
            File texDir = new File(tempDir, "assets/minecraft/textures/font");
            texDir.mkdirs();

            String fontJson = "{\"providers\":[{\"type\":\"bitmap\",\"file\":\"minecraft:font/dono\",\"height\":8,\"ascent\":7,\"chars\":[\"\\uE000\"]}]}";
            Files.write(new File(fontDir, "donorank.json").toPath(), fontJson.getBytes("UTF-8"));

            InputStream in = getResource("dono.png");
            if (in == null) {
                getLogger().warning("dono.png nao encontrado nos recursos do plugin.");
                return;
            }
            File donoFile = new File(texDir, "dono.png");
            copy(in, donoFile);

            packFile = new File(getDataFolder(), "tab-resourcepack.zip");
            zipDir(tempDir, packFile);
            packHash = sha1(packFile);
            getLogger().info("Resource pack gerado em " + packFile.getAbsolutePath());
        } catch (Exception e) {
            getLogger().warning("Erro ao gerar resource pack: " + e.getMessage());
            e.printStackTrace();
        } finally {
            deleteDir(tempDir);
        }
    }

    private void setupTagsIntegration() {
        Plugin tagsPlugin = Bukkit.getPluginManager().getPlugin("Tags");
        if (tagsPlugin == null) {
            getLogger().info("Tags nao encontrado. Tab usara nomes vanilla.");
            return;
        }
        try {
            Class<?> pluginClass = Class.forName("com.otalentz.tags.TagsPlugin");
            Method getInstance = pluginClass.getMethod("getInstance");
            tagsPluginInstance = getInstance.invoke(null);
            tagsGetTagMethod = pluginClass.getMethod("getTag", Player.class);

            Class<?> tagClass = Class.forName("com.otalentz.tags.Tag");
            tagGetDisplayMethod = tagClass.getMethod("getDisplay");
            tagGetPrefixMethod = tagClass.getMethod("getPrefix");

            tagsAvailable = true;
            getLogger().info("Integracao com Tags ativada.");
        } catch (Exception e) {
            getLogger().warning("Nao foi possivel integrar com Tags: " + e.getMessage());
        }
    }

    private Object getTag(Player player) {
        if (!tagsAvailable || tagsPluginInstance == null) return null;
        try {
            return tagsGetTagMethod.invoke(tagsPluginInstance, player);
        } catch (Exception e) {
            return null;
        }
    }

    public void sendPack(Player player) {
        if (packFile == null || !packFile.exists() || packHash == null || packUrl == null || packUrl.isEmpty()) {
            getLogger().warning("Resource pack nao esta pronto para enviar.");
            return;
        }
        try {
            player.setResourcePack(packUrl, packHash, false);
        } catch (Exception e) {
            getLogger().warning("Erro ao enviar resource pack para " + player.getName() + ": " + e.getMessage());
        }
    }

    public void updateTab(Player player) {
        Object tag = getTag(player);
        Component display;
        if (tag != null) {
            String displayName = "";
            try {
                displayName = (String) tagGetDisplayMethod.invoke(tag);
            } catch (Exception ignored) {
            }

            if ("Dono".equals(displayName)) {
                Component icon = Component.text("\uE000").font(Key.key("minecraft", "donorank"));
                Component name = Component.text(" " + player.getName()).font(Key.key("minecraft", "default"));
                display = Component.empty().append(icon).append(name);
            } else {
                String prefix = "";
                try {
                    prefix = (String) tagGetPrefixMethod.invoke(tag);
                } catch (Exception ignored) {
                }
                Component prefixComp = LegacyComponentSerializer.legacySection().deserialize(prefix);
                display = Component.empty().append(prefixComp).append(Component.text(player.getName()));
            }
        } else {
            display = Component.text(player.getName());
        }
        player.playerListName(display);
    }

    public void refreshAll() {
        if (packFile == null || !packFile.exists()) {
            generateResourcePack();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendPack(player);
            updateTab(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sendPack(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    updateTab(player);
                }
            }
        }.runTaskLater(this, 40L);
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            updateTab(event.getPlayer());
        }
    }

    private static void copy(InputStream in, File out) throws IOException {
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(out)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        }
        in.close();
    }

    private static byte[] sha1(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) {
                digest.update(buf, 0, len);
            }
        }
        return digest.digest();
    }

    private static void zipDir(File source, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            zipRecursive(source, source, zos);
        }
    }

    private static void zipRecursive(File root, File source, ZipOutputStream zos) throws IOException {
        File[] files = source.listFiles();
        if (files == null) return;
        for (File file : files) {
            String relative = root.toURI().relativize(file.toURI()).getPath();
            if (file.isDirectory()) {
                if (!relative.isEmpty()) {
                    zos.putNextEntry(new ZipEntry(relative));
                    zos.closeEntry();
                }
                zipRecursive(root, file, zos);
            } else {
                ZipEntry entry = new ZipEntry(relative);
                zos.putNextEntry(entry);
                Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        }
    }

    private static void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}
