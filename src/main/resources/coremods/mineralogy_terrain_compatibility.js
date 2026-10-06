var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

// Forge 36 has no loading-condition codec for block loot. Filter only guarded
// unregistered Mineralogy construction drops before the native Gson loader.
function initializeCoreMod() {
    return {
        'mineralogy_optional_construction_loot': {
            'target': {'type': 'CLASS', 'name': 'net.minecraftforge.common.ForgeHooks'},
            'transformer': function(classNode) {
                var hooks = 0;
                for (var index = 0; index < classNode.methods.size(); ++index) {
                    var method = classNode.methods.get(index);
                    if (method.name !== 'loadLootTable' || method.desc !== '(Lcom/google/gson/Gson;Lnet/minecraft/util/ResourceLocation;Lcom/google/gson/JsonElement;ZLnet/minecraft/loot/LootTableManager;)Lnet/minecraft/loot/LootTable;') continue;
                    var prefix = new InsnList();
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            'zone/moddev/mc/mineralogy/compat/ConstructionLootHooks', 'prepare',
                            '(Lnet/minecraft/util/ResourceLocation;Lcom/google/gson/JsonElement;)Lcom/google/gson/JsonElement;', false));
                    prefix.add(new VarInsnNode(Opcodes.ASTORE, 2));
                    method.instructions.insert(prefix);
                    ++hooks;
                }
                if (hooks !== 1) throw new Error('Mineralogy construction loot hook drift: expected 1, found ' + hooks);
                ASMAPI.log('INFO', 'Mineralogy construction compatibility: applied exactly one Forge 36 loot-loading hook');
                return classNode;
            }
        }
    };
}
