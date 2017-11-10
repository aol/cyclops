package cyclops.typeclasses.functor;

import com.oath.cyclops.hkt.Higher;

import java.util.function.Function;

public interface ProFunctor<CRE>{

    <A,B,C,D>   Higher<Higher<CRE,C>,D> dimap(Function<? super C,? extends A> f, Function<? super B,? extends D> g, Higher<Higher<CRE,A>,B> ds);

    default <A,B,C> Higher<Higher<CRE,A>,C> lmap(Function<? super A,? extends B> fn, Higher<Higher<CRE,B>,C> ds) {
        return dimap(fn, Function.identity(), ds);
    }

    default <A,B,C> Higher<Higher<CRE,A>,C> rmap(Function<? super B,? extends C> g, Higher<Higher<CRE,A>,B> ds) {
        return dimap(Function.identity(), g, ds);
    }


}
