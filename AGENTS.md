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

# Claude-Mem Skills

Imported from `https://github.com/thedotmack/claude-mem.git`.

Claude-Mem is a persistent memory system for AI agents. The worker runs locally on `http://127.0.0.1:37700`; start it with `npx claude-mem start` and stop with `npx claude-mem stop`.

Available under `.agents/skills/`:
- `mem-search` — search cross-session memory
- `knowledge-agent` — build/query knowledge bases from observations
- `learn-codebase` — read every source file to prime a project
- `make-plan` / `do` — phased planning and execution
- `smart-explore` — token-optimized structural code search
- `oh-my-issues` — cluster and triage GitHub issues
- `pathfinder` — map codebase into feature flowcharts
- `standup` — compare changes across worktrees/branches/PRs
- `timeline-report` / `weekly-digests` — project history narratives
- `babysit` — monitor a PR until merge-ready

Activate the relevant skill when the user asks about memory, planning, code exploration, issue triage, or project history.
