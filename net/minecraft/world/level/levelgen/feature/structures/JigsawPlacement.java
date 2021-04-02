package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JigsawPlacement {
   private static final Logger LOGGER = LogManager.getLogger();

   public static void addPieces(RegistryAccess var0, JigsawConfiguration var1, JigsawPlacement.PieceFactory var2, ChunkGenerator var3, StructureManager var4, BlockPos var5, List<? super PoolElementStructurePiece> var6, Random var7, boolean var8, boolean var9, LevelHeightAccessor var10) {
      StructureFeature.bootstrap();
      Registry var11 = var0.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
      Rotation var12 = Rotation.getRandom(var7);
      StructureTemplatePool var13 = (StructureTemplatePool)var1.startPool().get();
      StructurePoolElement var14 = var13.getRandomTemplate(var7);
      PoolElementStructurePiece var15 = var2.create(var4, var14, var5, var14.getGroundLevelDelta(), var12, var14.getBoundingBox(var4, var5, var12));
      BoundingBox var16 = var15.getBoundingBox();
      int var17 = (var16.x1 + var16.x0) / 2;
      int var18 = (var16.z1 + var16.z0) / 2;
      int var19;
      if (var9) {
         var19 = var5.getY() + var3.getFirstFreeHeight(var17, var18, Heightmap.Types.WORLD_SURFACE_WG, var10);
      } else {
         var19 = var5.getY();
      }

      int var20 = var16.y0 + var15.getGroundLevelDelta();
      var15.move(0, var19 - var20, 0);
      var6.add(var15);
      if (var1.maxDepth() > 0) {
         boolean var21 = true;
         AABB var22 = new AABB((double)(var17 - 80), (double)(var19 - 80), (double)(var18 - 80), (double)(var17 + 80 + 1), (double)(var19 + 80 + 1), (double)(var18 + 80 + 1));
         JigsawPlacement.Placer var23 = new JigsawPlacement.Placer(var11, var1.maxDepth(), var2, var3, var4, var6, var7);
         var23.placing.addLast(new JigsawPlacement.PieceState(var15, new MutableObject(Shapes.join(Shapes.create(var22), Shapes.create(AABB.of(var16)), BooleanOp.ONLY_FIRST)), var19 + 80, 0));

         while(!var23.placing.isEmpty()) {
            JigsawPlacement.PieceState var24 = (JigsawPlacement.PieceState)var23.placing.removeFirst();
            var23.tryPlacingChildren(var24.piece, var24.free, var24.boundsTop, var24.depth, var8, var10);
         }

      }
   }

   public static void addPieces(RegistryAccess var0, PoolElementStructurePiece var1, int var2, JigsawPlacement.PieceFactory var3, ChunkGenerator var4, StructureManager var5, List<? super PoolElementStructurePiece> var6, Random var7, LevelHeightAccessor var8) {
      Registry var9 = var0.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
      JigsawPlacement.Placer var10 = new JigsawPlacement.Placer(var9, var2, var3, var4, var5, var6, var7);
      var10.placing.addLast(new JigsawPlacement.PieceState(var1, new MutableObject(Shapes.INFINITY), 0, 0));

      while(!var10.placing.isEmpty()) {
         JigsawPlacement.PieceState var11 = (JigsawPlacement.PieceState)var10.placing.removeFirst();
         var10.tryPlacingChildren(var11.piece, var11.free, var11.boundsTop, var11.depth, false, var8);
      }

   }

   public interface PieceFactory {
      PoolElementStructurePiece create(StructureManager var1, StructurePoolElement var2, BlockPos var3, int var4, Rotation var5, BoundingBox var6);
   }

   static final class Placer {
      private final Registry<StructureTemplatePool> pools;
      private final int maxDepth;
      private final JigsawPlacement.PieceFactory factory;
      private final ChunkGenerator chunkGenerator;
      private final StructureManager structureManager;
      private final List<? super PoolElementStructurePiece> pieces;
      private final Random random;
      private final Deque<JigsawPlacement.PieceState> placing;

      private Placer(Registry<StructureTemplatePool> var1, int var2, JigsawPlacement.PieceFactory var3, ChunkGenerator var4, StructureManager var5, List<? super PoolElementStructurePiece> var6, Random var7) {
         super();
         this.placing = Queues.newArrayDeque();
         this.pools = var1;
         this.maxDepth = var2;
         this.factory = var3;
         this.chunkGenerator = var4;
         this.structureManager = var5;
         this.pieces = var6;
         this.random = var7;
      }

      private void tryPlacingChildren(PoolElementStructurePiece var1, MutableObject<VoxelShape> var2, int var3, int var4, boolean var5, LevelHeightAccessor var6) {
         StructurePoolElement var7 = var1.getElement();
         BlockPos var8 = var1.getPosition();
         Rotation var9 = var1.getRotation();
         StructureTemplatePool.Projection var10 = var7.getProjection();
         boolean var11 = var10 == StructureTemplatePool.Projection.RIGID;
         MutableObject var12 = new MutableObject();
         BoundingBox var13 = var1.getBoundingBox();
         int var14 = var13.y0;
         Iterator var15 = var7.getShuffledJigsawBlocks(this.structureManager, var8, var9, this.random).iterator();

         while(true) {
            while(true) {
               while(true) {
                  label93:
                  while(var15.hasNext()) {
                     StructureTemplate.StructureBlockInfo var16 = (StructureTemplate.StructureBlockInfo)var15.next();
                     Direction var17 = JigsawBlock.getFrontFacing(var16.state);
                     BlockPos var18 = var16.pos;
                     BlockPos var19 = var18.relative(var17);
                     int var20 = var18.getY() - var14;
                     int var21 = -1;
                     ResourceLocation var22 = new ResourceLocation(var16.nbt.getString("pool"));
                     Optional var23 = this.pools.getOptional(var22);
                     if (var23.isPresent() && (((StructureTemplatePool)var23.get()).size() != 0 || Objects.equals(var22, Pools.EMPTY.location()))) {
                        ResourceLocation var24 = ((StructureTemplatePool)var23.get()).getFallback();
                        Optional var25 = this.pools.getOptional(var24);
                        if (var25.isPresent() && (((StructureTemplatePool)var25.get()).size() != 0 || Objects.equals(var24, Pools.EMPTY.location()))) {
                           boolean var28 = var13.isInside(var19);
                           MutableObject var26;
                           int var27;
                           if (var28) {
                              var26 = var12;
                              var27 = var14;
                              if (var12.getValue() == null) {
                                 var12.setValue(Shapes.create(AABB.of(var13)));
                              }
                           } else {
                              var26 = var2;
                              var27 = var3;
                           }

                           ArrayList var29 = Lists.newArrayList();
                           if (var4 != this.maxDepth) {
                              var29.addAll(((StructureTemplatePool)var23.get()).getShuffledTemplates(this.random));
                           }

                           var29.addAll(((StructureTemplatePool)var25.get()).getShuffledTemplates(this.random));
                           Iterator var30 = var29.iterator();

                           while(var30.hasNext()) {
                              StructurePoolElement var31 = (StructurePoolElement)var30.next();
                              if (var31 == EmptyPoolElement.INSTANCE) {
                                 break;
                              }

                              Iterator var32 = Rotation.getShuffled(this.random).iterator();

                              label133:
                              while(var32.hasNext()) {
                                 Rotation var33 = (Rotation)var32.next();
                                 List var34 = var31.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, var33, this.random);
                                 BoundingBox var35 = var31.getBoundingBox(this.structureManager, BlockPos.ZERO, var33);
                                 int var36;
                                 if (var5 && var35.getYSpan() <= 16) {
                                    var36 = var34.stream().mapToInt((var2x) -> {
                                       if (!var35.isInside(var2x.pos.relative(JigsawBlock.getFrontFacing(var2x.state)))) {
                                          return 0;
                                       } else {
                                          ResourceLocation var3 = new ResourceLocation(var2x.nbt.getString("pool"));
                                          Optional var4 = this.pools.getOptional(var3);
                                          Optional var5 = var4.flatMap((var1) -> {
                                             return this.pools.getOptional(var1.getFallback());
                                          });
                                          int var6 = (Integer)var4.map((var1) -> {
                                             return var1.getMaxSize(this.structureManager);
                                          }).orElse(0);
                                          int var7 = (Integer)var5.map((var1) -> {
                                             return var1.getMaxSize(this.structureManager);
                                          }).orElse(0);
                                          return Math.max(var6, var7);
                                       }
                                    }).max().orElse(0);
                                 } else {
                                    var36 = 0;
                                 }

                                 Iterator var37 = var34.iterator();

                                 StructureTemplatePool.Projection var43;
                                 boolean var44;
                                 int var45;
                                 int var46;
                                 int var47;
                                 BoundingBox var49;
                                 BlockPos var50;
                                 int var51;
                                 do {
                                    StructureTemplate.StructureBlockInfo var38;
                                    do {
                                       if (!var37.hasNext()) {
                                          continue label133;
                                       }

                                       var38 = (StructureTemplate.StructureBlockInfo)var37.next();
                                    } while(!JigsawBlock.canAttach(var16, var38));

                                    BlockPos var39 = var38.pos;
                                    BlockPos var40 = new BlockPos(var19.getX() - var39.getX(), var19.getY() - var39.getY(), var19.getZ() - var39.getZ());
                                    BoundingBox var41 = var31.getBoundingBox(this.structureManager, var40, var33);
                                    int var42 = var41.y0;
                                    var43 = var31.getProjection();
                                    var44 = var43 == StructureTemplatePool.Projection.RIGID;
                                    var45 = var39.getY();
                                    var46 = var20 - var45 + JigsawBlock.getFrontFacing(var16.state).getStepY();
                                    if (var11 && var44) {
                                       var47 = var14 + var46;
                                    } else {
                                       if (var21 == -1) {
                                          var21 = this.chunkGenerator.getFirstFreeHeight(var18.getX(), var18.getZ(), Heightmap.Types.WORLD_SURFACE_WG, var6);
                                       }

                                       var47 = var21 - var45;
                                    }

                                    int var48 = var47 - var42;
                                    var49 = var41.moved(0, var48, 0);
                                    var50 = var40.offset(0, var48, 0);
                                    if (var36 > 0) {
                                       var51 = Math.max(var36 + 1, var49.y1 - var49.y0);
                                       var49.y1 = var49.y0 + var51;
                                    }
                                 } while(Shapes.joinIsNotEmpty((VoxelShape)var26.getValue(), Shapes.create(AABB.of(var49).deflate(0.25D)), BooleanOp.ONLY_SECOND));

                                 var26.setValue(Shapes.joinUnoptimized((VoxelShape)var26.getValue(), Shapes.create(AABB.of(var49)), BooleanOp.ONLY_FIRST));
                                 var51 = var1.getGroundLevelDelta();
                                 int var52;
                                 if (var44) {
                                    var52 = var51 - var46;
                                 } else {
                                    var52 = var31.getGroundLevelDelta();
                                 }

                                 PoolElementStructurePiece var53 = this.factory.create(this.structureManager, var31, var50, var52, var33, var49);
                                 int var54;
                                 if (var11) {
                                    var54 = var14 + var20;
                                 } else if (var44) {
                                    var54 = var47 + var45;
                                 } else {
                                    if (var21 == -1) {
                                       var21 = this.chunkGenerator.getFirstFreeHeight(var18.getX(), var18.getZ(), Heightmap.Types.WORLD_SURFACE_WG, var6);
                                    }

                                    var54 = var21 + var46 / 2;
                                 }

                                 var1.addJunction(new JigsawJunction(var19.getX(), var54 - var20 + var51, var19.getZ(), var46, var43));
                                 var53.addJunction(new JigsawJunction(var18.getX(), var54 - var45 + var52, var18.getZ(), -var46, var10));
                                 this.pieces.add(var53);
                                 if (var4 + 1 <= this.maxDepth) {
                                    this.placing.addLast(new JigsawPlacement.PieceState(var53, var26, var27, var4 + 1));
                                 }
                                 continue label93;
                              }
                           }
                        } else {
                           JigsawPlacement.LOGGER.warn("Empty or none existent fallback pool: {}", var24);
                        }
                     } else {
                        JigsawPlacement.LOGGER.warn("Empty or none existent pool: {}", var22);
                     }
                  }

                  return;
               }
            }
         }
      }

      // $FF: synthetic method
      Placer(Registry var1, int var2, JigsawPlacement.PieceFactory var3, ChunkGenerator var4, StructureManager var5, List var6, Random var7, Object var8) {
         this(var1, var2, var3, var4, var5, var6, var7);
      }
   }

   static final class PieceState {
      private final PoolElementStructurePiece piece;
      private final MutableObject<VoxelShape> free;
      private final int boundsTop;
      private final int depth;

      private PieceState(PoolElementStructurePiece var1, MutableObject<VoxelShape> var2, int var3, int var4) {
         super();
         this.piece = var1;
         this.free = var2;
         this.boundsTop = var3;
         this.depth = var4;
      }

      // $FF: synthetic method
      PieceState(PoolElementStructurePiece var1, MutableObject var2, int var3, int var4, Object var5) {
         this(var1, var2, var3, var4);
      }
   }
}
