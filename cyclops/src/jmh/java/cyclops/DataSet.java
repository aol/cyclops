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
import java.util.stream.Collectors;


@State(Scope.Benchmark)
public class DataSet {


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
    public void jsVectorSet(){
        for(int i=0;i<10000;i++){
            js = js.update(i,""+i);
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
            vector =  vector.updateAt(i,""+i);
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
            list.set(i,""+i);
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
        for(int i=0;i<10000;i++) {
            guava = com.google.common.collect.ImmutableList.<String>builder().addAll(guava.subList(0,i)).add(""+i).addAll(guava.subList(i+1,guava.size())).build();

        }
    }
}
/**
 Benchmark                                  Mode    Cnt    Score   Error  Units
 DataSet.guavaSet                         sample     30  403.649 ± 7.542  ms/op
 DataSet.guavaSet:guavaSet·p0.00          sample         384.827          ms/op
 DataSet.guavaSet:guavaSet·p0.50          sample         403.702          ms/op
 DataSet.guavaSet:guavaSet·p0.90          sample         421.318          ms/op
 DataSet.guavaSet:guavaSet·p0.95          sample         423.756          ms/op
 DataSet.guavaSet:guavaSet·p0.99          sample         425.198          ms/op
 DataSet.guavaSet:guavaSet·p0.999         sample         425.198          ms/op
 DataSet.guavaSet:guavaSet·p0.9999        sample         425.198          ms/op
 DataSet.guavaSet:guavaSet·p1.00          sample         425.198          ms/op
 DataSet.jsVectorSet                      sample  16864    0.594 ± 0.003  ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.00    sample           0.494          ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.50    sample           0.537          ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.90    sample           0.696          ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.95    sample           0.758          ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.99    sample           0.962          ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.999   sample           2.294          ms/op
 DataSet.jsVectorSet:jsVectorSet·p0.9999  sample           3.020          ms/op
 DataSet.jsVectorSet:jsVectorSet·p1.00    sample           3.129          ms/op
 DataSet.listSet                          sample  72219    0.139 ± 0.001  ms/op
 DataSet.listSet:listSet·p0.00            sample           0.121          ms/op
 DataSet.listSet:listSet·p0.50            sample           0.130          ms/op
 DataSet.listSet:listSet·p0.90            sample           0.147          ms/op
 DataSet.listSet:listSet·p0.95            sample           0.182          ms/op
 DataSet.listSet:listSet·p0.99            sample           0.246          ms/op
 DataSet.listSet:listSet·p0.999           sample           0.367          ms/op
 DataSet.listSet:listSet·p0.9999          sample           2.847          ms/op
 DataSet.listSet:listSet·p1.00            sample           3.269          ms/op
 DataSet.vectorSet                        sample  19946    0.503 ± 0.003  ms/op
 DataSet.vectorSet:vectorSet·p0.00        sample           0.422          ms/op
 DataSet.vectorSet:vectorSet·p0.50        sample           0.461          ms/op
 DataSet.vectorSet:vectorSet·p0.90        sample           0.615          ms/op
 DataSet.vectorSet:vectorSet·p0.95        sample           0.658          ms/op
 DataSet.vectorSet:vectorSet·p0.99        sample           0.815          ms/op
 DataSet.vectorSet:vectorSet·p0.999       sample           2.582          ms/op
 DataSet.vectorSet:vectorSet·p0.9999      sample           3.750          ms/op
 DataSet.vectorSet:vectorSet·p1.00        sample           4.899          ms/op

 */

