package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonsterRoomFeature extends Feature<NoneFeatureConfiguration> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final EntityType<?>[] MOBS;
   private static final BlockState AIR;

   public MonsterRoomFeature(Codec<NoneFeatureConfiguration> var1) {
      super(var1);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> var1) {
      BlockPos var2 = var1.origin();
      Random var3 = var1.random();
      WorldGenLevel var4 = var1.level();
      boolean var5 = true;
      int var6 = var3.nextInt(2) + 2;
      int var7 = -var6 - 1;
      int var8 = var6 + 1;
      boolean var9 = true;
      boolean var10 = true;
      int var11 = var3.nextInt(2) + 2;
      int var12 = -var11 - 1;
      int var13 = var11 + 1;
      int var14 = 0;

      int var15;
      int var16;
      int var17;
      BlockPos var18;
      for(var15 = var7; var15 <= var8; ++var15) {
         for(var16 = -1; var16 <= 4; ++var16) {
            for(var17 = var12; var17 <= var13; ++var17) {
               var18 = var2.offset(var15, var16, var17);
               Material var19 = var4.getBlockState(var18).getMaterial();
               boolean var20 = var19.isSolid();
               if (var16 == -1 && !var20) {
                  return false;
               }

               if (var16 == 4 && !var20) {
                  return false;
               }

               if ((var15 == var7 || var15 == var8 || var17 == var12 || var17 == var13) && var16 == 0 && var4.isEmptyBlock(var18) && var4.isEmptyBlock(var18.above())) {
                  ++var14;
               }
            }
         }
      }

      if (var14 >= 1 && var14 <= 5) {
         for(var15 = var7; var15 <= var8; ++var15) {
            for(var16 = 3; var16 >= -1; --var16) {
               for(var17 = var12; var17 <= var13; ++var17) {
                  var18 = var2.offset(var15, var16, var17);
                  BlockState var26 = var4.getBlockState(var18);
                  if (var15 != var7 && var16 != -1 && var17 != var12 && var15 != var8 && var16 != 4 && var17 != var13) {
                     if (!var26.is(Blocks.CHEST) && !var26.is(Blocks.SPAWNER)) {
                        var4.setBlock(var18, AIR, 2);
                     }
                  } else if (var18.getY() >= var4.getMinBuildHeight() && !var4.getBlockState(var18.below()).getMaterial().isSolid()) {
                     var4.setBlock(var18, AIR, 2);
                  } else if (var26.getMaterial().isSolid() && !var26.is(Blocks.CHEST)) {
                     if (var16 == -1 && var3.nextInt(4) != 0) {
                        var4.setBlock(var18, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                     } else {
                        var4.setBlock(var18, Blocks.COBBLESTONE.defaultBlockState(), 2);
                     }
                  }
               }
            }
         }

         for(var15 = 0; var15 < 2; ++var15) {
            for(var16 = 0; var16 < 3; ++var16) {
               var17 = var2.getX() + var3.nextInt(var6 * 2 + 1) - var6;
               int var25 = var2.getY();
               int var27 = var2.getZ() + var3.nextInt(var11 * 2 + 1) - var11;
               BlockPos var28 = new BlockPos(var17, var25, var27);
               if (var4.isEmptyBlock(var28)) {
                  int var21 = 0;
                  Iterator var22 = Direction.Plane.HORIZONTAL.iterator();

                  while(var22.hasNext()) {
                     Direction var23 = (Direction)var22.next();
                     if (var4.getBlockState(var28.relative(var23)).getMaterial().isSolid()) {
                        ++var21;
                     }
                  }

                  if (var21 == 1) {
                     var4.setBlock(var28, StructurePiece.reorient(var4, var28, Blocks.CHEST.defaultBlockState()), 2);
                     RandomizableContainerBlockEntity.setLootTable(var4, var3, var28, BuiltInLootTables.SIMPLE_DUNGEON);
                     break;
                  }
               }
            }
         }

         var4.setBlock(var2, Blocks.SPAWNER.defaultBlockState(), 2);
         BlockEntity var24 = var4.getBlockEntity(var2);
         if (var24 instanceof SpawnerBlockEntity) {
            ((SpawnerBlockEntity)var24).getSpawner().setEntityId(this.randomEntityId(var3));
         } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", var2.getX(), var2.getY(), var2.getZ());
         }

         return true;
      } else {
         return false;
      }
   }

   private EntityType<?> randomEntityId(Random var1) {
      return (EntityType)Util.getRandom((Object[])MOBS, var1);
   }

   static {
      MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
      AIR = Blocks.CAVE_AIR.defaultBlockState();
   }
}
