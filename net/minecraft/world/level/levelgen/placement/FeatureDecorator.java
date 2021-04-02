package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.configurations.BiasedRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.nether.CountMultiLayerDecorator;
import net.minecraft.world.level.levelgen.placement.nether.GlowstoneDecorator;

public abstract class FeatureDecorator<DC extends DecoratorConfiguration> {
   public static final FeatureDecorator<NoneDecoratorConfiguration> NOPE;
   public static final FeatureDecorator<DecoratedDecoratorConfiguration> DECORATED;
   public static final FeatureDecorator<CarvingMaskDecoratorConfiguration> CARVING_MASK;
   public static final FeatureDecorator<CountConfiguration> COUNT_MULTILAYER;
   public static final FeatureDecorator<NoneDecoratorConfiguration> SQUARE;
   public static final FeatureDecorator<NoneDecoratorConfiguration> DARK_OAK_TREE;
   public static final FeatureDecorator<NoneDecoratorConfiguration> ICEBERG;
   public static final FeatureDecorator<ChanceDecoratorConfiguration> CHANCE;
   public static final FeatureDecorator<CountConfiguration> COUNT;
   public static final FeatureDecorator<NoiseDependantDecoratorConfiguration> COUNT_NOISE;
   public static final FeatureDecorator<NoiseCountFactorDecoratorConfiguration> COUNT_NOISE_BIASED;
   public static final FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> COUNT_EXTRA;
   public static final FeatureDecorator<ChanceDecoratorConfiguration> LAVA_LAKE;
   public static final FeatureDecorator<CountConfiguration> GLOWSTONE;
   public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP;
   public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP_SPREAD_DOUBLE;
   public static final FeatureDecorator<NoneDecoratorConfiguration> TOP_SOLID_HEIGHTMAP;
   public static final FeatureDecorator<NoneDecoratorConfiguration> HEIGHTMAP_WORLD_SURFACE;
   public static final FeatureDecorator<RangeDecoratorConfiguration> RANGE;
   public static final FeatureDecorator<BiasedRangeDecoratorConfiguration> RANGE_BIASED_TO_BOTTOM;
   public static final FeatureDecorator<BiasedRangeDecoratorConfiguration> RANGE_VERY_BIASED_TO_BOTTOM;
   public static final FeatureDecorator<DepthAverageConfiguration> DEPTH_AVERAGE;
   public static final FeatureDecorator<NoneDecoratorConfiguration> SPREAD_32_ABOVE;
   public static final FeatureDecorator<NoneDecoratorConfiguration> END_GATEWAY;
   private final Codec<ConfiguredDecorator<DC>> configuredCodec;

   private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String var0, G var1) {
      return (FeatureDecorator)Registry.register(Registry.DECORATOR, (String)var0, var1);
   }

   public FeatureDecorator(Codec<DC> var1) {
      super();
      this.configuredCodec = var1.fieldOf("config").xmap((var1x) -> {
         return new ConfiguredDecorator(this, var1x);
      }, ConfiguredDecorator::config).codec();
   }

   public ConfiguredDecorator<DC> configured(DC var1) {
      return new ConfiguredDecorator(this, var1);
   }

   public Codec<ConfiguredDecorator<DC>> configuredCodec() {
      return this.configuredCodec;
   }

   public abstract Stream<BlockPos> getPositions(DecorationContext var1, Random var2, DC var3, BlockPos var4);

   public String toString() {
      return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode());
   }

   static {
      NOPE = register("nope", new NopePlacementDecorator(NoneDecoratorConfiguration.CODEC));
      DECORATED = register("decorated", new DecoratedDecorator(DecoratedDecoratorConfiguration.CODEC));
      CARVING_MASK = register("carving_mask", new CarvingMaskDecorator(CarvingMaskDecoratorConfiguration.CODEC));
      COUNT_MULTILAYER = register("count_multilayer", new CountMultiLayerDecorator(CountConfiguration.CODEC));
      SQUARE = register("square", new SquareDecorator(NoneDecoratorConfiguration.CODEC));
      DARK_OAK_TREE = register("dark_oak_tree", new DarkOakTreePlacementDecorator(NoneDecoratorConfiguration.CODEC));
      ICEBERG = register("iceberg", new IcebergPlacementDecorator(NoneDecoratorConfiguration.CODEC));
      CHANCE = register("chance", new ChanceDecorator(ChanceDecoratorConfiguration.CODEC));
      COUNT = register("count", new CountDecorator(CountConfiguration.CODEC));
      COUNT_NOISE = register("count_noise", new CountNoiseDecorator(NoiseDependantDecoratorConfiguration.CODEC));
      COUNT_NOISE_BIASED = register("count_noise_biased", new NoiseBasedDecorator(NoiseCountFactorDecoratorConfiguration.CODEC));
      COUNT_EXTRA = register("count_extra", new CountWithExtraChanceDecorator(FrequencyWithExtraChanceDecoratorConfiguration.CODEC));
      LAVA_LAKE = register("lava_lake", new LakeLavaPlacementDecorator(ChanceDecoratorConfiguration.CODEC));
      GLOWSTONE = register("glowstone", new GlowstoneDecorator(CountConfiguration.CODEC));
      HEIGHTMAP = register("heightmap", new HeightmapDecorator(NoneDecoratorConfiguration.CODEC));
      HEIGHTMAP_SPREAD_DOUBLE = register("heightmap_spread_double", new HeightmapDoubleDecorator(NoneDecoratorConfiguration.CODEC));
      TOP_SOLID_HEIGHTMAP = register("top_solid_heightmap", new TopSolidHeightMapDecorator(NoneDecoratorConfiguration.CODEC));
      HEIGHTMAP_WORLD_SURFACE = register("heightmap_world_surface", new HeightMapWorldSurfaceDecorator(NoneDecoratorConfiguration.CODEC));
      RANGE = register("range", new RangeDecorator(RangeDecoratorConfiguration.CODEC));
      RANGE_BIASED_TO_BOTTOM = register("range_biased_to_bottom", new RangeBiasedToBottomDecorator(BiasedRangeDecoratorConfiguration.CODEC));
      RANGE_VERY_BIASED_TO_BOTTOM = register("range_very_biased_to_bottom", new RangeVeryBiasedToBottomDecorator(BiasedRangeDecoratorConfiguration.CODEC));
      DEPTH_AVERAGE = register("depth_average", new DepthAverageDecorator(DepthAverageConfiguration.CODEC));
      SPREAD_32_ABOVE = register("spread_32_above", new Spread32Decorator(NoneDecoratorConfiguration.CODEC));
      END_GATEWAY = register("end_gateway", new EndGatewayPlacementDecorator(NoneDecoratorConfiguration.CODEC));
   }
}
