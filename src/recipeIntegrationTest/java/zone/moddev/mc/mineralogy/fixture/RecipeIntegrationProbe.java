package zone.moddev.mc.mineralogy.fixture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;

/** Exact-loader probe for generated recipe and advancement data. */
@Mod(RecipeIntegrationProbe.MODID)
public final class RecipeIntegrationProbe {
    public static final String MODID = "mineralogyrecipeprobe";

    private static final String[] RECIPE_NAMES = {
            "furnace", "brewing_stand", "lever", "piston", "dispenser", "dropper",
            "observer", "mossy_cobblestone_from_vine",
            "mossy_cobblestone_from_moss_block", "andesite", "diorite", "stone_axe",
            "stone_hoe", "stone_pickaxe", "stone_shovel", "stone_sword",
            "coast_armor_trim_smithing_template", "sentry_armor_trim_smithing_template",
            "vex_armor_trim_smithing_template"
    };

    private static final String[] LEGACY_ROCKS = {
            "andesite", "basalt", "diorite", "granite", "rhyolite", "pegmatite",
            "diabase", "gabbro", "peridotite", "basaltic_glass", "scoria", "tuff",
            "shale", "conglomerate", "dolomite", "limestone", "siltstone", "marble",
            "slate", "schist", "gneiss", "phyllite", "amphibolite", "hornfels",
            "quartzite", "novaculite", "rock_salt"
    };

    public RecipeIntegrationProbe() {
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void serverStarted(ServerStartedEvent event) {
        boolean enabled = Boolean.parseBoolean(System.getProperty(
                "mineralogy.recipeProbe.equivalence", "true"));
        String phase = System.getProperty("mineralogy.recipeProbe.phase", "single");
        Level level = event.getServer().overworld();
        Item basalt = requireItem("mineralogy", "basalt");

        verifyMiningTags();
        for (String name : RECIPE_NAMES) {
            CraftingRecipe recipe = requireCraftingRecipe(level, name);
            if (enabled) {
                for (String rockName : LEGACY_ROCKS) {
                    Item rock = requireItem("mineralogy", rockName);
                    require(recipe.matches(inventory(name, rock), level),
                            name + " rejected mineralogy:" + rockName);
                }
                for (Item nativeRock : new Item[] { Blocks.BASALT.asItem(), Blocks.TUFF.asItem(),
                        Blocks.ANDESITE.asItem(), Blocks.DIORITE.asItem(),
                        Blocks.GRANITE.asItem() }) {
                    require(recipe.matches(inventory(name, nativeRock), level),
                            name + " rejected native " + ForgeRegistries.ITEMS.getKey(nativeRock));
                }
            } else {
                require(!recipe.matches(inventory(name, basalt), level),
                        name + " accepted ordinary Mineralogy basalt while disabled");
                require(recipe.matches(inventory(name, Blocks.COBBLESTONE.asItem()), level),
                        name + " rejected vanilla cobblestone while disabled");
                if (!isTrimTemplateRecipe(name)) {
                    require(recipe.matches(inventory(name, requireItem("mineralogy", "chert")), level),
                            name + " rejected unconditional chert");
                    require(recipe.matches(inventory(name, requireItem("mineralogy", "pumice")), level),
                            name + " rejected unconditional pumice");
                }
            }

            ItemStack result = recipe.assemble(inventory(name,
                    enabled ? basalt : Blocks.COBBLESTONE.asItem()), level.registryAccess());
            require(expectedOutput(name).equals(ForgeRegistries.ITEMS.getKey(result.getItem())),
                    name + " produced " + ForgeRegistries.ITEMS.getKey(result.getItem()));
            require(result.getCount() == (isTrimTemplateRecipe(name) ? 2 : expectedCount(name)),
                    name + " produced count " + result.getCount());
        }

        verifySlabRoutes(level);
        verifyFurnaceStateTransitions(level, phase);
        verifyAdvancements(event);
        writeMarker(enabled, phase);
        event.getServer().halt(false);
    }

    private static void verifyMiningTags() {
        List<String> missingPickaxeTags = new ArrayList<>();
        int mineralogyBlocks = 0;
        int expectedPickaxeBlocks = 0;
        int ironToolBlocks = 0;
        int stoneToolBlocks = 0;
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id == null || !"mineralogy".equals(id.getNamespace())) continue;
            mineralogyBlocks++;
            String path = id.getPath();
            if ("crude_oil".equals(path) || "rocksaltlamp".equals(path)
                    || "rocksaltstreetlamp".equals(path)) continue;
            expectedPickaxeBlocks++;
            if (!block.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                missingPickaxeTags.add(id.toString());
            }
            if (block.defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL)) ironToolBlocks++;
            if (block.defaultBlockState().is(BlockTags.NEEDS_STONE_TOOL)) stoneToolBlocks++;
        }
        require(mineralogyBlocks == 1136, "expected 1136 registered Mineralogy blocks, found " + mineralogyBlocks);
        require(expectedPickaxeBlocks == 1133, "expected 1133 pickaxe-mined Mineralogy blocks, found " + expectedPickaxeBlocks);
        require(missingPickaxeTags.isEmpty(), "Mineralogy blocks missing minecraft:mineable/pickaxe: " + missingPickaxeTags);
        require(ironToolBlocks == 123, "expected 123 iron-tool Mineralogy blocks, found " + ironToolBlocks);
        require(stoneToolBlocks == 329, "expected 329 stone-tool Mineralogy blocks, found " + stoneToolBlocks);
        require(requireBlock("mineralogy", "basalt").defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL),
                "raw basalt lost its iron-tool tier");
        require(requireBlock("mineralogy", "basalt_smooth").defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL),
                "polished basalt lost its iron-tool tier");
        ItemStack diamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        for (String basalt : new String[] { "basalt", "basalt_smooth", "basalt_brick",
                "basalt_slab", "basalt_furnace" }) {
            Block block = requireBlock("mineralogy", basalt);
            require(diamondPickaxe.isCorrectToolForDrops(block.defaultBlockState()),
                    "diamond pickaxe cannot harvest mineralogy:" + basalt);
            require(diamondPickaxe.getDestroySpeed(block.defaultBlockState()) > 1.0F,
                    "diamond pickaxe has no mining-speed bonus for mineralogy:" + basalt);
        }
    }

    private static void verifyFurnaceStateTransitions(Level level, String phase) {
        BlockPos pos = new BlockPos(0, level.getMinBuildHeight() + 10, 0);
        Block unlit = requireBlock("mineralogy", "basalt_furnace");
        Block lit = requireBlock("mineralogy", "lit_basalt_furnace");

        if ("reload".equals(phase)) {
            require(level.getBlockState(pos).is(unlit), "persisted furnace is not unlit");
            BlockEntity persisted = level.getBlockEntity(pos);
            require(persisted instanceof TileEntityRockFurnace,
                    "persisted basalt furnace lost its block entity");
            TileEntityRockFurnace furnace = (TileEntityRockFurnace) persisted;
            require(furnace.getBlockState().equals(level.getBlockState(pos)),
                    "persisted furnace has a stale block-entity state");
            require(furnace.getItem(1).is(Items.COAL) && furnace.getItem(1).getCount() == 1,
                    "persisted furnace lost its fuel");
            RockFurnace.setState(true, level, pos);
            assertFurnaceState(level, pos, lit, furnace, "reload lit");
            RockFurnace.setState(false, level, pos);
            assertFurnaceState(level, pos, unlit, furnace, "reload unlit");
            level.removeBlock(pos, false);
            return;
        }

        level.setBlockAndUpdate(pos, unlit.defaultBlockState());
        try {
            BlockEntity initial = level.getBlockEntity(pos);
            require(initial instanceof TileEntityRockFurnace, "basalt furnace did not create its block entity");
            TileEntityRockFurnace furnace = (TileEntityRockFurnace) initial;
            furnace.setItem(1, new ItemStack(Items.COAL));
            RockFurnace.setState(true, level, pos);
            assertFurnaceState(level, pos, lit, furnace, "lit");
            require(furnace.getItem(1).is(Items.COAL) && furnace.getItem(1).getCount() == 1,
                    "lit transition lost the furnace fuel");
            RockFurnace.setState(false, level, pos);
            assertFurnaceState(level, pos, unlit, furnace, "unlit");
            require(furnace.getItem(1).is(Items.COAL) && furnace.getItem(1).getCount() == 1,
                    "unlit transition lost the furnace fuel");
        } finally {
            if (!"first".equals(phase)) level.removeBlock(pos, false);
        }
    }

    private static void assertFurnaceState(Level level, BlockPos pos, Block expectedBlock,
            TileEntityRockFurnace expectedEntity, String transition) {
        require(level.getBlockState(pos).is(expectedBlock), transition + " transition selected the wrong furnace block");
        require(level.getBlockEntity(pos) == expectedEntity, transition + " transition replaced the furnace block entity");
        require(expectedEntity.getBlockState().equals(level.getBlockState(pos)),
                transition + " transition left a stale block-entity state");
    }

    private static void verifySlabRoutes(Level level) {
        for (String family : new String[] { "andesite", "diorite", "granite" }) {
            Item nativeRock = requireItem("minecraft", family);
            CraftingRecipe slab = requireCraftingRecipe(level, family + "_slab");
            CraftingInput slabInput = shaped(new String[] { "###" }, Map.of('#', nativeRock));
            require(slab.matches(slabInput, level), family + " slab override does not match");
            require(ResourceLocation.fromNamespaceAndPath("mineralogy", family + "_slab").equals(
                    ForgeRegistries.ITEMS.getKey(slab.assemble(slabInput, level.registryAccess()).getItem())),
                    family + " slab override did not produce Mineralogy's slab");

            assertBridge(level, family + "_slab_to_vanilla",
                    requireItem("mineralogy", family + "_slab"),
                    requireItem("minecraft", family + "_slab"));
            assertBridge(level, family + "_slab_from_vanilla",
                    requireItem("minecraft", family + "_slab"),
                    requireItem("mineralogy", family + "_slab"));
            assertBridge(level, family + "_smooth_slab_to_vanilla",
                    requireItem("mineralogy", family + "_smooth_slab"),
                    requireItem("minecraft", "polished_" + family + "_slab"));
            assertBridge(level, family + "_smooth_slab_from_vanilla",
                    requireItem("minecraft", "polished_" + family + "_slab"),
                    requireItem("mineralogy", family + "_smooth_slab"));

            assertStonecutting(level, family + "_slab_from_" + family + "_stonecutting",
                    nativeRock, requireItem("mineralogy", family + "_slab"));
            assertStonecutting(level, "polished_" + family + "_slab_from_" + family
                    + "_stonecutting", nativeRock,
                    requireItem("mineralogy", family + "_smooth_slab"));
            assertStonecutting(level, "polished_" + family + "_slab_from_polished_"
                    + family + "_stonecutting", requireItem("minecraft", "polished_" + family),
                    requireItem("mineralogy", family + "_smooth_slab"));
        }

        verifyTuffSlabRoutes(level);
    }

    private static void verifyTuffSlabRoutes(Level level) {
        String[][] forms = {
                { "tuff", "tuff_slab", "tuff_slab" },
                { "polished_tuff", "polished_tuff_slab", "tuff_smooth_slab" },
                { "tuff_bricks", "tuff_brick_slab", "tuff_brick_slab" }
        };
        for (String[] form : forms) {
            Item source = requireItem("minecraft", form[0]);
            Item nativeSlab = requireItem("minecraft", form[1]);
            Item mineralogySlab = requireItem("mineralogy", form[2]);
            CraftingRecipe slab = requireCraftingRecipe(level, form[1]);
            CraftingInput input = shaped(new String[] { "###" }, Map.of('#', source));
            require(slab.matches(input, level), form[1] + " override does not match");
            ItemStack result = slab.assemble(input, level.registryAccess());
            require(result.getItem() == mineralogySlab && result.getCount() == 6,
                    form[1] + " override did not produce six matching Mineralogy slabs");

            String mineralogyName = form[2];
            assertBridge(level, mineralogyName + "_to_vanilla", mineralogySlab, nativeSlab);
            assertBridge(level, mineralogyName + "_from_vanilla", nativeSlab, mineralogySlab);
        }

        assertStonecutting(level, "tuff_slab_from_tuff_stonecutting",
                Blocks.TUFF.asItem(), requireItem("mineralogy", "tuff_slab"));
        assertStonecutting(level, "polished_tuff_slab_from_tuff_stonecutting",
                Blocks.TUFF.asItem(), requireItem("mineralogy", "tuff_smooth_slab"));
        assertStonecutting(level, "polished_tuff_slab_from_polished_tuff_stonecutting",
                Blocks.POLISHED_TUFF.asItem(), requireItem("mineralogy", "tuff_smooth_slab"));
        assertStonecutting(level, "tuff_brick_slab_from_tuff_stonecutting",
                Blocks.TUFF.asItem(), requireItem("mineralogy", "tuff_brick_slab"));
        assertStonecutting(level, "tuff_brick_slab_from_polished_tuff_stonecutting",
                Blocks.POLISHED_TUFF.asItem(), requireItem("mineralogy", "tuff_brick_slab"));
        assertStonecutting(level, "tuff_brick_slab_from_tuff_bricks_stonecutting",
                Blocks.TUFF_BRICKS.asItem(), requireItem("mineralogy", "tuff_brick_slab"));

        assertNativeTuffCrafting(level);
        assertNativeTuffStonecutting(level);
    }

    private static void assertNativeTuffCrafting(Level level) {
        assertCrafting(level, "polished_tuff", shapeless(Blocks.TUFF.asItem(), Blocks.SAND.asItem()),
                Blocks.POLISHED_TUFF.asItem(), 1);
        assertCrafting(level, "tuff_stairs", shaped(new String[] { "#  ", "## ", "###" },
                Map.of('#', Blocks.TUFF.asItem())), Blocks.TUFF_STAIRS.asItem(), 4);
        assertCrafting(level, "tuff_wall", shaped(new String[] { "###", "###" },
                Map.of('#', Blocks.TUFF.asItem())), Blocks.TUFF_WALL.asItem(), 6);
        assertCrafting(level, "polished_tuff_stairs", shaped(new String[] { "#  ", "## ", "###" },
                Map.of('#', Blocks.POLISHED_TUFF.asItem())), Blocks.POLISHED_TUFF_STAIRS.asItem(), 4);
        assertCrafting(level, "polished_tuff_wall", shaped(new String[] { "###", "###" },
                Map.of('#', Blocks.POLISHED_TUFF.asItem())), Blocks.POLISHED_TUFF_WALL.asItem(), 6);
        assertCrafting(level, "tuff_bricks", shaped(new String[] { "##", "##" },
                Map.of('#', Blocks.POLISHED_TUFF.asItem())), Blocks.TUFF_BRICKS.asItem(), 4);
        assertCrafting(level, "tuff_brick_stairs", shaped(new String[] { "#  ", "## ", "###" },
                Map.of('#', Blocks.TUFF_BRICKS.asItem())), Blocks.TUFF_BRICK_STAIRS.asItem(), 4);
        assertCrafting(level, "tuff_brick_wall", shaped(new String[] { "###", "###" },
                Map.of('#', Blocks.TUFF_BRICKS.asItem())), Blocks.TUFF_BRICK_WALL.asItem(), 6);
        assertCrafting(level, "chiseled_tuff", shaped(new String[] { "#", "#" },
                Map.of('#', Blocks.TUFF_SLAB.asItem())), Blocks.CHISELED_TUFF.asItem(), 1);
        assertCrafting(level, "chiseled_tuff_bricks", shaped(new String[] { "#", "#" },
                Map.of('#', Blocks.TUFF_BRICK_SLAB.asItem())), Blocks.CHISELED_TUFF_BRICKS.asItem(), 1);
    }

    private static void assertNativeTuffStonecutting(Level level) {
        Object[][] routes = {
                { "chiseled_tuff_from_tuff_stonecutting", Blocks.TUFF, Blocks.CHISELED_TUFF },
                { "chiseled_tuff_bricks_from_tuff_stonecutting", Blocks.TUFF, Blocks.CHISELED_TUFF_BRICKS },
                { "chiseled_tuff_bricks_from_polished_tuff_stonecutting", Blocks.POLISHED_TUFF, Blocks.CHISELED_TUFF_BRICKS },
                { "chiseled_tuff_bricks_from_tuff_bricks_stonecutting", Blocks.TUFF_BRICKS, Blocks.CHISELED_TUFF_BRICKS },
                { "polished_tuff_from_tuff_stonecutting", Blocks.TUFF, Blocks.POLISHED_TUFF },
                { "tuff_stairs_from_tuff_stonecutting", Blocks.TUFF, Blocks.TUFF_STAIRS },
                { "tuff_wall_from_tuff_stonecutting", Blocks.TUFF, Blocks.TUFF_WALL },
                { "polished_tuff_stairs_from_tuff_stonecutting", Blocks.TUFF, Blocks.POLISHED_TUFF_STAIRS },
                { "polished_tuff_stairs_from_polished_tuff_stonecutting", Blocks.POLISHED_TUFF, Blocks.POLISHED_TUFF_STAIRS },
                { "polished_tuff_wall_from_tuff_stonecutting", Blocks.TUFF, Blocks.POLISHED_TUFF_WALL },
                { "polished_tuff_wall_from_polished_tuff_stonecutting", Blocks.POLISHED_TUFF, Blocks.POLISHED_TUFF_WALL },
                { "tuff_bricks_from_tuff_stonecutting", Blocks.TUFF, Blocks.TUFF_BRICKS },
                { "tuff_bricks_from_polished_tuff_stonecutting", Blocks.POLISHED_TUFF, Blocks.TUFF_BRICKS },
                { "tuff_brick_stairs_from_tuff_stonecutting", Blocks.TUFF, Blocks.TUFF_BRICK_STAIRS },
                { "tuff_brick_stairs_from_polished_tuff_stonecutting", Blocks.POLISHED_TUFF, Blocks.TUFF_BRICK_STAIRS },
                { "tuff_brick_stairs_from_tuff_bricks_stonecutting", Blocks.TUFF_BRICKS, Blocks.TUFF_BRICK_STAIRS },
                { "tuff_brick_wall_from_tuff_stonecutting", Blocks.TUFF, Blocks.TUFF_BRICK_WALL },
                { "tuff_brick_wall_from_polished_tuff_stonecutting", Blocks.POLISHED_TUFF, Blocks.TUFF_BRICK_WALL },
                { "tuff_brick_wall_from_tuff_bricks_stonecutting", Blocks.TUFF_BRICKS, Blocks.TUFF_BRICK_WALL }
        };
        for (Object[] route : routes) {
            assertStonecutting(level, (String) route[0],
                    ((net.minecraft.world.level.block.Block) route[1]).asItem(),
                    ((net.minecraft.world.level.block.Block) route[2]).asItem(), 1);
        }
    }

    private static void assertStonecutting(Level level, String name, Item source, Item expected) {
        assertStonecutting(level, name, source, expected, 2);
    }

    private static void assertStonecutting(Level level, String name, Item source,
            Item expected, int expectedCount) {
        RecipeHolder<?> holder = level.getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath("minecraft", name)).orElseThrow(() ->
                        new IllegalStateException("Missing loaded stonecutting recipe minecraft:" + name));
        require(holder.value() instanceof StonecutterRecipe,
                "minecraft:" + name + " is not a stonecutting recipe");
        StonecutterRecipe recipe = (StonecutterRecipe) holder.value();
        SingleRecipeInput input = new SingleRecipeInput(new ItemStack(source));
        require(recipe.matches(input, level), name + " does not match its native source");
        ItemStack output = recipe.assemble(input, level.registryAccess());
        require(output.getItem() == expected && output.getCount() == expectedCount,
                name + " produced the wrong stonecutting result");
    }

    private static void assertCrafting(Level level, String name, CraftingInput input,
            Item expected, int expectedCount) {
        CraftingRecipe recipe = requireCraftingRecipe(level, name);
        require(recipe.matches(input, level), name + " does not match its native inputs");
        ItemStack output = recipe.assemble(input, level.registryAccess());
        require(output.getItem() == expected && output.getCount() == expectedCount,
                name + " produced the wrong native result");
    }

    private static void assertBridge(Level level, String name, Item source, Item expected) {
        CraftingRecipe recipe = requireCraftingRecipe(level, name, "mineralogy");
        CraftingInput input = shapeless(source);
        require(recipe.matches(input, level), name + " does not match its exact source");
        ItemStack output = recipe.assemble(input, level.registryAccess());
        require(output.getItem() == expected && output.getCount() == 1,
                name + " did not produce its exact one-for-one result");
    }

    private static void verifyAdvancements(ServerStartedEvent event) {
        for (String name : RECIPE_NAMES) {
            if (isTrimTemplateRecipe(name)) continue;
            String category = advancementCategory(name);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "recipes/" + category + "/" + name);
            require(event.getServer().getAdvancements().get(id) != null,
                    "Missing loaded advancement " + id);
        }
        for (String id : new String[] { "basalt_slab", "basalt_smooth_stairs",
                "basalt_furnace", "basalt_relief_blank", "basalt_relief_pickaxe" }) {
            ResourceLocation advancement = ResourceLocation.fromNamespaceAndPath(
                    "mineralogy", "recipes/" + id);
            require(event.getServer().getAdvancements().get(advancement) != null,
                    "Missing progressive advancement " + advancement);
        }
    }

    private static CraftingRecipe requireCraftingRecipe(Level level, String name) {
        return requireCraftingRecipe(level, name, "minecraft");
    }

    private static CraftingRecipe requireCraftingRecipe(Level level, String name, String namespace) {
        RecipeHolder<?> holder = level.getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(namespace, name)).orElseThrow(() ->
                        new IllegalStateException("Missing loaded recipe " + namespace + ':' + name));
        require(holder.value() instanceof CraftingRecipe,
                namespace + ':' + name + " is not a crafting recipe");
        return (CraftingRecipe) holder.value();
    }

    private static CraftingInput inventory(String name, Item rock) {
        if ("mossy_cobblestone_from_vine".equals(name)) return shapeless(rock, Blocks.VINE.asItem());
        if ("mossy_cobblestone_from_moss_block".equals(name)) {
            return shapeless(rock, Blocks.MOSS_BLOCK.asItem());
        }
        if ("andesite".equals(name)) return shapeless(Blocks.DIORITE.asItem(), rock);
        if (isTrimTemplateRecipe(name)) {
            Item template = "coast_armor_trim_smithing_template".equals(name)
                    ? Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE
                    : "sentry_armor_trim_smithing_template".equals(name)
                            ? Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE
                            : Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE;
            return shaped(new String[] { "#S#", "#C#", "###" },
                    Map.of('#', Items.DIAMOND, 'S', template, 'C', rock));
        }

        Map<Character, Item> ingredients = new LinkedHashMap<>();
        ingredients.put('#', rock);
        String[] pattern;
        switch (name) {
            case "furnace" -> pattern = new String[] { "###", "# #", "###" };
            case "brewing_stand" -> {
                pattern = new String[] { " B ", "###" };
                ingredients.put('B', Items.BLAZE_ROD);
            }
            case "lever" -> {
                pattern = new String[] { "X", "#" };
                ingredients.put('X', Items.STICK);
            }
            case "piston" -> {
                pattern = new String[] { "TTT", "#X#", "#R#" };
                ingredients.put('T', Blocks.OAK_PLANKS.asItem());
                ingredients.put('X', Items.IRON_INGOT);
                ingredients.put('R', Items.REDSTONE);
            }
            case "dispenser" -> {
                pattern = new String[] { "###", "#X#", "#R#" };
                ingredients.put('X', Items.BOW);
                ingredients.put('R', Items.REDSTONE);
            }
            case "dropper" -> {
                pattern = new String[] { "###", "# #", "#R#" };
                ingredients.put('R', Items.REDSTONE);
            }
            case "observer" -> {
                pattern = new String[] { "###", "RRQ", "###" };
                ingredients.put('R', Items.REDSTONE);
                ingredients.put('Q', Items.QUARTZ);
            }
            case "diorite" -> {
                pattern = new String[] { "CQ", "QC" };
                ingredients.put('C', rock);
                ingredients.put('Q', Items.QUARTZ);
            }
            case "stone_axe" -> {
                pattern = new String[] { "XX", "X#", " #" };
                ingredients.put('X', rock);
                ingredients.put('#', Items.STICK);
            }
            case "stone_hoe" -> {
                pattern = new String[] { "XX", " #", " #" };
                ingredients.put('X', rock);
                ingredients.put('#', Items.STICK);
            }
            case "stone_pickaxe" -> {
                pattern = new String[] { "XXX", " # ", " # " };
                ingredients.put('X', rock);
                ingredients.put('#', Items.STICK);
            }
            case "stone_shovel" -> {
                pattern = new String[] { "X", "#", "#" };
                ingredients.put('X', rock);
                ingredients.put('#', Items.STICK);
            }
            case "stone_sword" -> {
                pattern = new String[] { "X", "X", "#" };
                ingredients.put('X', rock);
                ingredients.put('#', Items.STICK);
            }
            default -> throw new IllegalArgumentException(name);
        }
        return shaped(pattern, ingredients);
    }

    private static CraftingInput shaped(String[] pattern, Map<Character, Item> ingredients) {
        List<ItemStack> inventory = emptyGrid();
        for (int row = 0; row < pattern.length; row++) {
            for (int column = 0; column < pattern[row].length(); column++) {
                Item item = ingredients.get(pattern[row].charAt(column));
                if (item != null) inventory.set(row * 3 + column, new ItemStack(item));
            }
        }
        return CraftingInput.of(3, 3, inventory);
    }

    private static CraftingInput shapeless(Item... items) {
        List<ItemStack> inventory = emptyGrid();
        for (int index = 0; index < items.length; index++) {
            inventory.set(index, new ItemStack(items[index]));
        }
        return CraftingInput.of(3, 3, inventory);
    }

    private static List<ItemStack> emptyGrid() {
        List<ItemStack> result = new ArrayList<>(9);
        for (int index = 0; index < 9; index++) result.add(ItemStack.EMPTY);
        return result;
    }

    private static Item requireItem(String namespace, String path) {
        Item result = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (result == null) throw new IllegalStateException("Missing item " + namespace + ':' + path);
        return result;
    }

    private static Block requireBlock(String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        require(block != null && block != Blocks.AIR, "missing block " + id);
        return block;
    }

    private static ResourceLocation expectedOutput(String name) {
        String path = name.startsWith("mossy_cobblestone_from_") ? "mossy_cobblestone" : name;
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    private static int expectedCount(String name) {
        return "andesite".equals(name) || "diorite".equals(name) ? 2 : 1;
    }

    private static boolean isTrimTemplateRecipe(String name) {
        return name.endsWith("_armor_trim_smithing_template");
    }

    private static String advancementCategory(String name) {
        if ("furnace".equals(name)) return "decorations";
        if ("brewing_stand".equals(name)) return "brewing";
        if (name.startsWith("stone_")) return "stone_sword".equals(name) ? "combat" : "tools";
        if ("andesite".equals(name) || "diorite".equals(name)
                || name.startsWith("mossy_cobblestone")) return "building_blocks";
        return "redstone";
    }

    private static void writeMarker(boolean enabled, String phase) {
        String result = "forge_recipe_manager_loaded=true\n"
                + "equivalence_enabled=" + enabled + "\n"
                + "phase=" + phase + "\n"
                + "covered_vanilla_recipes=19\n"
                + "legacy_rock_families=27\n"
                + "native_rock_aliases=5\n"
                + "pickaxe_mining_blocks_verified=1133\n"
                + "iron_tool_blocks_verified=123\n"
                + "stone_tool_blocks_verified=329\n"
                + "diamond_pickaxe_harvest_verified=true\n"
                + "furnace_state_transitions_verified=true\n"
                + "furnace_reload_verified=" + "reload".equals(phase) + "\n"
                + "shared_stonecutting_routes=15\n"
                + "slab_bridges=18\n"
                + "progressive_advancements_loaded=true\n";
        try {
            Files.writeString(Path.of("recipe-integration-pass.properties"), result,
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write recipe integration marker", exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
