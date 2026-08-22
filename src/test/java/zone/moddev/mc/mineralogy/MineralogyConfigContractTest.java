package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;
import org.junit.Test;
import org.mockito.invocation.Invocation;

public class MineralogyConfigContractTest {
    @Test
    public void cleanConfigWritesContentOnlyMineralogySixOptions() {
        Configuration config = defaultsConfiguration();

        MineralogyConfig.apply(config, false);

        verify(config).save();
        Set<String> keys = booleanKeys(config);
        assertTrue(keys.contains(ContentPolicy.ENABLE_DRYWALLS));
        assertTrue(keys.contains(ContentPolicy.ENABLE_ROCK_SALT_LAMPS));
        assertTrue(keys.contains(ContentPolicy.ENABLE_MINERAL_DUSTS));
        assertTrue(keys.contains(ContentPolicy.ENABLE_MINERAL_FERTILIZER));
        assertTrue(keys.contains(OreDictionaryPolicy.COBBLESTONE_EQUIVILENT));
        assertTrue(keys.contains(CreativeTabPolicy.GROUP_TABS_BY_TYPE));
        assertTrue(keys.contains("GENERATE_ROCKSLAB"));
        assertFalse(keys.stream().anyMatch(key -> key.toLowerCase().contains("geology")));
        assertFalse(keys.stream().anyMatch(key -> key.toLowerCase().contains("whitelist")));
        assertFalse(keys.stream().anyMatch(key -> key.toLowerCase().contains("_ore")));
    }

    @Test
    public void existingConfigMissingNewKeysIsReadWithoutRewriteAndUsesDefaults() {
        Configuration config = defaultsConfiguration();

        MineralogyConfig.apply(config, true);

        verify(config, never()).save();
        assertTrue(MineralogyConfig.contentPolicy().drywallsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().rockSaltLampsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().mineralDustsEnabled());
        assertTrue(MineralogyConfig.contentPolicy().mineralFertilizerEnabled());
        assertTrue(MineralogyConfig.makeRockCobblestoneEquivilent());
        assertFalse(MineralogyConfig.groupCreativeTabItemsByType());
    }

    private static Configuration defaultsConfiguration() {
        Configuration config = mock(Configuration.class);
        when(config.getBoolean(anyString(), eq("options"), anyBoolean(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        return config;
    }

    private static Set<String> booleanKeys(Configuration config) {
        Set<String> keys = new HashSet<String>();
        for (Invocation invocation : org.mockito.Mockito.mockingDetails(config).getInvocations()) {
            if ("getBoolean".equals(invocation.getMethod().getName())) {
                keys.add((String) invocation.getArgument(0));
            }
        }
        return keys;
    }
}
