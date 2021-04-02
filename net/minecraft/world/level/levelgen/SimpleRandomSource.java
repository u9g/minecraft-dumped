package net.minecraft.world.level.levelgen;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.ThreadingDetector;

public class SimpleRandomSource implements RandomSource {
   private final AtomicLong seed = new AtomicLong();
   private boolean haveNextNextGaussian = false;

   public SimpleRandomSource(long var1) {
      super();
      this.setSeed(var1);
   }

   public void setSeed(long var1) {
      if (!this.seed.compareAndSet(this.seed.get(), (var1 ^ 25214903917L) & 281474976710655L)) {
         throw ThreadingDetector.makeThreadingException("SimpleRandomSource", (DebugBuffer)null);
      }
   }

   private int next(int var1) {
      long var2 = this.seed.get();
      long var4 = var2 * 25214903917L + 11L & 281474976710655L;
      if (!this.seed.compareAndSet(var2, var4)) {
         throw ThreadingDetector.makeThreadingException("SimpleRandomSource", (DebugBuffer)null);
      } else {
         return (int)(var4 >> 48 - var1);
      }
   }

   public int nextInt() {
      return this.next(32);
   }

   public int nextInt(int var1) {
      if (var1 <= 0) {
         throw new IllegalArgumentException("Bound must be positive");
      } else if ((var1 & var1 - 1) == 0) {
         return (int)((long)var1 * (long)this.next(31) >> 31);
      } else {
         int var2;
         int var3;
         do {
            var2 = this.next(31);
            var3 = var2 % var1;
         } while(var2 - var3 + (var1 - 1) < 0);

         return var3;
      }
   }

   public long nextLong() {
      return ((long)this.nextInt() << 32) + (long)this.nextInt();
   }

   public double nextDouble() {
      return (double)(((long)this.next(26) << 27) + (long)this.next(27)) * 1.1102230246251565E-16D;
   }
}
