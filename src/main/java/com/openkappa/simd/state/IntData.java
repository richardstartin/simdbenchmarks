package com.openkappa.simd.state;

import org.openjdk.jmh.annotations.*;

import static com.openkappa.simd.DataUtil.createIntArray;

@State(Scope.Thread)
public class IntData {

    @Param({"1024", "4096", "8192"})
    int size;

    public int[] data1;
    public int[] data2;

    @Setup(Level.Trial)
    public void init() {
        this.data1 = createIntArray(size);
        this.data2 = createIntArray(size);
    }
}
