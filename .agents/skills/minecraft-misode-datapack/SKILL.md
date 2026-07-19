---
name: minecraft-misode-datapack
description: "Use the Misode data pack generators (misode.github.io) to create and edit Minecraft datapack JSON files for advancements, loot tables, recipes, predicates, dimensions, worldgen and more. Use when the user wants to generate datapack JSON visually, validate vanilla schemas or quickly prototype datapacks for Minecraft 1.21.x."
---

# Misode Datapack Generators

Reference: `https://github.com/misode/misode.github.io`

Site: `https://misode.github.io/`

The Misode website is a collection of visual generators for Minecraft datapack JSON. It is useful for producing correct schemas without writing JSON by hand.

## When to use

- Creating or editing **advancements**, **loot tables**, **recipes**, **predicates**, **item modifiers**, **damage types**, **dimensions**, **worldgen** files, etc.
- Validating that a datapack JSON matches the vanilla format for 1.21.x.
- Prototyping a datapack before moving files into `server/data/<world>/datapacks/`.

## Workflow

1. Identify the type of file you need (advancement, recipe, loot table, etc.).
2. Open `https://misode.github.io/` and select the matching generator.
3. Fill the form; the generator updates the JSON preview in real time.
4. Set the correct version/pack format for 1.21.x (usually `61` or higher for 1.21.8).
5. Copy the generated JSON.
6. Save it under the datapack path:
   ```
   <datapack>/data/<namespace>/<type>/<name>.json
   ```
7. Run `/reload` on the server or restart to apply.

## Local usage

The repository is a Preact/Vite app. If you want to run it locally:

```bash
cd /tmp/misode-site # or clone https://github.com/misode/misode.github.io.git
npm install
npm run dev
```

Then open `http://localhost:3000`.

## Common generator paths

| Type | URL path |
|------|----------|
| Advancement | `https://misode.github.io/advancement/` |
| Loot table | `https://misode.github.io/loot-table/` |
| Recipe | `https://misode.github.io/recipe/` |
| Predicate | `https://misode.github.io/predicate/` |
| Dimension | `https://misode.github.io/dimension/` |
| Worldgen | `https://misode.github.io/worldgen/` |

## Notes

- The generated JSON is vanilla-only. For modded generators see `https://misode.github.io/predicate/` and the `mcdoc` contribution docs.
- Misode is read-only: it generates files, it does not install them on the server. Move the files manually or via script.
- If the user asks about datapack logic beyond JSON generation, prefer `minecraft-datapack` or `minecraft-commands-scripting`.
