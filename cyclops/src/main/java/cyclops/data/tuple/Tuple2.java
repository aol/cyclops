package cyclops.data.tuple;


import com.oath.cyclops.hkt.DataWitness.tuple2;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.types.foldable.EqualTo;
import com.oath.cyclops.types.foldable.OrderedBy;
import com.oath.cyclops.types.foldable.To;
import cyclops.control.Either;
import cyclops.data.Comparators;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/*
  A Tuple implementation that can be either eager / strict or lazy
 */
@AllArgsConstructor
public class Tuple2<T1,T2> implements To<Tuple2<T1,T2>>,
                                        Serializable,
                                        EqualTo<Higher<tuple2,T1>,T2,Tuple2<T1,T2>>,
                                        OrderedBy<Higher<tuple2,T1>,T2,Tuple2<T1,T2>>,
                                        Comparable<Tuple2<T1,T2>>,
                                        Higher2<tuple2,T1,T2> {

    private static final long serialVersionUID = 1L;

    public static <T1,T2> Tuple2<T1,T2> of(T1 value1, T2 value2) {
        return new Tuple2<T1,T2>(value1,value2);
    }
    public static <T1,T2> Tuple2<T1,T2> lazy(Supplier<? extends T1> supplier1,Supplier<? extends T2> supplier2) {
        return new Tuple2<T1,T2>(null,null) {
            @Override
            public T1 _1() {
                return supplier1.get();
            }
            @Override
            public T2 _2() {
                return supplier2.get();
            }
        };
    }
    private final T1 _1;
    private final T2 _2;

    public T1 _1(){
        return _1;
    }

    public  T2 _2(){
        return _2;
    }

    public Tuple1<T1> first(){
        return Tuple.tuple(_1());
    }

    public Tuple1<T2> second(){
        return Tuple.tuple(_2());
    }

    public Tuple2<T1,T2> eager(){
        return of(_1(),_2());
    }

    public Tuple2<T1,T2> memo(){
        Tuple2<T1,T2> host = this;
        return new Tuple2<T1,T2>(null,null){
            final Supplier<T1> memo1 = Memoize.memoizeSupplier(host::_1);
            final Supplier<T2> memo2 = Memoize.memoizeSupplier(host::_2);
            @Override
            public T1 _1() {

                return memo1.get();
            }

            @Override
            public T2 _2() {
                return memo2.get();
            }
        };
    }

    public <R> Tuple2<T1,R> flatMap(Monoid<T1> m,Function<? super T2, ? extends Tuple2<T1,R>> fn){
        return fn.apply(_2()).map1(t1->m.apply(t1,_1()));
    }

    public <R1> R1 transform(BiFunction<? super T1, ? super T2, ? extends R1> fn1){
        return fn1.apply(_1(),_2());
    }

    public <R1,R2> Tuple2<R1,R2> bimap(Function<? super T1, ? extends R1> fn1,Function<? super T2,? extends R2> fn2){
        return of((fn1.apply(_1())),fn2.apply(_2()));
    }

    public <R1,R2> Tuple2<R1,R2> lazyBimap(Function<? super T1, ? extends R1> fn1,Function<? super T2,? extends R2> fn2){
        return lazy(()->(fn1.apply(_1())),()->fn2.apply(_2()));
    }
    public <R> Tuple2<R, T2> map1(Function<? super T1, ? extends R> fn) {
        return of(fn.apply(_1()), _2());
    }
    public <R> Tuple2<R, T2> lazyMap1(Function<? super T1, ? extends R> fn) {
        return lazy(()->fn.apply(_1()),()-> _2());
    }
    public <R> Tuple2<T1, R> map2(Function<? super T2, ? extends R> fn) {
        return of(_1(), fn.apply(_2()));
    }
    public <R> Tuple2<T1, R> lazyMap2(Function<? super T2, ? extends R> fn) {
        return lazy(() -> _1(), () -> fn.apply(_2()));
    }


    public Tuple2<T2,T1> swap(){
        return of(_2(),_1());
    }


    public Tuple2<T2,T1> lazySwap(){
        return lazy(()->_2(),()->_1());
    }

    public <R> R visit(BiFunction<? super T1, ? super T2, ? extends R> fn){
        return fn.apply(_1(),_2());
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", _1(),_2());
    }



    public static <T1,T2> Tuple2<T1,T2> narrowK2(Higher2<tuple2,T1,T2> ds){
        return (Tuple2<T1,T2>)ds;
    }
    public static <T1,T2> Tuple2<T1,T2> narrowK(Higher<Higher<tuple2, T1>, T2> ds){
        return (Tuple2<T1,T2>)ds;
    }

    public static  <T1,T2,R> Tuple2<T1,R> tailRec(Monoid<T1> op,T2 initial, Function<? super T2, ? extends Tuple2<T1,? extends Either<T2, R>>> fn){
        Tuple2<T1,? extends Either<T2, R>> next[] = new Tuple2[1];
        next[0] = Tuple2.of(op.zero(), Either.left(initial));
        boolean cont = true;
        do {

            cont = next[0].visit((a,p) -> p.visit(s -> {
                next[0] = narrowK(fn.apply(s)).map1(t1->op.apply(next[0]._1(),t1));
                return true;
            }, __ -> false));
        } while (cont);
        return next[0].map2(x->x.orElse(null));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Tuple2)) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(_1(), tuple2._1()) &&
                Objects.equals(_2(), tuple2._2());
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1(), _2());
    }

    @Override
    public int compareTo(Tuple2<T1, T2> o) {
        int result = Comparators.naturalOrderIdentityComparator().compare(_1(),o._1());
        if(result==0){
            result = Comparators.naturalOrderIdentityComparator().compare(_2(),o._2());
        }
        return result;
    }
    public final Object[] toArray() {
        return new Object[] { _1(),_2() };
    }

    public static <K, V> Tuple2< K, V> narrow(Tuple2<? extends K, ? extends V> t) {
        return (Tuple2<K,V>)t;
    }
    public static <T1,T2> Higher2<tuple2,T1, T2> widen(Tuple2<T1,T2> narrow) {
    return narrow;
  }


}
