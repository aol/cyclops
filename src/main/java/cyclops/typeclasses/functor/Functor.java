package cyclops.typeclasses.functor;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.State;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;

/**
 * Functor type class, performs a transformation operation over the supplied data structure
 * 
 * @author johnmcclean
 *
 * @param <CRE>
 */
@FunctionalInterface
public interface Functor<CRE> {
    /**
     * Transform the supplied data structure using the supplied transformation function
     * 
     * <pre>
     * {@code 
     *  ListX<Integer> listx = ListX.of(1,2,3);
        ListType<Integer> mapped1 =Lists.functor().transform(a->a+1, ListType.widen(listx));
        mapped1.add(1);
        ListX<Integer> listxMapped = mapped1.list();
     * }
     * </pre>
     * 
     * @param fn Transformation function
     * @param ds Datastructure to applyHKT
     * @return
     */
    public <T,R> Higher<CRE,R> map(Function<? super T, ? extends R> fn, Higher<CRE, T> ds);

    
    default <T,R> Higher<CRE,R> map_(Higher<CRE, T> ds, Function<? super T, ? extends R> fn){
        return map(fn,ds);
    }


    default <T> Higher<CRE,T> peek(Consumer<? super T> fn, Higher<CRE, T> ds){
        return map(t->{
            fn.accept(t);
            return t;
        },ds);
    }
    
    default <T, R> Function<Higher<CRE, T>, Higher<CRE, R>> lift(final Function<? super T, ? extends R> fn) {
        return t -> map(fn, t);
    }


 
}
