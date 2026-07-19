package com.otalentz.tags;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Tag {

    RECRUTA("Recruta", ChatColor.GRAY, 100, Collections.singletonList("coins.use"), "&7Jogador iniciante do servidor.", Material.GRAY_WOOL, "\uE000&f "),
    CADETE("Cadete", ChatColor.YELLOW, 200, Arrays.asList("coins.use", "coins.pay"), "&eJogador em ascensao.", Material.YELLOW_WOOL, "\uE001&f "),
    OFICIAL("Oficial", ChatColor.BLUE, 300, Arrays.asList("coins.use", "coins.pay", "coins.shop"), "&bOficial das forcas.", Material.LIGHT_BLUE_WOOL, "\uE002&f "),
    CAPITAO("Capitao", ChatColor.GREEN, 400, Arrays.asList("coins.use", "coins.pay", "coins.shop", "tags.tag.capitao"), "&aLider de esquadrao.", Material.GREEN_WOOL, "\uE003&f "),
    MESTRE("Mestre", ChatColor.AQUA, 500, Arrays.asList("coins.use", "coins.pay", "coins.shop", "tags.tag.mestre"), "&3Mestre experiente.", Material.CYAN_WOOL, "\uE004&f "),
    HEROI("Heroi", ChatColor.LIGHT_PURPLE, 600, Arrays.asList("coins.use", "coins.pay", "coins.shop", "tags.tag.heroi"), "&dHeroi lendario.", Material.PURPLE_WOOL, "\uE005&f ");

    private final String display;
    private final ChatColor color;
    private final int weight;
    private final List<String> permissions;
    private final String description;
    private final Material material;
    private final String icon;

    Tag(String display, ChatColor color, int weight, List<String> permissions, String description, Material material, String icon) {
        this.display = display;
        this.color = color;
        this.weight = weight;
        this.permissions = permissions;
        this.description = description;
        this.material = material;
        this.icon = icon;
    }

    public String getDisplay() {
        return display;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public String getDescription() {
        return description;
    }

    public Material getMaterial() {
        return material;
    }

    public String getIcon() {
        return icon;
    }

    public String getGroupName() {
        return name().toLowerCase();
    }

    public String getPrefix() {
        return color + "[" + display + "] " + ChatColor.RESET;
    }

    public static Tag fromString(String input) {
        if (input == null) return null;
        for (Tag tag : values()) {
            if (tag.name().equalsIgnoreCase(input) || tag.getDisplay().equalsIgnoreCase(input)) {
                return tag;
            }
        }
        return null;
    }
}
