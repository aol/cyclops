package cyclops.reactiveSeq;

import cyclops.reactive.ReactiveSeq;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class FlatMapLarge {

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

 public void streamFlatMap(Blackhole bh){

   Stream.iterate(1,i->i+1)
           .limit(10000)
           .flatMap(i -> Stream.of(i * 2,i*2,i*2,i*2))
           .forEach(bh::consume);

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
 public void reactiveSeqFlatMap(Blackhole bh) {

   ReactiveSeq.iterate(1,i->i+1)
           .limit(10000)
           .flatMap(i -> Stream.of(i * 2,i*2,i*2,i*2))
           .forEach(bh::consume);

 }



}