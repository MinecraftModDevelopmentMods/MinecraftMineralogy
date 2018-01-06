package com.mcmoddev.mineralogy.patching;

import static com.mcmoddev.mineralogy.Mineralogy.MODID;

import java.util.HashMap;
import java.util.Map;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.init.MineralogyRegistry;
import com.mcmoddev.mineralogy.ioc.MinIoC;
import com.mcmoddev.mineralogy.util.BlockItemPair;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

/**
 * Created by Chris on 5/10/2016.
 */
public class PatchHandler {

	public static final Map<String, Block> MineralogyPatchRegistry = new HashMap<>();

	private static PatchHandler instance = null;

	private PatchHandler() {
		//
	}

	Block saprolite;
	Block pummice; // note the misspelling

	public static PatchHandler getInstance() {
		if (instance == null) {
			instance = new PatchHandler();
		}
		return instance;
	}

	public void init(boolean enabled) {
		if (enabled) {
			saprolite = legacyBlock("saprolite",
					MineralogyRegistry.MineralogyBlockRegistry.get("limestone").PairedBlock.getDefaultState());
			
			Block blockPumice = MinIoC.getInstance().resolve(BlockItemPair.class, "blockPumice", Mineralogy.MODID).PairedBlock;
			
			pummice = legacyBlock("pummice", blockPumice.getDefaultState());

			MineralogyPatchRegistry.put("saprolite", saprolite);
			MineralogyPatchRegistry.put("pummice", pummice);
		}
	}

	private static Block legacyBlock(String name, IBlockState replacement) {
		Block b = new UpdateBlock(replacement);
		b.setUnlocalizedName(MODID + "." + name);
		return b;
	}
}
