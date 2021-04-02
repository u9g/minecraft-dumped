package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class FossilFeature extends Feature<NoneFeatureConfiguration> {
   private static final ResourceLocation SPINE_1 = new ResourceLocation("fossil/spine_1");
   private static final ResourceLocation SPINE_2 = new ResourceLocation("fossil/spine_2");
   private static final ResourceLocation SPINE_3 = new ResourceLocation("fossil/spine_3");
   private static final ResourceLocation SPINE_4 = new ResourceLocation("fossil/spine_4");
   private static final ResourceLocation SPINE_1_COAL = new ResourceLocation("fossil/spine_1_coal");
   private static final ResourceLocation SPINE_2_COAL = new ResourceLocation("fossil/spine_2_coal");
   private static final ResourceLocation SPINE_3_COAL = new ResourceLocation("fossil/spine_3_coal");
   private static final ResourceLocation SPINE_4_COAL = new ResourceLocation("fossil/spine_4_coal");
   private static final ResourceLocation SKULL_1 = new ResourceLocation("fossil/skull_1");
   private static final ResourceLocation SKULL_2 = new ResourceLocation("fossil/skull_2");
   private static final ResourceLocation SKULL_3 = new ResourceLocation("fossil/skull_3");
   private static final ResourceLocation SKULL_4 = new ResourceLocation("fossil/skull_4");
   private static final ResourceLocation SKULL_1_COAL = new ResourceLocation("fossil/skull_1_coal");
   private static final ResourceLocation SKULL_2_COAL = new ResourceLocation("fossil/skull_2_coal");
   private static final ResourceLocation SKULL_3_COAL = new ResourceLocation("fossil/skull_3_coal");
   private static final ResourceLocation SKULL_4_COAL = new ResourceLocation("fossil/skull_4_coal");
   private static final ResourceLocation[] fossils;
   private static final ResourceLocation[] fossilsCoal;

   public FossilFeature(Codec<NoneFeatureConfiguration> var1) {
      super(var1);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> var1) {
      Random var2 = var1.random();
      WorldGenLevel var3 = var1.level();
      BlockPos var4 = var1.origin();
      Rotation var5 = Rotation.getRandom(var2);
      int var6 = var2.nextInt(fossils.length);
      StructureManager var7 = var3.getLevel().getServer().getStructureManager();
      StructureTemplate var8 = var7.getOrCreate(fossils[var6]);
      StructureTemplate var9 = var7.getOrCreate(fossilsCoal[var6]);
      ChunkPos var10 = new ChunkPos(var4);
      BoundingBox var11 = new BoundingBox(var10.getMinBlockX(), var3.getMinBuildHeight(), var10.getMinBlockZ(), var10.getMaxBlockX(), var3.getMaxBuildHeight(), var10.getMaxBlockZ());
      StructurePlaceSettings var12 = (new StructurePlaceSettings()).setRotation(var5).setBoundingBox(var11).setRandom(var2).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
      BlockPos var13 = var8.getSize(var5);
      int var14 = var2.nextInt(16 - var13.getX());
      int var15 = var2.nextInt(16 - var13.getZ());
      int var16 = var3.getMaxBuildHeight();

      int var17;
      for(var17 = 0; var17 < var13.getX(); ++var17) {
         for(int var18 = 0; var18 < var13.getZ(); ++var18) {
            var16 = Math.min(var16, var3.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var4.getX() + var17 + var14, var4.getZ() + var18 + var15));
         }
      }

      var17 = Math.max(var16 - 15 - var2.nextInt(10), var3.getMinBuildHeight() + 10);
      BlockPos var21 = var8.getZeroPositionWithTransform(var4.offset(var14, var17, var15), Mirror.NONE, var5);
      BlockRotProcessor var19 = new BlockRotProcessor(0.9F);
      var12.clearProcessors().addProcessor(var19);
      var8.placeInWorld(var3, var21, var21, var12, var2, 4);
      var12.popProcessor(var19);
      BlockRotProcessor var20 = new BlockRotProcessor(0.1F);
      var12.clearProcessors().addProcessor(var20);
      var9.placeInWorld(var3, var21, var21, var12, var2, 4);
      return true;
   }

   static {
      fossils = new ResourceLocation[]{SPINE_1, SPINE_2, SPINE_3, SPINE_4, SKULL_1, SKULL_2, SKULL_3, SKULL_4};
      fossilsCoal = new ResourceLocation[]{SPINE_1_COAL, SPINE_2_COAL, SPINE_3_COAL, SPINE_4_COAL, SKULL_1_COAL, SKULL_2_COAL, SKULL_3_COAL, SKULL_4_COAL};
   }
}
