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
public class DataAppend {

    Vector<String> vector;
    List<String> list;
    io.vavr.collection.Vector<String> js;
    ImmutableList<String> guava;
    @Setup(Level.Iteration)
    public void before(){
        vector = Vector.range(0,1000).map(i->""+i);;
        list = Stream.range(0,1000).map(i->""+i).toJavaList();
        js = io.vavr.collection.Vector.range(0,1000).map(i->""+i);
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
        for(int i=0;i<1000;i++){
            vector = vector.plus(""+i);
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
        for(int i=0;i<1000;i++){
            js= js.append(""+i);
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
        for(int i=0;i<1000;i++){
            list.add(""+i);
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
        for(int i=0;i<1000;i++){
            guava = ImmutableList.<String>builder().addAll(guava).add(""+i).build();
        }
    }
}


/**
 Benchmark                                       Mode     Cnt     Score   Error  Units
 DataAppend.guavaAppend                        sample     210    49.027 ± 6.040  ms/op
 DataAppend.guavaAppend:guavaAppend·p0.00      sample             6.169          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.50      sample            50.201          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.90      sample            86.481          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.95      sample            90.446          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.99      sample            95.610          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.999     sample            95.814          ms/op
 DataAppend.guavaAppend:guavaAppend·p0.9999    sample            95.814          ms/op
 DataAppend.guavaAppend:guavaAppend·p1.00      sample            95.814          ms/op
 DataAppend.jsAppend                           sample   40041     0.253 ± 0.019  ms/op
 DataAppend.jsAppend:jsAppend·p0.00            sample             0.165          ms/op
 DataAppend.jsAppend:jsAppend·p0.50            sample             0.222          ms/op
 DataAppend.jsAppend:jsAppend·p0.90            sample             0.262          ms/op
 DataAppend.jsAppend:jsAppend·p0.95            sample             0.298          ms/op
 DataAppend.jsAppend:jsAppend·p0.99            sample             0.362          ms/op
 DataAppend.jsAppend:jsAppend·p0.999           sample             0.514          ms/op
 DataAppend.jsAppend:jsAppend·p0.9999          sample            64.676          ms/op
 DataAppend.jsAppend:jsAppend·p1.00            sample            73.531          ms/op
 DataAppend.listAppend                         sample  112243     0.187 ± 0.208  ms/op
 DataAppend.listAppend:listAppend·p0.00        sample             0.012          ms/op
 DataAppend.listAppend:listAppend·p0.50        sample             0.013          ms/op
 DataAppend.listAppend:listAppend·p0.90        sample             0.015          ms/op
 DataAppend.listAppend:listAppend·p0.95        sample             0.022          ms/op
 DataAppend.listAppend:listAppend·p0.99        sample             0.038          ms/op
 DataAppend.listAppend:listAppend·p0.999       sample             0.360          ms/op
 DataAppend.listAppend:listAppend·p0.9999      sample           428.763          ms/op
 DataAppend.listAppend:listAppend·p1.00        sample          4949.279          ms/op
 DataAppend.vectorAppend                       sample  115949     0.106 ± 0.058  ms/op
 DataAppend.vectorAppend:vectorAppend·p0.00    sample             0.037          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.50    sample             0.041          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.90    sample             0.046          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.95    sample             0.060          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.99    sample             0.090          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.999   sample             0.145          ms/op
 DataAppend.vectorAppend:vectorAppend·p0.9999  sample           147.982          ms/op
 DataAppend.vectorAppend:vectorAppend·p1.00    sample          1249.903          ms/op
 **/
