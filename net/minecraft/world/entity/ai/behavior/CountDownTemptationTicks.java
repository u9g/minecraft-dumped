package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CountDownTemptationTicks extends Behavior<LivingEntity> {
   public CountDownTemptationTicks() {
      super(ImmutableMap.of(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT));
   }

   private Optional<Integer> getCalmDownTickMemory(LivingEntity var1) {
      return var1.getBrain().getMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS);
   }

   protected boolean canStillUse(ServerLevel var1, LivingEntity var2, long var3) {
      Optional var5 = this.getCalmDownTickMemory(var2);
      return var5.isPresent() && (Integer)var5.get() > 0;
   }

   protected void tick(ServerLevel var1, LivingEntity var2, long var3) {
      Optional var5 = this.getCalmDownTickMemory(var2);
      var2.getBrain().setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, (Object)((Integer)var5.get() - 1));
   }

   protected void stop(ServerLevel var1, LivingEntity var2, long var3) {
      var2.getBrain().eraseMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS);
   }
}
