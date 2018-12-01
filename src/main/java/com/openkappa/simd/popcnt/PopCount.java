package com.openkappa.simd.popcnt;

import com.openkappa.simd.state.IntData;
import com.openkappa.simd.state.LongData;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsPrepend = {
        "-XX:-TieredCompilation"
//        ,
//        "-XX:+UnlockExperimentalVMOptions",
//        "-XX:+EnableJVMCI" ,
//        "-XX:+UseJVMCICompiler"
})
public class PopCount {

    @Benchmark
    public long PopCount(LongData state) {
        long popCnt = 0L;
        long[] data = state.data1;
        for (int i = 0; i < data.length; ++i) {
            popCnt += Long.bitCount(data[i]);
        }
        return popCnt;
    }

    @Benchmark
    public int PopCountInt(IntData state) {
        int popCnt = 0;
        int[] data = state.data1;
        for (int i = 0; i < data.length; ++i) {
            popCnt += Integer.bitCount(data[i]);
        }
        return popCnt;
    }
}
