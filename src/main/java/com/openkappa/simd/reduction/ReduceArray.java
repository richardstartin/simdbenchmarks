package com.openkappa.simd.reduction;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.openkappa.simd.DataUtil.createDoubleArray;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class ReduceArray {

  public static void main(String[] args) {
    ReduceArray benchmark = new ReduceArray();
    benchmark.size = 65536;
    benchmark.setup();
    System.out.println(benchmark.reducePaginated());
    System.out.println(benchmark.reduceUnrolledPaginated());
  }


  @Param({"1024",
          "65536",
          "1048576"
  })
  int size;
  private double[] data;

  private double[][] paginated;

  @Setup(Level.Trial)
  public void setup() {
    this.data = createDoubleArray(size);
    this.paginated = new double[size/1024][];
    for (int i = 0; i < size / 1024; ++i) {
      paginated[i] = createDoubleArray(1024);
    }
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
  public double reduceUnrolled() {
    double a0 = 0.0;
    double a1 = 0.0;
    double a2 = 0.0;
    double a3 = 0.0;
    for (int i = 0; i < data.length >>> 2; i++) {
      a0 += data[i * 8 + 0];
      a1 += data[i * 8 + 1];
      a2 += data[i * 8 + 2];
      a3 += data[i * 8 + 3];
    }
    return a0 + a1 + a2 + a3;
  }


  @Benchmark
  public double reducePaginated() {
    double[] buffer = Arrays.copyOf(paginated[0], paginated[0].length);
    for (int i = 1; i < paginated.length; ++i) {
      double[] page = paginated[i];
      for (int j = 0; j < page.length && j < buffer.length; ++j) {
        buffer[j] += page[j];
      }
    }
    return reduceUnrolled(buffer);
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

  @Benchmark
  public double reduceUnrolledPaginated() {
    double a0 = 0.0;
    double a1 = 0.0;
    double a2 = 0.0;
    double a3 = 0.0;
    for (int i = 0; i < paginated.length; ++i) {
      double[] page = paginated[i];
      for (int j = 0; j < paginated[0].length; j += 4) {
        a0 += page[j + 0];
        a1 += page[j + 1];
        a2 += page[j + 2];
        a3 += page[j + 3];
      }
    }
    return a0 + a1 + a2 + a3;
  }

  private double reduceUnrolled(double[] data) {
    double a0 = 0.0;
    double a1 = 0.0;
    double a2 = 0.0;
    double a3 = 0.0;
    for (int i = 0; i < data.length >>> 2; i++) {
      a0 += data[i * 4 + 0];
      a1 += data[i * 4 + 1];
      a2 += data[i * 4 + 2];
      a3 += data[i * 4 + 3];
    }
    return a0 + a1 + a2 + a3;
  }


  private double reduce(double[] data) {
    double reduced = 0D;
    for (int i = 0; i < data.length; ++i) {
      reduced += data[i];
    }
    return reduced;
  }
}
