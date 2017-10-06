package com.aol.cyclops2.types.foldable;

import cyclops.collections.mutable.ListX;

import cyclops.function.Semigroup;
import cyclops.stream.ReactiveSeq;
import org.jooq.lambda.Seq;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public interface StatCollectors<T> {

    ReactiveSeq<T> stream();
    default <R1, R2, A1, A2> Tuple2<R1, R2> collect(Collector<? super T, A1, R1> c1, Collector<? super T, A2, R2> c2) {
        return stream().collect(Collector.of(() -> Tuple.tuple(c1.supplier().get(),c2.supplier().get()),
                (t2, next) -> {
                    c1.accumulator().accept(t2._1(), next);
                    c2.accumulator().accept(t2._2(), next);
                },(t2, t2b) -> Tuple.tuple(c1.combiner().apply(t2._1(), t2b._1()),c2.combiner().apply(t2._2(), t2b._2())),
                t2 -> Tuple.tuple(c1.finisher().apply(t2._1()),c2.finisher().apply(t2._2()))));
    }
    default <R1, R2, R3, A1, A2, A3> Tuple3<R1, R2, R3> collect(Collector<? super T, A1, R1> c1, Collector<? super T, A2, R2> c2, Collector<? super T, A3, R3> c3) {
        return stream().collect(Collector.of(() -> Tuple.tuple(c1.supplier().get(),c2.supplier().get(),c3.supplier().get()),
                (t3, next) -> {
                    c1.accumulator().accept(t3._1(), next);
                    c2.accumulator().accept(t3._2(), next);
                    c3.accumulator().accept(t3._3(), next);
                },(t3, t3b) -> Tuple.tuple(c1.combiner().apply(t3._1(), t3b._1()),c2.combiner().apply(t3._2(), t3b._2()),c3.combiner().apply(t3._3(), t3b._3())),
                t3 -> Tuple.tuple(c1.finisher().apply(t3._1()),c2.finisher().apply(t3._2()),c3.finisher().apply(t3._3()))));
    }
    default long count(){
        return stream().reduce(0l,(a,b)->a+1);
    }
    default long countDistinct(){
        return stream().distinct().count();

    }
    default <U> Optional<T> maxBy(Function<? super T, ? extends U> function,Comparator<? super U> comparator){
        return stream().sorted(function,comparator)
                .get(0l)
                .toOptional();
    }
    default <U extends Comparable<? super U>> Optional<T> maxBy(Function<? super T, ? extends U> function){
        return stream().sorted(function,Comparator.reverseOrder())
                        .get(0l)
                        .toOptional();
    }
    default <U extends Comparable<? super U>> Optional<T> minBy(Function<? super T, ? extends U> function){
        return stream().sorted(function, Comparator.reverseOrder())
                .get(0l)
                .toOptional();
    }
    default <U extends Comparable<? super U>> Optional<T> minBy(Function<? super T, ? extends U> function,Comparator<? super U> comparator){
        return stream().sorted(function, Comparator.reverseOrder())
                        .takeRight(1)
                        .findFirst();
    }
    default Optional<T> mode(){
        Map<T,Integer> map = stream().collect(Collectors.toMap(k->k,v->1,(a,b)->a+b));

        return ReactiveSeq.fromIterable(map.entrySet())
                          .stats()
                         .maxBy(k -> k.getValue())
                        .map(t -> t.getKey());
    }
    default ReactiveSeq<Tuple2<T,Integer>> occurances(){
        Map<T,Integer> map = stream().collect(Collectors.toMap(k->k,v->1,(a,b)->a+b));

        return ReactiveSeq.fromIterable(map.entrySet())
                         .map(e->Tuple.tuple(e.getKey(),e.getValue()));
    }

    default double mean(ToDoubleFunction<T> fn){
        return stream().collect(Collectors.<T>averagingDouble(fn));
    }
    default T median(){
        return atPercentile(50.0);
    }


    default ListX<Tuple2<T,BigDecimal>> withPercentiles(){

        ListX<T> list = stream().toListX();

        int precision = new Double(Math.log10(list.size())).intValue();


        return list.zipWithIndex().map(t -> t.map2(idx -> {
            double d = (idx / new Double(list.size()));
            return new BigDecimal((d*100),new MathContext(precision));
        }));

    }
    /*
        Value at percentile denoted by a double value between 0 and 100
        Assumes the data is already sorted
     */
    default T atPercentile(double percentile){
        List<T> list = stream().collect(Collectors.toList());
        Long pos = Math.round(((list.size()-1) * (percentile/100)));
        return list.get(pos.intValue());
    }


    default double variance(ToDoubleFunction<T> fn){
        ListX<T> list = stream().toListX();
        double avg = list.collect(Collectors.<T>averagingDouble(fn));
        return list.map(t -> fn.applyAsDouble(t))
                .map(t -> t - avg)
                .map(t -> t * t)
                .sum(i -> i)
                .map(total -> total/(list.size()-1))
                .get();

    }
    default double populationVariance(ToDoubleFunction<T> fn){
        ListX<T> list = stream().toListX();
        double avg = list.collect(Collectors.<T>averagingDouble(fn));
        return list.map(t -> fn.applyAsDouble(t))
                .map(t -> t - avg)
                .map(t -> t * t)
                .sum(i -> i)
                .map(total -> total/(list.size()))
                .get();

    }

    default double stdDeviation(ToDoubleFunction<T> fn){
        ListX<T> list = stream().toListX();
        double avg = list.collect(Collectors.<T>averagingDouble(fn));
       return Math.sqrt( list.mapToDouble(fn)
                             .map(i->i-avg)
                             .map(i->i*i)
                             .average()
                             .getAsDouble());
    }

    default LongSummaryStatistics longStats(ToLongFunction<T> fn){
        return stream().collect(Collectors.summarizingLong(fn));
    }
    default IntSummaryStatistics intStats(ToIntFunction<T> fn){
        return stream().collect(Collectors.summarizingInt(fn));
    }
    default DoubleSummaryStatistics doubleStats(ToDoubleFunction<T> fn){
        return stream().collect(Collectors.summarizingDouble(fn));
    }

}
