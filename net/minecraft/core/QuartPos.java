package net.minecraft.core;

public final class QuartPos {
   public static int fromBlock(int var0) {
      return var0 >> 2;
   }

   public static int toBlock(int var0) {
      return var0 << 2;
   }

   public static int fromSection(int var0) {
      return var0 << 2;
   }

   public static int toSection(int var0) {
      return var0 >> 2;
   }
}
