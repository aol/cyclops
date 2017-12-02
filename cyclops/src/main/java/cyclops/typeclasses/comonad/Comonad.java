package cyclops.typeclasses.comonad;



import com.oath.cyclops.hkt.Higher;

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
public interface Comonad<CRE>  {

    /**
     * Nest a value inside a value  (e.g. {@code List<List<Integer>> })
     *
     * @param ds Value to nest
     * @return Nested value
     */
    <T> Higher<CRE,Higher<CRE,T>> nest(Higher<CRE, T> ds);

    /**
     * Contra-variant flatMap
     * Transform the supplied data structure with the supplied transformation function
     * Datastructure is provided to the function which returns a single value
     *
     * @param mapper Transformation function
     * @param ds Datastructure
     * @return Coflatmapped result
     */
    <T,R> Higher<CRE,R> coflatMap(final Function<? super Higher<CRE, T>, R> mapper, Higher<CRE, T> ds);

    /**
     * Extract value embedded in datastructure
     *
     * @param ds Datatructure to extract value from
     * @return Value
     */
     <T> T extract(Higher<CRE, T> ds);

    default <T, R> Higher<CRE, R> inject(R t,Higher<CRE, T> ds) {
        return coflatMap(h->t,ds);
    }

    default <T, R> Function<Higher<CRE, T>, R> liftComonad(Function<? super T,? extends R> fn) {
        return higher -> fn.apply(extract(higher));
    }

}
