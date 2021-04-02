package net.minecraft.world.phys.shapes;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
   protected static final CollisionContext EMPTY;
   private final boolean descending;
   private final double entityBottom;
   private final ItemStack heldItem;
   private final ItemStack footItem;
   private final Predicate<Fluid> canStandOnFluid;
   private final Optional<Entity> entity;

   protected EntityCollisionContext(boolean var1, double var2, ItemStack var4, ItemStack var5, Predicate<Fluid> var6, Optional<Entity> var7) {
      super();
      this.descending = var1;
      this.entityBottom = var2;
      this.footItem = var4;
      this.heldItem = var5;
      this.canStandOnFluid = var6;
      this.entity = var7;
   }

   @Deprecated
   protected EntityCollisionContext(Entity var1) {
      boolean var10001 = var1.isDescending();
      double var10002 = var1.getY();
      ItemStack var10003 = var1 instanceof LivingEntity ? ((LivingEntity)var1).getItemBySlot(EquipmentSlot.FEET) : ItemStack.EMPTY;
      ItemStack var10004 = var1 instanceof LivingEntity ? ((LivingEntity)var1).getMainHandItem() : ItemStack.EMPTY;
      Predicate var2;
      if (var1 instanceof LivingEntity) {
         LivingEntity var10005 = (LivingEntity)var1;
         ((LivingEntity)var1).getClass();
         var2 = var10005::canStandOnFluid;
      } else {
         var2 = (var0) -> {
            return false;
         };
      }

      this(var10001, var10002, var10003, var10004, var2, Optional.of(var1));
   }

   public boolean isHoldingItem(Item var1) {
      return this.heldItem.is(var1);
   }

   public boolean canStandOnFluid(FluidState var1, FlowingFluid var2) {
      return this.canStandOnFluid.test(var2) && !var1.getType().isSame(var2);
   }

   public boolean isDescending() {
      return this.descending;
   }

   public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3) {
      return this.entityBottom > (double)var2.getY() + var1.max(Direction.Axis.Y) - 9.999999747378752E-6D;
   }

   public Optional<Entity> getEntity() {
      return this.entity;
   }

   static {
      EMPTY = new EntityCollisionContext(false, -1.7976931348623157E308D, ItemStack.EMPTY, ItemStack.EMPTY, (var0) -> {
         return false;
      }, Optional.empty()) {
         public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3) {
            return var3;
         }
      };
   }
}
