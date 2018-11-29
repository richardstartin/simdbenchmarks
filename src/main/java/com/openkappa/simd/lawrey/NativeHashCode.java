package com.openkappa.simd.lawrey;

import com.openkappa.simd.DataUtil;
import org.openjdk.jmh.annotations.*;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class NativeHashCode {

  private static final Unsafe UNSAFE;
  private static final long BYTE_ARRAY_OFFSET;

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

  @Param({"128", "256", "512", "1024"})
  int size;

  byte[] data;

  @Setup(Level.Trial)
  public void setup() {
    data = DataUtil.createByteArray(size);
  }

  @Benchmark
  public int nativeHashCode() {
    return nativeHashCode(data);
  }

  @Benchmark
  public int unrolledHashCode() {
    int result = 1;
    int i = 0;
    for (; i + 3 < data.length; i += 4) {
      result =  31 * 31 * 31 * 31 * result
              + 31 * 31 * 31 * data[i]
              + 31 * 31 * data[i + 1]
              + 31 * data[i + 2]
              + data[i + 3]
      ;
    }
    return result;
  }

  public static void main(String[] args) {
    Set<Integer> set = new HashSet<>();
    int multiplier = 1;
    while (!set.contains(multiplier)) {
      set.add(multiplier);
      multiplier *= 31;
    }
    System.out.println(set.size());
  }


  public static int nativeHashCode(byte[] value) {
    long h = getIntFromArray(value, 0);
    for (int i = 4; i < value.length; i += 4)
      h = h * M2 + getIntFromArray(value, i);
    h *= M2;
    return (int) h ^ (int) (h >>> 25);
  }


  private static final int M2 = 0x7A646E4D;

  // read 4 bytes at a time from a byte[] assuming Java 9+ Compact Strings
  private static int getIntFromArray(byte[] value, int i) {
    return UNSAFE.getInt(value, BYTE_ARRAY_OFFSET + i);
  }
}
