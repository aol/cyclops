package cyclops.typeclasses.functor;

import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops2.hkt.Higher;

import cyclops.typeclasses.monad.Monad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
/**
 * Compose two functors so operations are applied to the inner functor
 * 
 * e.g.  given an Optional containing a List we can compose the functor for Optionals and the functor for List
 * to transform the values inside the List
 * 
 * <pre>
 * {@code 
 *    OptionalType<ListX<Integer>> nested;
 *    
 *    Compose.compose(Optionals.functor(),Lists.functor())
 *           .map(i->i*2,nested);
 *   
 * }
 * </pre>
 * 
 * @author johnmcclean
 *
 * @param <CRE>
 * @param <C2>
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class Compose<CRE,C2>{
    Functor<CRE> f;
    Functor<C2> g;


    /**
     * Compose two functors
     * 
     * @param f First functor to compose
     * @param g Second functor to compose
     * @return Composed functor
     */
    public static <CRE,C2> Compose<CRE,C2> compose(Functor<CRE> f,Functor<C2> g){
        return new Compose<>(f,g);
    }
    
    /**
     * Transformation operation 
     * 
     * @param fn Transformation function
     * @param ds Datastructure to transform
     * @return Transformed data structure
     */
    public <T,R> Higher<CRE,Higher<C2,R>> map(Function<? super T,? extends R> fn, Higher<CRE,Higher<C2,T>> ds){
       return f.map(h->g.map(fn,h) ,ds);
    }
    public <T> Higher<CRE,Higher<C2,T>> peek(Consumer<? super T> fn, Higher<CRE,Higher<C2,T>> ds){
        return map(t->{
            fn.accept(t);
            return t;
        },ds);
    }
    public <T,R> Higher<CRE,R> map1(Function<? super T,? extends R> fn, Higher<CRE,T> ds){
        return f.map(fn ,ds);
    }

}
