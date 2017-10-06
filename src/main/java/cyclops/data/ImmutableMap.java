package cyclops.data;


import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.foldable.Folds;
import com.aol.cyclops2.types.functor.BiTransformable;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.types.recoverable.OnEmpty;
import com.aol.cyclops2.types.recoverable.OnEmptySwitch;
import com.aol.cyclops2.util.ExceptionSoftener;
import cyclops.collections.immutable.PersistentMapX;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;

import java.util.concurrent.TimeUnit;
import java.util.function.*;

public interface ImmutableMap<K,V> extends Iterable<Tuple2<K,V>>,
        Folds<Tuple2<K,V>>,
        Filters<Tuple2<K,V>>,
        Transformable<V>,
        BiTransformable<K, V>,
        OnEmpty<Tuple2<K, V>>,
        OnEmptySwitch<Tuple2<K, V>,ImmutableMap<K, V>> {

    PersistentMapX<K,V> persistentMapX();
    ImmutableMap<K,V> put(K key, V value);
    ImmutableMap<K,V> put(Tuple2<K, V> keyAndValue);
    ImmutableMap<K,V> putAll(ImmutableMap<K, V> map);

    ImmutableMap<K,V> remove(K key);
    ImmutableMap<K,V> removeAll(K... keys);


    boolean containsKey(K key);

    boolean contains(Tuple2<K, V> t);
    Maybe<V> get(K key);
    V getOrElse(K key, V alt);
    V getOrElseGet(K key, Supplier<V> alt);

    int size();

    <K2,V2> DMap.Two<K,V,K2,V2> merge(ImmutableMap<K2, V2> one);
    <K2,V2,K3,V3> DMap.Three<K,V,K2,V2,K3,V3> merge(DMap.Two<K2, V2, K3, V3> two);

    ReactiveSeq<Tuple2<K,V>> stream();

    <R> ImmutableMap<K,R> mapValues(Function<? super V, ? extends R> map);
    <R> ImmutableMap<R,V> mapKeys(Function<? super K, ? extends R> map);
    <R1,R2> ImmutableMap<R1,R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> map);

    <K2, V2> ImmutableMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper);
    <K2, V2> ImmutableMap<K2, V2> flatMapI(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper);

    ImmutableMap<K,V> filter(Predicate<? super Tuple2<K, V>> predicate);


    ImmutableMap<K,V> filterKeys(Predicate<? super K> predicate);
    ImmutableMap<K,V> filterValues(Predicate<? super V> predicate);

    @Override
    default <U> ImmutableMap<K,U> cast(Class<? extends U> type) {
        return (HashMap<K,U>)Transformable.super.cast(type);
    }
    @Override
    default ImmutableMap<K,V> filterNot(Predicate<? super Tuple2<K, V>> predicate){
        return (HashMap<K,V>)Filters.super.filterNot(predicate);
    }
    @Override
    default ImmutableMap<K,V> notNull(){
        return (HashMap<K,V>)Filters.super.notNull();
    }

    @Override
    <R> ImmutableMap<K,R> map(Function<? super V, ? extends R> fn);

    @Override
    default ImmutableMap<K,V> peek(Consumer<? super V> c) {
        return (HashMap<K,V>)Transformable.super.peek(c);
    }

    @Override
    default <R> ImmutableMap<K,R> trampoline(Function<? super V, ? extends Trampoline<? extends R>> mapper) {
        return (ImmutableMap<K,R>)Transformable.super.trampoline(mapper);
    }

    @Override
    default <R> ImmutableMap<K,R> retry(Function<? super V, ? extends R> fn) {
        return (ImmutableMap<K,R>)Transformable.super.retry(fn);
    }

    @Override
    default <R> ImmutableMap<K,R> retry(Function<? super V, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (ImmutableMap<K,R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    <R1, R2> ImmutableMap<R1, R2> bimap(Function<? super K, ? extends R1> fn1, Function<? super V, ? extends R2> fn2);

    @Override
    default ImmutableMap<K, V> bipeek(Consumer<? super K> c1, Consumer<? super V> c2) {
        return (ImmutableMap<K,V>)BiTransformable.super.bipeek(c1,c2);
    }

    @Override
    default <U1, U2> ImmutableMap<U1, U2> bicast(Class<U1> type1, Class<U2> type2) {
        return (ImmutableMap<U1,U2>)BiTransformable.super.bicast(type1,type2);
    }

    @Override
    default <R1, R2> ImmutableMap<R1, R2> bitrampoline(Function<? super K, ? extends Trampoline<? extends R1>> mapper1, Function<? super V, ? extends Trampoline<? extends R2>> mapper2) {
        return (ImmutableMap<R1,R2>)BiTransformable.super.bitrampoline(mapper1,mapper2);
    }

    @Override
    default ImmutableMap<K, V> onEmpty(Tuple2<K, V> value){
        if(size()==0){
            return put(value);
        }
        return this;
    }

    @Override
    default ImmutableMap<K, V> onEmptyGet(Supplier<? extends Tuple2<K, V>> supplier){
        return onEmpty(supplier.get());
    }

    @Override
    default <X extends Throwable> ImmutableMap<K, V> onEmptyThrow(Supplier<? extends X> supplier){
        if(size()==0)
            throw ExceptionSoftener.throwSoftenedException(supplier.get());
        return this;
    }

    @Override
    default ImmutableMap<K, V> onEmptySwitch(Supplier<? extends ImmutableMap<K, V>> supplier){
        if(size()==0){
            return supplier.get();
        }
        return this;
    }

    default <K1,K2,K3,K4,R1, R2, R3, R> ImmutableMap<K4,R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
                                                                    BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
                                                                    Fn3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3,
                                                                    Fn4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction) {

        return this.flatMapI((a1,b1) -> {
            Tuple2<K, V> in = Tuple.tuple(a1, b1);
            ReactiveSeq<Tuple2<K1,R1>> a = ReactiveSeq.fromIterable(iterable1.apply(in));
            return a.flatMap(ina -> {
                ReactiveSeq<Tuple2<K2,R2>> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.flatMap(inb -> {
                    ReactiveSeq<Tuple2<K3,R3>> c = ReactiveSeq.fromIterable(iterable3.apply(in, ina, inb));
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    default <K1,K2,K3,K4,R1, R2, R3, R> ImmutableMap<K4,R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
                                                                    BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
                                                                    Fn3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3,
                                                                    Fn4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, Boolean> filterFunction,
                                                                    Fn4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction) {

        return this.flatMapI((a1,b1) -> {
            Tuple2<K, V> in = Tuple.tuple(a1, b1);
            ReactiveSeq<Tuple2<K1,R1>> a = ReactiveSeq.fromIterable(iterable1.apply(in));
            return a.flatMap(ina -> {
                ReactiveSeq<Tuple2<K2,R2>> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.flatMap(inb -> {
                    ReactiveSeq<Tuple2<K3,R3>> c = ReactiveSeq.fromIterable(iterable3.apply(in, ina, inb));
                    return c.filter(in2 -> filterFunction.apply(in, ina, inb, in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    default <K1,K2,K3,R1, R2, R> ImmutableMap<K3,R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
                                                             BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
                                                             Fn3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction) {

        return this.flatMapI((a1,b1) -> {
            Tuple2<K, V> in = Tuple.tuple(a1, b1);

            Iterable<Tuple2<K1,R1>> a = iterable1.apply(in);
            return ReactiveSeq.fromIterable(a)
                    .flatMap(ina -> {
                        ReactiveSeq<Tuple2<K2,R2>> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                        return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
                    });

        });
    }


    default <K1,K2,K3,R1, R2, R> ImmutableMap<K3,R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
                                                             BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
                                                             Fn3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, Boolean> filterFunction,
                                                             Fn3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction) {

        return this.flatMapI((a1,b1) -> {
            Tuple2<K, V> in = Tuple.tuple(a1, b1);
            Iterable<Tuple2<K1,R1>> a = iterable1.apply(in);
            return ReactiveSeq.fromIterable(a)
                    .flatMap(ina -> {
                        ReactiveSeq<Tuple2<K2,R2>> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                        return b.filter(in2 -> filterFunction.apply(in, ina, in2))
                                .map(in2 -> yieldingFunction.apply(in, ina, in2));
                    });

        });
    }


    default <K1,K2,R1, R> ImmutableMap<K2,R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
                                                      BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction) {

        return this.flatMapI((a1,b1) -> {
            Tuple2<K, V> in = Tuple.tuple(a1, b1);
            Iterable<Tuple2<K1,R1>> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }


    default <K1,K2,R1, R> ImmutableMap<K2,R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
                                                      BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, Boolean> filterFunction,
                                                      BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction) {

        return this.flatMapI((a1,b1) -> {
            Tuple2<K, V> in = Tuple.tuple(a1, b1);
            Iterable<? extends Tuple2<K1,R1>> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                    .filter(in2-> filterFunction.apply(in,in2))
                    .map(in2->yieldingFunction.apply(in, in2));
        });
    }
}
