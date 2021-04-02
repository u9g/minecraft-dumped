package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot {
   private final int slot;
   public final Container container;
   public int index;
   public final int x;
   public final int y;

   public Slot(Container var1, int var2, int var3, int var4) {
      super();
      this.container = var1;
      this.slot = var2;
      this.x = var3;
      this.y = var4;
   }

   public void onQuickCraft(ItemStack var1, ItemStack var2) {
      int var3 = var2.getCount() - var1.getCount();
      if (var3 > 0) {
         this.onQuickCraft(var2, var3);
      }

   }

   protected void onQuickCraft(ItemStack var1, int var2) {
   }

   protected void onSwapCraft(int var1) {
   }

   protected void checkTakeAchievements(ItemStack var1) {
   }

   public ItemStack onTake(Player var1, ItemStack var2) {
      this.setChanged();
      return var2;
   }

   public boolean mayPlace(ItemStack var1) {
      return true;
   }

   public ItemStack getItem() {
      return this.container.getItem(this.slot);
   }

   public boolean hasItem() {
      return !this.getItem().isEmpty();
   }

   public void set(ItemStack var1) {
      this.container.setItem(this.slot, var1);
      this.setChanged();
   }

   public void setChanged() {
      this.container.setChanged();
   }

   public int getMaxStackSize() {
      return this.container.getMaxStackSize();
   }

   public int getMaxStackSize(ItemStack var1) {
      return Math.min(this.getMaxStackSize(), var1.getMaxStackSize());
   }

   @Nullable
   public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
      return null;
   }

   public ItemStack remove(int var1) {
      return this.container.removeItem(this.slot, var1);
   }

   public boolean mayPickup(Player var1) {
      return true;
   }

   public boolean isActive() {
      return true;
   }

   public ItemStack safeTake(int var1, int var2, Player var3) {
      if (!this.mayPickup(var3)) {
         return ItemStack.EMPTY;
      } else if (!this.allowModification(var3) && var2 < this.getItem().getCount()) {
         return ItemStack.EMPTY;
      } else {
         if (!this.allowModification(var3)) {
            var1 = this.getItem().getCount();
         }

         var1 = Math.min(var1, var2);
         ItemStack var4 = this.remove(var1);
         if (this.getItem().isEmpty()) {
            this.set(ItemStack.EMPTY);
         }

         this.onTake(var3, var4);
         return var4;
      }
   }

   public ItemStack safeInsert(ItemStack var1) {
      return this.safeInsert(var1, var1.getCount());
   }

   public ItemStack safeInsert(ItemStack var1, int var2) {
      if (!var1.isEmpty() && this.mayPlace(var1)) {
         ItemStack var3 = this.getItem();
         int var4 = Math.min(Math.min(var2, var1.getCount()), this.getMaxStackSize(var1) - var3.getCount());
         if (var3.isEmpty()) {
            this.set(var1.split(var4));
         } else if (ItemStack.isSameItemSameTags(var3, var1)) {
            var1.shrink(var4);
            var3.grow(var4);
            this.set(var3);
         }

         return var1;
      } else {
         return var1;
      }
   }

   public boolean allowModification(Player var1) {
      return this.mayPickup(var1) && this.mayPlace(this.getItem());
   }
}
