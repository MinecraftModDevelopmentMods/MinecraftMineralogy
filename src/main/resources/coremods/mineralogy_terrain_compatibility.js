var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

// Only replace the one base-stone read in each proven dripstone helper. Other
// base-stone consumers, ore hosts and the vanilla tag itself remain unchanged.
function redirectBaseStone(classNode, descriptor) {
    var hooks = 0;
    for (var index = 0; index < classNode.methods.size(); ++index) {
        var method = classNode.methods.get(index);
        if (method.desc !== descriptor) continue;
        for (var instruction = method.instructions.getFirst(); instruction !== null;) {
            var next = instruction.getNext();
            if (instruction.getOpcode() === Opcodes.GETSTATIC
                    && instruction.owner === 'net/minecraft/tags/BlockTags'
                    && instruction.name === ASMAPI.mapField('f_13061_')
                    && instruction.desc === 'Lnet/minecraft/tags/Tag$Named;') {
                method.instructions.set(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        'zone/moddev/mc/mineralogy/compat/VanillaTerrainHooks', 'dripstoneBaseStones',
                        '()Lnet/minecraft/tags/Tag;', false));
                ++hooks;
            }
            instruction = next;
        }
    }
    if (hooks !== 1) throw new Error('Mineralogy dripstone hook drift in ' + classNode.name + ': expected 1, found ' + hooks);
    ASMAPI.log('INFO', 'Mineralogy terrain compatibility: applied exactly one hook to ' + classNode.name);
    return classNode;
}

function initializeCoreMod() {
    return {
        // Forge 37's Gson loot loader does not have loading-condition codecs.
        // Filter only Mineralogy's explicitly guarded, unregistered forms before
        // deserialization; registered forms and every other mod's table are intact.
        'mineralogy_optional_construction_loot': {
            'target': {'type': 'CLASS', 'name': 'net.minecraftforge.common.ForgeHooks'},
            'transformer': function(classNode) {
                var hooks = 0;
                for (var index = 0; index < classNode.methods.size(); ++index) {
                    var method = classNode.methods.get(index);
                    if (method.name !== 'loadLootTable' || method.desc !== '(Lcom/google/gson/Gson;Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonElement;ZLnet/minecraft/world/level/storage/loot/LootTables;)Lnet/minecraft/world/level/storage/loot/LootTable;') continue;
                    var prefix = new InsnList();
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            'zone/moddev/mc/mineralogy/compat/ConstructionLootHooks', 'prepare',
                            '(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonElement;)Lcom/google/gson/JsonElement;', false));
                    prefix.add(new VarInsnNode(Opcodes.ASTORE, 2));
                    method.instructions.insert(prefix);
                    ++hooks;
                }
                if (hooks !== 1) throw new Error('Mineralogy construction loot hook drift: expected 1, found ' + hooks);
                ASMAPI.log('INFO', 'Mineralogy construction compatibility: applied exactly one Forge 37 loot-loading hook');
                return classNode;
            }
        },
        'mineralogy_dripstone_water_pocket': {
            'target': {'type': 'CLASS', 'name': 'net.minecraft.world.level.levelgen.feature.DripstoneClusterFeature'},
            'transformer': function(classNode) {
                return redirectBaseStone(classNode, '(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Z');
            }
        },
        'mineralogy_large_dripstone_boundary': {
            'target': {'type': 'CLASS', 'name': 'net.minecraft.world.level.levelgen.feature.LargeDripstoneFeature$LargeDripstone'},
            'transformer': function(classNode) {
                return redirectBaseStone(classNode, '(Lnet/minecraft/world/level/WorldGenLevel;Ljava/util/Random;Lnet/minecraft/world/level/levelgen/feature/LargeDripstoneFeature$WindOffsetter;)V');
            }
        }
    };
}
