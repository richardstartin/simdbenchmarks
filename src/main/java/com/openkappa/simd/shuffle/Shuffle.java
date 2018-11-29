package com.openkappa.simd.shuffle;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

import static com.openkappa.simd.DataUtil.createIntArray;

@State(Scope.Thread)
public class Shuffle {

  public enum Mode {
    RANDOM {
      Random random = new Random();
      @Override
      public IntUnaryOperator scramble() {
        return i -> random.nextInt(i);
      }
    },
    THREAD_LOCAL_RANDOM {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      @Override
      public IntUnaryOperator scramble() {
        return i -> random.nextInt(i);
      }
    },
    SPLITTABLE_RANDOM {
      SplittableRandom random = new SplittableRandom(0);
      @Override
      public IntUnaryOperator scramble() {
        return i -> random.nextInt(i);
      }
    },
    MOD_65536 {
      @Override
      public IntUnaryOperator scramble() {
        return i -> (i - 1) & 65535;
      }
    },
    PRECOMPUTED {
      @Override
      public IntUnaryOperator scramble() {
        return i -> shuffle[i - 1];
      }

      private int[] shuffle = createShuffle(100_000_000);

    };

    public abstract IntUnaryOperator scramble();
  }


  @Param({
          //"65536",
          //"131072",
          "100000000"
  })
  int size;

  @Param({
          //"RANDOM",
          "THREAD_LOCAL_RANDOM",
          //"SPLITTABLE_RANDOM",
          //"MOD_65536",
          "PRECOMPUTED"
  })
  Mode mode;
  @Param({"8", "16", "32", "64"})
  int unroll;


  private int[] data;
  private IntUnaryOperator op;

  @Setup(Level.Trial)
  public void init() {
    data = createIntArray(size);
    op = mode.scramble();
  }

  @Benchmark
  public void shuffle(Blackhole bh) {
    for (int i = data.length; i > 1; i--)
      swap(data, i - 1, op.applyAsInt(i));
    bh.consume(data);
  }

  @Benchmark
  public void shuffleBuffered(Blackhole bh) {
    int[] buffer = new int[unroll];
    for (int i = data.length; i - unroll > 1; i -= unroll) {
      for (int j = 0; j < buffer.length; ++j) {
        buffer[j] = op.applyAsInt(i - j);
      }
      for (int j = 0; j < buffer.length; ++j) {
        swap(data, i - j - 1, buffer[j]);
      }
    }
    bh.consume(data);
  }

  private static void swap(int[] arr, int i, int j) {
    arr[i] ^= arr[j];
    arr[j] ^= arr[i];
    arr[i] ^= arr[j];
  }

  private static int[] createShuffle(int size) {
    int[] shuffle = new int[size];
    for (int i = shuffle.length; i > 1; i--)
      shuffle[i - 1] = ThreadLocalRandom.current().nextInt(i);
    return shuffle;
  }

}
