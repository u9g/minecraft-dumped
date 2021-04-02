package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class VillagerHostilesSensor extends HostilesSensor {
   private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES;

   public VillagerHostilesSensor() {
      super();
   }

   protected Optional<LivingEntity> getNearestHostile(LivingEntity var1) {
      return this.getVisibleEntities(var1).flatMap((var2) -> {
         Stream var10000 = var2.stream().filter(this::isHostile).filter((var2x) -> {
            return this.isClose(var1, var2x);
         });
         var1.getClass();
         return var10000.min(Comparator.comparingDouble(var1::distanceToSqr));
      });
   }

   protected boolean isClose(LivingEntity var1, LivingEntity var2) {
      float var3 = (Float)ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(var2.getType());
      return var2.distanceToSqr(var1) <= (double)(var3 * var3);
   }

   private boolean isHostile(LivingEntity var1) {
      return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(var1.getType());
   }

   static {
      ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.builder().put(EntityType.DROWNED, 8.0F).put(EntityType.EVOKER, 12.0F).put(EntityType.HUSK, 8.0F).put(EntityType.ILLUSIONER, 12.0F).put(EntityType.PILLAGER, 15.0F).put(EntityType.RAVAGER, 12.0F).put(EntityType.VEX, 8.0F).put(EntityType.VINDICATOR, 10.0F).put(EntityType.ZOGLIN, 10.0F).put(EntityType.ZOMBIE, 8.0F).put(EntityType.ZOMBIE_VILLAGER, 8.0F).build();
   }
}
