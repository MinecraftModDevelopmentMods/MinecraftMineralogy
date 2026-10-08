package zone.moddev.mc.mineralogy.compat;

import java.util.function.Predicate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/** Internal adapter for Forge 40's pre-codec loot loader. */
public final class ConstructionLootHooks {
    private ConstructionLootHooks() { }

    /** Only explicitly guarded Mineralogy block tables may be changed. */
    public static JsonElement prepare(ResourceLocation name, JsonElement data) {
        return prepare(name.getNamespace(), name.getPath(), data,
                item -> ForgeRegistries.ITEMS.containsKey(new ResourceLocation(item)));
    }

    static JsonElement prepare(String namespace, String path, JsonElement data,
            Predicate<String> registered) {
        if (!"mineralogy".equals(namespace) || !path.startsWith("blocks/") || !data.isJsonObject()) return data;
        JsonObject table = data.getAsJsonObject();
        JsonElement guard = table.get("mineralogy:construction_item");
        if (guard == null) return data;
        String item = guard.getAsString();
        if (!item.matches("mineralogy:(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)"))
            throw new IllegalArgumentException("Invalid Mineralogy construction loot guard: " + item);
        if (registered.test(item)) return data;
        JsonObject empty = table.deepCopy();
        empty.add("pools", new JsonArray());
        return empty;
    }
}
