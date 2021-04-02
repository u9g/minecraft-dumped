package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class RangeDecorator extends AbstractRangeDecorator {
   public RangeDecorator(Codec<RangeDecoratorConfiguration> var1) {
      super(var1);
   }

   protected int y(Random var1, int var2, int var3) {
      return Mth.nextInt(var1, var2, var3);
   }
}
