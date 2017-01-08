package cyclops.reactiveSeq;

import cyclops.stream.ReactiveSeq;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Map5 {

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

 public void streamMap(Blackhole bh){
  for(int k=0;k<100;k++) {
   bh.consume(Stream.of(1, 2, 3)
           .map(i -> i * 2)
           .map(i -> i * 2)
           .map(i -> i * 2)
           .map(i -> i * 2)
           .map(i -> i * 2)
           .collect(Collectors.toList()));
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
 public void reactiveSeqMap(Blackhole bh) {
  for(int k=0;k<100;k++) {
   bh.consume(ReactiveSeq.of(1, 2, 3)
           .map(i -> i * 2)
           .map(i -> i * 2)
           .map(i -> i * 2)
           .map(i -> i * 2)
           .map(i -> i * 2)
           .collect(Collectors.toList()));
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
 public void reactiveSeqMapPrebuilt(Blackhole bh) {
  ReactiveSeq<Integer> stream = ReactiveSeq.of(1,2,3)
          .map(i -> i * 2)
          .map(i -> i * 2)
          .map(i -> i * 2)
          .map(i -> i * 2)
          .map(i -> i * 2);
  for(int i=0;i<100;i++) {
      bh.consume(stream
              .collect(Collectors.toList()));
  }
 }

}