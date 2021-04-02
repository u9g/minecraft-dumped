package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DepthBasedReplacingBaseStoneSource implements BaseStoneSource {
   private final WorldgenRandom random;
   private final long seed;
   private final BlockState normalBlock;
   private final BlockState replacementBlock;

   public DepthBasedReplacingBaseStoneSource(long var1, BlockState var3, BlockState var4) {
      super();
      this.random = new WorldgenRandom(var1);
      this.seed = var1;
      this.normalBlock = var3;
      this.replacementBlock = var4;
   }

   public BlockState getBaseStone(int var1, int var2, int var3, NoiseGeneratorSettings var4) {
      if (!var4.isGrimstoneEnabled()) {
         return this.normalBlock;
      } else {
         this.random.setBaseStoneSeed(this.seed, var1, var2, var3);
         double var5 = Mth.clampedMap((double)var2, -8.0D, 0.0D, 1.0D, 0.0D);
         return (double)this.random.nextFloat() < var5 ? this.replacementBlock : this.normalBlock;
      }
   }
}
