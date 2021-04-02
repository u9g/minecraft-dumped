package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

public class GlowstoneDecorator extends RepeatingDecorator<CountConfiguration> {
   public GlowstoneDecorator(Codec<CountConfiguration> var1) {
      super(var1);
   }

   protected int count(Random var1, CountConfiguration var2, BlockPos var3) {
      return var1.nextInt(var1.nextInt(var2.count().sample(var1)) + 1);
   }
}
