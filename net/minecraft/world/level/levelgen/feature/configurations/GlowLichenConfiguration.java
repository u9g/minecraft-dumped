package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GlowLichenConfiguration implements FeatureConfiguration {
   public static final Codec<GlowLichenConfiguration> CODEC = RecordCodecBuilder.create((var0) -> {
      return var0.group(Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter((var0x) -> {
         return var0x.searchRange;
      }), Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter((var0x) -> {
         return var0x.canPlaceOnFloor;
      }), Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter((var0x) -> {
         return var0x.canPlaceOnCeiling;
      }), Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter((var0x) -> {
         return var0x.canPlaceOnWall;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spreading").orElse(0.5F).forGetter((var0x) -> {
         return var0x.chanceOfSpreading;
      }), BlockState.CODEC.listOf().fieldOf("can_be_placed_on").forGetter((var0x) -> {
         return new ArrayList(var0x.canBePlacedOn);
      })).apply(var0, GlowLichenConfiguration::new);
   });
   public final int searchRange;
   public final boolean canPlaceOnFloor;
   public final boolean canPlaceOnCeiling;
   public final boolean canPlaceOnWall;
   public final float chanceOfSpreading;
   public final List<BlockState> canBePlacedOn;
   public final List<Direction> validDirections;

   public GlowLichenConfiguration(int var1, boolean var2, boolean var3, boolean var4, float var5, List<BlockState> var6) {
      super();
      this.searchRange = var1;
      this.canPlaceOnFloor = var2;
      this.canPlaceOnCeiling = var3;
      this.canPlaceOnWall = var4;
      this.chanceOfSpreading = var5;
      this.canBePlacedOn = var6;
      ArrayList var7 = Lists.newArrayList();
      if (var3) {
         var7.add(Direction.UP);
      }

      if (var2) {
         var7.add(Direction.DOWN);
      }

      if (var4) {
         Direction.Plane.HORIZONTAL.forEach(var7::add);
      }

      this.validDirections = Collections.unmodifiableList(var7);
   }

   public boolean canBePlacedOn(Block var1) {
      return this.canBePlacedOn.stream().anyMatch((var1x) -> {
         return var1x.is(var1);
      });
   }
}
