package zone.moddev.mc.mineralogy.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

/** Exact-loader mining-tag and furnace-state regression probe. */
@Mod(MiningIntegrationProbe.MODID)
public final class MiningIntegrationProbe {
    public static final String MODID = "mineralogyminingprobe";
    private static final String PHASE_PROPERTY = "mineralogy.miningProbe.phase";

    public MiningIntegrationProbe() {
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void serverStarted(ServerStartedEvent event) {
        String phase = System.getProperty(PHASE_PROPERTY, "single");
        Level level = event.getServer().overworld();
        verifyMiningTags();
        verifyFurnace(level, phase);
        writeMarker(phase);
        event.getServer().halt(false);
    }

    private static void verifyMiningTags() {
        List<String> missingPickaxeTags = new ArrayList<String>();
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

        require(mineralogyBlocks == 1136,
                "expected 1136 registered Mineralogy blocks, found " + mineralogyBlocks);
        require(expectedPickaxeBlocks == 1133,
                "expected 1133 pickaxe-mined Mineralogy blocks, found " + expectedPickaxeBlocks);
        require(missingPickaxeTags.isEmpty(),
                "Mineralogy blocks missing minecraft:mineable/pickaxe: " + missingPickaxeTags);
        require(ironToolBlocks == 123,
                "expected 123 iron-tool Mineralogy blocks, found " + ironToolBlocks);
        require(stoneToolBlocks == 329,
                "expected 329 stone-tool Mineralogy blocks, found " + stoneToolBlocks);
        require(requireBlock("basalt").defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL),
                "raw basalt lost its iron-tool tier");
        require(requireBlock("basalt_smooth").defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL),
                "polished basalt lost its iron-tool tier");

        ItemStack diamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        for (String path : new String[] {
                "basalt", "basalt_smooth", "basalt_brick", "basalt_slab", "basalt_furnace"
        }) {
            Block block = requireBlock(path);
            require(diamondPickaxe.isCorrectToolForDrops(block.defaultBlockState()),
                    "diamond pickaxe cannot harvest mineralogy:" + path);
            require(diamondPickaxe.getDestroySpeed(block.defaultBlockState()) > 1.0F,
                    "diamond pickaxe has no mining-speed bonus for mineralogy:" + path);
        }
    }

    private static void verifyFurnace(Level level, String phase) {
        BlockPos pos = new BlockPos(0, level.getMinBuildHeight() + 10, 0);
        Block unlit = requireBlock("basalt_furnace");
        Block lit = requireBlock("lit_basalt_furnace");

        if ("reload".equals(phase)) {
            require(level.getBlockState(pos).is(unlit), "persisted furnace is not unlit");
            BlockEntity persisted = level.getBlockEntity(pos);
            require(persisted instanceof TileEntityRockFurnace,
                    "persisted basalt furnace lost its block entity");
            TileEntityRockFurnace furnace = (TileEntityRockFurnace) persisted;
            require(((BlockEntity) furnace).getBlockState().equals(level.getBlockState(pos)),
                    "persisted furnace has a stale block-entity state");
            requireFuel(furnace,
                    "persisted furnace lost its fuel");
            transition(level, pos, furnace, lit, true, "reload lit");
            transition(level, pos, furnace, unlit, false, "reload unlit");
            level.removeBlock(pos, false);
            return;
        }

        level.setBlockAndUpdate(pos, unlit.defaultBlockState());
        BlockEntity initial = level.getBlockEntity(pos);
        require(initial instanceof TileEntityRockFurnace,
                "basalt furnace did not create its block entity");
        TileEntityRockFurnace furnace = (TileEntityRockFurnace) initial;
        ((Container) furnace).setItem(1, new ItemStack(Items.COAL));
        transition(level, pos, furnace, lit, true, "lit");
        transition(level, pos, furnace, unlit, false, "unlit");
        if (!"first".equals(phase)) level.removeBlock(pos, false);
    }

    private static void transition(Level level, BlockPos pos, TileEntityRockFurnace furnace,
            Block expectedBlock, boolean active, String name) {
        RockFurnace.setState(active, level, pos);
        require(level.getBlockState(pos).is(expectedBlock), name + " transition selected the wrong block");
        require(level.getBlockEntity(pos) == furnace, name + " transition replaced the block entity");
        require(((BlockEntity) furnace).getBlockState().equals(level.getBlockState(pos)),
                name + " transition left a stale block-entity state");
        requireFuel(furnace,
                name + " transition lost the furnace fuel");
    }

    private static void requireFuel(Container furnace, String message) {
        ItemStack fuel = furnace.getItem(1);
        require(fuel.is(Items.COAL) && fuel.getCount() == 1, message);
    }

    private static Block requireBlock(String path) {
        ResourceLocation id = new ResourceLocation("mineralogy", path);
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        require(block != null && block != Blocks.AIR, "missing block " + id);
        return block;
    }

    private static void writeMarker(String phase) {
        String result = "phase=" + phase + "\n"
                + "pickaxe_mining_blocks_verified=1133\n"
                + "iron_tool_blocks_verified=123\n"
                + "stone_tool_blocks_verified=329\n"
                + "diamond_pickaxe_harvest_verified=true\n"
                + "furnace_state_transitions_verified=true\n"
                + "furnace_reload_verified=" + "reload".equals(phase) + "\n";
        try {
            Files.write(Path.of("mining-integration-pass.properties"),
                    result.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write mining integration marker", exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
