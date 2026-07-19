var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function initializeCoreMod() {
    return {
        'mineralogy_legacy_world_info': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.storage.SaveFormatOld'
            },
            'transformer': function(classNode) {
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    var forgeLoader = method.desc ===
                            '(Ljava/io/File;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/world/storage/SaveHandler;)Lnet/minecraft/world/storage/WorldInfo;';
                    if (!forgeLoader) {
                        continue;
                    }

                    var prefix = new InsnList();
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    prefix.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'com/mcmoddev/mineralogy/patching/LegacyWorldDataHook',
                            'prepareLegacyWorld',
                            '(Ljava/io/File;)V',
                            false));
                    method.instructions.insert(prefix);
                }
                return classNode;
            }
        },
        'mineralogy_legacy_chunk_status': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.chunk.storage.AnvilChunkLoader'
            },
            'transformer': function(classNode) {
                var targetDescriptor =
                        '(Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/world/storage/WorldSavedDataStorage;II)Lnet/minecraft/nbt/NBTTagCompound;';
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.desc !== targetDescriptor) {
                        continue;
                    }

                    for (var instruction = method.instructions.getFirst(); instruction !== null;
                            instruction = instruction.getNext()) {
                        var previous = instruction.getPrevious();
                        if (instruction.getOpcode() === Opcodes.ASTORE
                                && previous !== null
                                && previous.getOpcode() === Opcodes.INVOKESTATIC
                                && previous.owner === 'net/minecraft/nbt/CompressedStreamTools'
                                && previous.name === 'read') {
                            var prepare = new InsnList();
                            prepare.add(new VarInsnNode(Opcodes.ALOAD, instruction.var));
                            prepare.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    'com/mcmoddev/mineralogy/patching/LegacyWorldDataHook',
                                    'prepareLegacyChunk',
                                    '(Lnet/minecraft/nbt/NBTTagCompound;)V',
                                    false));
                            method.instructions.insert(instruction, prepare);
                        } else if (instruction.getOpcode() === Opcodes.ARETURN) {
                            method.instructions.insertBefore(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    'com/mcmoddev/mineralogy/patching/LegacyWorldDataHook',
                                    'finalizeLegacyChunk',
                                    '(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;',
                                    false));
                        }
                    }
                }
                return classNode;
            }
        },
        'mineralogy_legacy_leaves_fix': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.util.datafix.fixes.LeavesFix$Section'
            },
            'transformer': function(classNode) {
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.desc !== '(Lcom/mojang/datafixers/Typed;)Lcom/mojang/datafixers/Typed;') {
                        continue;
                    }

                    var malformed = false;
                    for (var instruction = method.instructions.getFirst(); instruction !== null;
                            instruction = instruction.getNext()) {
                        var previous = instruction.getPrevious();
                        if (instruction.getOpcode() === Opcodes.CHECKCAST
                                && instruction.desc === 'com/mojang/datafixers/Typed'
                                && previous !== null
                                && previous.getOpcode() === Opcodes.INVOKEINTERFACE
                                && previous.owner === 'java/util/stream/Stream'
                                && previous.name === 'collect') {
                            malformed = true;
                            break;
                        }
                    }

                    if (malformed) {
                        // ForgeGradle 3 recompiled this generic call incorrectly. Production
                        // Minecraft has valid bytecode and therefore never enters this branch.
                        method.instructions.clear();
                        method.tryCatchBlocks.clear();
                        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        method.instructions.add(new InsnNode(Opcodes.ARETURN));
                    }
                }
                return classNode;
            }
        }
    };
}
