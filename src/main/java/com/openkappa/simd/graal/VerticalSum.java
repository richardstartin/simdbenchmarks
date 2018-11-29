package com.openkappa.simd.graal;

import org.openjdk.jmh.annotations.*;

import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsPrepend = {
        "-XX:-TieredCompilation",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+EnableJVMCI" ,
        "-XX:+UseJVMCICompiler"
})
public class VerticalSum {

  @State(Scope.Benchmark)
  public static class VerticalSumState {
    @Param({"1024"})
    int size;

    @Param({"0", "0.001", "0.01"})
    double probabilityOfNaN;
  }

  public static class VeriticalSumFloatArrayState extends VerticalSumState {
    public float[] left;
    public float[] right;
    public float[] result;

    @Setup(Level.Trial)
    public void init() {
      this.left = newFloatVector(size, probabilityOfNaN);
      this.right = newFloatVector(size, probabilityOfNaN);
      this.result = newFloatVector(size, 0);
    }
  }

  public static float[] newFloatVector(int size) {
    float[] vector = new float[size];
    for (int i = 0; i < vector.length; ++i) {
      vector[i] = ThreadLocalRandom.current().nextFloat();
    }
    return vector;
  }


  public static float[] newFloatVector(int size, double probabilityOfNaN) {
    SplittableRandom random = new SplittableRandom(0);
    float[] vector = newFloatVector(size);
    for (int i = 0; i < vector.length; ++i) {
      if (random.nextDouble() < probabilityOfNaN) {
        vector[i] = Float.NaN;
      }
    }
    return vector;
  }

  @Benchmark
  public float[] verticalSum(VeriticalSumFloatArrayState state) {
    for (int i = 0; i < state.size; ++i) {
      state.result[i] = state.left[i] + state.right[i];
    }
    return state.result;
  }

  @Benchmark
  public float[] verticalSumNaNCheckPessimistic(VeriticalSumFloatArrayState state) {
    for (int i = 0; i < state.size; ++i) {
      if (!Float.isNaN(state.left[i]) && !Float.isNaN(state.right[i])) {
        state.result[i] = state.left[i] + state.right[i];
      }
    }
    return state.result;
  }

  @Benchmark
  public float[] verticalSumNaNCheckOptimistic(VeriticalSumFloatArrayState state) {
    for (int i = 0; i < state.size; ++i) {
      boolean leftIsNaN = Float.isNaN(state.left[i]);
      boolean rightIsNaN = Float.isNaN(state.left[i]);
      if (leftIsNaN && !rightIsNaN) {
        state.result[i] = state.right[i];
      } else if (rightIsNaN && !leftIsNaN) {
        state.result[i] = state.left[i];
      } else if (!leftIsNaN) {
        state.result[i] = state.left[i] + state.right[i];
      }
    }
    return state.result;
  }


}
