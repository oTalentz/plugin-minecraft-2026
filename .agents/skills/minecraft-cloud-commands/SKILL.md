---
name: minecraft-cloud-commands
description: "Build modern, type-safe Minecraft commands with the Incendo Cloud Command Framework on Paper/Spigot, Velocity, BungeeCord, Fabric, Sponge and more. Use for typed arguments, subcommands, Brigadier integration, annotations, suggestions and help handling."
---

# Cloud Command Framework

Reference: `https://github.com/Incendo/cloud-minecraft`

Docs: `https://cloud.incendo.org/minecraft/`

Example plugin: linked from `https://cloud.incendo.org/minecraft/paper/`

Cloud is a JVM command-creation framework. `cloud-paper` is the recommended module for Bukkit-based platforms.

## When to use

- Commands with typed arguments (Player, Integer, Double, String, enums, custom parsers).
- Subcommands, permissions, suggestions, Brigadier/1.13+ command tree support.
- Commands shared across platforms (Paper, Velocity, BungeeCord, Fabric, etc.).
- Complex command trees where raw Bukkit command registration is verbose or fragile.

## Installation

### Maven

```xml
<dependency>
    <groupId>org.incendo</groupId>
    <artifactId>cloud-paper</artifactId>
    <version>2.0.0-beta.10</version>
</dependency>
```

### Gradle

```kotlin
dependencies {
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
}
```

## Paper setup

For Paper 1.20.6+ use the modern `PaperCommandManager`. Do **not** register commands in `plugin.yml` or `paper-plugin.yml`; Cloud registers them itself.

```java
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        PaperCommandManager commandManager = PaperCommandManager
            .builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.nonSchedulingExecutor())
            .buildOnEnable(this);

        commandManager.command(commandManager.commandBuilder("player_command")
            .senderType(PlayerSource.class)
            .handler(context -> {
                Player player = context.sender().source();
                player.sendMessage("Hello, player!");
            })
        );
    }
}
```

## Command with arguments

```java
commandManager.command(commandManager.commandBuilder("givecoins")
    .required("player", PlayerParser.playerParser())
    .required("amount", IntegerParser.integerParser(1, Integer.MAX_VALUE))
    .handler(context -> {
        Player target = context.get("player");
        int amount = context.get("amount");
        // do something
    })
);
```

## Capabilities

Enable Brigadier and asynchronous completions when available:

```java
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;

if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
    commandManager.registerBrigadier();
}

if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
    commandManager.registerAsynchronousCompletions();
}
```

## Annotations

Cloud also supports annotated command methods via `cloud-annotations`:

```xml
<dependency>
    <groupId>org.incendo</groupId>
    <artifactId>cloud-annotations</artifactId>
    <version>2.0.0</version>
</dependency>
```

```java
@Command("givecoins <player> <amount>")
public void giveCoins(CommandSender sender, @Argument("player") Player target, @Argument("amount") int amount) {
    // handle command
}
```

## Notes

- Use `LegacyPaperCommandManager` only if you must support older Paper/Spigot versions.
- Use `ExecutionCoordinator.nonSchedulingExecutor()` on Bukkit to avoid blocking the main thread for suggestions.
- Combine with `minecraft-plugin-dev` for general Paper plugin structure.
