package net.minecraft.world.level.levelgen;

public interface RandomSource {
   int nextInt();

   int nextInt(int var1);

   long nextLong();

   double nextDouble();

   default void consumeCount(int var1) {
      for(int var2 = 0; var2 < var1; ++var2) {
         this.nextInt();
      }

   }
}
