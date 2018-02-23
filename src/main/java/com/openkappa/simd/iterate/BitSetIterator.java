package com.openkappa.simd.iterate;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

import static com.openkappa.simd.DataUtil.createIntArray;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BitSetIterator {

  public enum Scenario {
    FULL {
      @Override
      void fillBitmap(long[] bitmap) {
        Arrays.fill(bitmap, -1L);
      }
    },
    SPARSE_16_FULL_WORDS {
      @Override
      void fillBitmap(long[] bitmap) {
        for (int i = 0; i < bitmap.length; ++i) {
          if ((i & 63) == 0) {
            bitmap[i] = -1L;
          } else {
            bitmap[i] = 1L << ThreadLocalRandom.current().nextInt();
          }
        }
      }
    },
    SPARSE_1_16_WORD_RUN {
      @Override
      void fillBitmap(long[] bitmap) {
        for (int i = 0; i < bitmap.length; ++i) {
          if (i > 16 && i < 32) {
            bitmap[i] = -1L;
          } else {
            bitmap[i] = 1L << ThreadLocalRandom.current().nextInt();
          }
        }
      }
    },
    ONE_BIT_PER_WORD {
      @Override
      void fillBitmap(long[] bitmap) {
        for (int i = 0; i < bitmap.length; ++i) {
          bitmap[i] = 1L << ThreadLocalRandom.current().nextInt();
        }
      }
    };

    abstract void fillBitmap(long[] bitmap);
  }


  private long[] bitmap;
  private int[] data;
  private int[] output;
  private int factor;

  public static void main(String[] args) {
    BitSetIterator benchmark = new BitSetIterator();
    benchmark.scenario = Scenario.FULL;
    benchmark.init();
    System.out.println(benchmark.reduce());
    System.out.println(benchmark.reduceWithWordConsumer());
    System.out.println(benchmark.reduceWithRunConsumer());
  }

  @Param({"SPARSE_16_FULL_WORDS",
          "FULL",
          "ONE_BIT_PER_WORD",
          "SPARSE_1_16_WORD_RUN"
  })
  Scenario scenario;

  @Setup(Level.Trial)
  public void init() {
    this.bitmap = new long[1 << 10];
    scenario.fillBitmap(bitmap);
    this.data = createIntArray(1 << 16);
    this.output = new int[1 << 16];
    this.factor = ThreadLocalRandom.current().nextInt();
  }

  @Benchmark
  public int reduce() {
    int[] result = new int[1];
    forEach(bitmap, i -> result[0] += data[i]);
    return result[0];
  }


  @Benchmark
  public int reduceWithWordConsumer() {
    int[] result = new int[1];
    forEach(bitmap, i -> result[0] += data[i], (WordConsumer) (index, word) -> {
      if (word != -1L) {
        throw new IllegalStateException();
      }
      int sum = 0;
      for (int i = index * Long.SIZE; i < (index + 1) * Long.SIZE; ++i) {
        sum += data[i];
      }
      result[0] += sum;
    });
    return result[0];
  }


  @Benchmark
  public int reduceWithRunConsumer() {
    int[] result = new int[1];
    forEach(bitmap, i -> result[0] += data[i], (RunConsumer) (start, end) -> {
      int sum = 0;
      for (int i = start; i < end; ++i) {
        sum += data[i];
      }
      result[0] += sum;
    });
    return result[0];
  }

  @Benchmark
  public void map(Blackhole bh) {
    forEach(bitmap, i -> output[i] = data[i] * data[i] * factor);
    bh.consume(output);
  }

  @Benchmark
  public void mapWithWordConsumer(Blackhole bh) {
    forEach(bitmap,
            i -> output[0] = data[i] * data[i] * factor, (WordConsumer)
            (index, word) -> scaleSquareForWord(index, word, data, output, factor));
    bh.consume(output);
  }

  @Benchmark
  public void mapWithRunConsumer(Blackhole bh) {
    forEach(bitmap,
            i -> output[0] = data[i] * data[i] * factor,
            (RunConsumer) (start, end) -> scaleSquareInRun(start, end, data, output, factor));
    bh.consume(output);
  }

  public void forEach(long[] bitmap, IntConsumer consumer) {
    for (int i = 0; i < bitmap.length; ++i) {
      long word = bitmap[i];
      while (word != 0) {
        consumer.accept(Long.SIZE * i + Long.numberOfTrailingZeros(word));
        word ^= Long.lowestOneBit(word);
      }
    }
  }


  private interface WordConsumer {
    void acceptWord(int wordIndex, long word);
  }

  public void forEach(long[] bitmap, IntConsumer intConsumer, WordConsumer wordConsumer) {
    for (int i = 0; i < bitmap.length; ++i) {
      long word = bitmap[i];
      if (word == -1L) {
        wordConsumer.acceptWord(i, word);
      } else {
        while (word != 0) {
          intConsumer.accept(Long.SIZE * i + Long.numberOfTrailingZeros(word));
          word ^= Long.lowestOneBit(word);
        }
      }
    }
  }

  private interface RunConsumer {
    void acceptRun(int start, int end);
  }

  public void forEach(long[] bitmap, IntConsumer intConsumer, RunConsumer runConsumer) {
    int runStart = -1;
    for (int i = 0; i < bitmap.length; ++i) {
      long word = bitmap[i];
      if (word == -1L) {
        if (runStart == -1) {
          runStart = i;
        }
      } else {
        if (runStart != -1) {
          runConsumer.acceptRun(runStart * Long.SIZE, i * Long.SIZE);
          runStart = -1;
        }
        while (word != 0) {
          intConsumer.accept(Long.SIZE * i + Long.numberOfTrailingZeros(word));
          word ^= Long.lowestOneBit(word);
        }
      }
    }
    if (runStart != -1) {
      runConsumer.acceptRun(runStart * Long.SIZE, bitmap.length * Long.SIZE);
    }
  }


  private static void scaleSquareInRun(int start, int end, int[] data, int[] output, int factor) {
    Objects.checkFromToIndex(start, end, data.length);
    Objects.checkFromToIndex(start, end, output.length);
    for (int i = start; i < end; ++i) {
      output[i] = data[i] * data[i] * factor;
    }
  }


  private static void scaleSquareForWord(int wordIndex, long word, int[] data, int[] output, int factor) {
    if (word != -1L) {
      throw new IllegalStateException();
    }
    int start = wordIndex * Long.SIZE;
    int end = (wordIndex + 1) * Long.SIZE;
    Objects.checkFromToIndex(start, end, data.length);
    Objects.checkFromToIndex(start, end, output.length);
    for (int i = start; i < end; ++i) {
      output[i] = data[i] * data[i] * factor;
    }
  }
}
