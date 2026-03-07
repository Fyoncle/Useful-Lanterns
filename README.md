<div align="center">

# Useful Lanterns

This mod adds a Trinket slot called the "Lantern Slot" that let's you put a lantern on your hip which renders on your character with dynamic lighting and very cool physics.

![Preview](https://i.imgur.com/pgrwmGA.gif)

## Dependencies
</div>

This mod requires [Trinkets](https://modrinth.com/mod/trinkets) to function.

For it to emit light [LambDynamicLights](https://modrinth.com/mod/lambdynamiclights) is required.

For the in-game config screen you need to install [Mod Menu](https://modrinth.com/mod/modmenu) and [Cloth Config](https://modrinth.com/mod/cloth-config). Otherwise you need to manually edit the file `config/usefullanterns.json`.

Currently you can only configure which side the lantern is on. Making the `Flip Lantern` config value to `true` will move it to the right side rather than the left side.

<div align="center">

## Mod Compatibility
</div>

This mod by default works with:
- Vanilla Lanterns
- [Chipped](https://modrinth.com/mod/chipped)
- [Supplementaries Squared](https://modrinth.com/mod/supplementaries-squared)
- [[Let's Do] Meadow](https://modrinth.com/mod/lets-do-meadow)
- [Better Archeology](https://modrinth.com/mod/better-archeology)

But don't worry, you can easily add your own mod compatibility with just a single datapack. By editing the tag `data/trinkets/tags/items/legs/lantern.json` you can make any lantern work with Useful Lanterns!

Example:
```json
{
  "replace": false,
  "values": [
    "myfavoritemod:cute_lantern",
    "myfavoritemod:cool_lantern"
  ]
}
```

The `"replace": false` is really necessary here, since if you overwrite the default tag none of the lanterns this mod supports will work. Yes, not even the vanilla ones.

<div align="center">

## Credits
</div>

The majority of this mod is written with the help of [Claude AI](https://claude.ai/) but the mod logo and the description is entirely made by me. AI is used here as a tool instead of fully relying on it.