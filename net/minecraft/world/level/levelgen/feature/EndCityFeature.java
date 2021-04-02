package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
   public EndCityFeature(Codec<NoneFeatureConfiguration> var1) {
      super(var1);
   }

   protected boolean linearSeparation() {
      return false;
   }

   protected boolean isFeatureChunk(ChunkGenerator var1, BiomeSource var2, long var3, WorldgenRandom var5, int var6, int var7, Biome var8, ChunkPos var9, NoneFeatureConfiguration var10, LevelHeightAccessor var11) {
      return getYPositionForFeature(var6, var7, var1, var11) >= 60;
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return EndCityFeature.EndCityStart::new;
   }

   private static int getYPositionForFeature(int var0, int var1, ChunkGenerator var2, LevelHeightAccessor var3) {
      Random var4 = new Random((long)(var0 + var1 * 10387313));
      Rotation var5 = Rotation.getRandom(var4);
      byte var6 = 5;
      byte var7 = 5;
      if (var5 == Rotation.CLOCKWISE_90) {
         var6 = -5;
      } else if (var5 == Rotation.CLOCKWISE_180) {
         var6 = -5;
         var7 = -5;
      } else if (var5 == Rotation.COUNTERCLOCKWISE_90) {
         var7 = -5;
      }

      int var8 = SectionPos.sectionToBlockCoord(var0, 7);
      int var9 = SectionPos.sectionToBlockCoord(var1, 7);
      int var10 = var2.getFirstOccupiedHeight(var8, var9, Heightmap.Types.WORLD_SURFACE_WG, var3);
      int var11 = var2.getFirstOccupiedHeight(var8, var9 + var7, Heightmap.Types.WORLD_SURFACE_WG, var3);
      int var12 = var2.getFirstOccupiedHeight(var8 + var6, var9, Heightmap.Types.WORLD_SURFACE_WG, var3);
      int var13 = var2.getFirstOccupiedHeight(var8 + var6, var9 + var7, Heightmap.Types.WORLD_SURFACE_WG, var3);
      return Math.min(Math.min(var10, var11), Math.min(var12, var13));
   }

   public static class EndCityStart extends StructureStart<NoneFeatureConfiguration> {
      public EndCityStart(StructureFeature<NoneFeatureConfiguration> var1, int var2, int var3, BoundingBox var4, int var5, long var6) {
         super(var1, var2, var3, var4, var5, var6);
      }

      public void generatePieces(RegistryAccess var1, ChunkGenerator var2, StructureManager var3, int var4, int var5, Biome var6, NoneFeatureConfiguration var7, LevelHeightAccessor var8) {
         Rotation var9 = Rotation.getRandom(this.random);
         int var10 = EndCityFeature.getYPositionForFeature(var4, var5, var2, var8);
         if (var10 >= 60) {
            BlockPos var11 = new BlockPos(SectionPos.sectionToBlockCoord(var4, 8), var10, SectionPos.sectionToBlockCoord(var5, 8));
            EndCityPieces.startHouseTower(var3, var11, var9, this.pieces, this.random);
            this.calculateBoundingBox();
         }
      }
   }
}
