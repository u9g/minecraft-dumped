package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public final class NoiseGeneratorSettings {
   public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create((var0) -> {
      return var0.group(StructureSettings.CODEC.fieldOf("structures").forGetter(NoiseGeneratorSettings::structureSettings), NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings), BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::getDefaultBlock), BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::getDefaultFluid), Codec.INT.fieldOf("bedrock_roof_position").forGetter(NoiseGeneratorSettings::getBedrockRoofPosition), Codec.INT.fieldOf("bedrock_floor_position").forGetter(NoiseGeneratorSettings::getBedrockFloorPosition), Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel), Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration), Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled), Codec.BOOL.fieldOf("noise_caves_enabled").forGetter(NoiseGeneratorSettings::isNoiseCavesEnabled), Codec.BOOL.fieldOf("grimstone_enabled").forGetter(NoiseGeneratorSettings::isGrimstoneEnabled)).apply(var0, NoiseGeneratorSettings::new);
   });
   public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC;
   private final StructureSettings structureSettings;
   private final NoiseSettings noiseSettings;
   private final BlockState defaultBlock;
   private final BlockState defaultFluid;
   private final int bedrockRoofPosition;
   private final int bedrockFloorPosition;
   private final int seaLevel;
   private final boolean disableMobGeneration;
   private final boolean aquifersEnabled;
   private final boolean noiseCavesEnabled;
   private final boolean grimstoneEnabled;
   public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD;
   public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED;
   public static final ResourceKey<NoiseGeneratorSettings> NETHER;
   public static final ResourceKey<NoiseGeneratorSettings> END;
   public static final ResourceKey<NoiseGeneratorSettings> CAVES;
   public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS;
   private static final NoiseGeneratorSettings BUILTIN_OVERWORLD;

   private NoiseGeneratorSettings(StructureSettings var1, NoiseSettings var2, BlockState var3, BlockState var4, int var5, int var6, int var7, boolean var8, boolean var9, boolean var10, boolean var11) {
      super();
      this.structureSettings = var1;
      this.noiseSettings = var2;
      this.defaultBlock = var3;
      this.defaultFluid = var4;
      this.bedrockRoofPosition = var5;
      this.bedrockFloorPosition = var6;
      this.seaLevel = var7;
      this.disableMobGeneration = var8;
      this.aquifersEnabled = var9;
      this.noiseCavesEnabled = var10;
      this.grimstoneEnabled = var11;
   }

   public StructureSettings structureSettings() {
      return this.structureSettings;
   }

   public NoiseSettings noiseSettings() {
      return this.noiseSettings;
   }

   public BlockState getDefaultBlock() {
      return this.defaultBlock;
   }

   public BlockState getDefaultFluid() {
      return this.defaultFluid;
   }

   public int getBedrockRoofPosition() {
      return this.bedrockRoofPosition;
   }

   public int getBedrockFloorPosition() {
      return this.bedrockFloorPosition;
   }

   public int seaLevel() {
      return this.seaLevel;
   }

   @Deprecated
   protected boolean disableMobGeneration() {
      return this.disableMobGeneration;
   }

   protected boolean isAquifersEnabled() {
      return this.aquifersEnabled;
   }

   protected boolean isNoiseCavesEnabled() {
      return this.noiseCavesEnabled;
   }

   protected boolean isGrimstoneEnabled() {
      return this.grimstoneEnabled;
   }

   public boolean stable(ResourceKey<NoiseGeneratorSettings> var1) {
      return Objects.equals(this, BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(var1));
   }

   private static NoiseGeneratorSettings register(ResourceKey<NoiseGeneratorSettings> var0, NoiseGeneratorSettings var1) {
      BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, (ResourceLocation)var0.location(), var1);
      return var1;
   }

   public static NoiseGeneratorSettings bootstrap() {
      return BUILTIN_OVERWORLD;
   }

   private static NoiseGeneratorSettings endLikePreset(StructureSettings var0, BlockState var1, BlockState var2, boolean var3, boolean var4, boolean var5) {
      return new NoiseGeneratorSettings(var0, NoiseSettings.create(var5 ? -64 : 0, var5 ? 384 : 128, new NoiseSamplingSettings(2.0D, 1.0D, 80.0D, 160.0D), new NoiseSlideSettings(-3000, 64, -46), new NoiseSlideSettings(-30, 7, 1), 2, 1, 0.0D, 0.0D, true, false, var4, false), var1, var2, -2147483648, -2147483648, var5 ? -64 : 0, var3, false, false, false);
   }

   private static NoiseGeneratorSettings netherLikePreset(StructureSettings var0, BlockState var1, BlockState var2, boolean var3) {
      HashMap var4 = Maps.newHashMap(StructureSettings.DEFAULTS);
      var4.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
      return new NoiseGeneratorSettings(new StructureSettings(Optional.ofNullable(var0.stronghold()), var4), NoiseSettings.create(var3 ? -64 : 0, var3 ? 384 : 128, new NoiseSamplingSettings(1.0D, 3.0D, 80.0D, 60.0D), new NoiseSlideSettings(120, 3, 0), new NoiseSlideSettings(320, 4, -1), 1, 2, 0.0D, 0.019921875D, false, false, false, false), var1, var2, 0, 0, 32, false, false, false, false);
   }

   private static NoiseGeneratorSettings overworld(StructureSettings var0, boolean var1) {
      double var2 = 0.9999999814507745D;
      return new NoiseGeneratorSettings(var0, NoiseSettings.create(-64, 384, new NoiseSamplingSettings(0.9999999814507745D, 0.9999999814507745D, 80.0D, 160.0D), new NoiseSlideSettings(-10, 3, 0), new NoiseSlideSettings(15, 3, 0), 1, 2, 1.0D, -0.46875D, true, true, false, var1), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), -2147483648, 0, 63, false, true, true, true);
   }

   static {
      CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
      OVERWORLD = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld"));
      AMPLIFIED = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified"));
      NETHER = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether"));
      END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
      CAVES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves"));
      FLOATING_ISLANDS = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands"));
      BUILTIN_OVERWORLD = register(OVERWORLD, overworld(new StructureSettings(true), false));
      register(AMPLIFIED, overworld(new StructureSettings(true), true));
      register(NETHER, netherLikePreset(new StructureSettings(false), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false));
      register(END, endLikePreset(new StructureSettings(false), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), true, true, false));
      register(CAVES, netherLikePreset(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), true));
      register(FLOATING_ISLANDS, endLikePreset(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), false, false, true));
   }
}
