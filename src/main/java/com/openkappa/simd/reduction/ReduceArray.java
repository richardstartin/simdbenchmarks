package com.openkappa.simd.reduction;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.openkappa.simd.DataUtil.createDoubleArray;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ReduceArray {

  @Param({"1024",
          "65536",
          "131072"
  })
  int size;
  private double[] data;

  @Setup(Level.Trial)
  public void setup() {
    this.data = createDoubleArray(size);
  }

  @Benchmark
  public double reduceSimple() {
    return reduce(data);
  }

  @Benchmark
  public double reduceBuffered() {
    double[] buffer = new double[1024];
    for (int i = 0; i < data.length; ++i) {
      buffer[i & 1023] += data[i];
    }
    return reduce(buffer);
  }


  @Benchmark
  public double reduceVectorised() {
    double[] buffer = new double[1024];
    double[] temp = new double[1024];
    for (int i = 0; i < data.length >>> 10; ++i) {
      System.arraycopy(data, i * 1024, temp, 0,  temp.length);
      for (int j = 0; j < 1024; ++j) {
        buffer[j] += temp[j];
      }
    }
    return reduce(buffer);
  }


  private double reduce(double[] data) {
    double reduced = 0D;
    for (int i = 0; i < data.length; ++i) {
      reduced += data[i];
    }
    return reduced;
  }
}
