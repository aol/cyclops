package cyclops.reactiveSeq;

import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Combinations {

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
 public void streamableCombos(){
  Streamable.of("1", "2", "3", "4")
          .combinations(2).map(s -> s.join(",")).join();
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
 public void reactiveSeqCombos() {
  ReactiveSeq.of("1", "2", "3", "4")
          .combinations(2).map(s -> s.join(",")).join();
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
 public void iteratorCombos() {
     final int size = 2;
     Object[] a = ReactiveSeq.of( "1","2","3","4").toArray();
     final int fromIndex = 0;
     final int toIndex = a.length;

     final Iterator<List<String>> iter = new Iterator<List<String>>() {
         private final int[] indices = IntStream.range(fromIndex, fromIndex + size).toArray();

         @Override
         public boolean hasNext() {
             return indices[0] <= toIndex - size;
         }

         @Override
         public List<String> next() {
             final List<String> result = new ArrayList<>(size);

             for (int idx : indices) {
                 result.add((String)a[idx]);
             }

             if (++indices[size - 1] == toIndex) {
                 for (int i = size - 1; i > 0; i--) {
                     if (indices[i] > toIndex - (size - i)) {
                         indices[i - 1]++;

                         for (int j = i; j < size; j++) {
                             indices[j] = indices[j - 1] + 1;
                         }
                     }
                 }
             }

             return result;
         }
     };

     StringBuilder b = new StringBuilder();
     iter.forEachRemaining(s->b.append(s).append(","));
 }
}