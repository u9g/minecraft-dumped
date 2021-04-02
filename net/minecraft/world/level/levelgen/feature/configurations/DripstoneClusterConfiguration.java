package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;

public class DripstoneClusterConfiguration implements FeatureConfiguration {
   public static final Codec<DripstoneClusterConfiguration> CODEC = RecordCodecBuilder.create((var0) -> {
      return var0.group(Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").forGetter((var0x) -> {
         return var0x.floorToCeilingSearchRange;
      }), UniformInt.codec(1, 64, 64).fieldOf("height").forGetter((var0x) -> {
         return var0x.height;
      }), UniformInt.codec(1, 64, 64).fieldOf("radius").forGetter((var0x) -> {
         return var0x.radius;
      }), Codec.intRange(0, 64).fieldOf("max_stalagmite_stalactite_height_diff").forGetter((var0x) -> {
         return var0x.maxStalagmiteStalactiteHeightDiff;
      }), Codec.intRange(1, 64).fieldOf("height_deviation").forGetter((var0x) -> {
         return var0x.heightDeviation;
      }), UniformInt.codec(0, 64, 64).fieldOf("dripstone_block_layer_thickness").forGetter((var0x) -> {
         return var0x.dripstoneBlockLayerThickness;
      }), UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("density").forGetter((var0x) -> {
         return var0x.density;
      }), UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("wetness").forGetter((var0x) -> {
         return var0x.wetness;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("wetness_mean").forGetter((var0x) -> {
         return var0x.wetnessMean;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("wetness_deviation").forGetter((var0x) -> {
         return var0x.wetnessDeviation;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_dripstone_column_at_max_distance_from_center").forGetter((var0x) -> {
         return var0x.chanceOfDripstoneColumnAtMaxDistanceFromCenter;
      }), Codec.intRange(1, 64).fieldOf("max_distance_from_edge_affecting_chance_of_dripstone_column").forGetter((var0x) -> {
         return var0x.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn;
      }), Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter((var0x) -> {
         return var0x.maxDistanceFromCenterAffectingHeightBias;
      })).apply(var0, DripstoneClusterConfiguration::new);
   });
   public final int floorToCeilingSearchRange;
   public final UniformInt height;
   public final UniformInt radius;
   public final int maxStalagmiteStalactiteHeightDiff;
   public final int heightDeviation;
   public final UniformInt dripstoneBlockLayerThickness;
   public final UniformFloat density;
   public final UniformFloat wetness;
   public final float wetnessMean;
   public final float wetnessDeviation;
   public final float chanceOfDripstoneColumnAtMaxDistanceFromCenter;
   public final int maxDistanceFromEdgeAffectingChanceOfDripstoneColumn;
   public final int maxDistanceFromCenterAffectingHeightBias;

   public DripstoneClusterConfiguration(int var1, UniformInt var2, UniformInt var3, int var4, int var5, UniformInt var6, UniformFloat var7, UniformFloat var8, float var9, float var10, float var11, int var12, int var13) {
      super();
      this.floorToCeilingSearchRange = var1;
      this.height = var2;
      this.radius = var3;
      this.maxStalagmiteStalactiteHeightDiff = var4;
      this.heightDeviation = var5;
      this.dripstoneBlockLayerThickness = var6;
      this.density = var7;
      this.wetness = var8;
      this.wetnessMean = var9;
      this.wetnessDeviation = var10;
      this.chanceOfDripstoneColumnAtMaxDistanceFromCenter = var11;
      this.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn = var12;
      this.maxDistanceFromCenterAffectingHeightBias = var13;
   }
}
