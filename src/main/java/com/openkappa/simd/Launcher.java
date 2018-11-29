package com.openkappa.simd;

import com.lexicalscope.jewel.cli.CliFactory;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.profile.WinPerfAsmProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;

public class Launcher {

    public static void main(String[] args) throws RunnerException {
        ParsedArgs parsed = CliFactory.parseArguments(ParsedArgs.class, args);
        ChainedOptionsBuilder builder = new OptionsBuilder()
                .include(parsed.include())
                .mode(parsed.mode())
                .measurementTime(TimeValue.seconds(parsed.measurementTime()))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .shouldFailOnError(true)
                .verbosity(VerboseMode.EXTRA)
                .resultFormat(ResultFormatType.CSV);
        if (parsed.printAssembly()) {
            builder = builder.jvmArgsAppend("-XX:+UnlockDiagnosticVMOptions",
                                            "-XX:CompileCommand=print" + (null == parsed.methodName()
                                                    ? "" : ",*" + parsed.methodName()),
                                            "-XX:PrintAssemblyOptions=hsdis-print-bytes",
                                            "-XX:CompileCommand=print");
        }
        if (parsed.doPerfasm()) {
            String os = System.getProperty("os.name");
            if (os.toLowerCase().contains("windows")) {
                builder = builder.addProfiler(WinPerfAsmProfiler.class);
            }
            if (os.toLowerCase().contains("linux")) {
                builder = builder.addProfiler(LinuxPerfAsmProfiler.class);
            }
        }
        if (parsed.printCompilation()) {
            builder = builder.jvmArgsAppend("-XX:+PrintCompilation",
                                            "-XX:+UnlockDiagnosticVMOptions",
                                            "-XX:+PrintInlining");
        }
        if (null != parsed.output()) {
            builder = builder.output(parsed.output());
        }

        new Runner(builder.build()).run();
    }
}
