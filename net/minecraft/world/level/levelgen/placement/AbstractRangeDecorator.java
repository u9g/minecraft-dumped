package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractRangeDecorator extends VerticalDecorator<RangeDecoratorConfiguration> {
   private static final Logger LOGGER = LogManager.getLogger();

   public AbstractRangeDecorator(Codec<RangeDecoratorConfiguration> var1) {
      super(var1);
   }

   protected int y(DecorationContext var1, Random var2, RangeDecoratorConfiguration var3, int var4) {
      int var5 = var3.bottomInclusive().resolveY(var1);
      int var6 = var3.topInclusive().resolveY(var1);
      if (var5 >= var6) {
         LOGGER.warn("Empty range decorator: {} [{}-{}]", this, var5, var6);
         return var5;
      } else {
         return this.y(var2, var5, var6);
      }
   }

   protected abstract int y(Random var1, int var2, int var3);
}
