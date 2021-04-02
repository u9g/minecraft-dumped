package net.minecraft.network.protocol.game;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;

public class ClientboundUpdateTagsPacket implements Packet<ClientGamePacketListener> {
   private Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> tags;

   public ClientboundUpdateTagsPacket() {
      super();
   }

   public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> var1) {
      super();
      this.tags = var1;
   }

   public void read(FriendlyByteBuf var1) throws IOException {
      int var2 = var1.readVarInt();
      this.tags = Maps.newHashMapWithExpectedSize(var2);

      for(int var3 = 0; var3 < var2; ++var3) {
         ResourceLocation var4 = var1.readResourceLocation();
         ResourceKey var5 = ResourceKey.createRegistryKey(var4);
         TagCollection.NetworkPayload var6 = TagCollection.NetworkPayload.read(var1);
         this.tags.put(var5, var6);
      }

   }

   public void write(FriendlyByteBuf var1) throws IOException {
      var1.writeVarInt(this.tags.size());
      this.tags.forEach((var1x, var2) -> {
         var1.writeResourceLocation(var1x.location());
         var2.write(var1);
      });
   }

   public void handle(ClientGamePacketListener var1) {
      var1.handleUpdateTags(this);
   }

   public Map<ResourceKey<? extends Registry<?>>, TagCollection.NetworkPayload> getTags() {
      return this.tags;
   }
}
