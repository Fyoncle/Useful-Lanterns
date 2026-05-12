<div align="center">

# Useful Lanterns

<a href="https://modrinth.com/modpack/elysium-days" target="_blank" rel="noopener noreferrer">
  <img src="https://raw.githubusercontent.com/Fyoncle/Elysium-Days/main/ed_badge.png" alt="As seen in Elysium Days" width="200">
</a>

<br>
<br>

This mod adds a Trinket slot called the "Lantern Slot" that let's you put a lantern on your hip which renders on your character with dynamic lighting and very cool physics.

![Preview](https://i.imgur.com/pgrwmGA.gif)

## Dependencies

This mod requires [Trinkets](https://modrinth.com/mod/trinkets) to function.

For it to emit light [LambDynamicLights](https://modrinth.com/mod/lambdynamiclights) is required.

For the in-game config screen you need to install [Mod Menu](https://modrinth.com/mod/modmenu) and [Fzzy Config](https://modrinth.com/mod/fzzy-config). Otherwise you need to manually edit the file `config/usefullanterns/config.toml`.

Currently you can configure which side the lantern is on with the `Lantern On Right Side` config value and also the `Scale` value of the lantern.

## Mod Compatibility
</div>

This mod by default works with:
- Vanilla Lanterns
- [Chipped](https://modrinth.com/mod/chipped)
- [Supplementaries Squared](https://modrinth.com/mod/supplementaries-squared)
- [[Let's Do] Meadow](https://modrinth.com/mod/lets-do-meadow)
- [Better Archeology](https://modrinth.com/mod/better-archeology)

<div align="center">

But don't worry, you can easily add your own mod compatibility with just a single datapack. By editing the tag `data/trinkets/tags/items/legs/lantern.json` you can make any lantern work with Useful Lanterns!

</div>

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

<div align="center">

The `"replace": false` is really necessary here, since if you overwrite the default tag none of the lanterns this mod supports will work. Yes, not even the vanilla ones.

## Credits

The renderer of this mod is written with the help of [Claude AI](https://claude.ai/) but the mod logo and the description is entirely made by me, so is the other classes. AI is used as a tool here instead of fully relying on it.

</div>
