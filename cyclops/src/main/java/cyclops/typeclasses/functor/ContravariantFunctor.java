package cyclops.typeclasses.functor;

import com.oath.cyclops.hkt.Higher;

import java.util.function.Function;


public interface ContravariantFunctor<W>{

    <T, R> Higher<W,R> contramap(Function<? super R, ? extends T> fn, Higher<W,T> ds);
}

