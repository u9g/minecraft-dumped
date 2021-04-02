package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class MineshaftFeature extends StructureFeature<MineshaftConfiguration> {
   public MineshaftFeature(Codec<MineshaftConfiguration> var1) {
      super(var1);
   }

   protected boolean isFeatureChunk(ChunkGenerator var1, BiomeSource var2, long var3, WorldgenRandom var5, int var6, int var7, Biome var8, ChunkPos var9, MineshaftConfiguration var10, LevelHeightAccessor var11) {
      var5.setLargeFeatureSeed(var3, var6, var7);
      double var12 = (double)var10.probability;
      return var5.nextDouble() < var12;
   }

   public StructureFeature.StructureStartFactory<MineshaftConfiguration> getStartFactory() {
      return MineshaftFeature.MineShaftStart::new;
   }

   public static class MineShaftStart extends StructureStart<MineshaftConfiguration> {
      public MineShaftStart(StructureFeature<MineshaftConfiguration> var1, int var2, int var3, BoundingBox var4, int var5, long var6) {
         super(var1, var2, var3, var4, var5, var6);
      }

      public void generatePieces(RegistryAccess var1, ChunkGenerator var2, StructureManager var3, int var4, int var5, Biome var6, MineshaftConfiguration var7, LevelHeightAccessor var8) {
         MineShaftPieces.MineShaftRoom var9 = new MineShaftPieces.MineShaftRoom(0, this.random, SectionPos.sectionToBlockCoord(var4, 2), SectionPos.sectionToBlockCoord(var5, 2), var7.type);
         this.pieces.add(var9);
         var9.addChildren(var9, this.pieces, this.random);
         this.calculateBoundingBox();
         if (var7.type == MineshaftFeature.Type.MESA) {
            boolean var10 = true;
            int var11 = var2.getSeaLevel() - this.boundingBox.y1 + this.boundingBox.getYSpan() / 2 - -5;
            this.boundingBox.move(0, var11, 0);
            Iterator var12 = this.pieces.iterator();

            while(var12.hasNext()) {
               StructurePiece var13 = (StructurePiece)var12.next();
               var13.move(0, var11, 0);
            }
         } else {
            this.moveBelowSeaLevel(var2.getSeaLevel(), var2.getMinY(), this.random, 10);
         }

      }
   }

   public static enum Type implements StringRepresentable {
      NORMAL("normal", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE),
      MESA("mesa", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_FENCE);

      public static final Codec<MineshaftFeature.Type> CODEC = StringRepresentable.fromEnum(MineshaftFeature.Type::values, MineshaftFeature.Type::byName);
      private static final Map<String, MineshaftFeature.Type> BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(MineshaftFeature.Type::getName, (var0) -> {
         return var0;
      }));
      private final String name;
      private final BlockState woodState;
      private final BlockState planksState;
      private final BlockState fenceState;

      private Type(String var3, Block var4, Block var5, Block var6) {
         this.name = var3;
         this.woodState = var4.defaultBlockState();
         this.planksState = var5.defaultBlockState();
         this.fenceState = var6.defaultBlockState();
      }

      public String getName() {
         return this.name;
      }

      private static MineshaftFeature.Type byName(String var0) {
         return (MineshaftFeature.Type)BY_NAME.get(var0);
      }

      public static MineshaftFeature.Type byId(int var0) {
         return var0 >= 0 && var0 < values().length ? values()[var0] : NORMAL;
      }

      public BlockState getWoodState() {
         return this.woodState;
      }

      public BlockState getPlanksState() {
         return this.planksState;
      }

      public BlockState getFenceState() {
         return this.fenceState;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
