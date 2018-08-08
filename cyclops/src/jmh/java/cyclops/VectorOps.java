package cyclops;


import com.google.common.collect.ImmutableList;
import cyclops.data.Vector;
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
                .map(i -> i.length())
                .zipWithIndex()
                .map(i->i._1())
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
            .map(i -> i.length())
            .zipWithIndex()
            .map(i->i._1())
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
                .map(i -> i.length())
                .zipWithIndex()
                .map(i->i._1())
               .foldLeft((a, b) -> a + b);


        

    }

}
