package com.openkappa.simd.base64;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

import static com.openkappa.simd.DataUtil.createByteArray;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Base64 {

  @Param({"1024", "2048", "3072", "4096"})
  int size;

  private byte[] data;
  private int[] wordBuffer;
  private byte[] encoded;
  private java.util.Base64.Encoder encoder;
  private java.util.Base64.Decoder decoder;

  @Setup(Level.Trial)
  public void init() {
    encoder = java.util.Base64.getEncoder();
    decoder = java.util.Base64.getDecoder();
    data = createByteArray(size);
    encoded = encoder.encode(data);
  }

//  @Benchmark
//  public void decode(Blackhole bh) {
//    decoder.decode(encoded, data);
//    bh.consume(data);
//  }

  @Benchmark
  public void encode(Blackhole bh) {
    encoder.encode(data, encoded);
    bh.consume(encoded);
  }

  @Benchmark
  public void decoupled(Blackhole bh) {
    StreamingBase64.encodeChunkBuffered(data, encoded);
    bh.consume(encoded);
  }
}
