package cyclops.function;

import cyclops.typeclasses.Cokleisli;
import cyclops.typeclasses.Kleisli;
import cyclops.typeclasses.functions.GroupK;
import cyclops.typeclasses.functions.MonoidK;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;


public interface Group<T> extends Monoid<T> {

    T invert(T t);

    public static <T> Group<T> fromCurried(UnaryOperator<T> inverse,final T zero, final Function<T, Function<T, T>> combiner){
        return of(inverse,Monoid.of(zero,combiner));

    }
    public static <T> Group<T> fromBiFunction(UnaryOperator<T> inverse,final T zero, final BiFunction<T, T, T> combiner) {
        return of(inverse,Monoid.fromBiFunction(zero,combiner));

    }

    default <W,R> GroupK<W,R> toGroupK(Kleisli<W,T,R> widen,Cokleisli<W,R,T> narrow){
        return  GroupK.of(t->widen.apply(invert(narrow.apply(t))),toMonoidK(widen, narrow));
    }
     public static <T> Group<T> of(UnaryOperator<T> inverse,Monoid<T> monoid){
         return new Group<T>() {


             @Override
             public T invert(T t) {
                 return inverse.apply(t);
             }



             @Override
             public T zero() {
                 return monoid.zero();
             }

             @Override
             public T apply(T t, T u) {
                 return monoid.apply(t,u);
             }
         };
     }
}
