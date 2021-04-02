package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalFeature extends StructureFeature<RuinedPortalConfiguration> {
   private static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
   private static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};

   public RuinedPortalFeature(Codec<RuinedPortalConfiguration> var1) {
      super(var1);
   }

   public StructureFeature.StructureStartFactory<RuinedPortalConfiguration> getStartFactory() {
      return RuinedPortalFeature.FeatureStart::new;
   }

   private static boolean isCold(BlockPos var0, Biome var1) {
      return var1.getTemperature(var0) < 0.15F;
   }

   private static int findSuitableY(Random var0, ChunkGenerator var1, RuinedPortalPiece.VerticalPlacement var2, boolean var3, int var4, int var5, BoundingBox var6, LevelHeightAccessor var7) {
      int var8;
      if (var2 == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
         if (var3) {
            var8 = Mth.randomBetweenInclusive(var0, 32, 100);
         } else if (var0.nextFloat() < 0.5F) {
            var8 = Mth.randomBetweenInclusive(var0, 27, 29);
         } else {
            var8 = Mth.randomBetweenInclusive(var0, 29, 100);
         }
      } else {
         int var9;
         if (var2 == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
            var9 = var4 - var5;
            var8 = getRandomWithinInterval(var0, 70, var9);
         } else if (var2 == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
            var9 = var4 - var5;
            var8 = getRandomWithinInterval(var0, 15, var9);
         } else if (var2 == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
            var8 = var4 - var5 + Mth.randomBetweenInclusive(var0, 2, 8);
         } else {
            var8 = var4;
         }
      }

      ImmutableList var18 = ImmutableList.of(new BlockPos(var6.x0, 0, var6.z0), new BlockPos(var6.x1, 0, var6.z0), new BlockPos(var6.x0, 0, var6.z1), new BlockPos(var6.x1, 0, var6.z1));
      List var10 = (List)var18.stream().map((var2x) -> {
         return var1.getBaseColumn(var2x.getX(), var2x.getZ(), var7);
      }).collect(Collectors.toList());
      Heightmap.Types var11 = var2 == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
      BlockPos.MutableBlockPos var12 = new BlockPos.MutableBlockPos();

      int var13;
      for(var13 = var8; var13 > 15; --var13) {
         int var14 = 0;
         var12.set(0, var13, 0);
         Iterator var15 = var10.iterator();

         while(var15.hasNext()) {
            NoiseColumn var16 = (NoiseColumn)var15.next();
            BlockState var17 = var16.getBlockState(var12);
            if (var11.isOpaque().test(var17)) {
               ++var14;
               if (var14 == 3) {
                  return var13;
               }
            }
         }
      }

      return var13;
   }

   private static int getRandomWithinInterval(Random var0, int var1, int var2) {
      return var1 < var2 ? Mth.randomBetweenInclusive(var0, var1, var2) : var2;
   }

   public static enum Type implements StringRepresentable {
      STANDARD("standard"),
      DESERT("desert"),
      JUNGLE("jungle"),
      SWAMP("swamp"),
      MOUNTAIN("mountain"),
      OCEAN("ocean"),
      NETHER("nether");

      public static final Codec<RuinedPortalFeature.Type> CODEC = StringRepresentable.fromEnum(RuinedPortalFeature.Type::values, RuinedPortalFeature.Type::byName);
      private static final Map<String, RuinedPortalFeature.Type> BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(RuinedPortalFeature.Type::getName, (var0) -> {
         return var0;
      }));
      private final String name;

      private Type(String var3) {
         this.name = var3;
      }

      public String getName() {
         return this.name;
      }

      public static RuinedPortalFeature.Type byName(String var0) {
         return (RuinedPortalFeature.Type)BY_NAME.get(var0);
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   public static class FeatureStart extends StructureStart<RuinedPortalConfiguration> {
      protected FeatureStart(StructureFeature<RuinedPortalConfiguration> var1, int var2, int var3, BoundingBox var4, int var5, long var6) {
         super(var1, var2, var3, var4, var5, var6);
      }

      public void generatePieces(RegistryAccess var1, ChunkGenerator var2, StructureManager var3, int var4, int var5, Biome var6, RuinedPortalConfiguration var7, LevelHeightAccessor var8) {
         RuinedPortalPiece.Properties var10 = new RuinedPortalPiece.Properties();
         RuinedPortalPiece.VerticalPlacement var9;
         if (var7.portalType == RuinedPortalFeature.Type.DESERT) {
            var9 = RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED;
            var10.airPocket = false;
            var10.mossiness = 0.0F;
         } else if (var7.portalType == RuinedPortalFeature.Type.JUNGLE) {
            var9 = RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
            var10.airPocket = this.random.nextFloat() < 0.5F;
            var10.mossiness = 0.8F;
            var10.overgrown = true;
            var10.vines = true;
         } else if (var7.portalType == RuinedPortalFeature.Type.SWAMP) {
            var9 = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
            var10.airPocket = false;
            var10.mossiness = 0.5F;
            var10.vines = true;
         } else {
            boolean var11;
            if (var7.portalType == RuinedPortalFeature.Type.MOUNTAIN) {
               var11 = this.random.nextFloat() < 0.5F;
               var9 = var11 ? RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
               var10.airPocket = var11 || this.random.nextFloat() < 0.5F;
            } else if (var7.portalType == RuinedPortalFeature.Type.OCEAN) {
               var9 = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
               var10.airPocket = false;
               var10.mossiness = 0.8F;
            } else if (var7.portalType == RuinedPortalFeature.Type.NETHER) {
               var9 = RuinedPortalPiece.VerticalPlacement.IN_NETHER;
               var10.airPocket = this.random.nextFloat() < 0.5F;
               var10.mossiness = 0.0F;
               var10.replaceWithBlackstone = true;
            } else {
               var11 = this.random.nextFloat() < 0.5F;
               var9 = var11 ? RuinedPortalPiece.VerticalPlacement.UNDERGROUND : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
               var10.airPocket = var11 || this.random.nextFloat() < 0.5F;
            }
         }

         ResourceLocation var24;
         if (this.random.nextFloat() < 0.05F) {
            var24 = new ResourceLocation(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS.length)]);
         } else {
            var24 = new ResourceLocation(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS.length)]);
         }

         StructureTemplate var12 = var3.getOrCreate(var24);
         Rotation var13 = (Rotation)Util.getRandom((Object[])Rotation.values(), this.random);
         Mirror var14 = this.random.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
         BlockPos var15 = new BlockPos(var12.getSize().getX() / 2, 0, var12.getSize().getZ() / 2);
         BlockPos var16 = (new ChunkPos(var4, var5)).getWorldPosition();
         BoundingBox var17 = var12.getBoundingBox(var16, var13, var15, var14);
         Vec3i var18 = var17.getCenter();
         int var19 = var18.getX();
         int var20 = var18.getZ();
         int var21 = var2.getBaseHeight(var19, var20, RuinedPortalPiece.getHeightMapType(var9), var8) - 1;
         int var22 = RuinedPortalFeature.findSuitableY(this.random, var2, var9, var10.airPocket, var21, var17.getYSpan(), var17, var8);
         BlockPos var23 = new BlockPos(var16.getX(), var22, var16.getZ());
         if (var7.portalType == RuinedPortalFeature.Type.MOUNTAIN || var7.portalType == RuinedPortalFeature.Type.OCEAN || var7.portalType == RuinedPortalFeature.Type.STANDARD) {
            var10.cold = RuinedPortalFeature.isCold(var23, var6);
         }

         this.pieces.add(new RuinedPortalPiece(var23, var9, var10, var24, var12, var13, var14, var15));
         this.calculateBoundingBox();
      }
   }
}
