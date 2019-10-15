[![](https://img.shields.io/badge/Discord-MMD-green.svg?style=flat&logo=Discord)](https://discord.mcmoddev.com)
[![](http://cf.way2muchnoise.eu/full_minecraft-mineralogy_downloads.svg)](http://minecraft.curseforge.com/projects/minecraft-mineralogy)
[![](http://cf.way2muchnoise.eu/versions/Minecraft_minecraft-mineralogy_all.svg)](http://minecraft.curseforge.com/projects/minecraft-mineralogy)
[![Build Status](https://ci.mcmoddev.com/job/Minecraft%20Mineralogy/job/Minecraft%20Mineralogy%201.12/badge/icon)](https://ci.mcmoddev.com/job/Minecraft%20Mineralogy/job/Minecraft%20Mineralogy%201.12/)

# Mineralogy
## MMD's Minecraft Mineralogy Mod
This mod replaces the generic "stone" in Minecraft with real-world rock types.

## Texture Packs
There are three levels of texture resolution available. The default textures are low-resolution (16x16 pixels, same as Minecraft), but I recommend trying the high-resolution (64x64 pixels) textures via the high-res texture pack (available on the Release page).

## Q&A
Q: Why make this mod?
A: Minecraft is a game that involves a lot of mining, yet it takes very little inspiration from actual geology. We made this mod to give Minecraft more of a geology vibe. After all, there's no mineral called "stone" in the geology textbook.

Q: Where's the cobblestone?!
A: Many of the stone types can be used as cobblestone or as stone in crafting recipes. If you want "Stone", smelt gravel. If you want "Cobblestone", craft two blocks of rock with 2 blocks of gravel.

Q: There's too much lag when generating new chunks!
A: Yes, that can happen. Mineralogy puts a lot more computation into world generation, so you don't want to run the server (or play single-player) on a computer with a slow CPU. We tried to improve performance as much as possible, but there's no getting around the fact that the stone type needs to be calculated for every single underground block in the game.

Q: Why do the ores look funny?
A: We re-textured the ores to better match the appearance of the new rock types. You can change them back by making your own texture pack from the default Minecraft resources.

## Notes to Other Modders
This mod replaces the WorldProvider for the Overworld in Minecraft. There can only be one provider per dimension, so this mod is therefore incompatible with other mods that also replace the same WorldProvider.

The stone types are all registered with the OreDictionary as Stone and/or Cobblestone. If you use OreDictionary crafting recipes (ShapelessOreRecipe or ShapedOreRecipe), then there shouldn't be any trouble. However, if you manually specify the Stone or Cobblestone blocks in a standard crafting recipe object, then users of the Mineralogy mod will have to craft Cobblestone (2 gravel + 2 rock blocks) to use your recipe.
