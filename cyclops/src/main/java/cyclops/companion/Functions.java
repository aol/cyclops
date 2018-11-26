package cyclops.companion;

import com.oath.cyclops.internal.stream.spliterators.doubles.ReversingDoubleArraySpliterator;
import com.oath.cyclops.internal.stream.spliterators.ints.ReversingIntArraySpliterator;
import com.oath.cyclops.internal.stream.spliterators.longs.ReversingLongArraySpliterator;
import com.oath.cyclops.types.factory.Unit;
import cyclops.control.Option;
import cyclops.control.Maybe;
import cyclops.function.*;

import cyclops.reactive.ReactiveSeq;


import java.util.*;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Collection of useful arrow
 * Also see {@link Semigroups}
 *          {@link Monoids}
 *          {@link cyclops.function.Predicates}
 *          {@link cyclops.function.Curry}
 *          {@link cyclops.function.CurryVariance}
 *          {@link cyclops.function.CurryConsumer}
 *          {@link cyclops.function.PartialApplicator}
 *          {@link cyclops.function.Memoize}
 *          {@link cyclops.function.FluentFunctions}
 *          {@link Function1}
 *          {@link Function2}
 *          {@link Function3}
 *          {@link Function4}
 */
public class Functions {

    public static final  <T,R> Function1<? super T,? extends R> constant(R r){
        return t->r;
    }

    public static final  <T> Function1<? super T,? extends T> identity(){
        return t->t;
    }

    public static final  <T> Function1<? super T,? extends Maybe<? extends T>> lifted(){
        return t-> Maybe.ofNullable(t);
    }



    /**
     * Use an existing instance of a type that implements Unit to create a KleisliM arrow for that type
     *
     * <pre>
     *     {@code
     *      Seq<Integer> myList = Seq.of(1,2,3);
            Fn1<? super String, ? extends Seq<String>> arrow = Functions.arrowUnit(myList);

            Seq<String> list = arrow.applyHKT("hello world");
     *
     *     }
     * </pre>
     *
     * @param w
     * @param <T>
     * @param <W>
     * @return
     */
    public static final  <T,W extends Unit<T>> Function1<? super T,? extends W> arrowUnit(Unit<?> w){

        return t-> (W)w.unit(t);
    }



    public static final  <T> Function1<? super Iterable<T>,? extends T> head(){
        return it -> ReactiveSeq.fromIterable(it).firstValue(null);
    }

    public static final  <T> Function1<? super Iterable<T>,? extends T> tail(){
        return it -> ReactiveSeq.fromIterable(it)
                                .takeRight(1)
                                .firstValue(null);
    }
    public static final  <T> Function1<? super Iterable<T>,? extends T> reduce(Monoid<T> monoid){
        return it -> ReactiveSeq.fromIterable(it)
                                .reduce(monoid.zero(),monoid);
    }

    static <K,V> Function1<K,V> map(Map<K,V> map) {
        return map::get;
    }
    static <K,V> Function1<K,Maybe<V>> maybeMap(Map<K,V> map) {
        return k->Maybe.ofNullable(map.get(k));
    }
    static <K,V> Function1<K,Option<V>> optionalMap(Map<K,V> map) {
        return k-> Option.ofNullable(map.get(k));
    }

    static <T,R,R1, R2, R3, R4> Function<T,R4> forEach4(Function<? super T, ? extends R> fn,
                                                        Function<? super R, Function<? super T,? extends R1>> value2,
                                                        BiFunction<? super R, ? super R1, Function<? super T,? extends R2>> value3,
                                                        Function3<? super R, ? super R1, ? super R2, Function<? super T,? extends R3>> value4,
                                                        Function4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {

      Function1<T,R> fn1 = Function1.narrow(Function1.of(fn));
      return fn1.flatMapFn(in -> {

        Function1<T,R1> a = Function1.narrow(Function1.of(value2.apply(in)));
        return a.flatMapFn(ina -> {
          Function1<T,R2> b = Function1.narrow(Function1.of(value3.apply(in,ina)));
          return b.flatMapFn(inb -> {

            Function1<T,R3> c = Function1.narrow(Function1.of(value4.apply(in,ina,inb)));

            return c.mapFn(in2 -> {

              return yieldingFunction.apply(in, ina, inb, in2);


            });

          });


        });


      });




    }

    static <T,R,R1, R2, R4> Function<T,R4> forEach3(Function<? super T, ? extends R> fn,
                                                  Function<? super R, Function<? super T,? extends R1>> value2,
                                                  BiFunction<? super R, ? super R1, Function<? super T,? extends R2>> value3,
                                                  Function3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

      Function1<T,R> fn1 = Function1.narrow(Function1.of(fn));
      return fn1.flatMapFn(in -> {

        Function1<T,R1> a = Function1.narrow(Function1.of(value2.apply(in)));
        return a.flatMapFn(ina -> {
          Function1<T,R2> b = Function1.narrow(Function1.of(value3.apply(in,ina)));
          return b.mapFn(in2 -> {
            return yieldingFunction.apply(in, ina, in2);

          });



        });

      });
    }



    static <T,R,R1, R4> Function<T,R4> forEach2(Function<? super T, ? extends R> fn,
                                                Function<? super R, Function<? super T,? extends R1>> value2,
                                                 BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

      Function1<T,R> fn1 = Function1.narrow(Function1.of(fn));
      return fn1.flatMapFn(in -> {

        Function1<T,R1> a = Function1.narrow(Function1.of(value2.apply(in)));
        return a.mapFn(in2 -> {
          return yieldingFunction.apply(in, in2);

        });




      });



    }
    public static <T, R> Function<T,R> narrow(Function<? super T, ? extends R> fn) {
        return  (Function<T,R>)fn;
    }


    /**
     * @param values ints to populate Stream from
     * @return ReactiveSeq of multiple Integers
     */
    public static ReactiveSeq<Integer> ofInts(int... values){
        return ReactiveSeq.fromSpliterator(new ReversingIntArraySpliterator<>(values,0,values.length,false));

    }
    /*
     * Fluent limit operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.limitInts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .to(limitInts(1));
     *
     *   //[1]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> limitInts(long maxSize){

        return a->a.ints(i->i,s->s.limit(maxSize));
    }
    /*
     * Fluent limit operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.skipInts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .to(limitInts(1));
     *
     *   //[1]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> skipInts(long skip){

        return a->a.ints(i->i,s->s.skip(skip));
    }
    /*
     * Fluent transform operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.mapInts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .to(mapInts(i->i*2));
     *
     *   //[2,4,6]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> mapInts(IntUnaryOperator b){

        return a->a.ints(i->i,s->s.map(b));
    }
    /*
     * Fluent filter operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.filterInts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .to(filterInts(i->i>2));
     *
     *   //[3]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> filterInts(IntPredicate b){

        return a->a.ints(i->i,s->s.filter(b));
    }
    /*
     * Fluent flatMap operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.concatMapnts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .to(concatMapnts(i->IntStream.of(i*2)));
     *
     *   //[2,4,6]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> concatMapnts(IntFunction<? extends IntStream> b){

        return a->a.ints(i->i,s->s.flatMap(b));
    }
    /*
     * Fluent integer concat operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.concatInts;
     *
     *    ReactiveSeq.ofInts(1,2,3)
     *               .to(concatInts(ReactiveSeq.range(5,10)));
     *
     *   //[1,2,3,5,6,7,8,9]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Integer>, ? extends ReactiveSeq<Integer>> concatInts( ReactiveSeq<Integer> b){
        return a->ReactiveSeq.fromSpliterator(IntStream.concat(a.mapToInt(i->i),b.mapToInt(i->i)).spliterator());
    }




    /*
     * Fluent limit operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.limitLongs;
     *
     *    ReactiveSeq.ofLongs(1,2,3)
     *               .to(limitLongs(1));
     *
     *   //[1]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> limitLongs(long maxSize){

        return a->a.longs(i->i,s->s.limit(maxSize));
    }
    /*
     * Fluent limit operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.skipLongs;
     *
     *    ReactiveSeq.ofLongs(1,2,3)
     *               .to(limitLongs(1));
     *
     *   //[1l]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> skipLongs(long skip){

        return a->a.longs(i->i,s->s.skip(skip));
    }
    /*
     * Fluent transform operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.mapLongs;
     *
     *    ReactiveSeq.ofLongs(1l,2l,3l)
     *               .to(mapLongs(i->i*2));
     *
     *   //[2l,4l,6l]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> mapLongs(LongUnaryOperator b){

        return a->a.longs(i->i,s->s.map(b));
    }
    /*
     * Fluent filter operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.filterInts;
     *
     *    ReactiveSeq.ofLongs(1l,2l,3l)
     *               .to(filterLongs(i->i>2));
     *
     *   //[3l]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> filterLongs(LongPredicate b){

        return a->a.longs(i->i,s->s.filter(b));
    }
    /*
     * Fluent flatMap operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.flatMapLongs;
     *
     *    ReactiveSeq.ofLongs(1,2,3)
     *               .to(flatMapLongs(i->LongStream.of(i*2)));
     *
     *   //[2l,4l,6l]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> flatMapLongs(LongFunction<? extends LongStream> b){

        return a->a.longs(i->i,s->s.flatMap(b));
    }
    /*
     * Fluent integer concat operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.concatLongs;
     *
     *    ReactiveSeq.ofLongs(1l,2l,3l)
     *               .to(concatLongs(ReactiveSeq.ofLongs(5,10)));
     *
     *   //[1l,2l,3l,5l,6l,7l,8l,9l]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Long>, ? extends ReactiveSeq<Long>> concatLongs( ReactiveSeq<Long> b){
        return a->ReactiveSeq.fromSpliterator(LongStream.concat(a.mapToLong(i->i),b.mapToLong(i->i)).spliterator());
    }

    /**
     *
     * @param values longs to populate Stream from
     * @return ReactiveSeq of multiple Longs
     */
    public static ReactiveSeq<Double> ofDoubles(double... values){
        return ReactiveSeq.fromSpliterator(new ReversingDoubleArraySpliterator<>(values,0,values.length,false));
    }

    /*
     * Fluent limit operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.limitDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .to(limitDoubles(1));
     *
     *   //[1]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> limitDouble(long maxSize){

        return a->a.doubles(i->i,s->s.limit(maxSize));
    }
    /*
     * Fluent limit operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.skipDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .to(limitDoubles(1));
     *
     *   //[1d]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> skipDoubles(long skip){

        return a->a.doubles(i->i,s->s.skip(skip));
    }
    /*
     * Fluent transform operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.mapDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .to(mapDoubles(i->i*2));
     *
     *   //[2d,4d,6d]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> mapDoubles(DoubleUnaryOperator b){

        return a->a.doubles(i->i,s->s.map(b));
    }
    /*
     * Fluent filter operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.filterDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .to(filterDoubles(i->i>2));
     *
     *   //[3d]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> filterLongs(DoublePredicate b){

        return a->a.doubles(i->i,s->s.filter(b));
    }
    /*
     * Fluent flatMap operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.flatMapDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .to(flatMapDoubles(i->DoubleStream.of(i*2)));
     *
     *   //[2d,4d,6d]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> flatMapDoubles(DoubleFunction<? extends DoubleStream> b){

        return a->a.doubles(i->i,s->s.flatMap(b));
    }
    /*
     * Fluent integer concat operation using primitive types
     * e.g.
     * <pre>
     *  {@code
     *    import static cyclops.ReactiveSeq.concatDoubles;
     *
     *    ReactiveSeq.ofDoubles(1d,2d,3d)
     *               .to(concatDoubles(ReactiveSeq.ofDoubles(5,6,7,8,9)));
     *
     *   //[1d,2d,3d,5d,6d,7d,8d,9d]
     *  }
     *  </pre>
     *
     */
    public static Function<? super ReactiveSeq<Double>, ? extends ReactiveSeq<Double>> concatDoubles( ReactiveSeq<Double> b){

        return a->ReactiveSeq.fromSpliterator(DoubleStream.concat(a.mapToDouble(i->i),b.mapToDouble(i->i)).spliterator());
    }

}
