package cyclops.control;


import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.matching.Deconstruct;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple1;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;

import com.oath.cyclops.hkt.DataWitness.constant;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functions.SemigroupK;
import cyclops.typeclasses.monad.Applicative;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <T> Value type
 * @param <P> Phantom type
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constant<T,P> implements Higher2<constant,T,P> , Supplier<T>, Deconstruct.Deconstruct1<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private final T value;


    public <R> Constant<T,R> map(Function<? super P, ? extends R> fn){
        return of(value);
    }

    public static <T,P> Constant<T,P> of(T value){
        return new Constant<>(value);
    }

    public T get(){
        return value;
    }

    public static <T,P> Constant<T,P> narrowK2(Higher2<constant,T,P> constant){
        return (Constant<T,P>) constant;
    }
    public static <T,P> Constant<T,P> narrowK(Higher<Higher<constant,T>,P> constant){
        return (Constant<T,P>) constant;
    }
    public static <T,P> SemigroupK<Higher<constant,T>> semigroupK(Semigroup<T> monoid){
      return new SemigroupK<Higher<constant, T>>() {
        @Override
        public <T2> Higher<Higher<constant, T>, T2> apply(Higher<Higher<constant, T>, T2> t1, Higher<Higher<constant, T>, T2> t2) {
          return Constant.of(monoid.apply(narrowK(t1).value, narrowK(t2).value));
        }
      };

    }
    public static <T,P> MonoidK<Higher<constant,T>> monoidK(Monoid<T> monoid){
       return new MonoidK<Higher<constant, T>>() {
         @Override
         public <T2> Higher<Higher<constant, T>, T2> zero() {
           return Constant.of(monoid.zero());
         }

         @Override
         public <T2> Higher<Higher<constant, T>, T2> apply(Higher<Higher<constant, T>, T2> t1, Higher<Higher<constant, T>, T2> t2) {
           return Constant.of(monoid.apply(narrowK(t1).value, narrowK(t2).value));
         }
       };
    }

    @Override
    public Tuple1<T> unapply() {
        return Tuple.tuple(value);
    }

    public static class Instances{
        public static <T1,P> Applicative<Higher<constant,T1>> applicative(Monoid<T1> m){
            return new Applicative<Higher<constant,T1>>(){


                @Override
                public <T, R> Higher<Higher<constant, T1>, R> ap(Higher<Higher<constant, T1>, ? extends Function<T, R>> fn, Higher<Higher<constant, T1>, T> apply) {
                    return of(m.apply(narrowK(fn).value,narrowK(apply).value));
                }

                @Override
                public <T, R> Higher<Higher<constant, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<constant, T1>, T> ds) {
                    return narrowK(ds).map(fn);
                }

                @Override
                public <T> Higher<Higher<constant, T1>, T> unit(T value) {
                    return Constant.of(m.zero());

                }
            };
        }
    }

}
