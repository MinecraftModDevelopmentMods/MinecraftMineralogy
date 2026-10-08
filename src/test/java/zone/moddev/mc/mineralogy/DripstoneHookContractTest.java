package zone.moddev.mc.mineralogy;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.junit.Test;
import static org.junit.Assert.*;

/** Packaged native controls independently prove both transformed call sites. */
public class DripstoneHookContractTest {
    @Test public void onlyTheTwoProvenNativeChecksHaveExactCountGuards() throws Exception {
        String hook = Files.readString(Paths.get("src/main/resources/coremods/mineralogy_terrain_compatibility.js"), StandardCharsets.UTF_8);
        assertTrue(hook.contains("if (hooks !== 1) throw new Error"));
        assertTrue(hook.contains("DripstoneClusterFeature"));
        assertTrue(hook.contains("LargeDripstoneFeature$LargeDripstone"));
        assertTrue(hook.contains("instruction.owner === 'net/minecraft/tags/BlockTags'"));
        assertTrue(hook.contains("instruction.name === 'BASE_STONE_OVERWORLD'"));
        assertTrue(hook.contains("method.desc !== descriptor"));
        assertEquals(2, hook.split("'type': 'CLASS'", -1).length - 1);
        assertFalse(hook.contains("ore_replaceables"));
        assertFalse(Files.exists(Paths.get("src/main/resources/data/minecraft/tags/block/base_stone_overworld.json")));
        String bridge = Files.readString(Paths.get("src/main/java/zone/moddev/mc/mineralogy/compat/VanillaTerrainHooks.java"));
        assertTrue(bridge.contains("private static final TagKey<Block>"));
        assertTrue(bridge.contains("return DRIPSTONE_BASE_STONES;"));
    }
}
