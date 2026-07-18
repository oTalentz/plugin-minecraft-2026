---
name: minecraft-packetevents
description: "Intercept, read and modify Minecraft Java Edition network packets using PacketEvents on Paper/Spigot, Velocity, BungeeCord, Fabric or Sponge. Use for packet-level features such as anti-cheat, custom animations, protocol compatibility and entity/sound/particle injection."
---

# PacketEvents

Reference: `https://github.com/retrooper/packetevents`

Docs: `https://docs.packetevents.com/`

JavaDoc: `https://javadocs.packetevents.com/`

PacketEvents is a protocol library built on Netty. It lets you listen to inbound/outbound packets, read their fields and modify them before they are sent.

## When to use

- Intercepting or modifying player packets (movement, chat, inventory, entity, sound, particle, etc.).
- Building anti-cheat, replay systems, or custom animations.
- Cross-platform packet code that must run on Paper, Velocity, Fabric, etc.

## Installation

### Maven

```xml
<repositories>
    <repository>
        <id>codemc-releases</id>
        <url>https://repo.codemc.io/repository/maven-releases/</url>
    </repository>
    <repository>
        <id>codemc-snapshots</id>
        <url>https://repo.codemc.io/repository/maven-snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.retrooper</groupId>
        <artifactId>packetevents-spigot</artifactId>
        <version>2.13.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle

```kotlin
repositories {
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
}
```

### plugin.yml

If PacketEvents is not shaded, declare it as a dependency:

```yaml
depend:
  - packetevents
```

## Spigot/Paper setup

```java
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager()
            .registerListener(new MyPacketListener());
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
```

## Example listener

```java
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;

public class MyPacketListener implements PacketListener {
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_HEALTH) {
            WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth(event);
            float health = packet.getHealth();
            packet.setHealth(15.0f);
            event.markForReEncode(true);
        }
    }
}
```

## Important notes

- You must call `load()` before `init()`.
- Call `terminate()` in `onDisable`.
- Use `event.markForReEncode(true)` when modifying a packet.
- Prefer packet wrappers over raw byte manipulation; they are version-safe.
- PacketEvents works on multiple platforms with the same listener code for the most part.
