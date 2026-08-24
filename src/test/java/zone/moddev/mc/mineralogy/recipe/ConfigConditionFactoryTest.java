package zone.moddev.mc.mineralogy.recipe;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonSyntaxException;
import org.junit.Test;

import zone.moddev.mc.mineralogy.ContentPolicy;

public class ConfigConditionFactoryTest {
    @Test
    public void everyStableFlagReadsOnlyItsOwnPolicyValue() {
        for (int mask = 0; mask < 16; mask++) {
            ContentPolicy policy = new ContentPolicy(
                    (mask & 1) != 0,
                    (mask & 2) != 0,
                    (mask & 4) != 0,
                    (mask & 8) != 0);

            assertValue(ContentPolicy.ENABLE_DRYWALLS, policy, (mask & 1) != 0);
            assertValue(ContentPolicy.ENABLE_ROCK_SALT_LAMPS, policy, (mask & 2) != 0);
            assertValue(ContentPolicy.ENABLE_MINERAL_DUSTS, policy, (mask & 4) != 0);
            assertValue(ContentPolicy.ENABLE_MINERAL_FERTILIZER, policy, (mask & 8) != 0);
        }
    }

    @Test(expected = JsonSyntaxException.class)
    public void unknownFlagsFailDeterministically() {
        ConfigConditionFactory.enabled("UNKNOWN_FLAG", ContentPolicy.defaults());
    }

    private static void assertValue(String flag, ContentPolicy policy, boolean expected) {
        if (expected) {
            assertTrue(flag, ConfigConditionFactory.enabled(flag, policy));
        } else {
            assertFalse(flag, ConfigConditionFactory.enabled(flag, policy));
        }
    }
}
