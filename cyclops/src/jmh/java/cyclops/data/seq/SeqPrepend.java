package cyclops.data.seq;

import cyclops.data.Seq;
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
public class SeqPrepend {

    Seq<Integer> seq;
    io.vavr.collection.List<Integer> js;

    @Setup
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
        for(int i=0;i<10000;i++)
            seq.prepend(i);

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
        for(int i=0;i<10000;i++)
            js.prepend(i);

    }



}
