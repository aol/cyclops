package cyclops;


import com.google.common.collect.ImmutableList;
import cyclops.data.Vector;
import io.vavr.collection.Stream;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class DataGet {

    Vector<String> vector;
    List<String> list;
    io.vavr.collection.Vector<String> js;
    ImmutableList<String> guava;
    @Setup(Level.Iteration)
    public void before(){
        vector = Vector.range(0,10000).map(i->""+i);;
        list = Stream.range(0,10000).map(i->""+i).toJavaList();
        js = io.vavr.collection.Vector.range(0,10000).map(i->""+i);
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
        for(int i=0;i<10000;i++){
            vector.getOrElse(i,null);
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
        for(int i=0;i<10000;i++){
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
        for(int i=0;i<10000;i++){
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
        for(int i=0;i<10000;i++){
            guava.get(i);
        }
    }
}

/**
 Benchmark                              Mode     Cnt   Score    Error  Units
 DataGet.guavaGet                     sample  231585  ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.00      sample          ≈ 10⁻⁶           ms/op
 DataGet.guavaGet:guavaGet·p0.50      sample          ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.90      sample          ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.95      sample          ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.99      sample          ≈ 10⁻⁴           ms/op
 DataGet.guavaGet:guavaGet·p0.999     sample           0.009           ms/op
 DataGet.guavaGet:guavaGet·p0.9999    sample           0.021           ms/op
 DataGet.guavaGet:guavaGet·p1.00      sample           0.080           ms/op
 DataGet.jsGet                        sample  200427   0.025 ±  0.001  ms/op
 DataGet.jsGet:jsGet·p0.00            sample           0.022           ms/op
 DataGet.jsGet:jsGet·p0.50            sample           0.024           ms/op
 DataGet.jsGet:jsGet·p0.90            sample           0.025           ms/op
 DataGet.jsGet:jsGet·p0.95            sample           0.030           ms/op
 DataGet.jsGet:jsGet·p0.99            sample           0.059           ms/op
 DataGet.jsGet:jsGet·p0.999           sample           0.089           ms/op
 DataGet.jsGet:jsGet·p0.9999          sample           0.129           ms/op
 DataGet.jsGet:jsGet·p1.00            sample           0.295           ms/op
 DataGet.listGet                      sample  241869  ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.00        sample          ≈ 10⁻⁶           ms/op
 DataGet.listGet:listGet·p0.50        sample          ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.90        sample          ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.95        sample          ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.99        sample          ≈ 10⁻⁴           ms/op
 DataGet.listGet:listGet·p0.999       sample           0.003           ms/op
 DataGet.listGet:listGet·p0.9999      sample           0.020           ms/op
 DataGet.listGet:listGet·p1.00        sample           0.042           ms/op
 DataGet.vectorGet                    sample  232001   0.022 ±  0.001  ms/op
 DataGet.vectorGet:vectorGet·p0.00    sample           0.019           ms/op
 DataGet.vectorGet:vectorGet·p0.50    sample           0.020           ms/op
 DataGet.vectorGet:vectorGet·p0.90    sample           0.022           ms/op
 DataGet.vectorGet:vectorGet·p0.95    sample           0.031           ms/op
 DataGet.vectorGet:vectorGet·p0.99    sample           0.055           ms/op
 DataGet.vectorGet:vectorGet·p0.999   sample           0.085           ms/op
 DataGet.vectorGet:vectorGet·p0.9999  sample           0.145           ms/op
 DataGet.vectorGet:vectorGet·p1.00    sample           1.217           ms/op
 */

