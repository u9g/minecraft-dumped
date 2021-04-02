package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class UniformFloat {
   public static final Codec<UniformFloat> CODEC;
   private final float baseValue;
   private final float spread;

   public static Codec<UniformFloat> codec(float var0, float var1, float var2) {
      Function var3 = (var3x) -> {
         if (var3x.baseValue >= var0 && var3x.baseValue <= var1) {
            return var3x.spread <= var2 ? DataResult.success(var3x) : DataResult.error("Spread too big: " + var3x.spread + " > " + var2);
         } else {
            return DataResult.error("Base value out of range: " + var3x.baseValue + " [" + var0 + "-" + var1 + "]");
         }
      };
      return CODEC.flatXmap(var3, var3);
   }

   private UniformFloat(float var1, float var2) {
      super();
      this.baseValue = var1;
      this.spread = var2;
   }

   public static UniformFloat fixed(float var0) {
      return new UniformFloat(var0, 0.0F);
   }

   public static UniformFloat of(float var0, float var1) {
      return new UniformFloat(var0, var1);
   }

   public float sample(Random var1) {
      return this.spread == 0.0F ? this.baseValue : Mth.randomBetween(var1, this.baseValue, this.baseValue + this.spread);
   }

   public float getBaseValue() {
      return this.baseValue;
   }

   public float getMaxValue() {
      return this.baseValue + this.spread;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         UniformFloat var2 = (UniformFloat)var1;
         return this.baseValue == var2.baseValue && this.spread == var2.spread;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.baseValue, this.spread});
   }

   public String toString() {
      return "[" + this.baseValue + '-' + (this.baseValue + this.spread) + ']';
   }

   static {
      CODEC = Codec.either(Codec.FLOAT, RecordCodecBuilder.create((var0) -> {
         return var0.group(Codec.FLOAT.fieldOf("base").forGetter((var0x) -> {
            return var0x.baseValue;
         }), Codec.FLOAT.fieldOf("spread").forGetter((var0x) -> {
            return var0x.spread;
         })).apply(var0, UniformFloat::new);
      }).comapFlatMap((var0) -> {
         return var0.spread < 0.0F ? DataResult.error("Spread must be non-negative, got: " + var0.spread) : DataResult.success(var0);
      }, Function.identity())).xmap((var0) -> {
         return (UniformFloat)var0.map(UniformFloat::fixed, (var0x) -> {
            return var0x;
         });
      }, (var0) -> {
         return var0.spread == 0.0F ? Either.left(var0.baseValue) : Either.right(var0);
      });
   }
}
