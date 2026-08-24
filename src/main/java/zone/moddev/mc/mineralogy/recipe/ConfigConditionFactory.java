package zone.moddev.mc.mineralogy.recipe;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import zone.moddev.mc.mineralogy.ContentPolicy;
import zone.moddev.mc.mineralogy.MineralogyConfig;

/** Exposes Mineralogy's compatibility-safe content switches to Forge recipe JSON. */
public final class ConfigConditionFactory implements IConditionFactory {
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        final String flag = JsonUtils.getString(json, "flag");
        validate(flag);
        return () -> enabled(flag, MineralogyConfig.contentPolicy());
    }

    static boolean enabled(String flag, ContentPolicy policy) {
        switch (flag) {
            case ContentPolicy.ENABLE_DRYWALLS:
                return policy.drywallsEnabled();
            case ContentPolicy.ENABLE_ROCK_SALT_LAMPS:
                return policy.rockSaltLampsEnabled();
            case ContentPolicy.ENABLE_MINERAL_DUSTS:
                return policy.mineralDustsEnabled();
            case ContentPolicy.ENABLE_MINERAL_FERTILIZER:
                return policy.mineralFertilizerEnabled();
            default:
                throw new JsonSyntaxException("Unknown Mineralogy recipe config flag: " + flag);
        }
    }

    private static void validate(String flag) {
        enabled(flag, ContentPolicy.defaults());
    }
}
