---
name: minecraft-oraxen
description: "Create and configure custom items, blocks, paintings, glyphs, music discs, armor and resource packs with the Oraxen plugin for Paper/Spigot/Folia 1.20.1 through 26.2. Use when the user wants custom textures, models, sounds or item mechanics without writing a custom plugin."
---

# Oraxen - Custom Items, Blocks and More

Reference: `https://github.com/oraxen/oraxen`

Docs: `https://oraxen.mizius.com/`

Oraxen is a **paid plugin**. Obtain a license from SpigotMC or BuiltByBit before using it on a public server. The source repository is public for contributions only.

## When to use

- Custom items with custom textures/models (swords, pickaxes, food, shields, bows, etc.).
- Custom blocks, slabs, stairs, doors, trapdoors, lights, sounds and break/place mechanics.
- Custom paintings, glyphs/emojis, music discs and armor/elytra.
- Automatic resource pack generation and distribution.

## Installation

1. Purchase and download Oraxen from the official marketplace.
2. Place `Oraxen.jar` in `server/data/plugins/`.
3. Restart the server so Oraxen creates `plugins/Oraxen/`.
4. Put custom textures in `plugins/Oraxen/pack/textures/` and models in `plugins/Oraxen/pack/models/`.

## Main config files

- `plugins/Oraxen/items/*.yml` — item definitions.
- `plugins/Oraxen/settings.yml` — global plugin and pack settings.
- `plugins/Oraxen/gestures.yml`, `packs/` — additional features.

## Example item

```yaml
# plugins/Oraxen/items/example.yml
example_sword:
  displayname: "<#FF5555>Ruby Sword"
  material: DIAMOND_SWORD
  Pack:
    generate_model: true
    parent_model: item/handheld
    textures:
      - default/example_sword
  Mechanics:
    durability:
      value: 1561
```

## Example block

```yaml
# plugins/Oraxen/items/blocks.yml
example_block:
  displayname: "<#55FF55>Cool Block"
  material: PAPER
  Mechanics:
    block:
      type: FULL
      light: 15
      block-sounds:
        break-sound: block.wood.break
        place-sound: block.wood.place
      appearance:
        model: blocks/cool-block
```

## Useful commands

- `/oraxen reload all` — reload configs and regenerate the pack.
- `/oraxen reload items` — reload items only.
- `/oraxen inv` — open the item inventory.
- `/oraxen dump` — generate a debug report.

## Key concepts

- `Pack` section — resource pack model/texture configuration.
- `Mechanics` section — block/food/tool/furniture behaviors.
- `Components` section — modern 1.21 component settings (jukebox_playable, painting_variant, etc.).
- The pack is served automatically to players unless configured otherwise.

## Notes

- Oraxen includes built-in ViaVersion support. Test with client versions your server supports.
- For pure resource-pack-only work (no plugin install), prefer `minecraft-resource-pack`.
- If the user wants custom items through a custom plugin instead of a config, still use `minecraft-plugin-dev`.
