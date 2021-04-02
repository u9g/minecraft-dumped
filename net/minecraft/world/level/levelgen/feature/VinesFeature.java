package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature extends Feature<NoneFeatureConfiguration> {
   private static final Direction[] DIRECTIONS = Direction.values();

   public VinesFeature(Codec<NoneFeatureConfiguration> var1) {
      super(var1);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> var1) {
      Random var2 = var1.random();
      WorldGenLevel var3 = var1.level();
      BlockPos var4 = var1.origin();
      BlockPos.MutableBlockPos var5 = var4.mutable();
      BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

      for(int var7 = 64; var7 < 384; ++var7) {
         var5.set(var4);
         var5.move(var2.nextInt(4) - var2.nextInt(4), 0, var2.nextInt(4) - var2.nextInt(4));
         var5.setY(var7);
         if (var3.isEmptyBlock(var5)) {
            Direction[] var8 = DIRECTIONS;
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               Direction var11 = var8[var10];
               if (var11 != Direction.DOWN) {
                  var6.setWithOffset(var5, var11);
                  if (VineBlock.isAcceptableNeighbour(var3, var6, var11)) {
                     var3.setBlock(var5, (BlockState)Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(var11), true), 2);
                     break;
                  }
               }
            }
         }
      }

      return true;
   }
}
