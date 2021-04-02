package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class ThreadingDetector {
   public static void checkAndLock(Semaphore var0, @Nullable DebugBuffer<Pair<Thread, StackTraceElement[]>> var1, String var2) {
      boolean var3 = var0.tryAcquire();
      if (!var3) {
         throw makeThreadingException(var2, var1);
      }
   }

   public static ReportedException makeThreadingException(String var0, @Nullable DebugBuffer<Pair<Thread, StackTraceElement[]>> var1) {
      String var2 = (String)Thread.getAllStackTraces().keySet().stream().filter(Objects::nonNull).map((var0x) -> {
         return var0x.getName() + ": \n\tat " + (String)Arrays.stream(var0x.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "));
      }).collect(Collectors.joining("\n"));
      CrashReport var3 = new CrashReport("Accessing " + var0 + " from multiple threads", new IllegalStateException());
      CrashReportCategory var4 = var3.addCategory("Thread dumps");
      var4.setDetail("Thread dumps", (Object)var2);
      if (var1 != null) {
         StringBuilder var5 = new StringBuilder();
         List var6 = var1.dump();
         Iterator var7 = var6.iterator();

         while(var7.hasNext()) {
            Pair var8 = (Pair)var7.next();
            var5.append("Thread ").append(((Thread)var8.getFirst()).getName()).append(": \n\tat ").append((String)Arrays.stream((Object[])var8.getSecond()).map(Object::toString).collect(Collectors.joining("\n\tat "))).append("\n");
         }

         var4.setDetail("Last threads", (Object)var5.toString());
      }

      return new ReportedException(var3);
   }
}
