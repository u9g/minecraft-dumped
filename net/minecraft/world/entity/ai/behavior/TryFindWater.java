package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class TryFindWater extends Behavior<PathfinderMob> {
   private final int range;
   private final float speedModifier;

   public TryFindWater(int var1, float var2) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
      this.range = var1;
      this.speedModifier = var2;
   }

   protected boolean checkExtraStartConditions(ServerLevel var1, PathfinderMob var2) {
      return !var2.level.getFluidState(var2.blockPosition()).is(FluidTags.WATER);
   }

   protected void start(ServerLevel var1, PathfinderMob var2, long var3) {
      BlockPos var5 = null;
      Iterable var6 = BlockPos.withinManhattan(var2.blockPosition(), this.range, this.range, this.range);
      Iterator var7 = var6.iterator();

      while(var7.hasNext()) {
         BlockPos var8 = (BlockPos)var7.next();
         if (var2.level.getFluidState(var8).is(FluidTags.WATER)) {
            var5 = var8.immutable();
            break;
         }
      }

      if (var5 != null) {
         BehaviorUtils.setWalkAndLookTargetMemories(var2, (BlockPos)var5, this.speedModifier, 0);
      }

   }
}
