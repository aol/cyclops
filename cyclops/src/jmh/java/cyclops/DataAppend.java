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
public class DataAppend {

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
    public void vectorAppend(){
        for(int i=0;i<10000;i++){
            vector.plus(i);
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
    public void jsAppend(){
        for(int i=0;i<10000;i++){
            js.append(i);
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
    public void listAppend(){
        for(int i=0;i<10000;i++){
            list.add(i);
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
    public void guavaAppend(){
        for(int i=0;i<10000;i++){
            ImmutableList.builder().addAll(guava).add(i).build();
        }
    }
}

//Data.listGet:listGet·p0.999       sample           0.013           ms/op
//Data.vectorGet:vectorGet·p0.999   sample           0.019           ms/op

/**
 Benchmark                                       Mode    Cnt     Score   Error  Units
 DataAppend.guavaAppend                        sample     30   366.477 ± 6.416  ms/op
 DataAppend.guavaAppend:guavaAppend·p0.00      sample          348.652          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.50      sample          366.477          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.90      sample          382.154          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.95      sample          383.202          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.99      sample          383.779          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.999     sample          383.779          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.9999    sample          383.779          ms/op
 DataAppend.guavaAppend:guavaAppend·p1.00      sample          383.779          ms/op
 DataAppend.jsAppend                           sample   8419     1.189 ± 0.006  ms/op
 DataAppend.jsAppend:jsAppend·p0.00            sample            1.027          ms/op
 DataAppend.jsAppend:jsAppend·p0.50            sample            1.135          ms/op
 DataAppend.jsAppend:jsAppend·p0.90            sample            1.319          ms/op
 DataAppend.jsAppend:jsAppend·p0.95            sample            1.452          ms/op
 DataAppend.jsAppend:jsAppend·p0.99            sample            2.146          ms/op
 DataAppend.jsAppend:jsAppend·p0.999           sample            2.652          ms/op
 DataAppend.jsAppend:jsAppend·p0.9999          sample            3.650          ms/op
 DataAppend.jsAppend:jsAppend·p1.00            sample            3.650          ms/op
 DataAppend.listAppend                         sample   2070     0.949 ± 3.001  ms/op
 DataAppend.listAppend:listAppend·p0.00        sample            0.034          ms/op
 DataAppend.listAppend:listAppend·p0.50        sample            0.037          ms/op
 DataAppend.listAppend:listAppend·p0.90        sample            0.039          ms/op
 DataAppend.listAppend:listAppend·p0.95        sample            0.054          ms/op
 DataAppend.listAppend:listAppend·p0.99        sample            0.080          ms/op
 DataAppend.listAppend:listAppend·p0.999       sample            0.124          ms/op
 DataAppend.listAppend:listAppend·p0.9999      sample         1885.340          ms/op
 DataAppend.listAppend:listAppend·p1.00        sample         1885.340          ms/op
 DataAppend.vectorAppend                       sample  21350     0.470 ± 0.002  ms/op
 DataAppend.vectorAppend:vectorAppend·p0.00    sample            0.403          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.50    sample            0.439          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.90    sample            0.545          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.95    sample            0.600          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.99    sample            0.766          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.999   sample            1.700          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.9999  sample            2.371          ms/op
 DataAppend.vectorAppend:vectorAppend·p1.00    sample            3.875          ms/op
 **/
