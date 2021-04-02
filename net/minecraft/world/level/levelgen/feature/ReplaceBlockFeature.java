package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
   public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> var1) {
      super(var1);
   }

   public boolean place(FeaturePlaceContext<ReplaceBlockConfiguration> var1) {
      WorldGenLevel var2 = var1.level();
      BlockPos var3 = var1.origin();
      ReplaceBlockConfiguration var4 = (ReplaceBlockConfiguration)var1.config();
      if (var2.getBlockState(var3).is(var4.target.getBlock())) {
         var2.setBlock(var3, var4.state, 2);
      }

      return true;
   }
}
