package com.otalentz.tags;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Tag {

    RECRUTA("Recruta", ChatColor.GRAY, 100, Collections.singletonList("coins.use")),
    MODERADOR("Moderador", ChatColor.BLUE, 200, Arrays.asList("coins.use", "coins.pay", "minecraft.command.kick", "tags.tag.moderador")),
    ADMIN("Admin", ChatColor.RED, 300, Arrays.asList("coins.use", "coins.pay", "coins.admin", "minecraft.command.kick", "minecraft.command.ban", "tags.tag.admin")),
    DONO("Dono", ChatColor.GOLD, 400, Arrays.asList("*", "tags.tag.dono"));

    private final String display;
    private final ChatColor color;
    private final int weight;
    private final List<String> permissions;

    Tag(String display, ChatColor color, int weight, List<String> permissions) {
        this.display = display;
        this.color = color;
        this.weight = weight;
        this.permissions = permissions;
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
