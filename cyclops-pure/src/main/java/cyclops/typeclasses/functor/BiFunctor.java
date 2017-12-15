package cyclops.typeclasses.functor;

import com.oath.cyclops.hkt.Higher2;

import java.util.function.Function;


public interface BiFunctor<W> {

    <T, R, T2, R2> Higher2<W, R, R2> bimap(Function<? super T, ? extends R> fn, Function<? super T2, ? extends R2> fn2, Higher2<W, T, T2> ds);
}
