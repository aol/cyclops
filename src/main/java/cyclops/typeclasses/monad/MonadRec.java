package cyclops.typeclasses.monad;


import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Eval;
import cyclops.control.Xor;

import java.util.function.Function;

// ref http://functorial.com/stack-safety-for-free/index.pdf Stack Safety for Free

public interface MonadRec<W> {

     <T, R> Higher<W, R> tailRec(T initial,Function<? super T,? extends Higher<W, ? extends Xor<T, R>>> fn);
}
