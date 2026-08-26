var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function initializeCoreMod() {
    return {
        'mineralogy_legacy_chunk_data': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.chunk.storage.ChunkLoader'
            },
            'transformer': function(classNode) {
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.desc.indexOf('Ljava/util/function/Supplier;Lnet/minecraft/nbt/CompoundNBT;)') < 0
                            || !method.desc.endsWith('Lnet/minecraft/nbt/CompoundNBT;')) {
                        continue;
                    }

                    var prefix = new InsnList();
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    prefix.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                            'prepareLegacyChunk',
                            '(Lnet/minecraft/nbt/CompoundNBT;)V',
                            false));
                    method.instructions.insert(prefix);

                    for (var instruction = method.instructions.getFirst(); instruction !== null;
                            instruction = instruction.getNext()) {
                        if (instruction.getOpcode() === Opcodes.ARETURN) {
                            method.instructions.insertBefore(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                                    'finalizeLegacyChunk',
                                    '(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;',
                                    false));
                        }
                    }
                }
                return classNode;
            }
        }
    };
}
