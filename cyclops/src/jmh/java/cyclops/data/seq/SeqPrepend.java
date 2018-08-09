package cyclops.data.seq;

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
public class SeqPrepend {

    Seq<Integer> seq;
    io.vavr.collection.List<Integer> js;

    @Setup(Level.Iteration)
    public void before() {
        seq = Seq.range(0, 10000);
        js = io.vavr.collection.List.range(0, 10000);


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
        for(int i=0;i<1000;i++)
            seq = seq.prepend(i);

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
        for(int i=0;i<1000;i++)
            js =js.prepend(i);

    }



}
/**
 Benchmark                                   Mode     Cnt  Score    Error  Units
 SeqPrepend.cyclopsOps                     sample  204419  0.025 ±  0.001  ms/op
 SeqPrepend.cyclopsOps:cyclopsOps·p0.00    sample          0.020           ms/op
 SeqPrepend.cyclopsOps:cyclopsOps·p0.50    sample          0.023           ms/op
 SeqPrepend.cyclopsOps:cyclopsOps·p0.90    sample          0.025           ms/op
 SeqPrepend.cyclopsOps:cyclopsOps·p0.95    sample          0.036           ms/op
 SeqPrepend.cyclopsOps:cyclopsOps·p0.99    sample          0.059           ms/op
 SeqPrepend.cyclopsOps:cyclopsOps·p0.999   sample          0.102           ms/op
 SeqPrepend.cyclopsOps:cyclopsOps·p0.9999  sample          1.191           ms/op
 SeqPrepend.cyclopsOps:cyclopsOps·p1.00    sample          1.839           ms/op
 SeqPrepend.vavrOps                        sample  203457  0.025 ±  0.001  ms/op
 SeqPrepend.vavrOps:vavrOps·p0.00          sample          0.020           ms/op
 SeqPrepend.vavrOps:vavrOps·p0.50          sample          0.023           ms/op
 SeqPrepend.vavrOps:vavrOps·p0.90          sample          0.025           ms/op
 SeqPrepend.vavrOps:vavrOps·p0.95          sample          0.035           ms/op
 SeqPrepend.vavrOps:vavrOps·p0.99          sample          0.059           ms/op
 SeqPrepend.vavrOps:vavrOps·p0.999         sample          0.105           ms/op
 SeqPrepend.vavrOps:vavrOps·p0.9999        sample          1.104           ms/op
 SeqPrepend.vavrOps:vavrOps·p1.00          sample          2.724           ms/op
 **/
