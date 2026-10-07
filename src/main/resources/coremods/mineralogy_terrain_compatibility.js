var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

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
                    && instruction.name === 'BASE_STONE_OVERWORLD'
                    && instruction.desc === 'Lnet/minecraft/tags/TagKey;') {
                method.instructions.set(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        'zone/moddev/mc/mineralogy/compat/VanillaTerrainHooks', 'dripstoneBaseStones',
                        '()Lnet/minecraft/tags/TagKey;', false));
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
        'mineralogy_forest_rock_substrate': {
            'target': {'type': 'CLASS', 'name': 'net.minecraft.world.level.levelgen.feature.BlockBlobFeature'},
            'transformer': function(classNode) {
                var hooks = 0;
                for (var index = 0; index < classNode.methods.size(); ++index) {
                    var method = classNode.methods.get(index);
                    if (method.desc !== '(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z') continue;
                    for (var instruction = method.instructions.getFirst(); instruction !== null;) {
                        var next = instruction.getNext();
                        if (instruction.getOpcode() === Opcodes.INVOKESTATIC
                                && instruction.owner === 'net/minecraft/world/level/levelgen/feature/BlockBlobFeature'
                                && instruction.name === 'isStone'
                                && instruction.desc === '(Lnet/minecraft/world/level/block/state/BlockState;)Z') {
                            method.instructions.set(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    'zone/moddev/mc/mineralogy/compat/VanillaTerrainHooks', 'forestRockSubstrate',
                                    instruction.desc, false));
                            ++hooks;
                        }
                        instruction = next;
                    }
                }
                if (hooks !== 1) throw new Error('Mineralogy forest-rock hook drift: expected 1, found ' + hooks);
                ASMAPI.log('INFO', 'Mineralogy forest-rock compatibility: applied exactly one hook');
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
                return redirectBaseStone(classNode, '(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/feature/LargeDripstoneFeature$WindOffsetter;)V');
            }
        }
    };
}
