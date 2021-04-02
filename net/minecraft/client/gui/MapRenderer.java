package net.minecraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapRenderer implements AutoCloseable {
   private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
   private static final RenderType MAP_ICONS;
   private final TextureManager textureManager;
   private final Int2ObjectMap<MapRenderer.MapInstance> maps = new Int2ObjectOpenHashMap();

   public MapRenderer(TextureManager var1) {
      super();
      this.textureManager = var1;
   }

   public void update(int var1, MapItemSavedData var2) {
      this.getOrCreateMapInstance(var1, var2).updateTexture();
   }

   public void render(PoseStack var1, MultiBufferSource var2, int var3, MapItemSavedData var4, boolean var5, int var6) {
      this.getOrCreateMapInstance(var3, var4).draw(var1, var2, var5, var6);
   }

   private MapRenderer.MapInstance getOrCreateMapInstance(int var1, MapItemSavedData var2) {
      return (MapRenderer.MapInstance)this.maps.computeIfAbsent(var1, (var2x) -> {
         return new MapRenderer.MapInstance(var2x, var2);
      });
   }

   @Nullable
   public MapItemSavedData retrieveMapFromRenderer(int var1) {
      MapRenderer.MapInstance var2 = (MapRenderer.MapInstance)this.maps.get(var1);
      return var2 != null ? var2.data : null;
   }

   public void resetData() {
      ObjectIterator var1 = this.maps.values().iterator();

      while(var1.hasNext()) {
         MapRenderer.MapInstance var2 = (MapRenderer.MapInstance)var1.next();
         var2.close();
      }

      this.maps.clear();
   }

   public void close() {
      this.resetData();
   }

   static {
      MAP_ICONS = RenderType.text(MAP_ICONS_LOCATION);
   }

   class MapInstance implements AutoCloseable {
      private final MapItemSavedData data;
      private final DynamicTexture texture;
      private final RenderType renderType;

      private MapInstance(int var2, MapItemSavedData var3) {
         super();
         this.data = var3;
         this.texture = new DynamicTexture(128, 128, true);
         ResourceLocation var4 = MapRenderer.this.textureManager.register("map/" + var2, this.texture);
         this.renderType = RenderType.text(var4);
      }

      private void updateTexture() {
         for(int var1 = 0; var1 < 128; ++var1) {
            for(int var2 = 0; var2 < 128; ++var2) {
               int var3 = var2 + var1 * 128;
               int var4 = this.data.colors[var3] & 255;
               if (var4 / 4 == 0) {
                  this.texture.getPixels().setPixelRGBA(var2, var1, 0);
               } else {
                  this.texture.getPixels().setPixelRGBA(var2, var1, MaterialColor.MATERIAL_COLORS[var4 / 4].calculateRGBColor(var4 & 3));
               }
            }
         }

         this.texture.upload();
      }

      private void draw(PoseStack var1, MultiBufferSource var2, boolean var3, int var4) {
         boolean var5 = false;
         boolean var6 = false;
         float var7 = 0.0F;
         Matrix4f var8 = var1.last().pose();
         VertexConsumer var9 = var2.getBuffer(this.renderType);
         var9.vertex(var8, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(var4).endVertex();
         var9.vertex(var8, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(var4).endVertex();
         var9.vertex(var8, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(var4).endVertex();
         var9.vertex(var8, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(var4).endVertex();
         int var10 = 0;
         Iterator var11 = this.data.getDecorations().iterator();

         while(true) {
            MapDecoration var12;
            do {
               if (!var11.hasNext()) {
                  return;
               }

               var12 = (MapDecoration)var11.next();
            } while(var3 && !var12.renderOnFrame());

            var1.pushPose();
            var1.translate((double)(0.0F + (float)var12.getX() / 2.0F + 64.0F), (double)(0.0F + (float)var12.getY() / 2.0F + 64.0F), -0.019999999552965164D);
            var1.mulPose(Vector3f.ZP.rotationDegrees((float)(var12.getRot() * 360) / 16.0F));
            var1.scale(4.0F, 4.0F, 3.0F);
            var1.translate(-0.125D, 0.125D, 0.0D);
            byte var13 = var12.getImage();
            float var14 = (float)(var13 % 16 + 0) / 16.0F;
            float var15 = (float)(var13 / 16 + 0) / 16.0F;
            float var16 = (float)(var13 % 16 + 1) / 16.0F;
            float var17 = (float)(var13 / 16 + 1) / 16.0F;
            Matrix4f var18 = var1.last().pose();
            float var19 = -0.001F;
            VertexConsumer var20 = var2.getBuffer(MapRenderer.MAP_ICONS);
            var20.vertex(var18, -1.0F, 1.0F, (float)var10 * -0.001F).color(255, 255, 255, 255).uv(var14, var15).uv2(var4).endVertex();
            var20.vertex(var18, 1.0F, 1.0F, (float)var10 * -0.001F).color(255, 255, 255, 255).uv(var16, var15).uv2(var4).endVertex();
            var20.vertex(var18, 1.0F, -1.0F, (float)var10 * -0.001F).color(255, 255, 255, 255).uv(var16, var17).uv2(var4).endVertex();
            var20.vertex(var18, -1.0F, -1.0F, (float)var10 * -0.001F).color(255, 255, 255, 255).uv(var14, var17).uv2(var4).endVertex();
            var1.popPose();
            if (var12.getName() != null) {
               Font var21 = Minecraft.getInstance().font;
               Component var22 = var12.getName();
               float var23 = (float)var21.width((FormattedText)var22);
               float var10000 = 25.0F / var23;
               var21.getClass();
               float var24 = Mth.clamp(var10000, 0.0F, 6.0F / 9.0F);
               var1.pushPose();
               var1.translate((double)(0.0F + (float)var12.getX() / 2.0F + 64.0F - var23 * var24 / 2.0F), (double)(0.0F + (float)var12.getY() / 2.0F + 64.0F + 4.0F), -0.02500000037252903D);
               var1.scale(var24, var24, 1.0F);
               var1.translate(0.0D, 0.0D, -0.10000000149011612D);
               var21.drawInBatch((Component)var22, 0.0F, 0.0F, -1, false, var1.last().pose(), var2, false, -2147483648, var4);
               var1.popPose();
            }

            ++var10;
         }
      }

      public void close() {
         this.texture.close();
      }

      // $FF: synthetic method
      MapInstance(int var2, MapItemSavedData var3, Object var4) {
         this(var2, var3);
      }
   }
}
