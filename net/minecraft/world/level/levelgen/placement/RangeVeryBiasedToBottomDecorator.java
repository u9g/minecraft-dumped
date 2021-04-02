package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.configurations.BiasedRangeDecoratorConfiguration;

public class RangeVeryBiasedToBottomDecorator extends AbstractBiasedRangeDecorator {
   public RangeVeryBiasedToBottomDecorator(Codec<BiasedRangeDecoratorConfiguration> var1) {
      super(var1);
   }

   protected int y(Random var1, int var2, int var3, int var4) {
      int var5 = Mth.nextInt(var1, var2 + var4, var3);
      int var6 = Mth.nextInt(var1, var2, var5 - 1);
      return Mth.nextInt(var1, var2, var6 - 1 + var4);
   }
}
