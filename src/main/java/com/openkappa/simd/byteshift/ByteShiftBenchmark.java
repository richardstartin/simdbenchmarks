package com.openkappa.simd.byteshift;

import com.openkappa.simd.DataUtil;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.openkappa.simd.byteshift.ByteShiftState.MASKS;
import static com.openkappa.simd.byteshift.ByteShiftState.SIGN_BITS;

@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ByteShiftBenchmark {


  private static final Unsafe UNSAFE;
  private static final long BYTE_ARRAY_OFFSET;

  public static void main(String[] args) {
    byte[] data = DataUtil.createByteArray(1000);
    System.out.println(Arrays.toString(data));
    new ByteShiftBenchmark().shiftLogical(data, data, 4);
    System.out.println(Arrays.toString(data));
  }

  static {
    try {
      Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      UNSAFE = (Unsafe) f.get(null);
      BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Benchmark
  public void shiftLogical(ByteShiftState state, Blackhole bh) {
    byte[] data = state.data;
    byte[] target = state.target;
    int shift = state.shift;
    shiftLogical(data, target, shift);
    bh.consume(target);
  }

  void shiftLogical(byte[] data, byte[] target, int shift) {
    for (int i = 0; i < data.length; ++i) {
      target[i] = (byte)((data[i] & 0xFF) >>> shift);
    }
  }

  @Benchmark
  public void shiftArithmetic(ByteShiftState state, Blackhole bh) {
    byte[] data = state.data;
    byte[] target = state.target;
    int shift = state.shift;
    shiftArithmetic(data, target, shift);
    bh.consume(target);
  }

  void shiftArithmetic(byte[] data, byte[] target, int shift) {
    for (int i = 0; i < data.length; ++i) {
      target[i] = (byte)(data[i] >> shift);
    }
  }

  @Benchmark
  public void shiftLogicalUnsafe(ByteShiftState state, Blackhole bh) {
    byte[] data = state.data;
    byte[] target = state.target;
    int shift = state.shift;
    shiftLogicalUnsafe(data, target, shift);
    bh.consume(target);
  }

  void shiftLogicalUnsafe(byte[] data, byte[] target, int shift) {
    long mask = MASKS[shift];
    int i = 0;
    for (; i + 7 < data.length; i += 8) {
      long word = UNSAFE.getLong(data, BYTE_ARRAY_OFFSET + i);
      word >>>= shift;
      word &= mask;
      UNSAFE.putLong(target, BYTE_ARRAY_OFFSET + i, word);
    }
    for (; i < data.length; ++i) {
      target[i] = (byte)((data[i] & 0xFF) >>> shift);
    }
  }

  @Benchmark
  public void shiftArithmeticUnsafe(ByteShiftState state, Blackhole bh) {
    byte[] data = state.data;
    byte[] target = state.target;
    int shift = state.shift;
    shiftArithmeticUnsafe(data, target, shift);
    bh.consume(target);
  }

  void shiftArithmeticUnsafe(byte[] data, byte[] target, int shift) {
    long mask = MASKS[shift];
    int i = 0;
    for (; i  + 7 < data.length; i += 8) {
      long word = UNSAFE.getLong(data, BYTE_ARRAY_OFFSET + i);
      long signs = word & SIGN_BITS;
      word &= ~SIGN_BITS;
      word >>>= shift;
      word &= mask;
      word |= signs;
      UNSAFE.putLong(target, BYTE_ARRAY_OFFSET + i, word);
    }
    for (; i < data.length; ++i) {
      target[i] = (byte)(data[i] >> shift);
    }
  }
}
