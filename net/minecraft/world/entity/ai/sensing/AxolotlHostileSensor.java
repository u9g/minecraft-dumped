package net.minecraft.world.entity.ai.sensing;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlHostileSensor extends HostilesSensor {
   public AxolotlHostileSensor() {
      super();
   }

   protected Optional<LivingEntity> getNearestHostile(LivingEntity var1) {
      return this.getVisibleEntities(var1).flatMap((var2) -> {
         Stream var10000 = var2.stream().filter((var2x) -> {
            return this.shouldTarget(var1, var2x);
         }).filter((var2x) -> {
            return this.isClose(var1, var2x);
         }).filter(Entity::isInWaterOrBubble);
         var1.getClass();
         return var10000.min(Comparator.comparingDouble(var1::distanceToSqr));
      });
   }

   private boolean shouldTarget(LivingEntity var1, LivingEntity var2) {
      EntityType var3 = var2.getType();
      if (EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES.contains(var3)) {
         return true;
      } else if (!EntityTypeTags.AXOLOTL_TEMPTED_HOSTILES.contains(var3)) {
         return false;
      } else {
         Optional var4 = var1.getBrain().getMemory(MemoryModuleType.IS_TEMPTED);
         return var4.isPresent() && (Boolean)var4.get();
      }
   }

   protected boolean isClose(LivingEntity var1, LivingEntity var2) {
      return var2.distanceToSqr(var1) <= 64.0D;
   }
}
