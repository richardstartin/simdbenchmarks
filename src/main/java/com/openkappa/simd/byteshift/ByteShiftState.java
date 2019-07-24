package com.openkappa.simd.byteshift;

import com.openkappa.simd.DataUtil;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;

@State(Scope.Benchmark)
public class ByteShiftState {

  public static final long[] MASKS = new long[]{
          0b1111111111111111111111111111111111111111111111111111111111111111L,
          0b0111111101111111011111110111111101111111011111110111111101111111L,
          0b0011111100111111001111110011111100111111001111110011111100111111L,
          0b0001111100011111000111110001111100011111000111110001111100011111L,
          0b0000111100001111000011110000111100001111000011110000111100001111L,
          0b0000011100000111000001110000011100000111000001110000011100000111L,
          0b0000001100000011000000110000001100000011000000110000001100000011L,
          0b0000000100000001000000010000000100000001000000010000000100000001L,
          0b0000000000000000000000000000000000000000000000000000000000000000L
  };

  public static final long SIGN_BITS =
          0b1000000010000000100000001000000010000000100000001000000010000000L;



  @Param({"250", "256", "262", "1018", "1024", "1030"})
  int size;
  @Param({"0", "1", "7", "8"})
  int shift;

  byte[] data;

  byte[] target;


  @Setup(Level.Trial)
  public void init() {
    data = DataUtil.createByteArray(size);
    target = new byte[size];
    var benchmark = new ByteShiftBenchmark();
    benchmark.shiftLogical(data, target, shift);
    byte[] arr1 = Arrays.copyOf(target, target.length);
    benchmark.shiftLogicalUnsafe(data, target, shift);
    byte[] arr2 = Arrays.copyOf(target, target.length);
    if (!Arrays.equals(arr1, arr2)) {
      throw new IllegalStateException("Defective logical shift impl: " + Arrays.toString(arr1) + "/" + Arrays.toString(arr2));
    }
//    benchmark.shiftArithmetic(data, target, shift);
//    arr1 = Arrays.copyOf(target, target.length);
//    benchmark.shiftArithmeticUnsafe(data, target, shift);
//    arr2 = Arrays.copyOf(target, target.length);
//    if (!Arrays.equals(arr1, arr2)) {
//      throw new IllegalStateException("Defective arithmetic shift impl: "  + Arrays.toString(arr1) + "/" + Arrays.toString(arr2));
//    }
  }

}
