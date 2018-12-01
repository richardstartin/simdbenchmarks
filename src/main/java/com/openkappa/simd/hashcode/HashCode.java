package com.openkappa.simd.hashcode;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.openkappa.simd.DataUtil.createIntArray;

@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(value = 3, jvmArgsPrepend = {
        "-XX:-TieredCompilation"
        ,
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+EnableJVMCI" ,
        "-XX:+UseJVMCICompiler"
})
public class HashCode {


    @Param({
            "256",
            "1024",
            "8192"
    })
    private int size;


    public static void main(String[] args) {
        System.out.println(31 * 31 * 31 * 31 * 31 * 31 * 31 * 31);
        System.out.println(31 * 31 * 31 * 31 * 31 * 31 * 31);
        System.out.println(31 * 31 * 31 * 31 * 31 * 31);
        System.out.println(31 * 31 * 31 * 31 * 31);
        System.out.println(31 * 31 * 31 * 31);
        System.out.println(31 * 31 * 31);
        System.out.println(31 * 31);

        HashCode hashCode = new HashCode();
        hashCode.size = 256;
        hashCode.init();
        System.out.println(hashCode.BuiltIn());
        System.out.println(hashCode.StrengthReduction());
    }


    private int[] data;

    private int[] coefficients;
    private int seed;

    private FixedLengthHashCode hashCode;

    @Setup(Level.Trial)
    public void init() {
        data = createIntArray(size);
        this.coefficients = new int[size];
        coefficients[size - 1] = 1;
        for (int i = size - 2; i >= 0; --i) {
            coefficients[i] = 31 * coefficients[i + 1];
        }
        seed = 31 * coefficients[0];
        this.hashCode = new FixedLengthHashCode(size);
    }

    @Benchmark
    public int Unrolled() {
        if (data == null)
            return 0;

        int result = 1;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = -1807454463 * result
                   + 1742810335 * data[i]
                   + 887503681 * data[i + 1]
                   + 28629151 * data[i + 2]
                   + 923521 * data[i + 3]
                   + 29791 * data[i + 4]
                   + 961 * data[i + 5]
                   + 31 * data[i + 6]
                   + data[i + 7]
                    ;
        }
        for (; i < data.length; i++) {
            result = 31 * result + data[i];
        }
        return result;
    }

    @Benchmark
    public int StrengthReduction() {
        int result = 1;
        for (int i = 0; i < data.length; ++i) {
            result = (result << 5) - result + data[i];
        }
        return result;
    }

    @Benchmark
    public int BuiltIn() {
        return Arrays.hashCode(data);
    }

    @Benchmark
    public int FixedLength() {
        return hashCode.hashCode(data);
    }

    @Benchmark
    public int Vectorised() {
        int result = seed;
        for (int i = 0; i < data.length && i < coefficients.length; ++i) {
            result += coefficients[i] * data[i];
        }
        return result;
    }




}
