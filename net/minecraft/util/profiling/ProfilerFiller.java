package net.minecraft.util.profiling;

import java.util.function.Supplier;

public interface ProfilerFiller {
   void startTick();

   void endTick();

   void push(String var1);

   void push(Supplier<String> var1);

   void pop();

   void popPush(String var1);

   void popPush(Supplier<String> var1);

   void incrementCounter(String var1);

   void incrementCounter(Supplier<String> var1);

   static ProfilerFiller tee(final ProfilerFiller var0, final ProfilerFiller var1) {
      if (var0 == InactiveProfiler.INSTANCE) {
         return var1;
      } else {
         return var1 == InactiveProfiler.INSTANCE ? var0 : new ProfilerFiller() {
            public void startTick() {
               var0.startTick();
               var1.startTick();
            }

            public void endTick() {
               var0.endTick();
               var1.endTick();
            }

            public void push(String var1x) {
               var0.push(var1x);
               var1.push(var1x);
            }

            public void push(Supplier<String> var1x) {
               var0.push(var1x);
               var1.push(var1x);
            }

            public void pop() {
               var0.pop();
               var1.pop();
            }

            public void popPush(String var1x) {
               var0.popPush(var1x);
               var1.popPush(var1x);
            }

            public void popPush(Supplier<String> var1x) {
               var0.popPush(var1x);
               var1.popPush(var1x);
            }

            public void incrementCounter(String var1x) {
               var0.incrementCounter(var1x);
               var1.incrementCounter(var1x);
            }

            public void incrementCounter(Supplier<String> var1x) {
               var0.incrementCounter(var1x);
               var1.incrementCounter(var1x);
            }
         };
      }
   }
}
