package com.openkappa.simd.state;

import org.openjdk.jmh.annotations.*;

import static com.openkappa.simd.DataUtil.createByteArray;

@State(Scope.Thread)
public class BytePrefixData {

    @Param({"0.1", "0.5", "1.0"})
    double prefix;

    @Param({"100", "1000", "10000"})
    int size;

    public byte[] data1;
    public byte[] data2;

    @Setup(Level.Trial)
    public void init() {
        int prefixLength = (int)(prefix * size);
        byte[] commonPrefix = createByteArray(prefixLength);
        this.data1 = createByteArray(size);
        this.data2 = createByteArray(size);
        for (int i = 0; i < prefixLength; ++i) {
            data1[i] = commonPrefix[i];
            data2[i] = commonPrefix[i];
        }
    }
}
