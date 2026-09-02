var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function previousOpcode(instruction) {
    var current = instruction.getPrevious();
    while (current !== null && current.getOpcode() < 0) {
        current = current.getPrevious();
    }
    return current;
}

function initializeCoreMod() {
    return {
        'mineralogy_legacy_block_state_tables': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.util.datafix.fixes.BlockStateData'
            },
            'transformer': function(classNode) {
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.desc === '(ILjava/lang/String;[Ljava/lang/String;)V') {
                        method.access = (method.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED))
                                | Opcodes.ACC_PUBLIC;
                    }
                    if (method.name !== '<clinit>') {
                        continue;
                    }
                    for (var instruction = method.instructions.getFirst(); instruction !== null;
                            instruction = instruction.getNext()) {
                        if (instruction.getOpcode() !== Opcodes.ANEWARRAY
                                || instruction.desc !== 'com/mojang/serialization/Dynamic') {
                            continue;
                        }
                        var sizeInstruction = previousOpcode(instruction);
                        if (sizeInstruction !== null && sizeInstruction.getOpcode() === Opcodes.SIPUSH) {
                            if (sizeInstruction.operand === 4096) {
                                method.instructions.set(sizeInstruction, new LdcInsnNode(65536));
                            } else if (sizeInstruction.operand === 256) {
                                method.instructions.set(sizeInstruction, new IntInsnNode(Opcodes.SIPUSH, 4096));
                            }
                        }
                    }
                }
                return classNode;
            }
        },
        'mineralogy_legacy_level_registry': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.common.ForgeHooks'
            },
            'transformer': function(classNode) {
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.desc !== '(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelDirectory;)V') {
                        continue;
                    }
                    var prefix = new InsnList();
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    prefix.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                            'captureLegacyLevelData',
                            '(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelDirectory;)V',
                            false));
                    method.instructions.insert(prefix);
                }
                return classNode;
            }
        },
        'mineralogy_legacy_worldgen_guard': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.server.level.WorldGenRegion'
            },
            'transformer': function(classNode) {
                var ensureCanWriteName = ASMAPI.mapMethod('m_180807_');
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.name !== ensureCanWriteName
                            || method.desc !== '(Lnet/minecraft/core/BlockPos;)Z') {
                        continue;
                    }
                    var allowed = new LabelNode();
                    var prefix = new InsnList();
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    prefix.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                            'shouldBlockWorldgenWrite',
                            '(Lnet/minecraft/core/BlockPos;)Z',
                            false));
                    prefix.add(new JumpInsnNode(Opcodes.IFEQ, allowed));
                    prefix.add(new InsnNode(Opcodes.ICONST_0));
                    prefix.add(new InsnNode(Opcodes.IRETURN));
                    prefix.add(allowed);
                    method.instructions.insert(prefix);
                }
                return classNode;
            }
        },
        'mineralogy_legacy_chunk_data': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.chunk.storage.ChunkStorage'
            },
            'transformer': function(classNode) {
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.desc.indexOf('Ljava/util/function/Supplier;Lnet/minecraft/nbt/CompoundTag;') < 0
                            || !method.desc.endsWith('Lnet/minecraft/nbt/CompoundTag;')) {
                        continue;
                    }

                    var prefix = new InsnList();
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    prefix.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                            'prepareLegacyChunk',
                            '(Lnet/minecraft/nbt/CompoundTag;)V',
                            false));
                    method.instructions.insert(prefix);

                    for (var instruction = method.instructions.getFirst(); instruction !== null;
                            instruction = instruction.getNext()) {
                        if (instruction.getOpcode() === Opcodes.ARETURN) {
                            method.instructions.insertBefore(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                                    'finalizeLegacyChunk',
                                    '(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;',
                                    false));
                        }
                    }
                }
                return classNode;
            }
        }
    };
}
