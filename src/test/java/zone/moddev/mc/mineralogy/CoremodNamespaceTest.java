package zone.moddev.mc.mineralogy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CoremodNamespaceTest {
    private static final String COREMOD_RESOURCE = "/coremods/mineralogy_legacy_world_fix.js";
    private static final String CURRENT_HOOK = "zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook";
    private static final String LEGACY_HOOK = "com/mcmoddev/mineralogy/patching/LegacyWorldDataHook";

    @Test
    void coremodTargetsCurrentLegacyWorldDataHookPackage() throws IOException {
        try (InputStream stream = CoremodNamespaceTest.class.getResourceAsStream(COREMOD_RESOURCE)) {
            assertNotNull(stream, "Missing packaged Mineralogy legacy-world coremod");
            String coremod = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            assertFalse(coremod.contains(LEGACY_HOOK), "Coremod still targets the retired package");
            assertEquals(4, countOccurrences(coremod, CURRENT_HOOK));
        }
    }

    private static int countOccurrences(String value, String target) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(target, offset)) >= 0) {
            count++;
            offset += target.length();
        }
        return count;
    }
}
