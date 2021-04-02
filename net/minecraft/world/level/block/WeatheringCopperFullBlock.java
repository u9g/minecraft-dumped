package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperFullBlock extends Block implements WeatheringCopper {
   private final WeatheringCopper.WeatherState weatherState;
   private final Block changeTo;

   public WeatheringCopperFullBlock(BlockBehaviour.Properties var1) {
      super(var1);
      this.weatherState = WeatheringCopper.WeatherState.values()[WeatheringCopper.WeatherState.values().length - 1];
      this.changeTo = this;
   }

   public WeatheringCopperFullBlock(BlockBehaviour.Properties var1, WeatheringCopper.WeatherState var2, Block var3) {
      super(var1);
      this.weatherState = var2;
      this.changeTo = var3;
   }

   public void randomTick(BlockState var1, ServerLevel var2, BlockPos var3, Random var4) {
      this.onRandomTick(var1, var2, var3, var4);
   }

   public boolean isRandomlyTicking(BlockState var1) {
      return this.changeTo != this;
   }

   public WeatheringCopper.WeatherState getAge() {
      return this.weatherState;
   }

   public BlockState getChangeTo(BlockState var1) {
      return this.changeTo.defaultBlockState();
   }

   // $FF: synthetic method
   public Enum getAge() {
      return this.getAge();
   }
}
