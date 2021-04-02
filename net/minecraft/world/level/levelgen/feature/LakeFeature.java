package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

public class LakeFeature extends Feature<BlockStateConfiguration> {
   private static final BlockState AIR;

   public LakeFeature(Codec<BlockStateConfiguration> var1) {
      super(var1);
   }

   public boolean place(FeaturePlaceContext<BlockStateConfiguration> var1) {
      BlockPos var2 = var1.origin();
      WorldGenLevel var3 = var1.level();
      Random var4 = var1.random();

      BlockStateConfiguration var5;
      for(var5 = (BlockStateConfiguration)var1.config(); var2.getY() > var3.getMinBuildHeight() + 5 && var3.isEmptyBlock(var2); var2 = var2.below()) {
      }

      if (var2.getY() <= var3.getMinBuildHeight() + 4) {
         return false;
      } else {
         var2 = var2.below(4);
         if (var3.startsForFeature(SectionPos.of(var2), StructureFeature.VILLAGE).findAny().isPresent()) {
            return false;
         } else {
            boolean[] var6 = new boolean[2048];
            int var7 = var4.nextInt(4) + 4;

            int var8;
            for(var8 = 0; var8 < var7; ++var8) {
               double var9 = var4.nextDouble() * 6.0D + 3.0D;
               double var11 = var4.nextDouble() * 4.0D + 2.0D;
               double var13 = var4.nextDouble() * 6.0D + 3.0D;
               double var15 = var4.nextDouble() * (16.0D - var9 - 2.0D) + 1.0D + var9 / 2.0D;
               double var17 = var4.nextDouble() * (8.0D - var11 - 4.0D) + 2.0D + var11 / 2.0D;
               double var19 = var4.nextDouble() * (16.0D - var13 - 2.0D) + 1.0D + var13 / 2.0D;

               for(int var21 = 1; var21 < 15; ++var21) {
                  for(int var22 = 1; var22 < 15; ++var22) {
                     for(int var23 = 1; var23 < 7; ++var23) {
                        double var24 = ((double)var21 - var15) / (var9 / 2.0D);
                        double var26 = ((double)var23 - var17) / (var11 / 2.0D);
                        double var28 = ((double)var22 - var19) / (var13 / 2.0D);
                        double var30 = var24 * var24 + var26 * var26 + var28 * var28;
                        if (var30 < 1.0D) {
                           var6[(var21 * 16 + var22) * 8 + var23] = true;
                        }
                     }
                  }
               }
            }

            int var10;
            int var32;
            boolean var33;
            for(var8 = 0; var8 < 16; ++var8) {
               for(var32 = 0; var32 < 16; ++var32) {
                  for(var10 = 0; var10 < 8; ++var10) {
                     var33 = !var6[(var8 * 16 + var32) * 8 + var10] && (var8 < 15 && var6[((var8 + 1) * 16 + var32) * 8 + var10] || var8 > 0 && var6[((var8 - 1) * 16 + var32) * 8 + var10] || var32 < 15 && var6[(var8 * 16 + var32 + 1) * 8 + var10] || var32 > 0 && var6[(var8 * 16 + (var32 - 1)) * 8 + var10] || var10 < 7 && var6[(var8 * 16 + var32) * 8 + var10 + 1] || var10 > 0 && var6[(var8 * 16 + var32) * 8 + (var10 - 1)]);
                     if (var33) {
                        Material var12 = var3.getBlockState(var2.offset(var8, var10, var32)).getMaterial();
                        if (var10 >= 4 && var12.isLiquid()) {
                           return false;
                        }

                        if (var10 < 4 && !var12.isSolid() && var3.getBlockState(var2.offset(var8, var10, var32)) != var5.state) {
                           return false;
                        }
                     }
                  }
               }
            }

            for(var8 = 0; var8 < 16; ++var8) {
               for(var32 = 0; var32 < 16; ++var32) {
                  for(var10 = 0; var10 < 8; ++var10) {
                     if (var6[(var8 * 16 + var32) * 8 + var10]) {
                        var3.setBlock(var2.offset(var8, var10, var32), var10 >= 4 ? AIR : var5.state, 2);
                     }
                  }
               }
            }

            BlockPos var34;
            for(var8 = 0; var8 < 16; ++var8) {
               for(var32 = 0; var32 < 16; ++var32) {
                  for(var10 = 4; var10 < 8; ++var10) {
                     if (var6[(var8 * 16 + var32) * 8 + var10]) {
                        var34 = var2.offset(var8, var10 - 1, var32);
                        if (isDirt(var3.getBlockState(var34)) && var3.getBrightness(LightLayer.SKY, var2.offset(var8, var10, var32)) > 0) {
                           Biome var35 = var3.getBiome(var34);
                           if (var35.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(Blocks.MYCELIUM)) {
                              var3.setBlock(var34, Blocks.MYCELIUM.defaultBlockState(), 2);
                           } else {
                              var3.setBlock(var34, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                           }
                        }
                     }
                  }
               }
            }

            if (var5.state.getMaterial() == Material.LAVA) {
               for(var8 = 0; var8 < 16; ++var8) {
                  for(var32 = 0; var32 < 16; ++var32) {
                     for(var10 = 0; var10 < 8; ++var10) {
                        var33 = !var6[(var8 * 16 + var32) * 8 + var10] && (var8 < 15 && var6[((var8 + 1) * 16 + var32) * 8 + var10] || var8 > 0 && var6[((var8 - 1) * 16 + var32) * 8 + var10] || var32 < 15 && var6[(var8 * 16 + var32 + 1) * 8 + var10] || var32 > 0 && var6[(var8 * 16 + (var32 - 1)) * 8 + var10] || var10 < 7 && var6[(var8 * 16 + var32) * 8 + var10 + 1] || var10 > 0 && var6[(var8 * 16 + var32) * 8 + (var10 - 1)]);
                        if (var33 && (var10 < 4 || var4.nextInt(2) != 0) && var3.getBlockState(var2.offset(var8, var10, var32)).getMaterial().isSolid()) {
                           var3.setBlock(var2.offset(var8, var10, var32), Blocks.STONE.defaultBlockState(), 2);
                        }
                     }
                  }
               }
            }

            if (var5.state.getMaterial() == Material.WATER) {
               for(var8 = 0; var8 < 16; ++var8) {
                  for(var32 = 0; var32 < 16; ++var32) {
                     boolean var36 = true;
                     var34 = var2.offset(var8, 4, var32);
                     if (var3.getBiome(var34).shouldFreeze(var3, var34, false)) {
                        var3.setBlock(var34, Blocks.ICE.defaultBlockState(), 2);
                     }
                  }
               }
            }

            return true;
         }
      }
   }

   static {
      AIR = Blocks.CAVE_AIR.defaultBlockState();
   }
}
