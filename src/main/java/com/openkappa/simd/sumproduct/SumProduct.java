package com.openkappa.simd.sumproduct;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import static com.openkappa.simd.DataUtil.createDoubleArray;
import static com.openkappa.simd.DataUtil.createFloatArray;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SumProduct {

  @Param({"1024",
         "65536"
  })
  int size;
  private double[] xd;
  private double[] yd;
  private float[] xs;
  private float[] ys;

  @Setup(Level.Trial)
  public void init() {
    this.xd = createDoubleArray(size);
    this.yd = createDoubleArray(size);
    this.xs = createFloatArray(size);
    this.ys = createFloatArray(size);
  }

  public static void main(String[] args) {
    SumProduct b = new SumProduct();
    b.size = 1024;
    b.init();
    System.out.println(b.unrolledDoubleSumProduct());
    System.out.println(b.vectorisedDoubleSumProduct());
  }


  @Benchmark
  public double vectorisedDoubleSumProduct() {
    double sp = 0D;
    for (int i = 0; i < xd.length && i < yd.length; ++i) {
      sp += xd[i] * yd[i];
    }
    return sp;
  }

  @Benchmark
  public double unrolledDoubleSumProduct() {
    double sp1 = 0D;
    double sp2 = 0D;
    double sp3 = 0D;
    double sp4 = 0D;
    for (int i = 0; i < xd.length && i < yd.length; i += 4) {
      sp1 += xd[i] * yd[i];
      sp2 += xd[i + 1] * yd[i + 1];
      sp3 += xd[i + 2] * yd[i + 2];
      sp4 += xd[i + 3] * yd[i + 3];
    }
    return sp1 + sp2 + sp3 + sp4;
  }

  @Benchmark
  public float vectorisedSingleSumProduct() {
    float sp = 0f;
    for (int i = 0; i < xd.length && i < yd.length; ++i) {
      sp += xs[i] * ys[i];
    }
    return sp;
  }

  @Benchmark
  public float unrolledSingleSumProduct() {
    float sp1 = 0f;
    float sp2 = 0f;
    float sp3 = 0f;
    float sp4 = 0f;
    float sp5 = 0f;
    float sp6 = 0f;
    float sp7 = 0f;
    float sp8 = 0f;
    for (int i = 0; i < xd.length && i < yd.length; i += 8) {
      sp1 += xs[i] * ys[i];
      sp2 += xs[i + 1] * ys[i + 1];
      sp3 += xs[i + 2] * ys[i + 2];
      sp4 += xs[i + 3] * ys[i + 3];
      sp5 += xs[i + 4] * ys[i + 4];
      sp6 += xs[i + 5] * ys[i + 5];
      sp7 += xs[i + 6] * ys[i + 6];
      sp8 += xs[i + 7] * ys[i + 7];
    }
    return sp1 + sp2 + sp3 + sp4 + sp5 + sp6 + sp7 + sp8;
  }

}
