package zone.moddev.mc.mineralogy.patching;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LegacyTileEntityIdNormalizerTest {
	@Test
	public void mapsEveryLegacyVanillaTileSeenByPreFlatteningWorlds() {
		assertEquals("minecraft:chest", LegacyTileEntityIdNormalizer.normalize("Chest"));
		assertEquals("minecraft:furnace", LegacyTileEntityIdNormalizer.normalize("Furnace"));
		assertEquals("minecraft:enchanting_table", LegacyTileEntityIdNormalizer.normalize("EnchantTable"));
		assertEquals("minecraft:flower_pot", LegacyTileEntityIdNormalizer.normalize("FlowerPot"));
		assertEquals("minecraft:hopper", LegacyTileEntityIdNormalizer.normalize("Hopper"));
		assertEquals("minecraft:mob_spawner", LegacyTileEntityIdNormalizer.normalize("MobSpawner"));
		assertEquals("minecraft:jukebox", LegacyTileEntityIdNormalizer.normalize("RecordPlayer"));
		assertEquals("minecraft:brewing_stand", LegacyTileEntityIdNormalizer.normalize("Cauldron"));
	}

	@Test
	public void preservesAlreadyValidNamespacedModTiles() {
		assertEquals("mineralogy:rock_furnace",
				LegacyTileEntityIdNormalizer.normalize("mineralogy:rock_furnace"));
		assertEquals("poweradvantage.item_conveyor",
				LegacyTileEntityIdNormalizer.normalize("poweradvantage.item_conveyor"));
	}

	@Test
	public void makesUnknownOldModIdentifiersParseableWithoutDiscardingTheirNbt() {
		assertEquals("minecraft:pamapiary", LegacyTileEntityIdNormalizer.normalize("PamApiary"));
		assertEquals("old_mod:odd_tile", LegacyTileEntityIdNormalizer.normalize("Old Mod:Odd Tile"));
	}
}
