package cyclops.typeclasses.monad;

import com.aol.cyclops2.hkt.Higher;

import java.util.function.Function;



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

}
