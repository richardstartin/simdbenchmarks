package com.openkappa.simd.cmovd;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.openkappa.simd.DataUtil.createDoubleArray;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Max {

  private double[] data1;
  private double[] data2;

  @Param({"1024", "65536"})
  int size;

  private double threshold;

  @Setup(Level.Trial)
  public void setup() {
    data1 = createDoubleArray(size);
    data2 = createDoubleArray(size);
    threshold = ThreadLocalRandom.current().nextDouble();
  }

  @Benchmark
  public double[] maxInPlace() {
    double[] out = Arrays.copyOf(data1, data1.length);
    for (int i = 0; i < out.length && i < data2.length; ++i) {
      out[i] = out[i] > threshold ? out[i] : data2[i];
    }
    return out;
  }

  @Benchmark
  public double[] max() {
    double[] out = new double[size];
    for (int i = 0; i < out.length && i < data2.length; ++i) {
      out[i] = data1[i] > threshold ? data1[i] : data2[i];
    }
    return out;
  }
}
