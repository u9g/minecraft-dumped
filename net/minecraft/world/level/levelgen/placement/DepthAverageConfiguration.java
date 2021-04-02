package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class DepthAverageConfiguration implements DecoratorConfiguration {
   public static final Codec<DepthAverageConfiguration> CODEC = RecordCodecBuilder.create((var0) -> {
      return var0.group(VerticalAnchor.CODEC.fieldOf("baseline").forGetter(DepthAverageConfiguration::baseline), Codec.INT.fieldOf("spread").forGetter(DepthAverageConfiguration::spread)).apply(var0, DepthAverageConfiguration::new);
   });
   private final VerticalAnchor baseline;
   private final int spread;

   public DepthAverageConfiguration(VerticalAnchor var1, int var2) {
      super();
      this.baseline = var1;
      this.spread = var2;
   }

   public VerticalAnchor baseline() {
      return this.baseline;
   }

   public int spread() {
      return this.spread;
   }
}
