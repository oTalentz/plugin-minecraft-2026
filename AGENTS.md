<!-- GSD Configuration — managed by gsd-core installer -->
# Instructions for GSD

- Use the gsd-core skill when the user asks for GSD or uses a `gsd-*` command.
- Treat `/gsd-...` or `gsd-...` as command invocations and load the matching file from `.github/skills/gsd-*`.
- When a command says to spawn a subagent, prefer a matching custom agent from `.github/agents`.
- Do not apply GSD workflows unless the user explicitly asks for them.
- After completing any `gsd-*` command (or any deliverable it triggers: feature, bug fix, tests, docs, etc.), ALWAYS: (1) offer the user the next step by prompting via `ask_user`; repeat this feedback loop until the user explicitly indicates they are done.
<!-- /GSD Configuration -->

# Minecraft Agent Skills

Imported from `https://github.com/Jahrome907/minecraft-agent-skills.git`.

Available under `.agents/skills/`:
- `minecraft-plugin-dev` — Paper/Bukkit/Spigot plugins for 1.21.x
- `minecraft-modding` — NeoForge/Fabric/Forge 1.20.1 mods
- `minecraft-testing` — JUnit, MockBukkit, GameTests
- `minecraft-resource-pack` — textures, models, sounds, fonts, shaders
- `minecraft-server-admin` — hosting, tuning, backups, proxies, ops
- `minecraft-world-generation` — biomes, dimensions, structures, features

Activate the relevant skill when the user asks about any of those topics.
Do not load unrelated skills to save context.
