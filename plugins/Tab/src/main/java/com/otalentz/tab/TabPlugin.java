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
import java.util.HashMap;
import java.util.Map;
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
    private boolean resourcePackRequired = false;
    private Component resourcePackPrompt = Component.text("Ative o resource pack para ver as tags personalizadas.");
    private boolean forcePackAvailable = false;
    private final Map<UUID, Boolean> packLoaded = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        packUrl = getConfig().getString("resource-pack-url", "https://otalentz-mc-console.bore.digital/tab-resourcepack.zip");
        resourcePackRequired = getConfig().getBoolean("resource-pack-required", false);
        String prompt = getConfig().getString("resource-pack-prompt", "Ative o resource pack para ver as tags personalizadas.");
        resourcePackPrompt = Component.text(prompt);
        getDataFolder().mkdirs();
        generateResourcePack();
        setupTagsIntegration();
        forcePackAvailable = Bukkit.getPluginManager().getPlugin("ForcePack") != null;
        if (forcePackAvailable) {
            getLogger().info("ForcePack detectado. Tab nao enviara resource pack (ForcePack gerencia isso).");
        }

        getCommand("tab").setExecutor(new TabCommand(this));
        Bukkit.getPluginManager().registerEvents(this, this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            onPlayerLogin(player);
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
            Files.write(meta.toPath(), "{\"pack\":{\"pack_format\":64,\"supported_formats\":[64,64],\"description\":\"oTalentz Tab Rank Pack\"}}".getBytes());

            File fontDir = new File(tempDir, "assets/minecraft/font");
            fontDir.mkdirs();
            File includeDir = new File(fontDir, "include");
            includeDir.mkdirs();
            File texDir = new File(tempDir, "assets/minecraft/textures/font");
            texDir.mkdirs();

            copyResourceToFile("default_font/default.json", new File(fontDir, "default.json"));
            copyResourceToFile("default_font/include/space.json", new File(includeDir, "space.json"));
            copyResourceToFile("default_font/include/default.json", new File(includeDir, "default.json"));
            copyResourceToFile("default_font/include/unifont.json", new File(includeDir, "unifont.json"));

            copyResourceToFile("dono.png", new File(texDir, "dono.png"));

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
        if (forcePackAvailable) return;
        if (packFile == null || !packFile.exists() || packHash == null || packUrl == null || packUrl.isEmpty()) {
            getLogger().warning("Resource pack nao esta pronto para enviar.");
            return;
        }
        try {
            player.setResourcePack(packUrl, packHash, resourcePackPrompt, resourcePackRequired);
        } catch (Exception e) {
            getLogger().warning("Erro ao enviar resource pack para " + player.getName() + ": " + e.getMessage());
        }
    }

    public void updateTab(Player player) {
        Object tag = getTag(player);
        boolean loaded = packLoaded.getOrDefault(player.getUniqueId(), false);
        Component display;
        if (tag != null) {
            String displayName = "";
            try {
                displayName = (String) tagGetDisplayMethod.invoke(tag);
            } catch (Exception ignored) {
            }

            String prefix = "";
            try {
                prefix = (String) tagGetPrefixMethod.invoke(tag);
            } catch (Exception ignored) {
            }

            if ("Dono".equals(displayName) && loaded) {
                Component icon = Component.text("\uE000");
                Component name = Component.text(" " + player.getName());
                display = Component.empty().append(icon).append(name);
            } else {
                Component prefixComp = LegacyComponentSerializer.legacySection().deserialize(prefix);
                display = Component.empty().append(prefixComp).append(Component.text(player.getName()));
            }
        } else {
            display = Component.text(player.getName());
        }
        player.playerListName(display);
    }

    public void sendTest(Player player) {
        Component icon = Component.text("\uE000");
        Component msg = Component.text("[Tab Test] ").append(icon).append(Component.text(" " + player.getName()));
        player.sendMessage(msg);
        player.sendActionBar(Component.text("Se aparecer o icone acima, a fonte carregou."));
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

    private void onPlayerLogin(Player player) {
        packLoaded.put(player.getUniqueId(), false);
        sendPack(player);
        updateTab(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    updateTab(player);
                }
            }
        }.runTaskLater(this, 60L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        onPlayerLogin(event.getPlayer());
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        getLogger().info("Resource pack status de " + player.getName() + ": " + status.name());
        if (status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED || status == PlayerResourcePackStatusEvent.Status.DOWNLOADED) {
            packLoaded.put(player.getUniqueId(), true);
            updateTab(player);
        } else if (status == PlayerResourcePackStatusEvent.Status.DECLINED || status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD || status == PlayerResourcePackStatusEvent.Status.FAILED_RELOAD) {
            packLoaded.put(player.getUniqueId(), false);
            player.sendMessage("§cO resource pack nao foi carregado. A tag Dono sera exibida como texto ate que o pack seja aceito.");
            updateTab(player);
        }
    }

    private static void copyResourceToFile(String resource, File out) throws IOException {
        InputStream in = TabPlugin.class.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("Recurso nao encontrado: " + resource);
        }
        copy(in, out);
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
