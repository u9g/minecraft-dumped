package net.minecraft.gametest.framework;

public class GlobalTestReporter {
   private static TestReporter DELEGATE = new LogTestReporter();

   public static void onTestFailed(GameTestInfo var0) {
      DELEGATE.onTestFailed(var0);
   }

   public static void onTestSuccess(GameTestInfo var0) {
      DELEGATE.onTestSuccess(var0);
   }
}
