package cyano.mineralogy.patching;

import cyano.mineralogy.Mineralogy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import static cyano.mineralogy.Mineralogy.MODID;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 5/10/2016.
 */
public class PatchHandler {
	public static final Map<String,Block> MineralogyPatchRegistry = new HashMap<String, Block>();
	
	private static PatchHandler instance = null;
	private PatchHandler() {
		//
	}

	Block saprolite;
	Block pummice; // note the misspelling

	public static PatchHandler getInstance() {
		if(instance == null) {
			instance = new PatchHandler();
		}
		return instance;
	}

	public void init(boolean enabled) {
		if(enabled) {
			saprolite = legacyBlock("saprolite", Mineralogy.MineralogyBlockRegistry.get("limestone").getDefaultState());
			pummice = legacyBlock("pummice", Mineralogy.blockPumice.getDefaultState());
			
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