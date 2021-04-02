package net.minecraft.tags;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagLoader<T> {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = new Gson();
   private static final int PATH_SUFFIX_LENGTH = ".json".length();
   private final Function<ResourceLocation, Optional<T>> idToValue;
   private final String directory;

   public TagLoader(Function<ResourceLocation, Optional<T>> var1, String var2) {
      super();
      this.idToValue = var1;
      this.directory = var2;
   }

   public Map<ResourceLocation, Tag.Builder> load(ResourceManager var1) {
      HashMap var2 = Maps.newHashMap();
      Iterator var3 = var1.listResources(this.directory, (var0) -> {
         return var0.endsWith(".json");
      }).iterator();

      while(var3.hasNext()) {
         ResourceLocation var4 = (ResourceLocation)var3.next();
         String var5 = var4.getPath();
         ResourceLocation var6 = new ResourceLocation(var4.getNamespace(), var5.substring(this.directory.length() + 1, var5.length() - PATH_SUFFIX_LENGTH));

         try {
            Iterator var7 = var1.getResources(var4).iterator();

            while(var7.hasNext()) {
               Resource var8 = (Resource)var7.next();

               try {
                  InputStream var9 = var8.getInputStream();
                  Throwable var10 = null;

                  try {
                     BufferedReader var11 = new BufferedReader(new InputStreamReader(var9, StandardCharsets.UTF_8));
                     Throwable var12 = null;

                     try {
                        JsonObject var13 = (JsonObject)GsonHelper.fromJson(GSON, (Reader)var11, (Class)JsonObject.class);
                        if (var13 == null) {
                           LOGGER.error("Couldn't load tag list {} from {} in data pack {} as it is empty or null", var6, var4, var8.getSourceName());
                        } else {
                           ((Tag.Builder)var2.computeIfAbsent(var6, (var0) -> {
                              return Tag.Builder.tag();
                           })).addFromJson(var13, var8.getSourceName());
                        }
                     } catch (Throwable var53) {
                        var12 = var53;
                        throw var53;
                     } finally {
                        if (var11 != null) {
                           if (var12 != null) {
                              try {
                                 var11.close();
                              } catch (Throwable var52) {
                                 var12.addSuppressed(var52);
                              }
                           } else {
                              var11.close();
                           }
                        }

                     }
                  } catch (Throwable var55) {
                     var10 = var55;
                     throw var55;
                  } finally {
                     if (var9 != null) {
                        if (var10 != null) {
                           try {
                              var9.close();
                           } catch (Throwable var51) {
                              var10.addSuppressed(var51);
                           }
                        } else {
                           var9.close();
                        }
                     }

                  }
               } catch (RuntimeException | IOException var57) {
                  LOGGER.error("Couldn't read tag list {} from {} in data pack {}", var6, var4, var8.getSourceName(), var57);
               } finally {
                  IOUtils.closeQuietly(var8);
               }
            }
         } catch (IOException var59) {
            LOGGER.error("Couldn't read tag list {} from {}", var6, var4, var59);
         }
      }

      return var2;
   }

   private static void visitDependenciesAndElement(Map<ResourceLocation, Tag.Builder> var0, Multimap<ResourceLocation, ResourceLocation> var1, Set<ResourceLocation> var2, ResourceLocation var3, BiConsumer<ResourceLocation, Tag.Builder> var4) {
      if (var2.add(var3)) {
         var1.get(var3).forEach((var4x) -> {
            visitDependenciesAndElement(var0, var1, var2, var4x, var4);
         });
         Tag.Builder var5 = (Tag.Builder)var0.get(var3);
         if (var5 != null) {
            var4.accept(var3, var5);
         }

      }
   }

   private static boolean isCyclic(Multimap<ResourceLocation, ResourceLocation> var0, ResourceLocation var1, ResourceLocation var2) {
      Collection var3 = var0.get(var2);
      return var3.contains(var1) ? true : var3.stream().anyMatch((var2x) -> {
         return isCyclic(var0, var1, var2x);
      });
   }

   private static void addDependencyIfNotCyclic(Multimap<ResourceLocation, ResourceLocation> var0, ResourceLocation var1, ResourceLocation var2) {
      if (!isCyclic(var0, var1, var2)) {
         var0.put(var1, var2);
      }

   }

   public TagCollection<T> build(Map<ResourceLocation, Tag.Builder> var1) {
      HashMap var2 = Maps.newHashMap();
      Function var3 = var2::get;
      Function var4 = (var1x) -> {
         return ((Optional)this.idToValue.apply(var1x)).orElse((Object)null);
      };
      HashMultimap var5 = HashMultimap.create();
      var1.forEach((var1x, var2x) -> {
         var2x.visitRequiredDependencies((var2) -> {
            addDependencyIfNotCyclic(var5, var1x, var2);
         });
      });
      var1.forEach((var1x, var2x) -> {
         var2x.visitOptionalDependencies((var2) -> {
            addDependencyIfNotCyclic(var5, var1x, var2);
         });
      });
      HashSet var6 = Sets.newHashSet();
      var1.keySet().forEach((var6x) -> {
         visitDependenciesAndElement(var1, var5, var6, var6x, (var3x, var4x) -> {
            var4x.build(var3, var4).ifLeft((var1) -> {
               LOGGER.error("Couldn't load tag {} as it is missing following references: {}", var3x, var1.stream().map(Objects::toString).collect(Collectors.joining(",")));
            }).ifRight((var2x) -> {
               Tag var10000 = (Tag)var2.put(var3x, var2x);
            });
         });
      });
      return TagCollection.of(var2);
   }

   public TagCollection<T> loadAndBuild(ResourceManager var1) {
      return this.build(this.load(var1));
   }
}
