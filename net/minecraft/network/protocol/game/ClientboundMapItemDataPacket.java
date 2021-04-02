package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ClientboundMapItemDataPacket implements Packet<ClientGamePacketListener> {
   private int mapId;
   private byte scale;
   private boolean locked;
   @Nullable
   private MapDecoration[] decorations;
   @Nullable
   private MapItemSavedData.MapPatch colorPatch;

   public ClientboundMapItemDataPacket() {
      super();
   }

   public ClientboundMapItemDataPacket(int var1, byte var2, boolean var3, @Nullable Collection<MapDecoration> var4, @Nullable MapItemSavedData.MapPatch var5) {
      super();
      this.mapId = var1;
      this.scale = var2;
      this.locked = var3;
      this.decorations = var4 != null ? (MapDecoration[])var4.toArray(new MapDecoration[0]) : null;
      this.colorPatch = var5;
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      this.mapId = var1.readVarInt();
      this.scale = var1.readByte();
      this.locked = var1.readBoolean();
      if (var1.readBoolean()) {
         this.decorations = new MapDecoration[var1.readVarInt()];

         for(int var2 = 0; var2 < this.decorations.length; ++var2) {
            MapDecoration.Type var3 = (MapDecoration.Type)var1.readEnum(MapDecoration.Type.class);
            this.decorations[var2] = new MapDecoration(var3, var1.readByte(), var1.readByte(), (byte)(var1.readByte() & 15), var1.readBoolean() ? var1.readComponent() : null);
         }
      }

      short var7 = var1.readUnsignedByte();
      if (var7 > 0) {
         short var8 = var1.readUnsignedByte();
         short var4 = var1.readUnsignedByte();
         short var5 = var1.readUnsignedByte();
         byte[] var6 = var1.readByteArray();
         this.colorPatch = new MapItemSavedData.MapPatch(var4, var5, var7, var8, var6);
      }

   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeVarInt(this.mapId);
      var1.writeByte(this.scale);
      var1.writeBoolean(this.locked);
      if (this.decorations != null) {
         var1.writeBoolean(true);
         var1.writeVarInt(this.decorations.length);
         MapDecoration[] var2 = this.decorations;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            MapDecoration var5 = var2[var4];
            var1.writeEnum(var5.getType());
            var1.writeByte(var5.getX());
            var1.writeByte(var5.getY());
            var1.writeByte(var5.getRot() & 15);
            if (var5.getName() != null) {
               var1.writeBoolean(true);
               var1.writeComponent(var5.getName());
            } else {
               var1.writeBoolean(false);
            }
         }
      } else {
         var1.writeBoolean(false);
      }

      if (this.colorPatch != null) {
         var1.writeByte(this.colorPatch.width);
         var1.writeByte(this.colorPatch.height);
         var1.writeByte(this.colorPatch.startX);
         var1.writeByte(this.colorPatch.startY);
         var1.writeByteArray(this.colorPatch.mapColors);
      } else {
         var1.writeByte(0);
      }

   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleMapItemData(this);
   }

   public int getMapId() {
      return this.mapId;
   }

   public void applyToMap(MapItemSavedData var1) {
      if (this.decorations != null) {
         var1.addClientSideDecorations(this.decorations);
      }

      if (this.colorPatch != null) {
         this.colorPatch.applyToMap(var1);
      }

   }

   public byte getScale() {
      return this.scale;
   }

   public boolean isLocked() {
      return this.locked;
   }
}
