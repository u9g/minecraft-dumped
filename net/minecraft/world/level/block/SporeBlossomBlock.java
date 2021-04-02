package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SporeBlossomBlock extends Block {
   private static final VoxelShape SHAPE = Block.box(2.0D, 13.0D, 2.0D, 14.0D, 16.0D, 14.0D);

   public SporeBlossomBlock(BlockBehaviour.Properties var1) {
      super(var1);
   }

   public boolean canSurvive(BlockState var1, LevelReader var2, BlockPos var3) {
      return Block.canSupportCenter(var2, var3.above(), Direction.DOWN);
   }

   public void animateTick(BlockState var1, Level var2, BlockPos var3, Random var4) {
      int var5 = var3.getX();
      int var6 = var3.getY();
      int var7 = var3.getZ();
      double var8 = (double)var5 + var4.nextDouble();
      double var10 = (double)var6 + 0.7D;
      double var12 = (double)var7 + var4.nextDouble();
      var2.addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, var8, var10, var12, 0.0D, 0.0D, 0.0D);
   }

   public VoxelShape getShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      return SHAPE;
   }
}
