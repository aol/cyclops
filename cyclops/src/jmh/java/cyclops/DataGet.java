package cyclops;


import com.google.common.collect.ImmutableList;
import cyclops.data.Vector;
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
import java.util.stream.Collectors;

@State(Scope.Benchmark)
public class DataGet {

    Vector<Integer> vector;
    List<Integer> list;
    io.vavr.collection.Vector<Integer> js;
    ImmutableList<Integer> guava;

    @Setup
    public void before(){
        vector = Vector.range(0,10000);
        list = Stream.range(0,10000).toJavaList();
        js = io.vavr.collection.Vector.range(0,10000);
        guava = ImmutableList.copyOf(list);

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
    public void vectorGet(){
        for(int i=0;i<100;i++){
            vector.get(i);
        }
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
    public void jsGet(){
        for(int i=0;i<100;i++){
            js.get(i);
        }
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
    public void listGet(){
        for(int i=0;i<100;i++){
            list.get(i);
        }
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
    public void guavaGet(){
        for(int i=0;i<100;i++){
            guava.get(i);
        }
    }
}

/**
 Benchmark                              Mode     Cnt   Score    Error  Units
 DataGet.guavaGet                     sample  239840  ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.00      sample          ≈ 10⁻⁶           ms/op
 DataGet.guavaGet:guavaGet·p0.50      sample          ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.90      sample          ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.95      sample          ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.99      sample          ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.999     sample           0.013           ms/op
 DataGet.guavaGet:guavaGet·p0.9999    sample           0.026           ms/op
 DataGet.guavaGet:guavaGet·p1.00      sample           0.900           ms/op
 DataGet.jsGet                        sample  300010   0.001 ±  0.001  ms/op
 DataGet.jsGet:jsGet·p0.00            sample          ≈ 10⁻³           ms/op
 DataGet.jsGet:jsGet·p0.50            sample           0.001           ms/op
 DataGet.jsGet:jsGet·p0.90            sample           0.001           ms/op
 DataGet.jsGet:jsGet·p0.95            sample           0.001           ms/op
 DataGet.jsGet:jsGet·p0.99            sample           0.001           ms/op
 DataGet.jsGet:jsGet·p0.999           sample           0.020           ms/op
 DataGet.jsGet:jsGet·p0.9999          sample           0.056           ms/op
 DataGet.jsGet:jsGet·p1.00            sample           0.337           ms/op
 DataGet.listGet                      sample  243997  ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.00        sample          ≈ 10⁻⁶           ms/op
 DataGet.listGet:listGet·p0.50        sample          ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.90        sample          ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.95        sample          ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.99        sample          ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.999       sample           0.004           ms/op
 DataGet.listGet:listGet·p0.9999      sample           0.021           ms/op
 DataGet.listGet:listGet·p1.00        sample           0.071           ms/op
 DataGet.vectorGet                    sample  243028  ≈ 10⁻³           ms/op
 DataGet.vectorGet:vectorGet·p0.00    sample          ≈ 10⁻⁴           ms/op
 DataGet.vectorGet:vectorGet·p0.50    sample          ≈ 10⁻³           ms/op
 DataGet.vectorGet:vectorGet·p0.90    sample          ≈ 10⁻³           ms/op
 DataGet.vectorGet:vectorGet·p0.95    sample          ≈ 10⁻³           ms/op
 DataGet.vectorGet:vectorGet·p0.99    sample           0.001           ms/op
 DataGet.vectorGet:vectorGet·p0.999   sample           0.019           ms/op
 DataGet.vectorGet:vectorGet·p0.9999  sample           0.032           ms/op
 DataGet.vectorGet:vectorGet·p1.00    sample           0.097           ms/op
 */

