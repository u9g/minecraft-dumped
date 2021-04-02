package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SmoothSwimmingMoveControl extends MoveControl {
   private final int maxTurnX;
   private final int maxTurnY;
   private final float inWaterSpeedModifier;
   private final float outsideWaterSpeedModifier;
   private final boolean applyGravity;

   public SmoothSwimmingMoveControl(Mob var1, int var2, int var3, float var4, float var5, boolean var6) {
      super(var1);
      this.maxTurnX = var2;
      this.maxTurnY = var3;
      this.inWaterSpeedModifier = var4;
      this.outsideWaterSpeedModifier = var5;
      this.applyGravity = var6;
   }

   public void tick() {
      if (this.applyGravity && this.mob.isInWater()) {
         this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
      }

      if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
         double var1 = this.wantedX - this.mob.getX();
         double var3 = this.wantedY - this.mob.getY();
         double var5 = this.wantedZ - this.mob.getZ();
         double var7 = var1 * var1 + var3 * var3 + var5 * var5;
         if (var7 < 2.500000277905201E-7D) {
            this.mob.setZza(0.0F);
         } else {
            float var9 = (float)(Mth.atan2(var5, var1) * 57.2957763671875D) - 90.0F;
            this.mob.yRot = this.rotlerp(this.mob.yRot, var9, (float)this.maxTurnY);
            this.mob.yBodyRot = this.mob.yRot;
            this.mob.yHeadRot = this.mob.yRot;
            float var10 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            if (this.mob.isInWater()) {
               this.mob.setSpeed(var10 * this.inWaterSpeedModifier);
               float var11 = -((float)(Mth.atan2(var3, (double)Mth.sqrt(var1 * var1 + var5 * var5)) * 57.2957763671875D));
               var11 = Mth.clamp(Mth.wrapDegrees(var11), (float)(-this.maxTurnX), (float)this.maxTurnX);
               this.mob.xRot = this.rotlerp(this.mob.xRot, var11, 5.0F);
               float var12 = Mth.cos(this.mob.xRot * 0.017453292F);
               float var13 = Mth.sin(this.mob.xRot * 0.017453292F);
               this.mob.zza = var12 * var10;
               this.mob.yya = -var13 * var10;
            } else {
               this.mob.setSpeed(var10 * this.outsideWaterSpeedModifier);
            }

         }
      } else {
         this.mob.setSpeed(0.0F);
         this.mob.setXxa(0.0F);
         this.mob.setYya(0.0F);
         this.mob.setZza(0.0F);
      }
   }
}
