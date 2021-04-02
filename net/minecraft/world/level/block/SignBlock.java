package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED;
   public static final BooleanProperty LIT;
   protected static final VoxelShape SHAPE;
   private final WoodType type;

   protected SignBlock(BlockBehaviour.Properties var1, WoodType var2) {
      super(var1);
      this.type = var2;
   }

   public BlockState updateShape(BlockState var1, Direction var2, BlockState var3, LevelAccessor var4, BlockPos var5, BlockPos var6) {
      if ((Boolean)var1.getValue(WATERLOGGED)) {
         var4.getLiquidTicks().scheduleTick(var5, Fluids.WATER, Fluids.WATER.getTickDelay(var4));
      }

      return super.updateShape(var1, var2, var3, var4, var5, var6);
   }

   public VoxelShape getShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      return SHAPE;
   }

   public boolean isPossibleToRespawnInThis() {
      return true;
   }

   public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
      return new SignBlockEntity(var1, var2);
   }

   public InteractionResult use(BlockState var1, Level var2, BlockPos var3, Player var4, InteractionHand var5, BlockHitResult var6) {
      ItemStack var7 = var4.getItemInHand(var5);
      boolean var8 = var7.getItem() instanceof DyeItem;
      boolean var9 = var7.is(Items.GLOW_INK_SAC);
      boolean var10 = var7.is(Items.INK_SAC);
      boolean var11 = (var9 || var8 || var10) && var4.getAbilities().mayBuild;
      boolean var12 = (Boolean)var1.getValue(LIT);
      if (var9 && var12 || var10 && !var12) {
         return InteractionResult.PASS;
      } else if (var2.isClientSide) {
         return var11 ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
      } else {
         BlockEntity var13 = var2.getBlockEntity(var3);
         if (var13 instanceof SignBlockEntity) {
            SignBlockEntity var14 = (SignBlockEntity)var13;
            if (var11) {
               boolean var15;
               if (var9) {
                  var2.playSound((Player)null, (BlockPos)var3, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                  var2.setBlockAndUpdate(var3, (BlockState)var1.setValue(LIT, true));
                  var15 = true;
               } else if (var10) {
                  var2.playSound((Player)null, (BlockPos)var3, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                  var2.setBlockAndUpdate(var3, (BlockState)var1.setValue(LIT, false));
                  var15 = true;
               } else {
                  var2.playSound((Player)null, (BlockPos)var3, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                  var15 = var14.setColor(((DyeItem)var7.getItem()).getDyeColor());
               }

               if (var15 && !var4.isCreative()) {
                  var7.shrink(1);
               }
            }

            return var14.executeClickCommands((ServerPlayer)var4) ? InteractionResult.SUCCESS : InteractionResult.PASS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public FluidState getFluidState(BlockState var1) {
      return (Boolean)var1.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(var1);
   }

   public WoodType type() {
      return this.type;
   }

   static {
      WATERLOGGED = BlockStateProperties.WATERLOGGED;
      LIT = BlockStateProperties.LIT;
      SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
   }
}
