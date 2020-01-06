package cyclops.data.chain;

import cyclops.data.Chain;
import cyclops.data.Seq;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ChainAppend {

    Seq<Integer> seq;
    Chain<Integer> chain;


    @Setup(Level.Iteration)
    public void before() {
        seq = Seq.range(0, 10000);
        chain = Chain.wrap(seq);


    }


    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(
        iterations = 10
    )
    @Measurement(
        iterations = 10
    )
    @Fork(1)
    public void seqOps() {
        for(int i=0;i<1000;i++)
            seq = seq.append(i);

    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(
        iterations = 10
    )
    @Measurement(
        iterations = 10
    )
    @Fork(1)
    public void chainOps() {
        for(int i=0;i<1000;i++)
            chain =chain.prepend(i);

    }



}
/**
 Benchmark                                   Mode     Cnt  Score    Error  Units
 # JMH 1.14 (released 1208 days ago, please consider updating!)
 # VM version: JDK 1.8.0_161, VM 25.161-b12
 # VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_161.jdk/Contents/Home/jre/bin/java
 # VM options: -Dfile.encoding=UTF-8
 # Warmup: 10 iterations, 1 s each
 # Measurement: 10 iterations, 1 s each
 # Timeout: 10 min per iteration
 # Threads: 1 thread, will synchronize iterations
 # Benchmark mode: Sampling time
 # Benchmark: cyclops.data.chain.ChainAppend.chainOps

 # Run progress: 0.00% complete, ETA 00:00:40
 # Fork: 1 of 1
 # Warmup Iteration   1: 0.981 ±(99.9%) 2.732 ms/op
 # Warmup Iteration   2: 0.700 ±(99.9%) 2.236 ms/op
 # Warmup Iteration   3: 0.687 ±(99.9%) 1.885 ms/op
 # Warmup Iteration   4: 0.324 ±(99.9%) 1.033 ms/op
 # Warmup Iteration   5: 0.365 ±(99.9%) 1.064 ms/op
 # Warmup Iteration   6: 0.300 ±(99.9%) 0.957 ms/op
 # Warmup Iteration   7: 0.293 ±(99.9%) 0.900 ms/op
 # Warmup Iteration   8: 0.286 ±(99.9%) 0.911 ms/op
 # Warmup Iteration   9: 0.276 ±(99.9%) 0.883 ms/op
 # Warmup Iteration  10: 0.278 ±(99.9%) 0.888 ms/op
 Iteration   1: 0.276 ±(99.9%) 0.881 ms/op
 chainOps·p0.00:   0.007 ms/op
 chainOps·p0.50:   0.008 ms/op
 chainOps·p0.90:   0.008 ms/op
 chainOps·p0.95:   0.009 ms/op
 chainOps·p0.99:   0.021 ms/op
 chainOps·p0.999:  0.033 ms/op
 chainOps·p0.9999: 2057.306 ms/op
 chainOps·p1.00:   2057.306 ms/op

 Iteration   2: 0.281 ±(99.9%) 0.892 ms/op
 chainOps·p0.00:   0.008 ms/op
 chainOps·p0.50:   0.009 ms/op
 chainOps·p0.90:   0.009 ms/op
 chainOps·p0.95:   0.022 ms/op
 chainOps·p0.99:   0.028 ms/op
 chainOps·p0.999:  0.062 ms/op
 chainOps·p0.9999: 2082.472 ms/op
 chainOps·p1.00:   2082.472 ms/op

 Iteration   3: 0.280 ±(99.9%) 0.890 ms/op
 chainOps·p0.00:   0.008 ms/op
 chainOps·p0.50:   0.009 ms/op
 chainOps·p0.90:   0.012 ms/op
 chainOps·p0.95:   0.015 ms/op
 chainOps·p0.99:   0.028 ms/op
 chainOps·p0.999:  0.045 ms/op
 chainOps·p0.9999: 2078.278 ms/op
 chainOps·p1.00:   2078.278 ms/op

 Iteration   4: 0.280 ±(99.9%) 0.892 ms/op
 chainOps·p0.00:   0.008 ms/op
 chainOps·p0.50:   0.009 ms/op
 chainOps·p0.90:   0.012 ms/op
 chainOps·p0.95:   0.014 ms/op
 chainOps·p0.99:   0.021 ms/op
 chainOps·p0.999:  0.037 ms/op
 chainOps·p0.9999: 2082.472 ms/op
 chainOps·p1.00:   2082.472 ms/op

 Iteration   5: 0.280 ±(99.9%) 0.890 ms/op
 chainOps·p0.00:   0.008 ms/op
 chainOps·p0.50:   0.009 ms/op
 chainOps·p0.90:   0.010 ms/op
 chainOps·p0.95:   0.010 ms/op
 chainOps·p0.99:   0.015 ms/op
 chainOps·p0.999:  0.031 ms/op
 chainOps·p0.9999: 2078.278 ms/op
 chainOps·p1.00:   2078.278 ms/op

 Iteration   6: 0.279 ±(99.9%) 0.889 ms/op
 chainOps·p0.00:   0.007 ms/op
 chainOps·p0.50:   0.008 ms/op
 chainOps·p0.90:   0.009 ms/op
 chainOps·p0.95:   0.009 ms/op
 chainOps·p0.99:   0.025 ms/op
 chainOps·p0.999:  0.035 ms/op
 chainOps·p0.9999: 2074.083 ms/op
 chainOps·p1.00:   2074.083 ms/op

 Iteration   7: 0.279 ±(99.9%) 0.887 ms/op
 chainOps·p0.00:   0.008 ms/op
 chainOps·p0.50:   0.009 ms/op
 chainOps·p0.90:   0.009 ms/op
 chainOps·p0.95:   0.014 ms/op
 chainOps·p0.99:   0.028 ms/op
 chainOps·p0.999:  0.043 ms/op
 chainOps·p0.9999: 2071.986 ms/op
 chainOps·p1.00:   2071.986 ms/op

 Iteration   8: 0.278 ±(99.9%) 0.884 ms/op
 chainOps·p0.00:   0.008 ms/op
 chainOps·p0.50:   0.009 ms/op
 chainOps·p0.90:   0.010 ms/op
 chainOps·p0.95:   0.014 ms/op
 chainOps·p0.99:   0.028 ms/op
 chainOps·p0.999:  0.046 ms/op
 chainOps·p0.9999: 2063.598 ms/op
 chainOps·p1.00:   2063.598 ms/op

 Iteration   9: 0.280 ±(99.9%) 0.889 ms/op
 chainOps·p0.00:   0.008 ms/op
 chainOps·p0.50:   0.009 ms/op
 chainOps·p0.90:   0.010 ms/op
 chainOps·p0.95:   0.014 ms/op
 chainOps·p0.99:   0.028 ms/op
 chainOps·p0.999:  0.046 ms/op
 chainOps·p0.9999: 2076.180 ms/op
 chainOps·p1.00:   2076.180 ms/op

 Iteration  10: 0.283 ±(99.9%) 0.898 ms/op
 chainOps·p0.00:   0.008 ms/op
 chainOps·p0.50:   0.009 ms/op
 chainOps·p0.90:   0.011 ms/op
 chainOps·p0.95:   0.014 ms/op
 chainOps·p0.99:   0.027 ms/op
 chainOps·p0.999:  0.046 ms/op
 chainOps·p0.9999: 2097.152 ms/op
 chainOps·p1.00:   2097.152 ms/op



 Result "chainOps":
 N = 76864
 mean =      0.279 ±(99.9%) 0.281 ms/op

 Histogram, ms/op:
 [   0.000,  250.000) = 76854
 [ 250.000,  500.000) = 0
 [ 500.000,  750.000) = 0
 [ 750.000, 1000.000) = 0
 [1000.000, 1250.000) = 0
 [1250.000, 1500.000) = 0
 [1500.000, 1750.000) = 0
 [1750.000, 2000.000) = 0
 [2000.000, 2250.000) = 10
 [2250.000, 2500.000) = 0
 [2500.000, 2750.000) = 0

 Percentiles, ms/op:
 p(0.0000) =      0.007 ms/op
 p(50.0000) =      0.009 ms/op
 p(90.0000) =      0.010 ms/op
 p(95.0000) =      0.013 ms/op
 p(99.0000) =      0.027 ms/op
 p(99.9000) =      0.044 ms/op
 p(99.9900) =   2072.644 ms/op
 p(99.9990) =   2097.152 ms/op
 p(99.9999) =   2097.152 ms/op
 p(100.0000) =   2097.152 ms/op


 # JMH 1.14 (released 1208 days ago, please consider updating!)
 # VM version: JDK 1.8.0_161, VM 25.161-b12
 # VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0_161.jdk/Contents/Home/jre/bin/java
 # VM options: -Dfile.encoding=UTF-8
 # Warmup: 10 iterations, 1 s each
 # Measurement: 10 iterations, 1 s each
 # Timeout: 10 min per iteration
 # Threads: 1 thread, will synchronize iterations
 # Benchmark mode: Sampling time
 # Benchmark: cyclops.data.chain.ChainAppend.seqOps

 # Run progress: 50.00% complete, ETA 00:00:42
 # Fork: 1 of 1
 # Warmup Iteration   1: 250.924 ±(99.9%) 196.320 ms/op
 # Warmup Iteration   2: 223.871 ±(99.9%) 320.335 ms/op
 # Warmup Iteration   3: 191.016 ±(99.9%) 47.567 ms/op
 # Warmup Iteration   4: 196.739 ±(99.9%) 67.761 ms/op
 # Warmup Iteration   5: 222.980 ±(99.9%) 264.293 ms/op
 # Warmup Iteration   6: 190.142 ±(99.9%) 61.764 ms/op
 # Warmup Iteration   7: 182.889 ±(99.9%) 84.493 ms/op
 # Warmup Iteration   8: 210.921 ±(99.9%) 142.691 ms/op
 # Warmup Iteration   9: 208.876 ±(99.9%) 348.449 ms/op
 # Warmup Iteration  10: 198.181 ±(99.9%) 149.168 ms/op
 Iteration   1: 185.292 ±(99.9%) 59.936 ms/op
 seqOps·p0.00:   159.121 ms/op
 seqOps·p0.50:   182.583 ms/op
 seqOps·p0.90:   216.269 ms/op
 seqOps·p0.95:   216.269 ms/op
 seqOps·p0.99:   216.269 ms/op
 seqOps·p0.999:  216.269 ms/op
 seqOps·p0.9999: 216.269 ms/op
 seqOps·p1.00:   216.269 ms/op

 Iteration   2: 193.550 ±(99.9%) 64.098 ms/op
 seqOps·p0.00:   172.753 ms/op
 seqOps·p0.50:   188.482 ms/op
 seqOps·p0.90:   219.939 ms/op
 seqOps·p0.95:   219.939 ms/op
 seqOps·p0.99:   219.939 ms/op
 seqOps·p0.999:  219.939 ms/op
 seqOps·p0.9999: 219.939 ms/op
 seqOps·p1.00:   219.939 ms/op

 Iteration   3: 193.593 ±(99.9%) 100.569 ms/op
 seqOps·p0.00:   145.228 ms/op
 seqOps·p0.50:   192.807 ms/op
 seqOps·p0.90:   246.153 ms/op
 seqOps·p0.95:   246.153 ms/op
 seqOps·p0.99:   246.153 ms/op
 seqOps·p0.999:  246.153 ms/op
 seqOps·p0.9999: 246.153 ms/op
 seqOps·p1.00:   246.153 ms/op

 Iteration   4: 190.054 ±(99.9%) 87.666 ms/op
 seqOps·p0.00:   144.966 ms/op
 seqOps·p0.50:   187.171 ms/op
 seqOps·p0.90:   226.755 ms/op
 seqOps·p0.95:   226.755 ms/op
 seqOps·p0.99:   226.755 ms/op
 seqOps·p0.999:  226.755 ms/op
 seqOps·p0.9999: 226.755 ms/op
 seqOps·p1.00:   226.755 ms/op

 Iteration   5: 187.389 ±(99.9%) 85.399 ms/op
 seqOps·p0.00:   137.626 ms/op
 seqOps·p0.50:   192.938 ms/op
 seqOps·p0.90:   217.842 ms/op
 seqOps·p0.95:   217.842 ms/op
 seqOps·p0.99:   217.842 ms/op
 seqOps·p0.999:  217.842 ms/op
 seqOps·p0.9999: 217.842 ms/op
 seqOps·p1.00:   217.842 ms/op

 Iteration   6: 192.894 ±(99.9%) 96.868 ms/op
 seqOps·p0.00:   138.936 ms/op
 seqOps·p0.50:   195.166 ms/op
 seqOps·p0.90:   236.716 ms/op
 seqOps·p0.95:   236.716 ms/op
 seqOps·p0.99:   236.716 ms/op
 seqOps·p0.999:  236.716 ms/op
 seqOps·p0.9999: 236.716 ms/op
 seqOps·p1.00:   236.716 ms/op

 Iteration   7: 188.482 ±(99.9%) 78.119 ms/op
 seqOps·p0.00:   147.587 ms/op
 seqOps·p0.50:   189.399 ms/op
 seqOps·p0.90:   222.560 ms/op
 seqOps·p0.95:   222.560 ms/op
 seqOps·p0.99:   222.560 ms/op
 seqOps·p0.999:  222.560 ms/op
 seqOps·p0.9999: 222.560 ms/op
 seqOps·p1.00:   222.560 ms/op

 Iteration   8: 189.574 ±(99.9%) 82.896 ms/op
 seqOps·p0.00:   144.703 ms/op
 seqOps·p0.50:   193.855 ms/op
 seqOps·p0.90:   222.298 ms/op
 seqOps·p0.95:   222.298 ms/op
 seqOps·p0.99:   222.298 ms/op
 seqOps·p0.999:  222.298 ms/op
 seqOps·p0.9999: 222.298 ms/op
 seqOps·p1.00:   222.298 ms/op

 Iteration   9: 188.613 ±(99.9%) 76.855 ms/op
 seqOps·p0.00:   143.917 ms/op
 seqOps·p0.50:   188.875 ms/op
 seqOps·p0.90:   216.531 ms/op
 seqOps·p0.95:   216.531 ms/op
 seqOps·p0.99:   216.531 ms/op
 seqOps·p0.999:  216.531 ms/op
 seqOps·p0.9999: 216.531 ms/op
 seqOps·p1.00:   216.531 ms/op

 Iteration  10: 188.875 ±(99.9%) 92.864 ms/op
 seqOps·p0.00:   146.014 ms/op
 seqOps·p0.50:   190.448 ms/op
 seqOps·p0.90:   232.784 ms/op
 seqOps·p0.95:   232.784 ms/op
 seqOps·p0.99:   232.784 ms/op
 seqOps·p0.999:  232.784 ms/op
 seqOps·p0.9999: 232.784 ms/op
 seqOps·p1.00:   232.784 ms/op



 Result "seqOps":
 N = 60
 mean =    189.832 ±(99.9%) 12.309 ms/op

 Histogram, ms/op:
 [100.000, 112.500) = 0
 [112.500, 125.000) = 0
 [125.000, 137.500) = 0
 [137.500, 150.000) = 8
 [150.000, 162.500) = 2
 [162.500, 175.000) = 9
 [175.000, 187.500) = 10
 [187.500, 200.000) = 6
 [200.000, 212.500) = 7
 [212.500, 225.000) = 14
 [225.000, 237.500) = 3
 [237.500, 250.000) = 1
 [250.000, 262.500) = 0
 [262.500, 275.000) = 0
 [275.000, 287.500) = 0

 Percentiles, ms/op:
 p(0.0000) =    137.626 ms/op
 p(50.0000) =    188.744 ms/op
 p(90.0000) =    222.272 ms/op
 p(95.0000) =    232.482 ms/op
 p(99.0000) =    246.153 ms/op
 p(99.9000) =    246.153 ms/op
 p(99.9900) =    246.153 ms/op
 p(99.9990) =    246.153 ms/op
 p(99.9999) =    246.153 ms/op
 p(100.0000) =    246.153 ms/op


 # Run complete. Total time: 00:01:05

 Benchmark                                Mode    Cnt     Score    Error  Units
 ChainAppend.chainOps                   sample  76864     0.279 ±  0.281  ms/op
 ChainAppend.chainOps:chainOps·p0.00    sample            0.007           ms/op
 ChainAppend.chainOps:chainOps·p0.50    sample            0.009           ms/op
 ChainAppend.chainOps:chainOps·p0.90    sample            0.010           ms/op
 ChainAppend.chainOps:chainOps·p0.95    sample            0.013           ms/op
 ChainAppend.chainOps:chainOps·p0.99    sample            0.027           ms/op
 ChainAppend.chainOps:chainOps·p0.999   sample            0.044           ms/op
 ChainAppend.chainOps:chainOps·p0.9999  sample         2072.644           ms/op
 ChainAppend.chainOps:chainOps·p1.00    sample         2097.152           ms/op
 ChainAppend.seqOps                     sample     60   189.832 ± 12.309  ms/op
 ChainAppend.seqOps:seqOps·p0.00        sample          137.626           ms/op
 ChainAppend.seqOps:seqOps·p0.50        sample          188.744           ms/op
 ChainAppend.seqOps:seqOps·p0.90        sample          222.272           ms/op
 ChainAppend.seqOps:seqOps·p0.95        sample          232.482           ms/op
 ChainAppend.seqOps:seqOps·p0.99        sample          246.153           ms/op
 ChainAppend.seqOps:seqOps·p0.999       sample          246.153           ms/op
 ChainAppend.seqOps:seqOps·p0.9999      sample          246.153           ms/op
 ChainAppend.seqOps:seqOps·p1.00        sample          246.153           ms/op
 **/
