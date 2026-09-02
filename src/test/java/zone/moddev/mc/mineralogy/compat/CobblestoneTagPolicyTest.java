package zone.moddev.mc.mineralogy.compat;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class CobblestoneTagPolicyTest {
    @Test
    public void rebuiltTagPreservesVanillaAndAddsOrRemovesConfiguredElements() {
        assertEquals(list("minecraft:cobblestone", "mineralogy:basalt", "mineralogy:chert"),
                CobblestoneTagPolicy.rebuildValues(
                        list("minecraft:cobblestone"),
                        set("mineralogy:basalt"), true, set("mineralogy:chert")));
        assertEquals(list("minecraft:cobblestone", "mineralogy:chert"),
                CobblestoneTagPolicy.rebuildValues(
                        list("minecraft:cobblestone", "mineralogy:basalt", "mineralogy:chert"),
                        set("mineralogy:basalt"), false, set("mineralogy:chert")));
    }

    @SafeVarargs
    private static <T> Set<T> set(T... values) {
        Set<T> result = new LinkedHashSet<>();
        for (T value : values) result.add(value);
        return result;
    }

    @SafeVarargs
    private static <T> List<T> list(T... values) {
        List<T> result = new ArrayList<>();
        for (T value : values) result.add(value);
        return result;
    }
}
