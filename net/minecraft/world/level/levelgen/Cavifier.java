package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class Cavifier {
   private final int minCellY;
   private final NormalNoise layerNoiseSource;
   private final NormalNoise pillarNoiseSource;
   private final NormalNoise pillarRarenessModulator;
   private final NormalNoise pillarThicknessModulator;
   private final NormalNoise spaghetti2dNoiseSource;
   private final NormalNoise spaghetti2dElevationModulator;
   private final NormalNoise spaghetti2dRarityModulator;
   private final NormalNoise spaghetti2dThicknessModulator;
   private final NormalNoise spaghetti3dNoiseSource1;
   private final NormalNoise spaghetti3dNoiseSource2;
   private final NormalNoise spaghetti3dRarityModulator;
   private final NormalNoise spaghetti3dThicknessModulator;
   private final NormalNoise spaghettiRoughnessNoise;
   private final NormalNoise spaghettiRoughnessModulator;
   private final NormalNoise caveEntranceNoiseSource;

   public Cavifier(RandomSource var1, int var2) {
      super();
      this.minCellY = var2;
      this.pillarNoiseSource = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -7, (double[])(1.0D, 1.0D));
      this.pillarRarenessModulator = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -8, (double[])(1.0D));
      this.pillarThicknessModulator = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -8, (double[])(1.0D));
      this.spaghetti2dNoiseSource = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -7, (double[])(1.0D));
      this.spaghetti2dElevationModulator = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -8, (double[])(1.0D));
      this.spaghetti2dRarityModulator = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -11, (double[])(1.0D));
      this.spaghetti2dThicknessModulator = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -11, (double[])(1.0D));
      this.spaghetti3dNoiseSource1 = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -7, (double[])(1.0D));
      this.spaghetti3dNoiseSource2 = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -7, (double[])(1.0D));
      this.spaghetti3dRarityModulator = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -11, (double[])(1.0D));
      this.spaghetti3dThicknessModulator = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -8, (double[])(1.0D));
      this.spaghettiRoughnessNoise = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -5, (double[])(1.0D));
      this.spaghettiRoughnessModulator = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -8, (double[])(1.0D));
      this.caveEntranceNoiseSource = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -8, (double[])(1.0D, 1.0D, 1.0D));
      this.layerNoiseSource = NormalNoise.create(new SimpleRandomSource(var1.nextLong()), -8, (double[])(1.0D));
   }

   public double cavify(int var1, int var2, int var3, double var4, double var6) {
      boolean var8 = var6 >= 375.0D;
      double var9 = this.spaghettiRoughness(var1, var2, var3);
      double var11 = this.getSpaghetti3d(var1, var2, var3);
      if (var8) {
         double var13 = var4 / 128.0D;
         double var15 = Mth.clamp(var13 + 0.35D, -1.0D, 1.0D);
         double var17 = this.getLayerizedCaverns(var1, var2, var3);
         double var19 = this.getSpaghetti2d(var1, var2, var3);
         double var21 = var15 + var17;
         double var23 = Math.min(var21, Math.min(var11, var19) + var9);
         double var25 = Math.max(var23, this.getPillars(var1, var2, var3));
         return 128.0D * Mth.clamp(var25, -1.0D, 1.0D);
      } else {
         return Math.min(var6, (var11 + var9) * 128.0D);
      }
   }

   private double getPillars(int var1, int var2, int var3) {
      double var4 = 0.0D;
      double var6 = 2.0D;
      double var8 = NoiseUtils.sampleNoiseAndMapToRange(this.pillarRarenessModulator, (double)var1, (double)var2, (double)var3, 0.0D, 2.0D);
      boolean var10 = false;
      boolean var11 = true;
      double var12 = NoiseUtils.sampleNoiseAndMapToRange(this.pillarThicknessModulator, (double)var1, (double)var2, (double)var3, 0.0D, 1.0D);
      var12 = Math.pow(var12, 3.0D);
      double var14 = 25.0D;
      double var16 = 0.3D;
      double var18 = this.pillarNoiseSource.getValue((double)var1 * 25.0D, (double)var2 * 0.3D, (double)var3 * 25.0D);
      var18 = var12 * (var18 * 2.0D - var8);
      return var18 > 0.02D ? var18 : -1.0D / 0.0;
   }

   private double getLayerizedCaverns(int var1, int var2, int var3) {
      double var4 = this.layerNoiseSource.getValue((double)var1, (double)(var2 * 8), (double)var3);
      return Mth.square(var4) * 4.0D;
   }

   private double getSpaghetti3d(int var1, int var2, int var3) {
      double var4 = this.spaghetti3dRarityModulator.getValue((double)(var1 * 2), (double)var2, (double)(var3 * 2));
      double var6 = Cavifier.QuantizedSpaghettiRarity.getSpaghettiRarity3D(var4);
      double var8 = 0.065D;
      double var10 = 0.085D;
      double var12 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti3dThicknessModulator, (double)var1, (double)var2, (double)var3, 0.065D, 0.085D);
      double var14 = sampleWithRarity(this.spaghetti3dNoiseSource1, (double)var1, (double)var2, (double)var3, var6);
      double var16 = Math.abs(var6 * var14) - var12;
      double var18 = sampleWithRarity(this.spaghetti3dNoiseSource2, (double)var1, (double)var2, (double)var3, var6);
      double var20 = Math.abs(var6 * var18) - var12;
      return clampToUnit(Math.max(var16, var20));
   }

   private double getSpaghetti2d(int var1, int var2, int var3) {
      double var4 = this.spaghetti2dRarityModulator.getValue((double)(var1 * 2), (double)var2, (double)(var3 * 2));
      double var6 = Cavifier.QuantizedSpaghettiRarity.getSphaghettiRarity2D(var4);
      double var8 = 0.6D;
      double var10 = 1.3D;
      double var12 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2dThicknessModulator, (double)(var1 * 2), (double)var2, (double)(var3 * 2), 0.6D, 1.3D);
      double var14 = sampleWithRarity(this.spaghetti2dNoiseSource, (double)var1, (double)var2, (double)var3, var6);
      double var16 = 0.083D;
      double var18 = Math.abs(var6 * var14) - 0.083D * var12;
      int var20 = this.minCellY;
      boolean var21 = true;
      double var22 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2dElevationModulator, (double)var1, 0.0D, (double)var3, (double)var20, 8.0D);
      double var24 = Math.abs(var22 - (double)var2 / 8.0D) - 1.0D * var12;
      var24 = var24 * var24 * var24;
      return clampToUnit(Math.max(var24, var18));
   }

   private double spaghettiRoughness(int var1, int var2, int var3) {
      double var4 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghettiRoughnessModulator, (double)var1, (double)var2, (double)var3, 0.0D, 0.1D);
      return (0.4D - Math.abs(this.spaghettiRoughnessNoise.getValue((double)var1, (double)var2, (double)var3))) * var4;
   }

   private static double clampToUnit(double var0) {
      return Mth.clamp(var0, -1.0D, 1.0D);
   }

   private static double sampleWithRarity(NormalNoise var0, double var1, double var3, double var5, double var7) {
      return var0.getValue(var1 / var7, var3 / var7, var5 / var7);
   }

   static final class QuantizedSpaghettiRarity {
      private static double getSphaghettiRarity2D(double var0) {
         if (var0 < -0.75D) {
            return 0.5D;
         } else if (var0 < -0.5D) {
            return 0.75D;
         } else if (var0 < 0.5D) {
            return 1.0D;
         } else {
            return var0 < 0.75D ? 2.0D : 3.0D;
         }
      }

      private static double getSpaghettiRarity3D(double var0) {
         if (var0 < -0.5D) {
            return 0.75D;
         } else if (var0 < 0.0D) {
            return 1.0D;
         } else {
            return var0 < 0.5D ? 2.0D : 3.0D;
         }
      }
   }
}
