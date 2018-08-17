package cyclops;


import com.google.common.collect.ImmutableList;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.collections.immutable.VectorX;
import io.vavr.collection.Stream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.concurrent.TimeUnit;


@State(Scope.Benchmark)
public class VectorOps {


    Vector<Integer> vector;
    VectorX<Integer> vectorX;
    io.vavr.collection.Vector<Integer> js;

    @Setup
    public void before() {
        vector = Vector.range(0, 100_000);
        vectorX = VectorX.range(0, 100_000);
        js = io.vavr.collection.Vector.range(0, 100_000);

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
    public void vectorOps() {
        vector.map(i -> i * 2)
                .concatMap(i->Vector.range(0,10))
                .map(i -> i * 2)
                .filter(i -> i < 5000)
                .map(i -> "hello " + i)
                .zip(Vector.range(0,1000000))
                .map(i->i._1())
                .map(i -> i.length())
                .foldLeft((a, b) -> a + b);

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
    public void vavrOps() {
        js.map(i -> i * 2)
            .flatMap(i->Vector.range(0,10))
            .map(i -> i * 2)
            .filter(i -> i < 5000)
            .map(i -> "hello " + i)
            .zip(Vector.range(0,1000000))
            .map(i->i._1())
            .map(i -> i.length())
            .reduce((a, b) -> a + b);

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
    public void vectorXOps() {

        vectorX.map(i -> i * 2)
                .concatMap(i->Vector.range(0,10))
                .map(i -> i * 2)
                .filter(i -> i < 5000)
                .map(i -> "hello " + i)
                .zip(Vector.range(0,1000000))
                .map(i->i._1())
                .map(i -> i.length())
               .foldLeft((a, b) -> a + b);




    }

}

/**
 * Benchmark                                  Mode  Cnt    Score    Error  Units
 VectorOps.vavrOps                        sample   47  233.269 ± 24.097  ms/op
 VectorOps.vavrOps:vavrOps·p0.00          sample       208.667           ms/op
 VectorOps.vavrOps:vavrOps·p0.50          sample       217.842           ms/op
 VectorOps.vavrOps:vavrOps·p0.90          sample       263.193           ms/op
 VectorOps.vavrOps:vavrOps·p0.95          sample       382.206           ms/op
 VectorOps.vavrOps:vavrOps·p0.99          sample       443.023           ms/op
 VectorOps.vavrOps:vavrOps·p0.999         sample       443.023           ms/op
 VectorOps.vavrOps:vavrOps·p0.9999        sample       443.023           ms/op
 VectorOps.vavrOps:vavrOps·p1.00          sample       443.023           ms/op
 VectorOps.vectorOps                      sample   46  249.527 ±  8.966  ms/op
 VectorOps.vectorOps:vectorOps·p0.00      sample       208.142           ms/op
 VectorOps.vectorOps:vectorOps·p0.50      sample       254.542           ms/op
 VectorOps.vectorOps:vectorOps·p0.90      sample       263.350           ms/op
 VectorOps.vectorOps:vectorOps·p0.95      sample       265.631           ms/op
 VectorOps.vectorOps:vectorOps·p0.99      sample       301.466           ms/op
 VectorOps.vectorOps:vectorOps·p0.999     sample       301.466           ms/op
 VectorOps.vectorOps:vectorOps·p0.9999    sample       301.466           ms/op
 VectorOps.vectorOps:vectorOps·p1.00      sample       301.466           ms/op
 VectorOps.vectorXOps                     sample   60  197.770 ±  1.628  ms/op
 VectorOps.vectorXOps:vectorXOps·p0.00    sample       191.889           ms/op
 VectorOps.vectorXOps:vectorXOps·p0.50    sample       197.394           ms/op
 VectorOps.vectorXOps:vectorXOps·p0.90    sample       202.375           ms/op
 VectorOps.vectorXOps:vectorXOps·p0.95    sample       203.922           ms/op
 VectorOps.vectorXOps:vectorXOps·p0.99    sample       204.997           ms/op
 VectorOps.vectorXOps:vectorXOps·p0.999   sample       204.997           ms/op
 VectorOps.vectorXOps:vectorXOps·p0.9999  sample       204.997           ms/op
 VectorOps.vectorXOps:vectorXOps·p1.00    sample       204.997           ms/op

 */
