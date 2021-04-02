package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PointedDripstoneBlock extends Block implements Fallable, SimpleWaterloggedBlock {
   public static final DirectionProperty TIP_DIRECTION;
   public static final EnumProperty<DripstoneThickness> THICKNESS;
   public static final BooleanProperty WATERLOGGED;
   private static final VoxelShape TIP_MERGE_SHAPE;
   private static final VoxelShape TIP_SHAPE_UP;
   private static final VoxelShape TIP_SHAPE_DOWN;
   private static final VoxelShape FRUSTUM_SHAPE;
   private static final VoxelShape MIDDLE_SHAPE;
   private static final VoxelShape BASE_SHAPE;

   public PointedDripstoneBlock(BlockBehaviour.Properties var1) {
      super(var1);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(TIP_DIRECTION, Direction.UP)).setValue(THICKNESS, DripstoneThickness.TIP)).setValue(WATERLOGGED, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> var1) {
      var1.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
   }

   public boolean canSurvive(BlockState var1, LevelReader var2, BlockPos var3) {
      return isValidPointedDripstonePlacement(var2, var3, (Direction)var1.getValue(TIP_DIRECTION));
   }

   public BlockState updateShape(BlockState var1, Direction var2, BlockState var3, LevelAccessor var4, BlockPos var5, BlockPos var6) {
      if ((Boolean)var1.getValue(WATERLOGGED)) {
         var4.getLiquidTicks().scheduleTick(var5, Fluids.WATER, Fluids.WATER.getTickDelay(var4));
      }

      if (var2 != Direction.UP && var2 != Direction.DOWN) {
         return var1;
      } else if (var4.getBlockTicks().hasScheduledTick(var5, this)) {
         return var1;
      } else {
         Direction var7 = (Direction)var1.getValue(TIP_DIRECTION);
         if (var2 == var7.getOpposite() && !isValidPointedDripstonePlacement(var4, var5, var7)) {
            if (var7 == Direction.DOWN) {
               this.scheduleStalactiteFallTicks(var1, var4, var5);
               return var1;
            } else {
               return getAirOrWater(var1);
            }
         } else {
            boolean var8 = var1.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
            DripstoneThickness var9 = calculateDripstoneThickness(var4, var5, var7, var8);
            return var9 == null ? getAirOrWater(var1) : (BlockState)var1.setValue(THICKNESS, var9);
         }
      }
   }

   private static BlockState getAirOrWater(BlockState var0) {
      return (Boolean)var0.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
   }

   public void onProjectileHit(Level var1, BlockState var2, BlockHitResult var3, Projectile var4) {
      if (var4 instanceof ThrownTrident && var4.getDeltaMovement().length() > 0.6D) {
         var1.destroyBlock(var3.getBlockPos(), true);
      }

   }

   public void fallOn(Level var1, BlockPos var2, Entity var3, float var4) {
      BlockState var5 = var1.getBlockState(var2);
      if (var5.getValue(TIP_DIRECTION) == Direction.UP && var5.getValue(THICKNESS) == DripstoneThickness.TIP) {
         var3.causeFallDamage(var4 + 2.0F, 2.0F, DamageSource.STALAGMITE);
      } else {
         super.fallOn(var1, var2, var3, var4);
      }

   }

   public void animateTick(BlockState var1, Level var2, BlockPos var3, Random var4) {
      if (canDrip(var1)) {
         float var5 = var4.nextFloat();
         if (var5 <= 0.12F) {
            getFluidAboveStalactite(var2, var3, var1).filter((var1x) -> {
               return var5 < 0.02F || canFillCauldron(var1x);
            }).ifPresent((var3x) -> {
               spawnDripParticle(var2, var3, var1, var3x);
            });
         }
      }
   }

   public void tick(BlockState var1, ServerLevel var2, BlockPos var3, Random var4) {
      spawnFallingStalactite(var1, var2, var3);
   }

   public void randomTick(BlockState var1, ServerLevel var2, BlockPos var3, Random var4) {
      maybeFillCauldron(var1, var2, var3, var4.nextFloat());
   }

   @VisibleForTesting
   public static void maybeFillCauldron(BlockState var0, ServerLevel var1, BlockPos var2, float var3) {
      if (var3 <= 0.17578125F || var3 <= 0.05859375F) {
         if (isStalactiteStartPos(var0, var1, var2)) {
            Fluid var4 = getCauldronFillFluidType(var1, var2);
            float var5;
            if (var4 == Fluids.WATER) {
               var5 = 0.17578125F;
            } else {
               if (var4 != Fluids.LAVA) {
                  return;
               }

               var5 = 0.05859375F;
            }

            if (var3 < var5) {
               BlockPos var6 = findTip(var0, var1, var2, 10);
               if (var6 != null) {
                  BlockPos var7 = findFillableCauldronBelowStalactiteTip(var1, var6, var4);
                  if (var7 != null) {
                     var1.levelEvent(1504, var6, 0);
                     int var8 = var6.getY() - var7.getY();
                     int var9 = 50 + var8;
                     BlockState var10 = var1.getBlockState(var7);
                     var1.getBlockTicks().scheduleTick(var7, var10.getBlock(), var9);
                  }
               }
            }
         }
      }
   }

   public PushReaction getPistonPushReaction(BlockState var1) {
      return PushReaction.DESTROY;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext var1) {
      Level var2 = var1.getLevel();
      BlockPos var3 = var1.getClickedPos();
      Direction var4 = var1.getNearestLookingVerticalDirection().getOpposite();
      Direction var5 = calculateTipDirection(var2, var3, var4);
      if (var5 == null) {
         return null;
      } else {
         boolean var6 = !var1.isSecondaryUseActive();
         DripstoneThickness var7 = calculateDripstoneThickness(var2, var3, var5, var6);
         return var7 == null ? null : (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(TIP_DIRECTION, var5)).setValue(THICKNESS, var7)).setValue(WATERLOGGED, var2.getFluidState(var3).getType() == Fluids.WATER);
      }
   }

   public FluidState getFluidState(BlockState var1) {
      return (Boolean)var1.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(var1);
   }

   public VoxelShape getOcclusionShape(BlockState var1, BlockGetter var2, BlockPos var3) {
      return Shapes.empty();
   }

   public VoxelShape getShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      DripstoneThickness var6 = (DripstoneThickness)var1.getValue(THICKNESS);
      VoxelShape var5;
      if (var6 == DripstoneThickness.TIP_MERGE) {
         var5 = TIP_MERGE_SHAPE;
      } else if (var6 == DripstoneThickness.TIP) {
         if (var1.getValue(TIP_DIRECTION) == Direction.DOWN) {
            var5 = TIP_SHAPE_DOWN;
         } else {
            var5 = TIP_SHAPE_UP;
         }
      } else if (var6 == DripstoneThickness.FRUSTUM) {
         var5 = FRUSTUM_SHAPE;
      } else if (var6 == DripstoneThickness.MIDDLE) {
         var5 = MIDDLE_SHAPE;
      } else {
         var5 = BASE_SHAPE;
      }

      Vec3 var7 = var1.getOffset(var2, var3);
      return var5.move(var7.x, 0.0D, var7.z);
   }

   public BlockBehaviour.OffsetType getOffsetType() {
      return BlockBehaviour.OffsetType.XZ;
   }

   public float getMaxHorizontalOffset() {
      return 0.125F;
   }

   public void onBrokenAfterFall(Level var1, BlockPos var2, FallingBlockEntity var3) {
      if (!var3.isSilent()) {
         var1.levelEvent(1045, var2, 0);
      }

   }

   public DamageSource getFallDamageSource() {
      return DamageSource.FALLING_STALACTITE;
   }

   public Predicate<Entity> getHurtsEntitySelector() {
      return EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
   }

   private void scheduleStalactiteFallTicks(BlockState var1, LevelAccessor var2, BlockPos var3) {
      BlockPos var4 = findTip(var1, var2, var3, 2147483647);
      if (var4 != null) {
         BlockPos.MutableBlockPos var5 = var4.mutable();

         while(isStalactite(var2.getBlockState(var5))) {
            var2.getBlockTicks().scheduleTick(var5, this, 2);
            var5.move(Direction.UP);
         }

      }
   }

   private static int getStalactiteSizeFromTip(ServerLevel var0, BlockPos var1, int var2) {
      int var3 = 1;
      BlockPos.MutableBlockPos var4 = var1.mutable().move(Direction.UP);

      while(var3 < var2 && isStalactite(var0.getBlockState(var4))) {
         ++var3;
         var4.move(Direction.UP);
      }

      return var3;
   }

   private static void spawnFallingStalactite(BlockState var0, ServerLevel var1, BlockPos var2) {
      Vec3 var3 = Vec3.atBottomCenterOf(var2);
      FallingBlockEntity var4 = new FallingBlockEntity(var1, var3.x, var3.y, var3.z, var0);
      if (isTip(var0)) {
         int var5 = getStalactiteSizeFromTip(var1, var2, 6);
         float var6 = 1.0F * (float)var5;
         var4.setHurtsEntities(var6, 40);
      }

      var1.addFreshEntity(var4);
   }

   public static void spawnDripParticle(Level var0, BlockPos var1, BlockState var2) {
      getFluidAboveStalactite(var0, var1, var2).ifPresent((var3) -> {
         spawnDripParticle(var0, var1, var2, var3);
      });
   }

   private static void spawnDripParticle(Level var0, BlockPos var1, BlockState var2, Fluid var3) {
      Vec3 var4 = var2.getOffset(var0, var1);
      double var5 = 0.0625D;
      double var7 = (double)var1.getX() + 0.5D + var4.x;
      double var9 = (double)((float)(var1.getY() + 1) - 0.6875F) - 0.0625D;
      double var11 = (double)var1.getZ() + 0.5D + var4.z;
      Fluid var13 = getDripFluid(var0, var3);
      SimpleParticleType var14 = var13.is(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
      var0.addParticle(var14, var7, var9, var11, 0.0D, 0.0D, 0.0D);
   }

   @Nullable
   private static BlockPos findTip(BlockState var0, LevelAccessor var1, BlockPos var2, int var3) {
      if (isTip(var0)) {
         return var2;
      } else {
         Direction var4 = (Direction)var0.getValue(TIP_DIRECTION);
         Predicate var5 = (var1x) -> {
            return var1x.is(Blocks.POINTED_DRIPSTONE) && var1x.getValue(TIP_DIRECTION) == var4;
         };
         return (BlockPos)findBlockVertical(var1, var2, var4.getAxisDirection(), var5, PointedDripstoneBlock::isTip, var3).orElse((Object)null);
      }
   }

   @Nullable
   private static Direction calculateTipDirection(LevelReader var0, BlockPos var1, Direction var2) {
      Direction var3;
      if (isValidPointedDripstonePlacement(var0, var1, var2)) {
         var3 = var2;
      } else {
         if (!isValidPointedDripstonePlacement(var0, var1, var2.getOpposite())) {
            return null;
         }

         var3 = var2.getOpposite();
      }

      return var3;
   }

   @Nullable
   private static DripstoneThickness calculateDripstoneThickness(LevelReader var0, BlockPos var1, Direction var2, boolean var3) {
      Direction var4 = var2.getOpposite();
      BlockState var5 = var0.getBlockState(var1.relative(var2));
      if (isPointedDripstoneWithDirection(var5, var4)) {
         return !var3 && var5.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
      } else if (!isPointedDripstoneWithDirection(var5, var2)) {
         return DripstoneThickness.TIP;
      } else {
         DripstoneThickness var6 = (DripstoneThickness)var5.getValue(THICKNESS);
         if (var6 != DripstoneThickness.TIP && var6 != DripstoneThickness.TIP_MERGE) {
            BlockState var7 = var0.getBlockState(var1.relative(var4));
            return !isPointedDripstoneWithDirection(var7, var2) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
         } else {
            return DripstoneThickness.FRUSTUM;
         }
      }
   }

   public static boolean canDrip(BlockState var0) {
      return isStalactite(var0) && var0.getValue(THICKNESS) == DripstoneThickness.TIP && !(Boolean)var0.getValue(WATERLOGGED);
   }

   private static Optional<BlockPos> findRootBlock(Level var0, BlockPos var1, BlockState var2, int var3) {
      Direction var4 = (Direction)var2.getValue(TIP_DIRECTION);
      Predicate var5 = (var1x) -> {
         return var1x.is(Blocks.POINTED_DRIPSTONE) && var1x.getValue(TIP_DIRECTION) == var4;
      };
      return findBlockVertical(var0, var1, var4.getOpposite().getAxisDirection(), var5, (var0x) -> {
         return !var0x.is(Blocks.POINTED_DRIPSTONE);
      }, var3);
   }

   private static boolean isValidPointedDripstonePlacement(LevelReader var0, BlockPos var1, Direction var2) {
      BlockPos var3 = var1.relative(var2.getOpposite());
      BlockState var4 = var0.getBlockState(var3);
      return var4.isFaceSturdy(var0, var3, var2) || isPointedDripstoneWithDirection(var4, var2);
   }

   private static boolean isTip(BlockState var0) {
      if (!var0.is(Blocks.POINTED_DRIPSTONE)) {
         return false;
      } else {
         DripstoneThickness var1 = (DripstoneThickness)var0.getValue(THICKNESS);
         return var1 == DripstoneThickness.TIP || var1 == DripstoneThickness.TIP_MERGE;
      }
   }

   private static boolean isStalactite(BlockState var0) {
      return isPointedDripstoneWithDirection(var0, Direction.DOWN);
   }

   private static boolean isStalactiteStartPos(BlockState var0, LevelReader var1, BlockPos var2) {
      return isStalactite(var0) && !var1.getBlockState(var2.above()).is(Blocks.POINTED_DRIPSTONE);
   }

   public boolean isPathfindable(BlockState var1, BlockGetter var2, BlockPos var3, PathComputationType var4) {
      return false;
   }

   private static boolean isPointedDripstoneWithDirection(BlockState var0, Direction var1) {
      return var0.is(Blocks.POINTED_DRIPSTONE) && var0.getValue(TIP_DIRECTION) == var1;
   }

   @Nullable
   private static BlockPos findFillableCauldronBelowStalactiteTip(Level var0, BlockPos var1, Fluid var2) {
      Predicate var3 = (var1x) -> {
         return var1x.getBlock() instanceof AbstractCauldronBlock && ((AbstractCauldronBlock)var1x.getBlock()).canReceiveStalactiteDrip(var2);
      };
      return (BlockPos)findBlockVertical(var0, var1, Direction.DOWN.getAxisDirection(), BlockBehaviour.BlockStateBase::isAir, var3, 10).orElse((Object)null);
   }

   @Nullable
   public static BlockPos findStalactiteTipAboveCauldron(Level var0, BlockPos var1) {
      return (BlockPos)findBlockVertical(var0, var1, Direction.UP.getAxisDirection(), BlockBehaviour.BlockStateBase::isAir, PointedDripstoneBlock::canDrip, 10).orElse((Object)null);
   }

   public static Fluid getCauldronFillFluidType(Level var0, BlockPos var1) {
      return (Fluid)getFluidAboveStalactite(var0, var1, var0.getBlockState(var1)).filter(PointedDripstoneBlock::canFillCauldron).orElse(Fluids.EMPTY);
   }

   private static Optional<Fluid> getFluidAboveStalactite(Level var0, BlockPos var1, BlockState var2) {
      return !isStalactite(var2) ? Optional.empty() : findRootBlock(var0, var1, var2, 10).map((var1x) -> {
         return var0.getFluidState(var1x.above()).getType();
      });
   }

   private static boolean canFillCauldron(Fluid var0) {
      return var0 == Fluids.LAVA || var0 == Fluids.WATER;
   }

   private static Fluid getDripFluid(Level var0, Fluid var1) {
      if (var1.isSame(Fluids.EMPTY)) {
         return var0.dimensionType().ultraWarm() ? Fluids.LAVA : Fluids.WATER;
      } else {
         return var1;
      }
   }

   private static Optional<BlockPos> findBlockVertical(LevelAccessor var0, BlockPos var1, Direction.AxisDirection var2, Predicate<BlockState> var3, Predicate<BlockState> var4, int var5) {
      Direction var6 = Direction.get(var2, Direction.Axis.Y);
      BlockPos.MutableBlockPos var7 = var1.mutable();

      for(int var8 = 0; var8 < var5; ++var8) {
         var7.move(var6);
         BlockState var9 = var0.getBlockState(var7);
         if (var4.test(var9)) {
            return Optional.of(var7);
         }

         if (var0.isOutsideBuildHeight(var7.getY()) || !var3.test(var9)) {
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   static {
      TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
      THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
      WATERLOGGED = BlockStateProperties.WATERLOGGED;
      TIP_MERGE_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
      TIP_SHAPE_UP = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 11.0D, 11.0D);
      TIP_SHAPE_DOWN = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 16.0D, 11.0D);
      FRUSTUM_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
      MIDDLE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
      BASE_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
   }
}
