package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public final class ImprovedNoise {
   private final byte[] p;
   public final double xo;
   public final double yo;
   public final double zo;

   public ImprovedNoise(RandomSource var1) {
      super();
      this.xo = var1.nextDouble() * 256.0D;
      this.yo = var1.nextDouble() * 256.0D;
      this.zo = var1.nextDouble() * 256.0D;
      this.p = new byte[256];

      int var2;
      for(var2 = 0; var2 < 256; ++var2) {
         this.p[var2] = (byte)var2;
      }

      for(var2 = 0; var2 < 256; ++var2) {
         int var3 = var1.nextInt(256 - var2);
         byte var4 = this.p[var2];
         this.p[var2] = this.p[var2 + var3];
         this.p[var2 + var3] = var4;
      }

   }

   public double noise(double var1, double var3, double var5) {
      return this.noise(var1, var3, var5, 0.0D, 0.0D);
   }

   @Deprecated
   public double noise(double var1, double var3, double var5, double var7, double var9) {
      double var11 = var1 + this.xo;
      double var13 = var3 + this.yo;
      double var15 = var5 + this.zo;
      int var17 = Mth.floor(var11);
      int var18 = Mth.floor(var13);
      int var19 = Mth.floor(var15);
      double var20 = var11 - (double)var17;
      double var22 = var13 - (double)var18;
      double var24 = var15 - (double)var19;
      double var26;
      if (var7 != 0.0D) {
         double var28;
         if (var9 >= 0.0D && var9 < var22) {
            var28 = var9;
         } else {
            var28 = var22;
         }

         var26 = (double)Mth.floor(var28 / var7 + 1.0000000116860974E-7D) * var7;
      } else {
         var26 = 0.0D;
      }

      return this.sampleAndLerp(var17, var18, var19, var20, var22 - var26, var24, var22);
   }

   private static double gradDot(int var0, double var1, double var3, double var5) {
      return SimplexNoise.dot(SimplexNoise.GRADIENT[var0 & 15], var1, var3, var5);
   }

   private int p(int var1) {
      return this.p[var1 & 255] & 255;
   }

   private double sampleAndLerp(int var1, int var2, int var3, double var4, double var6, double var8, double var10) {
      int var12 = this.p(var1);
      int var13 = this.p(var1 + 1);
      int var14 = this.p(var12 + var2);
      int var15 = this.p(var12 + var2 + 1);
      int var16 = this.p(var13 + var2);
      int var17 = this.p(var13 + var2 + 1);
      double var18 = gradDot(this.p(var14 + var3), var4, var6, var8);
      double var20 = gradDot(this.p(var16 + var3), var4 - 1.0D, var6, var8);
      double var22 = gradDot(this.p(var15 + var3), var4, var6 - 1.0D, var8);
      double var24 = gradDot(this.p(var17 + var3), var4 - 1.0D, var6 - 1.0D, var8);
      double var26 = gradDot(this.p(var14 + var3 + 1), var4, var6, var8 - 1.0D);
      double var28 = gradDot(this.p(var16 + var3 + 1), var4 - 1.0D, var6, var8 - 1.0D);
      double var30 = gradDot(this.p(var15 + var3 + 1), var4, var6 - 1.0D, var8 - 1.0D);
      double var32 = gradDot(this.p(var17 + var3 + 1), var4 - 1.0D, var6 - 1.0D, var8 - 1.0D);
      double var34 = Mth.smoothstep(var4);
      double var36 = Mth.smoothstep(var10);
      double var38 = Mth.smoothstep(var8);
      return Mth.lerp3(var34, var36, var38, var18, var20, var22, var24, var26, var28, var30, var32);
   }
}
