package cyclops.function;

import cyclops.control.Option;
import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

public interface PartialFunction<T,R> {

  public static <T,R> PartialFunction<T,R> of(Function<? super T, ? extends Option<R>> fn){
    return in->Memoize.memoizeFunction(fn).apply(in);
  }

  public static <T,R> PartialFunction<T,R> unmemoized(Function<? super T, ? extends Option<R>> fn){
    return in->fn.apply(in);
  }

  default R apply(T t, Supplier<? extends R> s){
    return apply(t).orElseGet(s);
  }
  default R apply(T t, R alt){
    return apply(t).orElse(alt);
  }

  Option<R> apply(T t);

  default <V> PartialFunction<T,V> andThen(Function<? super R,? extends V> f2){
    return of((T t) -> apply(t).fold(r -> Option.some(f2.apply(r)), none -> Option.<V>none()));
  }


  default <V> PartialFunction<V,R> compose(Function<? super V,? extends T> f2){
   return of((V v) -> apply(f2.apply(v)));
  }
  default <V> PartialFunction<T,V> andThenUnmemoized(Function<? super R,? extends V> f2){
    return (T t) -> apply(t).fold(r -> Option.some(f2.apply(r)), none -> Option.<V>none());
  }


  default <V> PartialFunction<V,R> composeUnmemoized(Function<? super V,? extends T> f2){
    return (V v) -> apply(f2.apply(v));
  }

  default <R1> PartialFunction<T, R1> mapFn(final Function<? super R, ? extends R1> f2) {
    return andThen(f2);
  }

  default <R1> PartialFunction<T, R1> flatMapFn(final Function<? super R, ? extends Function<? super T, ? extends R1>> f) {
    return of(a -> apply(a).fold(r -> Option.some(f.apply(r).apply(a)), none -> Option.<R1>none()));
  }

  default Function<? super T,? extends Option<R>> asFunction(){
    return in->apply(in);
  }

  default Function<? super T, ? extends R> lifted(R alt){
    return in->apply(in,alt);
  }


  default Function<? super T, ? extends R> lifted(Supplier<? extends R> alt){
    return in->apply(in,alt);
  }

  default boolean isDefinedAt(T t){
    return apply(t).isPresent();
  }

}
