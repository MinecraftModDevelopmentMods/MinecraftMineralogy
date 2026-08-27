package zone.moddev.mc.mineralogy.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.tags.ITag;

import org.junit.Test;

public class CobblestoneTagPolicyTest {
    @Test
    public void retainedRecipeTagSeesAddedAndRemovedElements() {
        Object vanilla = new Object();
        Object mineralogy = new Object();
        ITag<Object> retainedByRecipe = ITag.getTagOf(set(vanilla));

        CobblestoneTagPolicy.replaceElementsInPlace(retainedByRecipe, set(vanilla, mineralogy));
        assertEquals(Arrays.asList(vanilla, mineralogy), retainedByRecipe.getAllElements());
        assertTrue(retainedByRecipe.contains(mineralogy));

        CobblestoneTagPolicy.replaceElementsInPlace(retainedByRecipe, set(vanilla));
        assertEquals(Arrays.asList(vanilla), retainedByRecipe.getAllElements());
        assertFalse(retainedByRecipe.contains(mineralogy));
    }

    private static Set<Object> set(Object... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }
}
