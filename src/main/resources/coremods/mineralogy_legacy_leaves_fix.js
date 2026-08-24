var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
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
                            'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
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
                var patchedRead = false;
                var patchedReturn = false;
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
                                // The Forge 25 production runtime presents the read method
                                // under its SRG name, while the development runtime exposes
                                // the mapped name. Its return descriptor is stable in both.
                                && previous.desc.endsWith(')Lnet/minecraft/nbt/NBTTagCompound;')) {
                            var prepare = new InsnList();
                            prepare.add(new VarInsnNode(Opcodes.ALOAD, instruction.var));
                            prepare.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                                    'prepareLegacyChunk',
                                    '(Lnet/minecraft/nbt/NBTTagCompound;)V',
                                    false));
                            method.instructions.insert(instruction, prepare);
                            patchedRead = true;
                        } else if (instruction.getOpcode() === Opcodes.ARETURN) {
                            method.instructions.insertBefore(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                                    'finalizeLegacyChunk',
                                    '(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;',
                                    false));
                            patchedReturn = true;
                        }
                    }
                }
                if (!patchedRead || !patchedReturn) {
                    throw new Error('Mineralogy could not patch the Forge 25 legacy chunk loader');
                }
                return classNode;
            }
        },
        'mineralogy_legacy_worldgen_guard': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.gen.WorldGenRegion'
            },
            'transformer': function(classNode) {
                var descriptor =
                        '(Lnet/minecraft/util/math/BlockPos;' +
                        'Lnet/minecraft/block/state/IBlockState;I)Z';
                var patched = false;
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.desc !== descriptor) {
                        continue;
                    }
                    var allowed = new LabelNode();
                    var prefix = new InsnList();
                    prefix.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    prefix.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook',
                            'shouldBlockWorldgenWrite',
                            '(Lnet/minecraft/util/math/BlockPos;)Z',
                            false));
                    prefix.add(new JumpInsnNode(Opcodes.IFEQ, allowed));
                    prefix.add(new InsnNode(Opcodes.ICONST_0));
                    prefix.add(new InsnNode(Opcodes.IRETURN));
                    prefix.add(allowed);
                    method.instructions.insert(prefix);
                    patched = true;
                }
                if (!patched) {
                    throw new Error('Mineralogy could not patch the Forge 25 legacy-world write guard');
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
        },
        'mineralogy_crude_oil_renderer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.BlockFluidRenderer'
            },
            'transformer': function(classNode) {
                var descriptor =
                        '(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;' +
                        'Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/fluid/IFluidState;)Z';
                for (var methodIndex = 0; methodIndex < classNode.methods.size(); ++methodIndex) {
                    var method = classNode.methods.get(methodIndex);
                    if (method.name !== 'render' || method.desc !== descriptor) {
                        continue;
                    }
                    var patchedFlag = false;
                    var patchedSprites = false;
                    var patchedColor = false;
                    for (var instruction = method.instructions.getFirst(); instruction !== null;
                            instruction = instruction.getNext()) {
                        if (!patchedFlag && instruction.getOpcode() === Opcodes.ISTORE && instruction.var === 5) {
                            var flagHook = new InsnList();
                            flagHook.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            flagHook.add(new VarInsnNode(Opcodes.ILOAD, 5));
                            flagHook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    'zone/moddev/mc/mineralogy/client/ClientOilRenderer',
                                    'useOpaqueFluidPath',
                                    '(Lnet/minecraft/fluid/IFluidState;Z)Z', false));
                            flagHook.add(new VarInsnNode(Opcodes.ISTORE, 5));
                            method.instructions.insert(instruction, flagHook);
                            patchedFlag = true;
                        } else if (!patchedSprites && instruction.getOpcode() === Opcodes.ASTORE
                                && instruction.var === 6) {
                            var spriteHook = new InsnList();
                            spriteHook.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            spriteHook.add(new VarInsnNode(Opcodes.ALOAD, 6));
                            spriteHook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    'zone/moddev/mc/mineralogy/client/ClientOilRenderer',
                                    'overrideSprites',
                                    '(Lnet/minecraft/fluid/IFluidState;[Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)' +
                                    '[Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;', false));
                            spriteHook.add(new VarInsnNode(Opcodes.ASTORE, 6));
                            method.instructions.insert(instruction, spriteHook);
                            patchedSprites = true;
                        } else if (!patchedColor && instruction.getOpcode() === Opcodes.ISTORE
                                && instruction.var === 7) {
                            var colorHook = new InsnList();
                            colorHook.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            colorHook.add(new VarInsnNode(Opcodes.ILOAD, 7));
                            colorHook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    'zone/moddev/mc/mineralogy/client/ClientOilRenderer',
                                    'overrideColor', '(Lnet/minecraft/fluid/IFluidState;I)I', false));
                            colorHook.add(new VarInsnNode(Opcodes.ISTORE, 7));
                            method.instructions.insert(instruction, colorHook);
                            patchedColor = true;
                        }
                    }
                    if (!patchedFlag || !patchedSprites || !patchedColor) {
                        throw new Error('Mineralogy could not patch the Forge 25 fluid renderer');
                    }
                }
                return classNode;
            }
        }
    };
}
