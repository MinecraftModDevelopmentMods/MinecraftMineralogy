package zone.moddev.mc.mineralogy.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConstructionLootHooksTest {
    private JsonElement table(String item) {
        return (new JsonParser()).parse("{\"type\":\"minecraft:block\",\"pools\":[{\"entries\":[{\"type\":\"minecraft:item\",\"name\":\"" + item
                + "\"}]}],\"mineralogy:construction_item\":\"" + item + "\"}");
    }
    @Test public void registeredDropsKeepTheExactInputObject() {
        JsonElement data = table("mineralogy:basalt_smooth");
        assertSame(data, ConstructionLootHooks.prepare("mineralogy", "blocks/basalt_smooth", data, item -> true));
    }
    @Test public void disabledDropsEmptyOnlyThePoolsWithoutChangingTheSource() {
        JsonElement data = table("mineralogy:basalt_smooth");
        JsonElement empty = ConstructionLootHooks.prepare("mineralogy", "blocks/basalt_smooth", data, item -> false);
        assertNotSame(data, empty);
        assertEquals(1, data.getAsJsonObject().getAsJsonArray("pools").size());
        assertEquals(0, empty.getAsJsonObject().getAsJsonArray("pools").size());
        assertEquals(data.getAsJsonObject().get("type"), empty.getAsJsonObject().get("type"));
    }
    @Test public void otherNamespacesAndNonBlockTablesRemainUntouched() {
        JsonElement data = table("mineralogy:basalt_smooth");
        assertSame(data, ConstructionLootHooks.prepare("minecraft", "blocks/basalt_smooth", data, item -> false));
        assertSame(data, ConstructionLootHooks.prepare("another_mod", "blocks/basalt_smooth", data, item -> false));
        assertSame(data, ConstructionLootHooks.prepare("mineralogy", "chests/test", data, item -> false));
    }
    @Test public void unguardedTablesRemainUntouched() {
        JsonElement data = table("mineralogy:basalt_smooth");
        data.getAsJsonObject().remove("mineralogy:construction_item");
        assertSame(data, ConstructionLootHooks.prepare("mineralogy", "blocks/test", data, item -> false));
    }
    @Test(expected = IllegalArgumentException.class) public void rawRockCannotBeGuarded() {
        ConstructionLootHooks.prepare("mineralogy", "blocks/basalt", table("mineralogy:basalt"), item -> false);
    }
}
