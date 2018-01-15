package com.openkappa.simd.mmm;


import com.openkappa.simd.DataUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class MMM {

  @Param({"8", "32", "64", "128", "192", "256", "320", "384", "448", "512" , "576", "640", "704", "768", "832", "896", "960", "1024"})
  int size;

  private float[] a;
  private float[] b;
  private float[] c;

  @Setup(Level.Trial)
  public void init() {
    a = DataUtil.createFloatArray(size * size);
    b = DataUtil.createFloatArray(size * size);
    c = new float[size * size];
  }

  @Benchmark
  public void fast(Blackhole bh) {
    fast(a, b, c, size);
    bh.consume(c);
  }

  @Benchmark
  public void fastBuffered(Blackhole bh) {
    fastBuffered(a, b, c, size);
    bh.consume(c);
  }

  @Benchmark
  public void tiled(Blackhole bh) {
    tiled(a, b, c, size);
    bh.consume(c);
  }

  @Benchmark
  public void baseline(Blackhole bh) {
    baseline(a, b, c, size);
    bh.consume(c);
  }


  @Benchmark
  public void blocked(Blackhole bh) {
    blocked(a, b, c, size);
    bh.consume(c);
  }

  //
  // Baseline implementation of a Matrix-Matrix-Multiplication
  //
  public void baseline (float[] a, float[] b, float[] c, int n){
    for (int i = 0; i < n; i += 1) {
      for (int j = 0; j < n; j += 1) {
        float sum = 0.0f;
        for (int k = 0; k < n; k += 1) {
          sum += a[i * n + k] * b[k * n + j];
        }
        c[i * n + j] = sum;
      }
    }
  }

  //
  // Blocked version of MMM, reference implementation available at:
  // http://csapp.cs.cmu.edu/2e/waside/waside-blocking.pdf
  //
  public void blocked(float[] a, float[] b, float[] c, int n) {
    int BLOCK_SIZE = 8;
    for (int kk = 0; kk < n; kk += BLOCK_SIZE) {
      for (int jj = 0; jj < n; jj += BLOCK_SIZE) {
        for (int i = 0; i < n; i++) {
          for (int j = jj; j < jj + BLOCK_SIZE; ++j) {
            float sum = c[i * n + j];
            for (int k = kk; k < kk + BLOCK_SIZE; ++k) {
              sum += a[i * n + k] * b[k * n + j];
            }
            c[i * n + j] = sum;
          }
        }
      }
    }
  }

  public void fast(float[] a, float[] b, float[] c, int n) {
    int in = 0;
    for (int i = 0; i < n; ++i) {
      int kn = 0;
      for (int k = 0; k < n; ++k) {
        float aik = a[in + k];
        for (int j = 0; j < n; ++j) {
          c[in + j] = Math.fma(aik,  b[kn + j], c[in + j]);
        }
        kn += n;
      }
      in += n;
    }
  }

  public void fastBuffered(float[] a, float[] b, float[] c, int n) {
    float[] bBuffer = new float[n];
    float[] cBuffer = new float[n];
    int in = 0;
    for (int i = 0; i < n; ++i) {
      int kn = 0;
      for (int k = 0; k < n; ++k) {
        float aik = a[in + k];
        System.arraycopy(b, kn, bBuffer, 0, n);
        saxpy(n, aik, bBuffer, cBuffer);
        kn += n;
      }
      System.arraycopy(cBuffer, 0, c, in, n);
      Arrays.fill(cBuffer, 0f);
      in += n;
    }
  }

  public void tiled(float[] a, float[] b, float[] c, int n) {
    if (n < 64) { // won't vectorise anyway
      fast(a, b, c, n);
      return;
    }
    final int width = Math.min(n, n >= 256 ? 512 : 256);
    final int height = Math.min(n, n >= 512 ? 8 : n >= 256 ? 16 : 32);
    float[] sum = new float[width];
    float[] vector = new float[width];
    for (int rowOffset = 0; rowOffset < n; rowOffset += height) {
      for (int columnOffset = 0; columnOffset < n; columnOffset += width) {
        for (int i = 0; i < n; ++i) {
          for (int j = columnOffset; j < columnOffset + width && j < n; j += width) {
            System.arraycopy(c, i * n + j, sum, 0, sum.length);
            for (int k = rowOffset; k < rowOffset + height && k < n; ++k) {
              float multiplier = a[i * n + k];
              System.arraycopy(b, k * n  + j, vector, 0, vector.length);
              for (int l = 0; l < width; ++l) {
                sum[l] = Math.fma(multiplier, vector[l], sum[l]);
              }
            }
            System.arraycopy(sum, 0, c,i * n + j, sum.length);
          }
        }
      }
    }
  }

  private void saxpy(int n, float aik, float[] b, float[] c) {
    for (int i = 0; i < n; ++i) {
      c[i] = Math.fma(aik, b[i], c[i]);
    }
  }
}
