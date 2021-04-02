package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AzaleaBlock extends BushBlock {
   protected AzaleaBlock(BlockBehaviour.Properties var1) {
      super(var1);
   }

   protected boolean mayPlaceOn(BlockState var1, BlockGetter var2, BlockPos var3) {
      return var1.is(Blocks.CLAY) || super.mayPlaceOn(var1, var2, var3);
   }
}
