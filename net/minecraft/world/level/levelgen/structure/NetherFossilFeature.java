package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFossilFeature extends StructureFeature<NoneFeatureConfiguration> {
   public NetherFossilFeature(Codec<NoneFeatureConfiguration> var1) {
      super(var1);
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return NetherFossilFeature.FeatureStart::new;
   }

   public static class FeatureStart extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {
      public FeatureStart(StructureFeature<NoneFeatureConfiguration> var1, int var2, int var3, BoundingBox var4, int var5, long var6) {
         super(var1, var2, var3, var4, var5, var6);
      }

      public void generatePieces(RegistryAccess var1, ChunkGenerator var2, StructureManager var3, int var4, int var5, Biome var6, NoneFeatureConfiguration var7, LevelHeightAccessor var8) {
         ChunkPos var9 = new ChunkPos(var4, var5);
         int var10 = var9.getMinBlockX() + this.random.nextInt(16);
         int var11 = var9.getMinBlockZ() + this.random.nextInt(16);
         int var12 = var2.getSeaLevel();
         int var13 = var12 + this.random.nextInt(var2.getGenDepth() - 2 - var12);
         NoiseColumn var14 = var2.getBaseColumn(var10, var11, var8);

         for(BlockPos.MutableBlockPos var15 = new BlockPos.MutableBlockPos(var10, var13, var11); var13 > var12; --var13) {
            BlockState var16 = var14.getBlockState(var15);
            var15.move(Direction.DOWN);
            BlockState var17 = var14.getBlockState(var15);
            if (var16.isAir() && (var17.is(Blocks.SOUL_SAND) || var17.isFaceSturdy(EmptyBlockGetter.INSTANCE, var15, Direction.UP))) {
               break;
            }
         }

         if (var13 > var12) {
            NetherFossilPieces.addPieces(var3, this.pieces, this.random, new BlockPos(var10, var13, var11));
            this.calculateBoundingBox();
         }
      }
   }
}
