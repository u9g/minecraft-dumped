package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class AxolotlModel<T extends Axolotl> extends AgeableListModel<T> {
   private final ModelPart tail;
   private final ModelPart leftHindLeg;
   private final ModelPart rightHindLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart topGills;
   private final ModelPart leftGills;
   private final ModelPart rightGills;

   public AxolotlModel(ModelPart var1) {
      super(true, 8.0F, 3.35F);
      this.body = var1.getChild("body");
      this.head = this.body.getChild("head");
      this.rightHindLeg = this.body.getChild("right_hind_leg");
      this.leftHindLeg = this.body.getChild("left_hind_leg");
      this.rightFrontLeg = this.body.getChild("right_front_leg");
      this.leftFrontLeg = this.body.getChild("left_front_leg");
      this.tail = this.body.getChild("tail");
      this.topGills = this.head.getChild("top_gills");
      this.leftGills = this.head.getChild("left_gills");
      this.rightGills = this.head.getChild("right_gills");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition var0 = new MeshDefinition();
      PartDefinition var1 = var0.getRoot();
      PartDefinition var2 = var1.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.0F, -2.0F, -9.0F, 8.0F, 4.0F, 10.0F).texOffs(2, 17).addBox(0.0F, -3.0F, -8.0F, 0.0F, 5.0F, 9.0F), PartPose.offset(0.0F, 20.0F, 5.0F));
      CubeDeformation var3 = new CubeDeformation(0.001F);
      PartDefinition var4 = var2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 5.0F, 5.0F, var3), PartPose.offset(0.0F, 0.0F, -9.0F));
      CubeListBuilder var5 = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 0.0F, var3);
      CubeListBuilder var6 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, var3);
      CubeListBuilder var7 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, var3);
      var4.addOrReplaceChild("top_gills", var5, PartPose.offset(0.0F, -3.0F, -1.0F));
      var4.addOrReplaceChild("left_gills", var6, PartPose.offset(-4.0F, 0.0F, -1.0F));
      var4.addOrReplaceChild("right_gills", var7, PartPose.offset(4.0F, 0.0F, -1.0F));
      CubeListBuilder var8 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, var3);
      CubeListBuilder var9 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, var3);
      var2.addOrReplaceChild("right_hind_leg", var9, PartPose.offset(-3.5F, 1.0F, -1.0F));
      var2.addOrReplaceChild("left_hind_leg", var8, PartPose.offset(3.5F, 1.0F, -1.0F));
      var2.addOrReplaceChild("right_front_leg", var9, PartPose.offset(-3.5F, 1.0F, -8.0F));
      var2.addOrReplaceChild("left_front_leg", var8, PartPose.offset(3.5F, 1.0F, -8.0F));
      var2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 1.0F));
      return LayerDefinition.create(var0, 64, 64);
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of();
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body);
   }

   public void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6) {
      this.setupInitialAnimationValues(var5, var6);
      if (var1.isPlayingDead()) {
         this.setupPlayDeadAnimation();
      } else {
         boolean var7 = Entity.getHorizontalDistanceSqr(var1.getDeltaMovement()) > 1.0E-7D;
         if (var1.isInWaterOrBubble()) {
            if (var7) {
               this.setupSwimmingAnimation(var4, var6);
            } else {
               this.setupWaterHoveringAnimation(var4);
            }

         } else {
            if (var1.isOnGround()) {
               if (var7) {
                  this.setupGroundCrawlingAnimation(var4);
               } else {
                  this.setupLayStillOnGroundAnimation(var4);
               }
            }

         }
      }
   }

   private void setupInitialAnimationValues(float var1, float var2) {
      this.body.x = 0.0F;
      this.head.y = 0.0F;
      this.body.y = 20.0F;
      this.body.setRotation(var2 * 0.017453292F, var1 * 0.017453292F, 0.0F);
      this.head.setRotation(0.0F, 0.0F, 0.0F);
      this.leftHindLeg.setRotation(0.0F, 0.0F, 0.0F);
      this.rightHindLeg.setRotation(0.0F, 0.0F, 0.0F);
      this.leftFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
      this.rightFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
      this.leftGills.setRotation(0.0F, 0.0F, 0.0F);
      this.rightGills.setRotation(0.0F, 0.0F, 0.0F);
      this.topGills.setRotation(0.0F, 0.0F, 0.0F);
      this.tail.setRotation(0.0F, 0.0F, 0.0F);
   }

   private void setupLayStillOnGroundAnimation(float var1) {
      float var2 = var1 * 0.09F;
      float var3 = Mth.sin(var2);
      float var4 = Mth.cos(var2);
      float var5 = var3 * var3 - 2.0F * var3;
      float var6 = var4 * var4 - 3.0F * var3;
      this.head.xRot = -0.09F * var5;
      this.head.zRot = -0.2F;
      this.tail.yRot = -0.1F + 0.1F * var5;
      this.topGills.xRot = 0.6F + 0.05F * var6;
      this.leftGills.yRot = -this.topGills.xRot;
      this.rightGills.yRot = -this.leftGills.yRot;
      this.leftHindLeg.setRotation(1.1F, 1.0F, 0.0F);
      this.leftFrontLeg.setRotation(0.8F, 2.3F, -0.5F);
      this.applyMirrorLegRotations();
   }

   private void setupGroundCrawlingAnimation(float var1) {
      float var2 = var1 * 0.11F;
      float var3 = Mth.cos(var2);
      float var4 = (var3 * var3 - 2.0F * var3) / 5.0F;
      float var5 = 0.7F * var3;
      this.head.yRot = 0.09F * var3;
      this.tail.yRot = this.head.yRot;
      this.topGills.xRot = 0.6F - 0.08F * (var3 * var3 + 2.0F * Mth.sin(var2));
      this.leftGills.yRot = -this.topGills.xRot;
      this.rightGills.yRot = -this.leftGills.yRot;
      this.leftHindLeg.setRotation(0.9424779F, 1.5F - var4, -0.1F);
      this.leftFrontLeg.setRotation(1.0995574F, 1.5707964F - var5, 0.0F);
      this.rightHindLeg.setRotation(this.leftHindLeg.xRot, -1.0F - var4, 0.0F);
      this.rightFrontLeg.setRotation(this.leftFrontLeg.xRot, -1.5707964F - var5, 0.0F);
   }

   private void setupWaterHoveringAnimation(float var1) {
      float var2 = var1 * 0.075F;
      float var3 = Mth.cos(var2);
      float var4 = Mth.sin(var2) * 0.15F;
      this.body.xRot = -0.15F + 0.075F * var3;
      ModelPart var10000 = this.body;
      var10000.y -= var4;
      this.head.xRot = -this.body.xRot;
      this.topGills.xRot = 0.2F * var3;
      this.leftGills.yRot = -0.3F * var3 - 0.19F;
      this.rightGills.yRot = -this.leftGills.yRot;
      this.leftHindLeg.setRotation(2.3561945F - var3 * 0.11F, 0.47123894F, 1.7278761F);
      this.leftFrontLeg.setRotation(0.7853982F - var3 * 0.2F, 2.042035F, 0.0F);
      this.applyMirrorLegRotations();
      this.tail.yRot = 0.5F * var3;
   }

   private void setupSwimmingAnimation(float var1, float var2) {
      float var3 = var1 * 0.33F;
      float var4 = Mth.sin(var3);
      float var5 = Mth.cos(var3);
      float var6 = 0.13F * var4;
      this.body.xRot = var2 * 0.017453292F + var6;
      this.head.xRot = -var6 * 1.8F;
      ModelPart var10000 = this.body;
      var10000.y -= 0.45F * var5;
      this.topGills.xRot = -0.5F * var4 - 0.8F;
      this.leftGills.yRot = 0.3F * var4 + 0.9F;
      this.rightGills.yRot = -this.leftGills.yRot;
      this.tail.yRot = 0.3F * Mth.cos(var3 * 0.9F);
      this.leftHindLeg.setRotation(1.8849558F, -0.4F * var4, 1.5707964F);
      this.leftFrontLeg.setRotation(1.8849558F, -0.2F * var5 - 0.1F, 1.5707964F);
      this.applyMirrorLegRotations();
   }

   private void setupPlayDeadAnimation() {
      this.leftHindLeg.setRotation(1.4137167F, 1.0995574F, 0.7853982F);
      this.leftFrontLeg.setRotation(0.7853982F, 2.042035F, 0.0F);
      this.body.xRot = -0.15F;
      this.body.zRot = 0.35F;
      this.applyMirrorLegRotations();
   }

   private void applyMirrorLegRotations() {
      this.rightHindLeg.setRotation(this.leftHindLeg.xRot, -this.leftHindLeg.yRot, -this.leftHindLeg.zRot);
      this.rightFrontLeg.setRotation(this.leftFrontLeg.xRot, -this.leftFrontLeg.yRot, -this.leftFrontLeg.zRot);
   }
}
