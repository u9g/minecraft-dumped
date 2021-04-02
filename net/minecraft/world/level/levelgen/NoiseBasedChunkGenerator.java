package net.minecraft.world.level.levelgen;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
   public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((var0) -> {
      return var0.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((var0x) -> {
         return var0x.biomeSource;
      }), Codec.LONG.fieldOf("seed").stable().forGetter((var0x) -> {
         return var0x.seed;
      }), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((var0x) -> {
         return var0x.settings;
      })).apply(var0, var0.stable(NoiseBasedChunkGenerator::new));
   });
   private static final BlockState AIR;
   private static final BlockState[] EMPTY_COLUMN;
   private final int cellHeight;
   private final int cellWidth;
   private final int cellCountX;
   private final int cellCountY;
   private final int cellCountZ;
   private final SurfaceNoise surfaceNoise;
   private final NormalNoise barrierNoise;
   private final NormalNoise waterLevelNoise;
   protected final BlockState defaultBlock;
   protected final BlockState defaultFluid;
   private final long seed;
   protected final Supplier<NoiseGeneratorSettings> settings;
   private final int height;
   private final NoiseSampler sampler;
   private final boolean aquifersEnabled;
   private final BaseStoneSource baseStoneSource;

   public NoiseBasedChunkGenerator(BiomeSource var1, long var2, Supplier<NoiseGeneratorSettings> var4) {
      this(var1, var1, var2, var4);
   }

   private NoiseBasedChunkGenerator(BiomeSource var1, BiomeSource var2, long var3, Supplier<NoiseGeneratorSettings> var5) {
      super(var1, var2, ((NoiseGeneratorSettings)var5.get()).structureSettings(), var3);
      this.seed = var3;
      NoiseGeneratorSettings var6 = (NoiseGeneratorSettings)var5.get();
      this.settings = var5;
      NoiseSettings var7 = var6.noiseSettings();
      this.height = var7.height();
      this.cellHeight = QuartPos.toBlock(var7.noiseSizeVertical());
      this.cellWidth = QuartPos.toBlock(var7.noiseSizeHorizontal());
      this.defaultBlock = var6.getDefaultBlock();
      this.defaultFluid = var6.getDefaultFluid();
      this.cellCountX = 16 / this.cellWidth;
      this.cellCountY = var7.height() / this.cellHeight;
      this.cellCountZ = 16 / this.cellWidth;
      WorldgenRandom var8 = new WorldgenRandom(var3);
      BlendedNoise var9 = new BlendedNoise(var8);
      this.surfaceNoise = (SurfaceNoise)(var7.useSimplexSurfaceNoise() ? new PerlinSimplexNoise(var8, IntStream.rangeClosed(-3, 0)) : new PerlinNoise(var8, IntStream.rangeClosed(-3, 0)));
      var8.consumeCount(2620);
      PerlinNoise var10 = new PerlinNoise(var8, IntStream.rangeClosed(-15, 0));
      SimplexNoise var11;
      if (var7.islandNoiseOverride()) {
         WorldgenRandom var12 = new WorldgenRandom(var3);
         var12.consumeCount(17292);
         var11 = new SimplexNoise(var12);
      } else {
         var11 = null;
      }

      this.barrierNoise = NormalNoise.create(new SimpleRandomSource(var8.nextLong()), -3, (double[])(1.0D));
      this.waterLevelNoise = NormalNoise.create(new SimpleRandomSource(var8.nextLong()), -3, (double[])(1.0D, 0.0D, 2.0D));
      Cavifier var13 = var6.isNoiseCavesEnabled() ? new Cavifier(var8, var7.minY() / this.cellHeight) : null;
      this.sampler = new NoiseSampler(var1, this.cellWidth, this.cellHeight, this.cellCountY, var7, var9, var11, var10, var13);
      this.aquifersEnabled = var6.isAquifersEnabled();
      this.baseStoneSource = new DepthBasedReplacingBaseStoneSource(var3, this.defaultBlock, Blocks.GRIMSTONE.defaultBlockState());
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public ChunkGenerator withSeed(long var1) {
      return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(var1), var1, this.settings);
   }

   public boolean stable(long var1, ResourceKey<NoiseGeneratorSettings> var3) {
      return this.seed == var1 && ((NoiseGeneratorSettings)this.settings.get()).stable(var3);
   }

   private double[] makeAndFillNoiseColumn(int var1, int var2, int var3, int var4) {
      double[] var5 = new double[var4 + 1];
      this.sampler.fillNoiseColumn(var5, var1, var2, ((NoiseGeneratorSettings)this.settings.get()).noiseSettings(), this.getSeaLevel(), var3, var4);
      return var5;
   }

   public int getBaseHeight(int var1, int var2, Heightmap.Types var3, LevelHeightAccessor var4) {
      int var5 = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), var4.getMinBuildHeight());
      int var6 = Math.min(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(), var4.getMaxBuildHeight());
      int var7 = Mth.intFloorDiv(var5, this.cellHeight);
      int var8 = Mth.intFloorDiv(var6 - var5, this.cellHeight);
      return var8 <= 0 ? var4.getMinBuildHeight() : this.iterateNoiseColumn(var1, var2, (BlockState[])null, var3.isOpaque(), var7, var8).orElse(var4.getMinBuildHeight());
   }

   public NoiseColumn getBaseColumn(int var1, int var2, LevelHeightAccessor var3) {
      int var4 = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), var3.getMinBuildHeight());
      int var5 = Math.min(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(), var3.getMaxBuildHeight());
      int var6 = Mth.intFloorDiv(var4, this.cellHeight);
      int var7 = Mth.intFloorDiv(var5 - var4, this.cellHeight);
      if (var7 <= 0) {
         return new NoiseColumn(var4, EMPTY_COLUMN);
      } else {
         BlockState[] var8 = new BlockState[var7 * this.cellHeight];
         this.iterateNoiseColumn(var1, var2, var8, (Predicate)null, var6, var7);
         return new NoiseColumn(var4, var8);
      }
   }

   private OptionalInt iterateNoiseColumn(int var1, int var2, @Nullable BlockState[] var3, @Nullable Predicate<BlockState> var4, int var5, int var6) {
      int var7 = SectionPos.blockToSectionCoord(var1);
      int var8 = SectionPos.blockToSectionCoord(var2);
      int var9 = Math.floorDiv(var1, this.cellWidth);
      int var10 = Math.floorDiv(var2, this.cellWidth);
      int var11 = Math.floorMod(var1, this.cellWidth);
      int var12 = Math.floorMod(var2, this.cellWidth);
      double var13 = (double)var11 / (double)this.cellWidth;
      double var15 = (double)var12 / (double)this.cellWidth;
      double[][] var17 = new double[][]{this.makeAndFillNoiseColumn(var9, var10, var5, var6), this.makeAndFillNoiseColumn(var9, var10 + 1, var5, var6), this.makeAndFillNoiseColumn(var9 + 1, var10, var5, var6), this.makeAndFillNoiseColumn(var9 + 1, var10 + 1, var5, var6)};
      Aquifer var18 = this.aquifersEnabled ? new Aquifer(var7, var8, this.barrierNoise, this.waterLevelNoise, (NoiseGeneratorSettings)this.settings.get(), this.sampler, var6 * this.cellHeight) : null;

      for(int var19 = var6 - 1; var19 >= 0; --var19) {
         double var20 = var17[0][var19];
         double var22 = var17[1][var19];
         double var24 = var17[2][var19];
         double var26 = var17[3][var19];
         double var28 = var17[0][var19 + 1];
         double var30 = var17[1][var19 + 1];
         double var32 = var17[2][var19 + 1];
         double var34 = var17[3][var19 + 1];

         for(int var36 = this.cellHeight - 1; var36 >= 0; --var36) {
            double var37 = (double)var36 / (double)this.cellHeight;
            double var39 = Mth.lerp3(var37, var13, var15, var20, var28, var24, var32, var22, var30, var26, var34);
            int var41 = var19 * this.cellHeight + var36;
            int var42 = var41 + var5 * this.cellHeight;
            BlockState var43 = this.updateNoiseAndGenerateBaseState(Beardifier.NO_BEARDS, var18, this.baseStoneSource, var1, var42, var2, var39);
            if (var3 != null) {
               var3[var41] = var43;
            }

            if (var4 != null && var4.test(var43)) {
               return OptionalInt.of(var42 + 1);
            }
         }
      }

      return OptionalInt.empty();
   }

   protected BlockState updateNoiseAndGenerateBaseState(Beardifier var1, @Nullable Aquifer var2, BaseStoneSource var3, int var4, int var5, int var6, double var7) {
      double var9 = Mth.clamp(var7 / 200.0D, -1.0D, 1.0D);
      var9 = var9 / 2.0D - var9 * var9 * var9 / 24.0D;
      var9 += var1.beardifyOrBury(var4, var5, var6);
      if (var2 != null) {
         var2.computeAt(var4, var5, var6);
         var9 += var2.getLastBarrierDensity();
      }

      BlockState var11;
      if (var9 > 0.0D) {
         var11 = var3.getBaseStone(var4, var5, var6, (NoiseGeneratorSettings)this.settings.get());
      } else {
         int var12 = var2 == null ? this.getSeaLevel() : var2.getLastWaterLevel();
         if (var5 < var12) {
            var11 = this.defaultFluid;
         } else {
            var11 = AIR;
         }
      }

      return var11;
   }

   public void buildSurfaceAndBedrock(WorldGenRegion var1, ChunkAccess var2) {
      ChunkPos var3 = var2.getPos();
      int var4 = var3.x;
      int var5 = var3.z;
      WorldgenRandom var6 = new WorldgenRandom();
      var6.setBaseChunkSeed(var4, var5);
      ChunkPos var7 = var2.getPos();
      int var8 = var7.getMinBlockX();
      int var9 = var7.getMinBlockZ();
      double var10 = 0.0625D;
      BlockPos.MutableBlockPos var12 = new BlockPos.MutableBlockPos();

      for(int var13 = 0; var13 < 16; ++var13) {
         for(int var14 = 0; var14 < 16; ++var14) {
            int var15 = var8 + var13;
            int var16 = var9 + var14;
            int var17 = var2.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var13, var14) + 1;
            double var18 = this.surfaceNoise.getSurfaceNoiseValue((double)var15 * 0.0625D, (double)var16 * 0.0625D, 0.0625D, (double)var13 * 0.0625D) * 15.0D;
            var1.getBiome(var12.set(var8 + var13, var17, var9 + var14)).buildSurfaceAt(var6, var2, var15, var16, var17, var18, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), var1.getSeed());
         }
      }

      this.setBedrock(var2, var6);
   }

   private void setBedrock(ChunkAccess var1, Random var2) {
      BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();
      int var4 = var1.getPos().getMinBlockX();
      int var5 = var1.getPos().getMinBlockZ();
      NoiseGeneratorSettings var6 = (NoiseGeneratorSettings)this.settings.get();
      int var7 = var6.noiseSettings().minY();
      int var8 = var7 + var6.getBedrockFloorPosition();
      int var9 = this.height - 1 + var7 - var6.getBedrockRoofPosition();
      boolean var10 = true;
      int var11 = var1.getMinBuildHeight();
      int var12 = var1.getMaxBuildHeight();
      boolean var13 = var9 + 5 - 1 >= var11 && var9 < var12;
      boolean var14 = var8 + 5 - 1 >= var11 && var8 < var12;
      if (var13 || var14) {
         Iterator var15 = BlockPos.betweenClosed(var4, 0, var5, var4 + 15, 0, var5 + 15).iterator();

         while(true) {
            BlockPos var16;
            int var17;
            do {
               if (!var15.hasNext()) {
                  return;
               }

               var16 = (BlockPos)var15.next();
               if (var13) {
                  for(var17 = 0; var17 < 5; ++var17) {
                     if (var17 <= var2.nextInt(5)) {
                        var1.setBlockState(var3.set(var16.getX(), var9 - var17, var16.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                     }
                  }
               }
            } while(!var14);

            for(var17 = 4; var17 >= 0; --var17) {
               if (var17 <= var2.nextInt(5)) {
                  var1.setBlockState(var3.set(var16.getX(), var8 + var17, var16.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
               }
            }
         }
      }
   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor var1, StructureFeatureManager var2, ChunkAccess var3) {
      NoiseSettings var4 = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
      int var5 = Math.max(var4.minY(), var3.getMinBuildHeight());
      int var6 = Math.min(var4.minY() + var4.height(), var3.getMaxBuildHeight());
      int var7 = Mth.intFloorDiv(var5, this.cellHeight);
      int var8 = Mth.intFloorDiv(var6 - var5, this.cellHeight);
      if (var8 <= 0) {
         return CompletableFuture.completedFuture(var3);
      } else {
         int var9 = var3.getSectionIndex(var8 * this.cellHeight - 1 + var5);
         int var10 = var3.getSectionIndex(var5);
         HashSet var11 = Sets.newHashSet();

         for(int var12 = var9; var12 >= var10; --var12) {
            LevelChunkSection var13 = var3.getOrCreateSection(var12);
            var13.acquire();
            var11.add(var13);
         }

         return CompletableFuture.supplyAsync(() -> {
            return this.doFill(var2, var3, var7, var8);
         }, Util.backgroundExecutor()).thenApplyAsync((var1x) -> {
            Iterator var2 = var11.iterator();

            while(var2.hasNext()) {
               LevelChunkSection var3 = (LevelChunkSection)var2.next();
               var3.release();
            }

            return var1x;
         }, var1);
      }
   }

   private ChunkAccess doFill(StructureFeatureManager var1, ChunkAccess var2, int var3, int var4) {
      NoiseSettings var5 = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
      int var6 = var5.minY();
      Heightmap var7 = var2.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
      Heightmap var8 = var2.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
      ChunkPos var9 = var2.getPos();
      int var10 = var9.x;
      int var11 = var9.z;
      int var12 = var9.getMinBlockX();
      int var13 = var9.getMinBlockZ();
      Beardifier var14 = new Beardifier(var1, var2);
      Aquifer var15 = this.aquifersEnabled ? new Aquifer(var10, var11, this.barrierNoise, this.waterLevelNoise, (NoiseGeneratorSettings)this.settings.get(), this.sampler, var4 * this.cellHeight) : null;
      double[][][] var16 = new double[2][this.cellCountZ + 1][var4 + 1];

      int var19;
      int var20;
      for(int var17 = 0; var17 < this.cellCountZ + 1; ++var17) {
         var16[0][var17] = new double[var4 + 1];
         double[] var18 = var16[0][var17];
         var19 = var10 * this.cellCountX;
         var20 = var11 * this.cellCountZ + var17;
         this.sampler.fillNoiseColumn(var18, var19, var20, var5, this.getSeaLevel(), var3, var4);
         var16[1][var17] = new double[var4 + 1];
      }

      BlockPos.MutableBlockPos var70 = new BlockPos.MutableBlockPos();

      for(int var71 = 0; var71 < this.cellCountX; ++var71) {
         var19 = var10 * this.cellCountX + var71 + 1;

         int var22;
         for(var20 = 0; var20 < this.cellCountZ + 1; ++var20) {
            double[] var21 = var16[1][var20];
            var22 = var11 * this.cellCountZ + var20;
            this.sampler.fillNoiseColumn(var21, var19, var22, var5, this.getSeaLevel(), var3, var4);
         }

         for(var20 = 0; var20 < this.cellCountZ; ++var20) {
            LevelChunkSection var72 = var2.getOrCreateSection(var2.getSectionsCount() - 1);

            for(var22 = var4 - 1; var22 >= 0; --var22) {
               double var23 = var16[0][var20][var22];
               double var25 = var16[0][var20 + 1][var22];
               double var27 = var16[1][var20][var22];
               double var29 = var16[1][var20 + 1][var22];
               double var31 = var16[0][var20][var22 + 1];
               double var33 = var16[0][var20 + 1][var22 + 1];
               double var35 = var16[1][var20][var22 + 1];
               double var37 = var16[1][var20 + 1][var22 + 1];

               for(int var39 = this.cellHeight - 1; var39 >= 0; --var39) {
                  int var40 = var22 * this.cellHeight + var39 + var6;
                  int var41 = var40 & 15;
                  int var42 = var2.getSectionIndex(var40);
                  if (var2.getSectionIndex(var72.bottomBlockY()) != var42) {
                     var72 = var2.getOrCreateSection(var42);
                  }

                  double var43 = (double)var39 / (double)this.cellHeight;
                  double var45 = Mth.lerp(var43, var23, var31);
                  double var47 = Mth.lerp(var43, var27, var35);
                  double var49 = Mth.lerp(var43, var25, var33);
                  double var51 = Mth.lerp(var43, var29, var37);

                  for(int var53 = 0; var53 < this.cellWidth; ++var53) {
                     int var54 = var12 + var71 * this.cellWidth + var53;
                     int var55 = var54 & 15;
                     double var56 = (double)var53 / (double)this.cellWidth;
                     double var58 = Mth.lerp(var56, var45, var47);
                     double var60 = Mth.lerp(var56, var49, var51);

                     for(int var62 = 0; var62 < this.cellWidth; ++var62) {
                        int var63 = var13 + var20 * this.cellWidth + var62;
                        int var64 = var63 & 15;
                        double var65 = (double)var62 / (double)this.cellWidth;
                        double var67 = Mth.lerp(var65, var58, var60);
                        BlockState var69 = this.updateNoiseAndGenerateBaseState(var14, var15, this.baseStoneSource, var54, var40, var63, var67);
                        if (var69 != AIR) {
                           if (var69.getLightEmission() != 0 && var2 instanceof ProtoChunk) {
                              var70.set(var54, var40, var63);
                              ((ProtoChunk)var2).addLight(var70);
                           }

                           var72.setBlockState(var55, var41, var64, var69, false);
                           var7.update(var55, var40, var64, var69);
                           var8.update(var55, var40, var64, var69);
                           if (var15 != null && var15.shouldScheduleWaterUpdate() && !var69.getFluidState().isEmpty()) {
                              var70.set(var54, var40, var63);
                              var2.getLiquidTicks().scheduleTick(var70, var69.getFluidState().getType(), 0);
                           }
                        }
                     }
                  }
               }
            }
         }

         this.swapFirstTwoElements(var16);
      }

      return var2;
   }

   public <T> void swapFirstTwoElements(T[] var1) {
      Object var2 = var1[0];
      var1[0] = var1[1];
      var1[1] = var2;
   }

   public int getGenDepth() {
      return this.height;
   }

   public int getSeaLevel() {
      return ((NoiseGeneratorSettings)this.settings.get()).seaLevel();
   }

   public int getMinY() {
      return ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY();
   }

   public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome var1, StructureFeatureManager var2, MobCategory var3, BlockPos var4) {
      if (var2.getStructureAt(var4, true, StructureFeature.SWAMP_HUT).isValid()) {
         if (var3 == MobCategory.MONSTER) {
            return StructureFeature.SWAMP_HUT.getSpecialEnemies();
         }

         if (var3 == MobCategory.CREATURE) {
            return StructureFeature.SWAMP_HUT.getSpecialAnimals();
         }
      }

      if (var3 == MobCategory.MONSTER) {
         if (var2.getStructureAt(var4, false, StructureFeature.PILLAGER_OUTPOST).isValid()) {
            return StructureFeature.PILLAGER_OUTPOST.getSpecialEnemies();
         }

         if (var2.getStructureAt(var4, false, StructureFeature.OCEAN_MONUMENT).isValid()) {
            return StructureFeature.OCEAN_MONUMENT.getSpecialEnemies();
         }

         if (var2.getStructureAt(var4, true, StructureFeature.NETHER_BRIDGE).isValid()) {
            return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
         }
      }

      return super.getMobsAt(var1, var2, var3, var4);
   }

   public void spawnOriginalMobs(WorldGenRegion var1) {
      if (!((NoiseGeneratorSettings)this.settings.get()).disableMobGeneration()) {
         ChunkPos var2 = var1.getCenter();
         Biome var3 = var1.getBiome(var2.getWorldPosition());
         WorldgenRandom var4 = new WorldgenRandom();
         var4.setDecorationSeed(var1.getSeed(), var2.getMinBlockX(), var2.getMinBlockZ());
         NaturalSpawner.spawnMobsForChunkGeneration(var1, var3, var2, var4);
      }
   }

   static {
      AIR = Blocks.AIR.defaultBlockState();
      EMPTY_COLUMN = new BlockState[0];
   }
}
