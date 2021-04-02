package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.UniformFloat;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;

public class LargeDripstoneFeature extends Feature<LargeDripstoneConfiguration> {
   public LargeDripstoneFeature(Codec<LargeDripstoneConfiguration> var1) {
      super(var1);
   }

   public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> var1) {
      WorldGenLevel var2 = var1.level();
      BlockPos var3 = var1.origin();
      LargeDripstoneConfiguration var4 = (LargeDripstoneConfiguration)var1.config();
      Random var5 = var1.random();
      if (!DripstoneUtils.isEmptyOrWater(var2, var3)) {
         return false;
      } else {
         Optional var6 = Column.scan(var2, var3, var4.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isDripstoneBaseOrLava);
         if (var6.isPresent() && var6.get() instanceof Column.Range) {
            Column.Range var7 = (Column.Range)var6.get();
            if (var7.height() < 4) {
               return false;
            } else {
               int var8 = (int)((float)var7.height() * var4.maxColumnRadiusToCaveHeightRatio);
               int var9 = Mth.clamp(var8, var4.columnRadius.getBaseValue(), var4.columnRadius.getMaxValue());
               int var10 = Mth.randomBetweenInclusive(var5, var4.columnRadius.getBaseValue(), var9);
               LargeDripstoneFeature.LargeDripstone var11 = makeDripstone(var3.atY(var7.ceiling() - 1), false, var5, var10, var4.stalactiteBluntness, var4.heightScale);
               LargeDripstoneFeature.LargeDripstone var12 = makeDripstone(var3.atY(var7.floor() + 1), true, var5, var10, var4.stalagmiteBluntness, var4.heightScale);
               LargeDripstoneFeature.WindOffsetter var13;
               if (var11.isSuitableForWind(var4) && var12.isSuitableForWind(var4)) {
                  var13 = new LargeDripstoneFeature.WindOffsetter(var3.getY(), var5, var4.windSpeed);
               } else {
                  var13 = LargeDripstoneFeature.WindOffsetter.noWind();
               }

               boolean var14 = var11.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(var2, var13);
               boolean var15 = var12.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(var2, var13);
               if (var14) {
                  var11.placeBlocks(var2, var5, var13);
               }

               if (var15) {
                  var12.placeBlocks(var2, var5, var13);
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   private static LargeDripstoneFeature.LargeDripstone makeDripstone(BlockPos var0, boolean var1, Random var2, int var3, UniformFloat var4, UniformFloat var5) {
      return new LargeDripstoneFeature.LargeDripstone(var0, var1, var3, (double)var4.sample(var2), (double)var5.sample(var2));
   }

   static final class WindOffsetter {
      private final int originY;
      @Nullable
      private final Vec3 windSpeed;

      private WindOffsetter(int var1, Random var2, UniformFloat var3) {
         super();
         this.originY = var1;
         float var4 = var3.sample(var2);
         float var5 = Mth.randomBetween(var2, 0.0F, 3.1415927F);
         this.windSpeed = new Vec3((double)(Mth.cos(var5) * var4), 0.0D, (double)(Mth.sin(var5) * var4));
      }

      private WindOffsetter() {
         super();
         this.originY = 0;
         this.windSpeed = null;
      }

      private static LargeDripstoneFeature.WindOffsetter noWind() {
         return new LargeDripstoneFeature.WindOffsetter();
      }

      private BlockPos offset(BlockPos var1) {
         if (this.windSpeed == null) {
            return var1;
         } else {
            int var2 = this.originY - var1.getY();
            Vec3 var3 = this.windSpeed.scale((double)var2);
            return var1.offset(var3.x, 0.0D, var3.z);
         }
      }

      // $FF: synthetic method
      WindOffsetter(int var1, Random var2, UniformFloat var3, Object var4) {
         this(var1, var2, var3);
      }
   }

   static final class LargeDripstone {
      private BlockPos root;
      private final boolean pointingUp;
      private int radius;
      private final double bluntness;
      private final double scale;

      private LargeDripstone(BlockPos var1, boolean var2, int var3, double var4, double var6) {
         super();
         this.root = var1;
         this.pointingUp = var2;
         this.radius = var3;
         this.bluntness = var4;
         this.scale = var6;
      }

      private int getHeight() {
         return this.getHeightAtRadius(0.0F);
      }

      private boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel var1, LargeDripstoneFeature.WindOffsetter var2) {
         while(this.radius > 1) {
            BlockPos.MutableBlockPos var3 = this.root.mutable();
            int var4 = Math.min(10, this.getHeight());

            for(int var5 = 0; var5 < var4; ++var5) {
               if (var1.getBlockState(var3).is(Blocks.LAVA)) {
                  return false;
               }

               if (DripstoneUtils.isCircleMostlyEmbeddedInStone(var1, var2.offset(var3), this.radius)) {
                  this.root = var3;
                  return true;
               }

               var3.move(this.pointingUp ? Direction.DOWN : Direction.UP);
            }

            this.radius /= 2;
         }

         return false;
      }

      private int getHeightAtRadius(float var1) {
         return (int)DripstoneUtils.getDripstoneHeight((double)var1, (double)this.radius, this.scale, this.bluntness);
      }

      private void placeBlocks(WorldGenLevel var1, Random var2, LargeDripstoneFeature.WindOffsetter var3) {
         for(int var4 = -this.radius; var4 <= this.radius; ++var4) {
            for(int var5 = -this.radius; var5 <= this.radius; ++var5) {
               float var6 = Mth.sqrt((float)(var4 * var4 + var5 * var5));
               if (var6 <= (float)this.radius) {
                  int var7 = this.getHeightAtRadius(var6);
                  if (var7 > 0) {
                     if ((double)var2.nextFloat() < 0.2D) {
                        var7 = (int)((float)var7 * Mth.randomBetween(var2, 0.8F, 1.0F));
                     }

                     BlockPos.MutableBlockPos var8 = this.root.offset(var4, 0, var5).mutable();
                     boolean var9 = false;

                     for(int var10 = 0; var10 < var7; ++var10) {
                        BlockPos var11 = var3.offset(var8);
                        if (DripstoneUtils.isEmptyOrWaterOrLava(var1, var11)) {
                           var9 = true;
                           Block var12 = Blocks.DRIPSTONE_BLOCK;
                           var1.setBlock(var11, var12.defaultBlockState(), 2);
                        } else if (var9 && var1.getBlockState(var11).is(BlockTags.BASE_STONE_OVERWORLD)) {
                           break;
                        }

                        var8.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                     }
                  }
               }
            }
         }

      }

      private boolean isSuitableForWind(LargeDripstoneConfiguration var1) {
         return this.radius >= var1.minRadiusForWind && this.bluntness >= (double)var1.minBluntnessForWind;
      }

      // $FF: synthetic method
      LargeDripstone(BlockPos var1, boolean var2, int var3, double var4, double var6, Object var8) {
         this(var1, var2, var3, var4, var6);
      }
   }
}
