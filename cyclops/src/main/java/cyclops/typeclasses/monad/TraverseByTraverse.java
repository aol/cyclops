package cyclops.typeclasses.monad;

import com.oath.cyclops.hkt.Higher;

import java.util.function.Function;



public interface TraverseByTraverse<CRE> extends Traverse<CRE> {
    <C2,T,R> Higher<C2, Higher<CRE, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn,
                                                  Higher<CRE, T> ds);

    default <C2,T> Higher<C2, Higher<CRE, T>> sequenceA(Applicative<C2> applicative, Higher<CRE, Higher<C2, T>> ds){
        return traverseA(applicative,Function.identity(),ds);
    }
}
