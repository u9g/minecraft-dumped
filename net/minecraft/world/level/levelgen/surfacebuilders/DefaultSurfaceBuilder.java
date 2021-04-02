package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> var1) {
      super(var1);
   }

   public void apply(Random var1, ChunkAccess var2, Biome var3, int var4, int var5, int var6, double var7, BlockState var9, BlockState var10, int var11, long var12, SurfaceBuilderBaseConfiguration var14) {
      this.apply(var1, var2, var3, var4, var5, var6, var7, var9, var10, var14.getTopMaterial(), var14.getUnderMaterial(), var14.getUnderwaterMaterial(), var11);
   }

   protected void apply(Random var1, ChunkAccess var2, Biome var3, int var4, int var5, int var6, double var7, BlockState var9, BlockState var10, BlockState var11, BlockState var12, BlockState var13, int var14) {
      BlockPos.MutableBlockPos var15 = new BlockPos.MutableBlockPos();
      int var16 = (int)(var7 / 3.0D + 3.0D + var1.nextDouble() * 0.25D);
      int var18;
      BlockState var20;
      if (var16 == 0) {
         boolean var17 = false;

         for(var18 = var6; var18 >= 50; --var18) {
            var15.set(var4, var18, var5);
            BlockState var19 = var2.getBlockState(var15);
            if (var19.isAir()) {
               var17 = false;
            } else if (var19.is(var9.getBlock())) {
               if (!var17) {
                  if (var18 >= var14) {
                     var20 = Blocks.AIR.defaultBlockState();
                  } else if (var18 == var14 - 1) {
                     var20 = var3.getTemperature(var15) < 0.15F ? Blocks.ICE.defaultBlockState() : var10;
                  } else if (var18 >= var14 - (7 + var16)) {
                     var20 = var9;
                  } else {
                     var20 = var13;
                  }

                  var2.setBlockState(var15, var20, false);
               }

               var17 = true;
            }
         }
      } else {
         BlockState var22 = var12;
         var18 = -1;

         for(int var23 = var6; var23 >= 50; --var23) {
            var15.set(var4, var23, var5);
            var20 = var2.getBlockState(var15);
            if (var20.isAir()) {
               var18 = -1;
            } else if (var20.is(var9.getBlock())) {
               if (var18 == -1) {
                  var18 = var16;
                  BlockState var21;
                  if (var23 >= var14 + 2) {
                     var21 = var11;
                  } else if (var23 >= var14 - 1) {
                     var22 = var12;
                     var21 = var11;
                  } else if (var23 >= var14 - 4) {
                     var22 = var12;
                     var21 = var12;
                  } else if (var23 >= var14 - (7 + var16)) {
                     var21 = var22;
                  } else {
                     var22 = var9;
                     var21 = var13;
                  }

                  var2.setBlockState(var15, var21, false);
               } else if (var18 > 0) {
                  --var18;
                  var2.setBlockState(var15, var22, false);
                  if (var18 == 0 && var22.is(Blocks.SAND) && var16 > 1) {
                     var18 = var1.nextInt(4) + Math.max(0, var23 - var14);
                     var22 = var22.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                  }
               }
            }
         }
      }

   }
}
