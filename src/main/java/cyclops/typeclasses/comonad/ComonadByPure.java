package cyclops.typeclasses.comonad;



import com.aol.cyclops2.hkt.Higher;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.functor.Functor;

import java.util.function.Function;

/**
 * Contra-variant Monad type class
 * 
 * Nest values (contra-variant to flatten)
 * Extract values (contra-varaiant to of) 
 * coFlatMap - a transformation that accepts a comand and returns a value
 * 
 * @author johnmcclean
 *
 * @param <CRE> Witness type of Kind to process
 */
public interface ComonadByPure<CRE> extends Comonad<CRE>,Pure<CRE>, Functor<CRE> {

    /**
     * Nest a value inside a value  (e.g. {@code List<List<Integer>> })
     * 
     * @param ds Value to nest
     * @return Nested value
     */
    default <T> Higher<CRE,Higher<CRE,T>> nest(Higher<CRE, T> ds){
        return map(i->unit(i),ds);
    }
    
    /**
     * Contra-variant flatMap
     * Transform the supplied data structure with the supplied transformation function
     * Datastructure is provided to the function which returns a single value
     * 
     * @param mapper Transformation function
     * @param ds Datastructure
     * @return Coflatmapped result
     */
    default <T,R> Higher<CRE,R> coflatMap(final Function<? super Higher<CRE, T>, R> mapper, Higher<CRE, T> ds) {
        return mapper.andThen(r -> unit(r))
                     .apply(ds);
    }
    
    /**
     * Extract value embedded in datastructure
     * 
     * @param ds Datatructure to extract value from
     * @return Value
     */
    public <T> T extract(Higher<CRE, T> ds);
}
