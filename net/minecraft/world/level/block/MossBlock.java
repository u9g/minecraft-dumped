package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MossBlock extends Block implements BonemealableBlock {
   public MossBlock(BlockBehaviour.Properties var1) {
      super(var1);
   }

   public boolean isValidBonemealTarget(BlockGetter var1, BlockPos var2, BlockState var3, boolean var4) {
      return var1.getBlockState(var2.above()).isAir();
   }

   public boolean isBonemealSuccess(Level var1, Random var2, BlockPos var3, BlockState var4) {
      return true;
   }

   public void performBonemeal(ServerLevel var1, Random var2, BlockPos var3, BlockState var4) {
      place(var1, var2, var3.above());
   }

   public static boolean place(WorldGenLevel var0, Random var1, BlockPos var2) {
      if (!var0.getBlockState(var2).isAir()) {
         return false;
      } else {
         int var3 = 0;
         int var4 = Mth.randomBetweenInclusive(var1, 1, 3);
         int var5 = Mth.randomBetweenInclusive(var1, 1, 3);

         for(int var6 = -var4; var6 <= var4; ++var6) {
            for(int var7 = -var5; var7 <= var5; ++var7) {
               BlockPos var8 = var2.offset(var6, 0, var7);
               var3 += placeFeature(var0, var1, var8);
            }
         }

         return var3 > 0;
      }
   }

   private static int placeFeature(WorldGenLevel var0, Random var1, BlockPos var2) {
      int var3 = 0;
      BlockPos var4 = var2.below();
      BlockState var5 = var0.getBlockState(var4);
      if (var0.isEmptyBlock(var2) && var5.isFaceSturdy(var0, var4, Direction.UP)) {
         createMossPatch(var0, var1, var2.below());
         if (var1.nextFloat() < 0.8F) {
            BlockState var6 = getVegetationBlockState(var1);
            if (var6.canSurvive(var0, var2)) {
               if (var6.getBlock() instanceof DoublePlantBlock && var0.isEmptyBlock(var2.above())) {
                  DoublePlantBlock var7 = (DoublePlantBlock)var6.getBlock();
                  var7.placeAt(var0, var2, 2);
                  ++var3;
               } else {
                  var0.setBlock(var2, var6, 2);
                  ++var3;
               }
            }
         }
      }

      return var3;
   }

   private static void createMossPatch(WorldGenLevel var0, Random var1, BlockPos var2) {
      if (var0.getBlockState(var2).is(BlockTags.LUSH_PLANTS_REPLACEABLE)) {
         var0.setBlock(var2, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
      }

   }

   private static BlockState getVegetationBlockState(Random var0) {
      int var1 = var0.nextInt(100) + 1;
      if (var1 < 5) {
         return Blocks.FLOWERING_AZALEA.defaultBlockState();
      } else if (var1 < 15) {
         return Blocks.AZALEA.defaultBlockState();
      } else if (var1 < 40) {
         return Blocks.MOSS_CARPET.defaultBlockState();
      } else {
         return var1 < 90 ? Blocks.GRASS.defaultBlockState() : Blocks.TALL_GRASS.defaultBlockState();
      }
   }
}
