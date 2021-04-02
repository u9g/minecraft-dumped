package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
   public SimpleBlockFeature(Codec<SimpleBlockConfiguration> var1) {
      super(var1);
   }

   public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> var1) {
      SimpleBlockConfiguration var2 = (SimpleBlockConfiguration)var1.config();
      WorldGenLevel var3 = var1.level();
      BlockPos var4 = var1.origin();
      if (var2.placeOn.contains(var3.getBlockState(var4.below())) && var2.placeIn.contains(var3.getBlockState(var4)) && var2.placeUnder.contains(var3.getBlockState(var4.above()))) {
         var3.setBlock(var4, var2.toPlace, 2);
         return true;
      } else {
         return false;
      }
   }
}
