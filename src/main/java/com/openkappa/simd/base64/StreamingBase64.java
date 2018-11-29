package com.openkappa.simd.base64;

import java.util.concurrent.ThreadLocalRandom;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StreamingBase64 {


  private static final byte[] ENCODING = {
          'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
          'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
          'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
          'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
          '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };

  private static final byte END = '=';

  public static void main(String... args) {
    byte[] in = new byte[1024];
    ThreadLocalRandom.current().nextBytes(in);
    byte[] out = new byte[2000];
    encodeChunkBuffered(in, out);
    System.out.println(new String(out, UTF_8));
    System.out.println(new String(java.util.Base64.getEncoder().encode(in), UTF_8));
  }


  public static void encodeChunkBuffered(byte[] in, byte[] out) {
    int i = 0;
    int j = 0;
    // 3 bytes in, 4 bytes out
    for (; j + 3 < out.length && i + 2 < in.length; i += 3, j += 4) {
      // map 3 bytes into the lower 24 bits of an integer
      int word = (in[i + 0] & 0xFF) << 16 | (in[i + 1] & 0xFF) << 8 | (in[i + 2] & 0xFF);
      // for each 6-bit subword, find the appropriate character in the lookup table
      out[j + 0] = (byte)lookup((word >>> 18) & 0x3F);
      out[j + 1] = (byte)lookup((word >>> 12) & 0x3F);
      out[j + 2] = (byte)lookup((word >>> 6) & 0x3F);
      out[j + 3] = (byte)lookup(word & 0x3F);
    }
  }

  private static int lookup(int i) {
    int shift = 'A';
    shift += i >= 26 ? 6 : 0;
    shift -= i >= 52 ? 75 : 0;
    shift += i == 62 ? 241 : 0;
    shift -= i == 63 ? 12 : 0;
    return shift + i;
  }
  
}

