package cyclops.typeclasses.monad;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Constant;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.constant;

import java.util.function.Function;

import static cyclops.control.Constant.Instances.applicative;


public interface Traverse<CRE> extends Applicative<CRE>{
    
   <C2,T,R> Higher<C2, Higher<CRE, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                 Higher<CRE, T> ds);
   
    <C2,T> Higher<C2, Higher<CRE, T>> sequenceA(Applicative<C2> applicative,
                                                Higher<CRE, Higher<C2, T>> ds);

    default  <C2, T, R> Higher<C2, Higher<CRE, R>> flatTraverse(Applicative<C2> applicative, Monad<CRE> monad, Higher<CRE, T> fa,
                                                              Function<? super T,? extends Higher<C2, Higher<CRE, R>>>f) {
       return applicative.map_(traverseA(applicative,f,fa), it->monad.flatten(it));
    }

    default <C2, T> Higher<C2, Higher<CRE, T>> flatSequence(Applicative<C2> applicative, Monad<CRE> monad,Higher<CRE,Higher<C2,Higher<CRE,T>>> fgfa) {
        return applicative.map(i -> monad.flatMap(Function.identity(), i), sequenceA(applicative, fgfa));
    }

    default <T, R> R foldMap(Monoid<R> mb, final Function<? super T,? extends R> fn, Higher<CRE, T> ds) {
        return Constant.narrowK(traverseA(applicative(mb), a -> Constant.of(fn.apply(a)), ds)).get();
    }
}
