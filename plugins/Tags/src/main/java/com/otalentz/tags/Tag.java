package com.otalentz.tags;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Tag {

    RECRUTA("Recruta", ChatColor.GRAY, 100, Collections.singletonList("coins.use"), "&7Jogador iniciante do servidor.", Material.GRAY_WOOL),
    MODERADOR("Moderador", ChatColor.BLUE, 200, Arrays.asList("coins.use", "coins.pay", "minecraft.command.kick", "tags.tag.moderador"), "&9Moderador da equipe.", Material.BLUE_WOOL),
    ADMIN("Admin", ChatColor.RED, 300, Arrays.asList("coins.use", "coins.pay", "coins.admin", "minecraft.command.kick", "minecraft.command.ban", "tags.tag.admin"), "&cAdministrador do servidor.", Material.RED_WOOL),
    DONO("Dono", ChatColor.GOLD, 400, Arrays.asList("*", "tags.tag.dono"), "&6Dono do servidor.", Material.GOLD_BLOCK);

    private final String display;
    private final ChatColor color;
    private final int weight;
    private final List<String> permissions;
    private final String description;
    private final Material material;

    Tag(String display, ChatColor color, int weight, List<String> permissions, String description, Material material) {
        this.display = display;
        this.color = color;
        this.weight = weight;
        this.permissions = permissions;
        this.description = description;
        this.material = material;
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
