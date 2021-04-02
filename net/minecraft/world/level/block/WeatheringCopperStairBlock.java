package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperStairBlock extends StairBlock implements WeatheringCopper {
   private final WeatheringCopper.WeatherState weatherState;
   private final Block changeTo;

   public WeatheringCopperStairBlock(BlockState var1, BlockBehaviour.Properties var2) {
      super(var1, var2);
      this.weatherState = WeatheringCopper.WeatherState.values()[WeatheringCopper.WeatherState.values().length - 1];
      this.changeTo = this;
   }

   public WeatheringCopperStairBlock(BlockState var1, BlockBehaviour.Properties var2, WeatheringCopper.WeatherState var3, Block var4) {
      super(var1, var2);
      this.weatherState = var3;
      this.changeTo = var4;
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
      return (BlockState)((BlockState)((BlockState)((BlockState)this.changeTo.defaultBlockState().setValue(FACING, var1.getValue(FACING))).setValue(HALF, var1.getValue(HALF))).setValue(SHAPE, var1.getValue(SHAPE))).setValue(WATERLOGGED, var1.getValue(WATERLOGGED));
   }

   // $FF: synthetic method
   public Enum getAge() {
      return this.getAge();
   }
}
