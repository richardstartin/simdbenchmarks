package com.openkappa.simd.ss;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static com.openkappa.simd.DataUtil.createDoubleArray;
import static com.openkappa.simd.DataUtil.createIntArray;


@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsPrepend = {
        "-XX:-TieredCompilation"
//        ,
//        "-XX:+UnlockExperimentalVMOptions",
//        "-XX:+EnableJVMCI" ,
//        "-XX:+UseJVMCICompiler"
})
public class SumOfSquaresBlogPost {


  @Param({"1024", "8192"})
  int size;

  private double[] data;

  private int[] intData;


  @Setup(Level.Trial)
  public void init() {
    this.data = createDoubleArray(size);
    this.intData = createIntArray(size);
  }

  @Benchmark
  public double SS_SequentialStream() {
    return DoubleStream.of(data)
            .map(x -> x * x)
            .reduce((x, y) -> x + y)
            .orElse(0D);
  }

  @Benchmark
  public double SS_ParallelStream() {
    return DoubleStream.of(data)
            .parallel()
            .map(x -> x * x)
            .reduce((x, y) -> x + y)
            .orElse(0);
  }

  @Benchmark
  public double SS_ForLoop() {
    double result = 0D;
    for (int i = 0; i < data.length; ++i) {
      result += data[i] * data[i];
    }
    return result;
  }

  @Benchmark
  public double SS_GenerativeSequentialStream() {
    return IntStream.iterate(0, i -> i < size, i -> i + 1)
            .mapToDouble(i -> data[i])
            .map(x -> x * x)
            .reduce((x, y) -> x + y)
            .orElse(0);
  }

  @Benchmark
  public int SS_SequentialStream_Int() {
    return IntStream.of(intData)
            .map(x -> x * x)
            .reduce((x, y) -> x + y)
            .orElse(0);
  }

  @Benchmark
  public int SS_ParallelStream_Int() {
    return IntStream.of(intData)
            .parallel()
            .map(x -> x * x)
            .reduce((x, y) -> x + y)
            .orElse(0);
  }

  @Benchmark
  public int SS_ForLoop_Int() {
    int result = 0;
    for (int i = 0; i < intData.length; ++i) {
      result += intData[i] * intData[i];
    }
    return result;
  }

  @Benchmark
  public int SS_GenerativeSequentialStream_Int() {
    return IntStream.iterate(0, i -> i < size, i -> i + 1)
            .map(i -> intData[i])
            .map(x -> x * x)
            .reduce((x, y) -> x + y)
            .orElse(0);
  }

}
