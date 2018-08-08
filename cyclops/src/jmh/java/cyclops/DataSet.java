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
public class DataSet {


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
    public void jsVectorSet(){
        for(int i=0;i<10000;i++){
            js.update(i,i);
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
    public void vectorSet(){
        for(int i=0;i<10000;i++){
            vector.set(i,i);
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
    public void listSet(){
        for(int i=0;i<10000;i++){
            list.set(i,i);
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
    public void guavaSet(){
        for(int i=0;i<10000;i++){
            ImmutableList.builder().addAll(guava.subList(0,i)).addAll(guava.subList(i+1,guava.size())).build();
        }
    }
}
/**
 Benchmark                                  Mode     Cnt    Score    Error  Units
 DataSet.guavaSet                         sample      30  439.930 ± 26.522  ms/op
 DataSet.guavaSet:guavaSet·p0.00          sample          397.410           ms/op
 DataSet.guavaSet:guavaSet·p0.50          sample          427.819           ms/op
 DataSet.guavaSet:guavaSet·p0.90          sample          523.187           ms/op
 DataSet.guavaSet:guavaSet·p0.95          sample          543.267           ms/op
 DataSet.guavaSet:guavaSet·p0.99          sample          553.648           ms/op
 DataSet.guavaSet:guavaSet·p0.999         sample          553.648           ms/op
 DataSet.guavaSet:guavaSet·p0.9999        sample          553.648           ms/op
 DataSet.guavaSet:guavaSet·p1.00          sample          553.648           ms/op
 DataSet.jsVectorSet                      sample   19584    0.511 ±  0.003  ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.00    sample            0.387           ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.50    sample            0.466           ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.90    sample            0.663           ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.95    sample            0.724           ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.99    sample            0.878           ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.999   sample            1.992           ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.9999  sample            4.436           ms/op
 DataSet.jsVectorSet:jsVectorSet·p1.00    sample            4.538           ms/op
 DataSet.listSet                          sample  153856    0.033 ±  0.001  ms/op
 DataSet.listSet:listSet·p0.00            sample            0.028           ms/op
 DataSet.listSet:listSet·p0.50            sample            0.031           ms/op
 DataSet.listSet:listSet·p0.90            sample            0.033           ms/op
 DataSet.listSet:listSet·p0.95            sample            0.046           ms/op
 DataSet.listSet:listSet·p0.99            sample            0.072           ms/op
 DataSet.listSet:listSet·p0.999           sample            0.117           ms/op
 DataSet.listSet:listSet·p0.9999          sample            1.769           ms/op
 DataSet.listSet:listSet·p1.00            sample            2.388           ms/op
 DataSet.vectorSet                        sample   14971    0.669 ±  0.004  ms/op
 DataSet.vectorSet:vectorSet·p0.00        sample            0.555           ms/op
 DataSet.vectorSet:vectorSet·p0.50        sample            0.605           ms/op
 DataSet.vectorSet:vectorSet·p0.90        sample            0.805           ms/op
 DataSet.vectorSet:vectorSet·p0.95        sample            0.871           ms/op
 DataSet.vectorSet:vectorSet·p0.99        sample            1.104           ms/op
 DataSet.vectorSet:vectorSet·p0.999       sample            2.274           ms/op
 DataSet.vectorSet:vectorSet·p0.9999      sample            3.061           ms/op
 DataSet.vectorSet:vectorSet·p1.00        sample            3.330           ms/op

 */

