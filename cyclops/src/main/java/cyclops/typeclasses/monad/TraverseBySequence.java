package cyclops.typeclasses.monad;

import com.oath.cyclops.hkt.Higher;

import java.util.function.Function;



public interface TraverseBySequence<CRE> extends Traverse<CRE> {
    default <C2,T,R> Higher<C2, Higher<CRE, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                          Higher<CRE, T> ds){
        return sequenceA(applicative, map(fn, ds));
    }

    <C2,T> Higher<C2, Higher<CRE, T>> sequenceA(Applicative<C2> applicative, Higher<CRE, Higher<C2, T>> ds);
}
