package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;

public class DepthAverageDecorator extends VerticalDecorator<DepthAverageConfiguration> {
   public DepthAverageDecorator(Codec<DepthAverageConfiguration> var1) {
      super(var1);
   }

   protected int y(DecorationContext var1, Random var2, DepthAverageConfiguration var3, int var4) {
      int var5 = var3.spread();
      return var2.nextInt(var5) + var2.nextInt(var5) - var5 + var3.baseline().resolveY(var1);
   }
}
