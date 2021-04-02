package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class RangeDecoratorConfiguration implements DecoratorConfiguration {
   public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create((var0) -> {
      return var0.group(VerticalAnchor.CODEC.fieldOf("bottom_inclusive").forGetter(RangeDecoratorConfiguration::bottomInclusive), VerticalAnchor.CODEC.fieldOf("top_inclusive").forGetter(RangeDecoratorConfiguration::topInclusive)).apply(var0, RangeDecoratorConfiguration::new);
   });
   private final VerticalAnchor bottomInclusive;
   private final VerticalAnchor topInclusive;

   public RangeDecoratorConfiguration(VerticalAnchor var1, VerticalAnchor var2) {
      super();
      this.bottomInclusive = var1;
      this.topInclusive = var2;
   }

   public VerticalAnchor bottomInclusive() {
      return this.bottomInclusive;
   }

   public VerticalAnchor topInclusive() {
      return this.topInclusive;
   }
}
