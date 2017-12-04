package cyclops.reactiveSeq;

import cyclops.reactive.ReactiveSeq;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class FlatMapForEach {

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
  for(int k=0;k<100;k++) {
   Stream.of(1, 2, 3)
           .flatMap(i -> Stream.of(i * 2,i*2,i*2,i*2))
           .forEach(bh::consume);
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
 public void reactiveSeqFlatMap(Blackhole bh) {
  for(int k=0;k<100;k++) {
   ReactiveSeq.of(1, 2, 3)
           .flatMap(i -> Stream.of(i * 2,i*2,i*2,i*2))
           .forEach(bh::consume);
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
 public void reactiveSeqFlatMapPrebuilt(Blackhole bh) {
  ReactiveSeq<Integer> stream = ReactiveSeq.of(1,2,3)
          .flatMap(i -> Stream.of(i * 2,i*2,i*2,i*2));
  for(int i=0;i<100;i++) {
       stream.forEach(bh::consume);

  }
 }

}