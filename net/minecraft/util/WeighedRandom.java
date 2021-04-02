package net.minecraft.util;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WeighedRandom {
   private static final Logger LOGGER = LogManager.getLogger();

   public static int getTotalWeight(List<? extends WeighedRandom.WeighedRandomItem> var0) {
      long var1 = 0L;
      int var3 = 0;

      for(int var4 = var0.size(); var3 < var4; ++var3) {
         WeighedRandom.WeighedRandomItem var5 = (WeighedRandom.WeighedRandomItem)var0.get(var3);
         var1 += (long)var5.weight;
      }

      if (var1 > 2147483647L) {
         throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
      } else {
         return (int)var1;
      }
   }

   public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getRandomItem(Random var0, List<T> var1, int var2) {
      if (var2 < 0) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
      } else if (var2 == 0) {
         return Optional.empty();
      } else {
         int var3 = var0.nextInt(var2);
         return getWeightedItem(var1, var3);
      }
   }

   public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getWeightedItem(List<T> var0, int var1) {
      int var2 = 0;

      for(int var3 = var0.size(); var2 < var3; ++var2) {
         WeighedRandom.WeighedRandomItem var4 = (WeighedRandom.WeighedRandomItem)var0.get(var2);
         var1 -= var4.weight;
         if (var1 < 0) {
            return Optional.of(var4);
         }
      }

      return Optional.empty();
   }

   public static <T extends WeighedRandom.WeighedRandomItem> Optional<T> getRandomItem(Random var0, List<T> var1) {
      return getRandomItem(var0, var1, getTotalWeight(var1));
   }

   public static class WeighedRandomItem {
      protected final int weight;

      public WeighedRandomItem(int var1) {
         super();
         if (var1 < 0) {
            throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
         } else {
            if (var1 == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
               WeighedRandom.LOGGER.warn("Found 0 weight, make sure this is intentional!");
            }

            this.weight = var1;
         }
      }
   }
}
