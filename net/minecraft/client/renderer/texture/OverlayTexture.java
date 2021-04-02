package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

public class OverlayTexture implements AutoCloseable {
   public static final int NO_OVERLAY = pack(0, 10);
   private final DynamicTexture texture = new DynamicTexture(16, 16, false);

   public OverlayTexture() {
      super();
      NativeImage var1 = this.texture.getPixels();

      for(int var2 = 0; var2 < 16; ++var2) {
         for(int var3 = 0; var3 < 16; ++var3) {
            if (var2 < 8) {
               var1.setPixelRGBA(var3, var2, -1308622593);
            } else {
               int var4 = (int)((1.0F - (float)var3 / 15.0F * 0.75F) * 255.0F);
               var1.setPixelRGBA(var3, var2, var4 << 24 | 16777215);
            }
         }
      }

      RenderSystem.activeTexture(33985);
      this.texture.bind();
      RenderSystem.matrixMode(5890);
      RenderSystem.loadIdentity();
      float var5 = 0.06666667F;
      RenderSystem.scalef(0.06666667F, 0.06666667F, 0.06666667F);
      RenderSystem.matrixMode(5888);
      this.texture.bind();
      var1.upload(0, 0, 0, 0, 0, var1.getWidth(), var1.getHeight(), false, true, false, false);
      RenderSystem.activeTexture(33984);
   }

   public void close() {
      this.texture.close();
   }

   public void setupOverlayColor() {
      RenderSystem.setupOverlayColor(this.texture::getId, 16);
   }

   public static int u(float var0) {
      return (int)(var0 * 15.0F);
   }

   public static int v(boolean var0) {
      return var0 ? 3 : 10;
   }

   public static int pack(int var0, int var1) {
      return var0 | var1 << 16;
   }

   public static int pack(float var0, boolean var1) {
      return pack(u(var0), v(var1));
   }

   public void teardownOverlayColor() {
      RenderSystem.teardownOverlayColor();
   }
}
