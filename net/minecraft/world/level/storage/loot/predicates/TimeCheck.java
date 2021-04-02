package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class TimeCheck implements LootItemCondition {
   @Nullable
   private final Long period;
   private final IntRange value;

   private TimeCheck(@Nullable Long var1, IntRange var2) {
      super();
      this.period = var1;
      this.value = var2;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.TIME_CHECK;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.value.getReferencedContextParams();
   }

   public boolean test(LootContext var1) {
      ServerLevel var2 = var1.getLevel();
      long var3 = var2.getDayTime();
      if (this.period != null) {
         var3 %= this.period;
      }

      return this.value.test(var1, (int)var3);
   }

   // $FF: synthetic method
   public boolean test(Object var1) {
      return this.test((LootContext)var1);
   }

   // $FF: synthetic method
   TimeCheck(Long var1, IntRange var2, Object var3) {
      this(var1, var2);
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
      public Serializer() {
         super();
      }

      public void serialize(JsonObject var1, TimeCheck var2, JsonSerializationContext var3) {
         var1.addProperty("period", var2.period);
         var1.add("value", var3.serialize(var2.value));
      }

      public TimeCheck deserialize(JsonObject var1, JsonDeserializationContext var2) {
         Long var3 = var1.has("period") ? GsonHelper.getAsLong(var1, "period") : null;
         IntRange var4 = (IntRange)GsonHelper.getAsObject(var1, "value", var2, IntRange.class);
         return new TimeCheck(var3, var4);
      }

      // $FF: synthetic method
      public Object deserialize(JsonObject var1, JsonDeserializationContext var2) {
         return this.deserialize(var1, var2);
      }
   }
}
