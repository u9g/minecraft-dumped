package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PowderSnowBlock extends Block implements BucketPickup {
   public PowderSnowBlock(BlockBehaviour.Properties var1) {
      super(var1);
   }

   public boolean skipRendering(BlockState var1, BlockState var2, Direction var3) {
      return var2.is(this) ? true : super.skipRendering(var1, var2, var3);
   }

   public VoxelShape getOcclusionShape(BlockState var1, BlockGetter var2, BlockPos var3) {
      return Shapes.empty();
   }

   public void entityInside(BlockState var1, Level var2, BlockPos var3, Entity var4) {
      if (!(var4 instanceof LivingEntity) || ((LivingEntity)var4).getFeetBlockState().is(Blocks.POWDER_SNOW)) {
         var4.makeStuckInBlock(var1, new Vec3(0.8999999761581421D, 0.9900000095367432D, 0.8999999761581421D));
      }

      var4.setIsInPowderSnow(true);
      if (var2.isClientSide) {
         var4.clearFire();
      } else {
         var4.setSharedFlagOnFire(false);
      }

      if (!var4.isSpectator() && (var4.xOld != var4.getX() || var4.zOld != var4.getZ()) && var2.random.nextBoolean()) {
         spawnPowderSnowParticles(var2, new Vec3(var4.getX(), (double)var3.getY(), var4.getZ()));
      }

   }

   public VoxelShape getCollisionShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      if (var4 instanceof EntityCollisionContext) {
         EntityCollisionContext var5 = (EntityCollisionContext)var4;
         Optional var6 = var5.getEntity();
         boolean var7 = var6.isPresent() && var6.get() instanceof FallingBlockEntity;
         if (var7 || var6.isPresent() && canEntityWalkOnPowderSnow((Entity)var6.get()) && var4.isAbove(Shapes.block(), var3, false) && !var4.isDescending()) {
            return super.getCollisionShape(var1, var2, var3, var4);
         }
      }

      return Shapes.empty();
   }

   public VoxelShape getVisualShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      return Shapes.empty();
   }

   public static void spawnPowderSnowParticles(Level var0, Vec3 var1) {
      if (var0.isClientSide) {
         Random var2 = var0.getRandom();
         double var3 = var1.y + 1.0D;

         for(int var5 = 0; var5 < var2.nextInt(3); ++var5) {
            var0.addParticle(ParticleTypes.SNOWFLAKE, var1.x, var3, var1.z, (double)((-1.0F + var2.nextFloat() * 2.0F) / 12.0F), 0.05000000074505806D, (double)((-1.0F + var2.nextFloat() * 2.0F) / 12.0F));
         }

      }
   }

   public static boolean canEntityWalkOnPowderSnow(Entity var0) {
      if (var0.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
         return true;
      } else {
         return var0 instanceof LivingEntity ? ((LivingEntity)var0).getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS) : false;
      }
   }

   public ItemStack pickupBlock(LevelAccessor var1, BlockPos var2, BlockState var3) {
      var1.setBlock(var2, Blocks.AIR.defaultBlockState(), 11);
      if (!var1.isClientSide()) {
         var1.levelEvent(2001, var2, Block.getId(var3));
      }

      return new ItemStack(Items.POWDER_SNOW_BUCKET);
   }

   public Optional<SoundEvent> getPickupSound() {
      return Optional.of(SoundEvents.BUCKET_FILL_POWDER_SNOW);
   }
}
