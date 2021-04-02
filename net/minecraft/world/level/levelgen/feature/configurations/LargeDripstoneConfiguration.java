package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformFloat;
import net.minecraft.util.UniformInt;

public class LargeDripstoneConfiguration implements FeatureConfiguration {
   public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create((var0) -> {
      return var0.group(Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").orElse(30).forGetter((var0x) -> {
         return var0x.floorToCeilingSearchRange;
      }), UniformInt.codec(1, 30, 30).fieldOf("column_radius").forGetter((var0x) -> {
         return var0x.columnRadius;
      }), UniformFloat.codec(0.0F, 10.0F, 10.0F).fieldOf("height_scale").forGetter((var0x) -> {
         return var0x.heightScale;
      }), Codec.floatRange(0.1F, 1.0F).fieldOf("max_column_radius_to_cave_height_ratio").forGetter((var0x) -> {
         return var0x.maxColumnRadiusToCaveHeightRatio;
      }), UniformFloat.codec(0.1F, 5.0F, 5.0F).fieldOf("stalactite_bluntness").forGetter((var0x) -> {
         return var0x.stalactiteBluntness;
      }), UniformFloat.codec(0.1F, 5.0F, 5.0F).fieldOf("stalagmite_bluntness").forGetter((var0x) -> {
         return var0x.stalagmiteBluntness;
      }), UniformFloat.codec(0.0F, 1.0F, 1.0F).fieldOf("wind_speed").forGetter((var0x) -> {
         return var0x.windSpeed;
      }), Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter((var0x) -> {
         return var0x.minRadiusForWind;
      }), Codec.floatRange(0.0F, 5.0F).fieldOf("min_bluntness_for_wind").forGetter((var0x) -> {
         return var0x.minBluntnessForWind;
      })).apply(var0, LargeDripstoneConfiguration::new);
   });
   public final int floorToCeilingSearchRange;
   public final UniformInt columnRadius;
   public final UniformFloat heightScale;
   public final float maxColumnRadiusToCaveHeightRatio;
   public final UniformFloat stalactiteBluntness;
   public final UniformFloat stalagmiteBluntness;
   public final UniformFloat windSpeed;
   public final int minRadiusForWind;
   public final float minBluntnessForWind;

   public LargeDripstoneConfiguration(int var1, UniformInt var2, UniformFloat var3, float var4, UniformFloat var5, UniformFloat var6, UniformFloat var7, int var8, float var9) {
      super();
      this.floorToCeilingSearchRange = var1;
      this.columnRadius = var2;
      this.heightScale = var3;
      this.maxColumnRadiusToCaveHeightRatio = var4;
      this.stalactiteBluntness = var5;
      this.stalagmiteBluntness = var6;
      this.windSpeed = var7;
      this.minRadiusForWind = var8;
      this.minBluntnessForWind = var9;
   }
}
