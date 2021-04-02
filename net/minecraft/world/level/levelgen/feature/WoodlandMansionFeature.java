package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
   public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> var1) {
      super(var1);
   }

   protected boolean linearSeparation() {
      return false;
   }

   protected boolean isFeatureChunk(ChunkGenerator var1, BiomeSource var2, long var3, WorldgenRandom var5, int var6, int var7, Biome var8, ChunkPos var9, NoneFeatureConfiguration var10, LevelHeightAccessor var11) {
      Set var12 = var2.getBiomesWithin(SectionPos.sectionToBlockCoord(var6, 9), var1.getSeaLevel(), SectionPos.sectionToBlockCoord(var7, 9), 32);
      Iterator var13 = var12.iterator();

      Biome var14;
      do {
         if (!var13.hasNext()) {
            return true;
         }

         var14 = (Biome)var13.next();
      } while(var14.getGenerationSettings().isValidStart(this));

      return false;
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return WoodlandMansionFeature.WoodlandMansionStart::new;
   }

   public static class WoodlandMansionStart extends StructureStart<NoneFeatureConfiguration> {
      public WoodlandMansionStart(StructureFeature<NoneFeatureConfiguration> var1, int var2, int var3, BoundingBox var4, int var5, long var6) {
         super(var1, var2, var3, var4, var5, var6);
      }

      public void generatePieces(RegistryAccess var1, ChunkGenerator var2, StructureManager var3, int var4, int var5, Biome var6, NoneFeatureConfiguration var7, LevelHeightAccessor var8) {
         Rotation var9 = Rotation.getRandom(this.random);
         byte var10 = 5;
         byte var11 = 5;
         if (var9 == Rotation.CLOCKWISE_90) {
            var10 = -5;
         } else if (var9 == Rotation.CLOCKWISE_180) {
            var10 = -5;
            var11 = -5;
         } else if (var9 == Rotation.COUNTERCLOCKWISE_90) {
            var11 = -5;
         }

         int var12 = SectionPos.sectionToBlockCoord(var4, 7);
         int var13 = SectionPos.sectionToBlockCoord(var5, 7);
         int var14 = var2.getFirstOccupiedHeight(var12, var13, Heightmap.Types.WORLD_SURFACE_WG, var8);
         int var15 = var2.getFirstOccupiedHeight(var12, var13 + var11, Heightmap.Types.WORLD_SURFACE_WG, var8);
         int var16 = var2.getFirstOccupiedHeight(var12 + var10, var13, Heightmap.Types.WORLD_SURFACE_WG, var8);
         int var17 = var2.getFirstOccupiedHeight(var12 + var10, var13 + var11, Heightmap.Types.WORLD_SURFACE_WG, var8);
         int var18 = Math.min(Math.min(var14, var15), Math.min(var16, var17));
         if (var18 >= 60) {
            BlockPos var19 = new BlockPos(SectionPos.sectionToBlockCoord(var4, 8), var18 + 1, SectionPos.sectionToBlockCoord(var5, 8));
            LinkedList var20 = Lists.newLinkedList();
            WoodlandMansionPieces.generateMansion(var3, var19, var9, var20, this.random);
            this.pieces.addAll(var20);
            this.calculateBoundingBox();
         }
      }

      public void placeInChunk(WorldGenLevel var1, StructureFeatureManager var2, ChunkGenerator var3, Random var4, BoundingBox var5, ChunkPos var6) {
         super.placeInChunk(var1, var2, var3, var4, var5, var6);
         int var7 = this.boundingBox.y0;

         for(int var8 = var5.x0; var8 <= var5.x1; ++var8) {
            for(int var9 = var5.z0; var9 <= var5.z1; ++var9) {
               BlockPos var10 = new BlockPos(var8, var7, var9);
               if (!var1.isEmptyBlock(var10) && this.boundingBox.isInside(var10)) {
                  boolean var11 = false;
                  Iterator var12 = this.pieces.iterator();

                  while(var12.hasNext()) {
                     StructurePiece var13 = (StructurePiece)var12.next();
                     if (var13.getBoundingBox().isInside(var10)) {
                        var11 = true;
                        break;
                     }
                  }

                  if (var11) {
                     for(int var14 = var7 - 1; var14 > 1; --var14) {
                        BlockPos var15 = new BlockPos(var8, var14, var9);
                        if (!var1.isEmptyBlock(var15) && !var1.getBlockState(var15).getMaterial().isLiquid()) {
                           break;
                        }

                        var1.setBlock(var15, Blocks.COBBLESTONE.defaultBlockState(), 2);
                     }
                  }
               }
            }
         }

      }
   }
}
