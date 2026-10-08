package zone.moddev.mc.mineralogy.compatprobe;

import com.google.gson.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.inventory.*;
import net.minecraft.inventory.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;

/** Non-shipping target-native assertions. No modern cave or mining tags are invented. */
@Mod("mineralogycompatprobe")
public final class TerrainCompatibilityProbe {
    private final List<String> evidence = new ArrayList<>();
    private static final String[] FAMILIES = ("andesite basalt diorite granite rhyolite pegmatite diabase gabbro "
            + "peridotite basaltic_glass scoria tuff shale conglomerate dolomite limestone siltstone marble slate "
            + "schist gneiss phyllite amphibolite hornfels quartzite novaculite rock_salt").split(" ");
    private static final BlockPos FURNACE = new BlockPos(30, 64, 30);

    public TerrainCompatibilityProbe() {
        ProbeWorldType.register();
        MinecraftForge.EVENT_BUS.addListener(this::started);
        if (Boolean.getBoolean("mineralogy.compatProbe.client"))
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> ClientSmoke.register());
    }
    private void started(FMLServerStartedEvent event) {
        if (Boolean.getBoolean("mineralogy.compatProbe.client")) return;
        MinecraftServer server=event.getServer();
        try {
            ServerWorld world=server.getWorld(World.OVERWORLD);
            if(Boolean.getBoolean("mineralogy.compatProbe.forestSample")) {
                forestSample(world,!Boolean.getBoolean("mineralogy.compatProbe.forestBaseline"));
                finish(server,null); return;
            }
            if(Boolean.getBoolean("mineralogy.compatProbe.constructionBaseline")) {
                loot(world); finish(server,null); return;
            }
            if(Boolean.getBoolean("mineralogy.compatProbe.furnaceBaseline")) {
                furnace(world, "fresh"); finish(server,null); return;
            }
            if(Boolean.getBoolean("mineralogy.compatProbe.baseline")) {
                forest(world,false); lamp(false); finish(server,null); return;
            }
            test(world,"initial");
            server.func_240780_a_(server.getResourcePacks().func_232621_d_()).whenComplete((unused,failure) -> server.execute(() -> {
                try { if(failure!=null) throw new IllegalStateException("Resource reload",failure);
                    test(world,"reload"); finish(server,null);
                } catch(Throwable problem) { finish(server,problem); }
            }));
        } catch(Throwable failure) {finish(server,failure);}
    }
    private void test(ServerWorld world,String phase) throws Exception {
        classifications(); cobblestone(); forest(world,true); lamp(true); loot(world);
        if(!Boolean.getBoolean("mineralogy.compatProbe.constructionDisabled")) {
            recipes(world); mining(world);
            String cycle=phase.equals("reload") ? "resource-reload" : System.getProperty("mineralogy.compatProbe.cycle","fresh");
            furnace(world,cycle);
        }
        evidence.add(phase+": native tags, recipes, forest rocks, wall posts, mining and furnace assertions passed");
    }
    private void classifications() throws Exception {
        JsonObject contracts;
        try(Reader reader=new InputStreamReader(getClass().getResourceAsStream("/tag-compatibility-contract.json"),StandardCharsets.UTF_8)) {
            contracts=new JsonParser().parse(reader).getAsJsonObject().getAsJsonObject("tags");
        }
        int count=0;
        for(Map.Entry<String,JsonElement> entry:contracts.entrySet()) {
            String path=entry.getKey(),namespace=path.substring(0,path.indexOf('/'));
            boolean blocks=path.contains("/tags/blocks/");
            String prefix=blocks ? "/tags/blocks/" : "/tags/items/";
            ResourceLocation tag=id(namespace+":"+path.substring(path.indexOf(prefix)+prefix.length(),path.length()-5));
            for(JsonElement value:entry.getValue().getAsJsonArray()) {
                boolean optional=value.isJsonObject();
                String name=optional ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
                if(name.startsWith("#")) {
                    if(blocks) for(Block member:blockTag(name.substring(1)).getAllElements())
                        require(blockTag(tag.toString()).contains(member),"Lost inherited block "+name);
                    else for(Item member:itemTag(name.substring(1)).getAllElements())
                        require(itemTag(tag.toString()).contains(member),"Lost inherited item "+name);
                    continue;
                }
                if(blocks) {
                    Block member=ForgeRegistries.BLOCKS.getValue(id(name));
                    if(optional && (member==null || member==Blocks.AIR)) continue;
                    require(member!=null && member!=Blocks.AIR && blockTag(tag.toString()).contains(member),"Block classification "+tag+" "+name);
                } else {
                    Item member=ForgeRegistries.ITEMS.getValue(id(name));
                    if(optional && (member==null || member==Items.AIR)) continue;
                    require(member!=null && member!=Items.AIR && itemTag(tag.toString()).contains(member),"Item classification "+tag+" "+name);
                }
                count++;
            }
        }
        require(blockTag("mineralogy:terrain_rocks").getAllElements().size()==36,"Terrain count");
        require(itemTag("mineralogy:terrain_rocks").getAllElements().size()==36,"Item terrain count");
        require(blockTag("mineralogy:raw_stones").getAllElements().size()==35,"Raw stone count");
        evidence.add(count+" registered direct classifications; 36 terrain identities; recursive ore/storage tags");
    }
    private void cobblestone() {
        boolean enabled=MineralogyConfig.makeRockCobblestoneEquivilent();
        for(String family:FAMILIES) for(String name:Arrays.asList("mineralogy:"+family)) {
            Block member=block(name);
            require(blockTag("forge:cobblestone").contains(member)==enabled,"Root cobblestone "+name);
            require(blockTag("forge:cobblestone/normal").contains(member)==enabled,"Normal cobblestone "+name);
            require(itemTag("forge:cobblestone").contains(member.asItem())==enabled,"Root item "+name);
            require(itemTag("forge:cobblestone/normal").contains(member.asItem())==enabled,"Normal item "+name);
        }
        for(String tag:Arrays.asList("forge:cobblestone","forge:cobblestone/normal")) {
            require(blockTag(tag).contains(Blocks.COBBLESTONE),"Vanilla member lost");
            require(blockTag(tag).contains(Blocks.GOLD_BLOCK),"Third-party member lost");
            require(itemTag(tag).contains(Items.GOLD_BLOCK),"Third-party item lost");
            for(String name:Arrays.asList("chert","pumice")) {
                require(blockTag(tag).contains(block("mineralogy:"+name)),"Unconditional block "+name);
                require(itemTag(tag).contains(block("mineralogy:"+name).asItem()),"Unconditional item "+name);
            }
        }
    }
    private void forest(ServerWorld world,boolean fixed) {
        for(String name:Arrays.asList("minecraft:stone","mineralogy:basalt","mineralogy:chalk","mineralogy:basalt_brick")) {
            Block source=ForgeRegistries.BLOCKS.getValue(id(name));
            if(source==null || source==Blocks.AIR) continue;
            BlockPos center=new BlockPos(96,80,96);
            for(BlockPos pos:BlockPos.getAllInBoxMutable(new BlockPos(91,3,91),center.add(5,4,5)))
                world.setBlockState(pos,pos.getY()<80 ? source.getDefaultState() : Blocks.AIR.getDefaultState(),2);
            require(world.getBlockState(center.down()).getBlock()==source,"Uncontrolled forest substrate");
            boolean result=Feature.FOREST_ROCK.generate(world,world.getChunkProvider().getChunkGenerator(),new Random(43),center,
                    new BlockStateFeatureConfig(Blocks.MOSSY_COBBLESTONE.getDefaultState()));
            boolean positive=name.equals("minecraft:stone") || (fixed && !name.endsWith("_brick"));
            require(result==positive,"Native forest-rock control "+name+"="+result);
            evidence.add("Native forest-rock "+name+"="+result);
        }
    }
    private void forestSample(ServerWorld world,boolean fixed) {
        List<String> substrates=new ArrayList<>();
        for(String family:FAMILIES) substrates.add("mineralogy:"+family);
        for(String name:Arrays.asList("chalk","chert","gypsum","pumice")) substrates.add("mineralogy:"+name);
        substrates.add("minecraft:stone"); substrates.add("mineralogy:basalt_brick");
        for(String name:substrates) {
            Block source=block(name); int placed=0;
            for(int index=0;index<81;index++) {
                BlockPos center=new BlockPos((12+index%9)*16+8,40,(12+index/9)*16+8);
                for(BlockPos pos:BlockPos.getAllInBoxMutable(center.add(-5,-37,-5),center.add(5,4,5)))
                    world.setBlockState(pos,pos.getY()<40 ? source.getDefaultState() : Blocks.AIR.getDefaultState(),2);
                boolean result=Feature.FOREST_ROCK.generate(world,world.getChunkProvider().getChunkGenerator(),
                        new Random(4300+index),center,new BlockStateFeatureConfig(Blocks.MOSSY_COBBLESTONE.getDefaultState()));
                boolean positive=name.equals("minecraft:stone") || (fixed && !name.endsWith("_brick"));
                require(result==positive,"Paired forest control "+name+" at "+index+"="+result);
                if(result) placed++;
            }
            evidence.add("Paired forest-rock placements "+name+"="+placed+"/81");
        }
        evidence.add("Paired 81-chunk controlled forest sample across 31 raw families, vanilla stone and crafted-brick control");
    }
    private void lamp(boolean fixed) throws Exception {
        WallBlock wall=(WallBlock)Blocks.COBBLESTONE_WALL;
        BlockState straight=wall.getDefaultState().with(WallBlock.WALL_HEIGHT_NORTH,WallHeight.LOW)
                .with(WallBlock.WALL_HEIGHT_SOUTH,WallHeight.LOW);
        Method method=WallBlock.class.getDeclaredMethod("func_235628_a_",BlockState.class,BlockState.class,VoxelShape.class);
        method.setAccessible(true);
        for(String name:Arrays.asList("minecraft:torch","mineralogy:rocksaltlamp")) {
            boolean actual=(Boolean)method.invoke(wall,straight,block(name).getDefaultState(),VoxelShapes.empty());
            require(actual==(name.equals("minecraft:torch") || fixed),"Native wall post "+name+"="+actual);
            evidence.add("Native straight-wall post "+name+"="+actual);
        }
        BlockState junction=straight.with(WallBlock.WALL_HEIGHT_EAST,WallHeight.LOW);
        require((Boolean)method.invoke(wall,junction,block("mineralogy:rocksaltlamp").getDefaultState(),VoxelShapes.empty()),"Junction post");
    }
    private void recipes(ServerWorld world) {
        String[] names=("furnace brewing_stand lever piston dispenser dropper observer mossy_cobblestone andesite diorite "
                + "stone_axe stone_hoe stone_pickaxe stone_shovel stone_sword").split(" ");
        List<String> materials=new ArrayList<>();
        for(String family:FAMILIES)materials.add("mineralogy:"+family);
        materials.addAll(Arrays.asList("minecraft:basalt","minecraft:andesite","minecraft:diorite","minecraft:granite",
                "minecraft:cobblestone","mineralogy:chert","mineralogy:pumice"));
        boolean enabled=MineralogyConfig.makeRockCobblestoneEquivilent();
        for(String name:names) {
            IRecipe<?> recipe=world.getRecipeManager().getRecipe(id("minecraft:"+name)).orElseThrow(() -> new AssertionError("Missing native recipe "+name));
            require(recipe instanceof ICraftingRecipe,"Not crafting "+name);
            for(String material:materials) {
                Item stone=block(material).asItem();
                boolean eligible=enabled || material.equals("minecraft:cobblestone") || material.equals("mineralogy:chert") || material.equals("mineralogy:pumice");
                CraftingInventory inventory=inventory(recipe,stone);
                boolean matches=((ICraftingRecipe)recipe).matches(inventory,world);
                require(matches==eligible,"Recipe "+name+" "+material+"="+matches);
            }
            require(world.getServer().getAdvancementManager().getAdvancement(id("minecraft:recipes/"+advancementFolder(name)+"/"+name))!=null,
                    "Missing native advancement "+name);
        }
        for(String nativeName:Arrays.asList("andesite","diorite","granite","polished_andesite","polished_diorite","polished_granite")) {
            IRecipe<?> recipe=world.getRecipeManager().getRecipe(id("minecraft:"+nativeName+"_slab")).get();
            ItemStack result=recipe.getRecipeOutput();
            String family=nativeName.replace("polished_","")+(nativeName.startsWith("polished_") ? "_smooth" : "");
            require(result.getCount()==6 && result.getItem()==block("mineralogy:"+family+"_slab").asItem(),"Shared slab precedence "+nativeName);
        }
        evidence.add("All 15 actual recipe-manager routes over 34 materials; native advancements and six shared slab outputs");
    }
    private CraftingInventory inventory(IRecipe<?> recipe,Item stone) {
        Container dummy=new Container(null,-1) { @Override public boolean canInteractWith(PlayerEntity player) {return false;} };
        CraftingInventory inventory=new CraftingInventory(dummy,3,3);
        int width=recipe instanceof ShapedRecipe ? ((ShapedRecipe)recipe).getWidth() : 3;
        int index=0;
        for(Ingredient ingredient:recipe.getIngredients()) {
            ItemStack[] stacks=ingredient.getMatchingStacks();
            ItemStack stack=stacks.length==0 ? ItemStack.EMPTY : stacks[0].copy();
            // Material slots contain cobblestone/blackstone, not unrelated sand/quartz.
            boolean material=false;
            for(ItemStack candidate:stacks) if(candidate.getItem()==Items.COBBLESTONE || candidate.getItem()==Items.BLACKSTONE)material=true;
            if(material)stack=new ItemStack(stone);
            inventory.setInventorySlotContents(index%width+(index/width)*3,stack); index++;
        }
        return inventory;
    }
    private String advancementFolder(String recipe) {
        if(recipe.equals("stone_sword"))return "combat";
        if(recipe.startsWith("stone_"))return "tools";
        if(recipe.equals("andesite") || recipe.equals("diorite") || recipe.equals("mossy_cobblestone"))return "building_blocks";
        if(recipe.equals("brewing_stand"))return "brewing";
        if(recipe.equals("furnace"))return "decorations";
        return "redstone";
    }
    private void mining(ServerWorld world) {
        ItemStack pick=new ItemStack(Items.DIAMOND_PICKAXE);
        for(String form:Arrays.asList("basalt","basalt_smooth","basalt_brick","basalt_slab","basalt_furnace")) {
            BlockState state=block("mineralogy:"+form).getDefaultState();
            require(pick.getDestroySpeed(state)>1,"Pickaxe speed "+form);
            require(pick.canHarvestBlock(state),"Pickaxe harvesting "+form);
            List<ItemStack> drops=Block.getDrops(state,world,new BlockPos(20,64,20),null,null,pick);
            require(!drops.isEmpty(),"Missing drop "+form);
        }
        evidence.add("Native diamond-pick speed, harvest and drops for basalt raw/smooth/brick/slab/furnace");
    }
    private void furnace(ServerWorld world,String cycle) {
        boolean persisted=cycle.equals("reload");
        if(!persisted && !cycle.equals("resource-reload")) {
            world.setBlockState(FURNACE,block("mineralogy:basalt_furnace").getDefaultState().with(RockFurnace.FACING,Direction.WEST),3);
            TileEntity tile=world.getTileEntity(FURNACE);
            ((IInventory)tile).setInventorySlotContents(0,new ItemStack(Items.IRON_ORE,7));
            ((IInventory)tile).setInventorySlotContents(1,new ItemStack(Items.COAL,11));
            CompoundNBT data=tile.write(new CompoundNBT());
            data.putInt("BurnTime",120); data.putInt("CookTime",37); data.putInt("CookTimeTotal",200);
            tile.read(world.getBlockState(FURNACE),data);
        }
        TileEntity tile=world.getTileEntity(FURNACE);
        require(tile!=null,"Missing persisted furnace");
        BlockState old=tile.getBlockState(); // Prime the native cache before swapping blocks.
        CompoundNBT before=tile.write(new CompoundNBT());
        RockFurnace.setState(true,world,FURNACE);
        require(world.getTileEntity(FURNACE)==tile,"Lost furnace identity");
        require(tile.getBlockState().equals(world.getBlockState(FURNACE)),"lit transition left a stale block-entity state");
        require(world.getBlockState(FURNACE).get(RockFurnace.FACING)==Direction.WEST,"Lost furnace facing");
        RockFurnace.setState(false,world,FURNACE);
        require(tile.getBlockState().equals(world.getBlockState(FURNACE)),"unlit transition left a stale block-entity state");
        CompoundNBT after=tile.write(new CompoundNBT());
        for(String key:Arrays.asList("Items","BurnTime","CookTime","CookTimeTotal"))
            require(Objects.equals(before.get(key),after.get(key)),"Furnace transition changed "+key);
        require(((IInventory)tile).getStackInSlot(1).getCount()==11,"Persisted fuel lost");
        evidence.add("Furnace "+cycle+" preserved state, entity, facing, inventory and burn/cook NBT");
    }
    private void loot(ServerWorld world) throws Exception {
        Object manager=world.getServer().getLootTableManager();
        Map<?,?> tables=(Map<?,?>)field(manager,"registeredLootTables",Map.class);
        int total=0,guarded=0;
        boolean disabled=Boolean.getBoolean("mineralogy.compatProbe.constructionDisabled");
        for(Map.Entry<?,?> entry:tables.entrySet()) {
            String name=entry.getKey().toString();
            if(!name.startsWith("mineralogy:blocks/"))continue;
            total++;
            if(!name.matches("mineralogy:blocks/(lit_)?(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)"))continue;
            boolean populated=false;
            for(Object pool:(List<?>)field(entry.getValue(),"pools",List.class))
                if(!((List<?>)field(pool,"lootEntries",List.class)).isEmpty())populated=true;
            require(populated!=disabled,"Native construction loot "+name);guarded++;
        }
        require(total==919 && guarded==864,"Loaded loot inventory "+total+"/"+guarded);
        evidence.add("All 919 loot tables loaded; 864 construction pools "+(disabled ? "empty" : "retained"));
    }
    private static Object field(Object object,String preferred,Class<?> type) throws Exception {
        for(Field field:object.getClass().getDeclaredFields())if(type.isAssignableFrom(field.getType())) {
            field.setAccessible(true); return field.get(object);
        }
        throw new IllegalStateException("Missing native "+type+" field "+preferred);
    }
    private static ResourceLocation id(String value) {return new ResourceLocation(value);}
    private static Block block(String name) {Block value=ForgeRegistries.BLOCKS.getValue(id(name)); require(value!=null && value!=Blocks.AIR,"Missing block "+name);return value;}
    private static ITag<Block> blockTag(String name) {ITag<Block> tag=BlockTags.getCollection().get(id(name));require(tag!=null,"Missing block tag "+name);return tag;}
    private static ITag<Item> itemTag(String name) {ITag<Item> tag=ItemTags.getCollection().get(id(name));require(tag!=null,"Missing item tag "+name);return tag;}
    private static void require(boolean condition,String message) {if(!condition)throw new AssertionError(message);}
    private void finish(MinecraftServer server,Throwable failure) {
        try {
            if(failure!=null) {StringWriter writer=new StringWriter();failure.printStackTrace(new PrintWriter(writer));evidence.add(writer.toString());}
            evidence.add(failure==null ? "PASS" : "FAIL");
            Files.write(Paths.get("terrain-compatibility-result.txt"),evidence,StandardCharsets.UTF_8);
        }catch(IOException exception){throw new RuntimeException(exception);}
        server.initiateShutdown(false);
    }
    private static final class ClientSmoke {
        private static boolean opening;
        private static int ticks;
        private static Object accepted;
        static void register() {MinecraftForge.EVENT_BUS.addListener(ClientSmoke::tick);}
        private static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
            net.minecraft.client.Minecraft game=net.minecraft.client.Minecraft.getInstance();
            if(!opening && game.currentScreen instanceof net.minecraft.client.gui.screen.MainMenuScreen) {
                opening=true;game.loadWorld("compatibility-world");
            }
            if(game.currentScreen instanceof net.minecraft.client.gui.screen.ConfirmBackupScreen && accepted!=game.currentScreen) {
                accepted=game.currentScreen;
                try {
                    for(Field field:game.currentScreen.getClass().getDeclaredFields())if(field.getType().getName().contains("ConfirmBackupScreen$I")) {
                        field.setAccessible(true);Object listener=field.get(game.currentScreen);
                        for(Method method:listener.getClass().getMethods())if(method.getParameterCount()==2 && method.getParameterTypes()[0]==boolean.class) {
                            method.invoke(listener,false,false);break;
                        }
                    }
                }catch(Exception failure){throw new RuntimeException(failure);}
            }
            if(game.world!=null && game.player!=null && ++ticks>=200) {
                try {Files.write(Paths.get("terrain-client-result.txt"),Arrays.asList("PASS: entered and rendered packaged world for 200 ticks"),StandardCharsets.UTF_8);}
                catch(IOException failure){throw new RuntimeException(failure);}
                game.shutdown();
            }
        }
    }
}
