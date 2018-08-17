package cyclops.data.vector;

import cyclops.data.Vector;
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

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class VectorFlatMap {

    Vector<String> vector;
    io.vavr.collection.Vector<String> js;

    @Setup
    public void before() {
        vector = Vector.range(0, 1000).map(i->""+i);
        js = io.vavr.collection.Vector.range(0, 1000).map(i->""+i);

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
    public void cyclopsOps() {
        vector.concatMap(i -> Vector.range(0,100));

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
        js.flatMap(i -> Vector.range(0,100));

    }



}
/**
    Benchmark                                      Mode   Cnt   Score   Error  Units
    VectorFlatMap.cyclopsOps                     sample  1729   5.803 ± 0.033  ms/op
    VectorFlatMap.cyclopsOps:cyclopsOps·p0.00    sample         5.104          ms/op
    VectorFlatMap.cyclopsOps:cyclopsOps·p0.50    sample         5.669          ms/op
    VectorFlatMap.cyclopsOps:cyclopsOps·p0.90    sample         6.431          ms/op
    VectorFlatMap.cyclopsOps:cyclopsOps·p0.95    sample         6.640          ms/op
    VectorFlatMap.cyclopsOps:cyclopsOps·p0.99    sample         7.212          ms/op
    VectorFlatMap.cyclopsOps:cyclopsOps·p0.999   sample         8.570          ms/op
    VectorFlatMap.cyclopsOps:cyclopsOps·p0.9999  sample         8.749          ms/op
    VectorFlatMap.cyclopsOps:cyclopsOps·p1.00    sample         8.749          ms/op
    VectorFlatMap.vavrOps                        sample  1296   7.760 ± 0.059  ms/op
    VectorFlatMap.vavrOps:vavrOps·p0.00          sample         6.955          ms/op
    VectorFlatMap.vavrOps:vavrOps·p0.50          sample         7.619          ms/op
    VectorFlatMap.vavrOps:vavrOps·p0.90          sample         8.137          ms/op
    VectorFlatMap.vavrOps:vavrOps·p0.95          sample         8.440          ms/op
    VectorFlatMap.vavrOps:vavrOps·p0.99          sample        11.422          ms/op
    VectorFlatMap.vavrOps:vavrOps·p0.999         sample        14.860          ms/op
    VectorFlatMap.vavrOps:vavrOps·p0.9999        sample        15.352          ms/op
    VectorFlatMap.vavrOps:vavrOps·p1.00          sample        15.352          ms/op

    Process finished with exit code 0
**/
