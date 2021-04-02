package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StrongholdFeature extends StructureFeature<NoneFeatureConfiguration> {
   public StrongholdFeature(Codec<NoneFeatureConfiguration> var1) {
      super(var1);
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return StrongholdFeature.StrongholdStart::new;
   }

   protected boolean isFeatureChunk(ChunkGenerator var1, BiomeSource var2, long var3, WorldgenRandom var5, int var6, int var7, Biome var8, ChunkPos var9, NoneFeatureConfiguration var10, LevelHeightAccessor var11) {
      return var1.hasStronghold(new ChunkPos(var6, var7));
   }

   public static class StrongholdStart extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {
      private final long seed;

      public StrongholdStart(StructureFeature<NoneFeatureConfiguration> var1, int var2, int var3, BoundingBox var4, int var5, long var6) {
         super(var1, var2, var3, var4, var5, var6);
         this.seed = var6;
      }

      public void generatePieces(RegistryAccess var1, ChunkGenerator var2, StructureManager var3, int var4, int var5, Biome var6, NoneFeatureConfiguration var7, LevelHeightAccessor var8) {
         int var9 = 0;

         StrongholdPieces.StartPiece var10;
         do {
            this.pieces.clear();
            this.boundingBox = BoundingBox.getUnknownBox();
            this.random.setLargeFeatureSeed(this.seed + (long)(var9++), var4, var5);
            StrongholdPieces.resetPieces();
            var10 = new StrongholdPieces.StartPiece(this.random, SectionPos.sectionToBlockCoord(var4, 2), SectionPos.sectionToBlockCoord(var5, 2));
            this.pieces.add(var10);
            var10.addChildren(var10, this.pieces, this.random);
            List var11 = var10.pendingChildren;

            while(!var11.isEmpty()) {
               int var12 = this.random.nextInt(var11.size());
               StructurePiece var13 = (StructurePiece)var11.remove(var12);
               var13.addChildren(var10, this.pieces, this.random);
            }

            this.calculateBoundingBox();
            this.moveBelowSeaLevel(var2.getSeaLevel(), var2.getMinY(), this.random, 10);
         } while(this.pieces.isEmpty() || var10.portalRoomPiece == null);

      }
   }
}
