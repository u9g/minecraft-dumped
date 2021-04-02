package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class OreFeature extends Feature<OreConfiguration> {
   public OreFeature(Codec<OreConfiguration> var1) {
      super(var1);
   }

   public boolean place(FeaturePlaceContext<OreConfiguration> var1) {
      Random var2 = var1.random();
      BlockPos var3 = var1.origin();
      WorldGenLevel var4 = var1.level();
      OreConfiguration var5 = (OreConfiguration)var1.config();
      float var6 = var2.nextFloat() * 3.1415927F;
      float var7 = (float)var5.size / 8.0F;
      int var8 = Mth.ceil(((float)var5.size / 16.0F * 2.0F + 1.0F) / 2.0F);
      double var9 = (double)var3.getX() + Math.sin((double)var6) * (double)var7;
      double var11 = (double)var3.getX() - Math.sin((double)var6) * (double)var7;
      double var13 = (double)var3.getZ() + Math.cos((double)var6) * (double)var7;
      double var15 = (double)var3.getZ() - Math.cos((double)var6) * (double)var7;
      boolean var17 = true;
      double var18 = (double)(var3.getY() + var2.nextInt(3) - 2);
      double var20 = (double)(var3.getY() + var2.nextInt(3) - 2);
      int var22 = var3.getX() - Mth.ceil(var7) - var8;
      int var23 = var3.getY() - 2 - var8;
      int var24 = var3.getZ() - Mth.ceil(var7) - var8;
      int var25 = 2 * (Mth.ceil(var7) + var8);
      int var26 = 2 * (2 + var8);

      for(int var27 = var22; var27 <= var22 + var25; ++var27) {
         for(int var28 = var24; var28 <= var24 + var25; ++var28) {
            if (var23 <= var4.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var27, var28)) {
               return this.doPlace(var4, var2, var5, var9, var11, var13, var15, var18, var20, var22, var23, var24, var25, var26);
            }
         }
      }

      return false;
   }

   protected boolean doPlace(LevelAccessor var1, Random var2, OreConfiguration var3, double var4, double var6, double var8, double var10, double var12, double var14, int var16, int var17, int var18, int var19, int var20) {
      int var21 = 0;
      BitSet var22 = new BitSet(var19 * var20 * var19);
      BlockPos.MutableBlockPos var23 = new BlockPos.MutableBlockPos();
      int var24 = var3.size;
      double[] var25 = new double[var24 * 4];

      int var26;
      double var28;
      double var30;
      double var32;
      double var34;
      for(var26 = 0; var26 < var24; ++var26) {
         float var27 = (float)var26 / (float)var24;
         var28 = Mth.lerp((double)var27, var4, var6);
         var30 = Mth.lerp((double)var27, var12, var14);
         var32 = Mth.lerp((double)var27, var8, var10);
         var34 = var2.nextDouble() * (double)var24 / 16.0D;
         double var36 = ((double)(Mth.sin(3.1415927F * var27) + 1.0F) * var34 + 1.0D) / 2.0D;
         var25[var26 * 4 + 0] = var28;
         var25[var26 * 4 + 1] = var30;
         var25[var26 * 4 + 2] = var32;
         var25[var26 * 4 + 3] = var36;
      }

      int var58;
      for(var26 = 0; var26 < var24 - 1; ++var26) {
         if (var25[var26 * 4 + 3] > 0.0D) {
            for(var58 = var26 + 1; var58 < var24; ++var58) {
               if (var25[var58 * 4 + 3] > 0.0D) {
                  var28 = var25[var26 * 4 + 0] - var25[var58 * 4 + 0];
                  var30 = var25[var26 * 4 + 1] - var25[var58 * 4 + 1];
                  var32 = var25[var26 * 4 + 2] - var25[var58 * 4 + 2];
                  var34 = var25[var26 * 4 + 3] - var25[var58 * 4 + 3];
                  if (var34 * var34 > var28 * var28 + var30 * var30 + var32 * var32) {
                     if (var34 > 0.0D) {
                        var25[var58 * 4 + 3] = -1.0D;
                     } else {
                        var25[var26 * 4 + 3] = -1.0D;
                     }
                  }
               }
            }
         }
      }

      HashSet var57 = Sets.newHashSet();

      for(var58 = 0; var58 < var24; ++var58) {
         var28 = var25[var58 * 4 + 3];
         if (var28 >= 0.0D) {
            var30 = var25[var58 * 4 + 0];
            var32 = var25[var58 * 4 + 1];
            var34 = var25[var58 * 4 + 2];
            int var61 = Math.max(Mth.floor(var30 - var28), var16);
            int var37 = Math.max(Mth.floor(var32 - var28), var17);
            int var38 = Math.max(Mth.floor(var34 - var28), var18);
            int var39 = Math.max(Mth.floor(var30 + var28), var61);
            int var40 = Math.max(Mth.floor(var32 + var28), var37);
            int var41 = Math.max(Mth.floor(var34 + var28), var38);

            for(int var42 = var61; var42 <= var39; ++var42) {
               double var43 = ((double)var42 + 0.5D - var30) / var28;
               if (var43 * var43 < 1.0D) {
                  for(int var45 = var37; var45 <= var40; ++var45) {
                     double var46 = ((double)var45 + 0.5D - var32) / var28;
                     if (var43 * var43 + var46 * var46 < 1.0D) {
                        for(int var48 = var38; var48 <= var41; ++var48) {
                           double var49 = ((double)var48 + 0.5D - var34) / var28;
                           if (var43 * var43 + var46 * var46 + var49 * var49 < 1.0D && !var1.isOutsideBuildHeight(var45)) {
                              int var51 = var42 - var16 + (var45 - var17) * var19 + (var48 - var18) * var19 * var20;
                              if (!var22.get(var51)) {
                                 var22.set(var51);
                                 var23.set(var42, var45, var48);
                                 ChunkAccess var52 = var1.getChunk(SectionPos.blockToSectionCoord(var42), SectionPos.blockToSectionCoord(var48));
                                 LevelChunkSection var53 = var52.getOrCreateSection(var52.getSectionIndex(var45));
                                 if (var57.add(var53)) {
                                    var53.acquire();
                                 }

                                 int var54 = SectionPos.sectionRelative(var42);
                                 int var55 = SectionPos.sectionRelative(var45);
                                 int var56 = SectionPos.sectionRelative(var48);
                                 if (var3.target.test(var53.getBlockState(var54, var55, var56), var2)) {
                                    var53.setBlockState(var54, var55, var56, var3.state, false);
                                    ++var21;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      Iterator var59 = var57.iterator();

      while(var59.hasNext()) {
         LevelChunkSection var60 = (LevelChunkSection)var59.next();
         var60.release();
      }

      return var21 > 0;
   }
}
