package cyclops.companion;

import com.oath.cyclops.types.factory.Unit;
import cyclops.control.Option;
import cyclops.control.Maybe;
import cyclops.function.*;

import cyclops.reactive.ReactiveSeq;


import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

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
     *      ListX<Integer> myList = ListX.of(1,2,3);
            Fn1<? super String, ? extends ListX<String>> arrow = Functions.arrowUnit(myList);

            ListX<String> list = arrow.applyHKT("hello world");
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
                                .limitLast(1)
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
}
