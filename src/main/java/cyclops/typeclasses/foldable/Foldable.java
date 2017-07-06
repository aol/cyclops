package cyclops.typeclasses.foldable;

import cyclops.companion.Monoids;
import cyclops.function.Monoid;
import com.aol.cyclops2.hkt.Higher;

import java.util.function.BinaryOperator;
import java.util.function.Function;


/**
 * Type class for foldables
 * 
 * @author johnmcclean
 *
 * @param <CRE> The core type of the foldable (e.g. the HKT witness type, not the generic type : ListType.µ)
 */
public interface Foldable<CRE> {

    /**
     * Starting from the right combine each value in turn with an accumulator
     * 
     * @param monoid Monoid toNested combine values
     * @param ds DataStructure toNested foldRight
     * @return Reduced value
     */
    public <T> T foldRight(Monoid<T> monoid, Higher<CRE, T> ds);
    
    /**
     * Starting from the right combine each value in turn with an accumulator
     * 
     * @param identity Identity value &amp; default
     * @param semigroup Combining function
     * @param ds DataStructure toNested foldRight
     * @return reduced value
     */
    default <T>  T foldRight(T identity, BinaryOperator<T> semigroup, Higher<CRE, T> ds){
        return foldRight(Monoid.fromBiFunction(identity, semigroup),ds);
    }
    /**
     * Starting from the left combine each value in turn with an accumulator
     * 
     * @param monoid  Monoid toNested combine values
     * @param ds DataStructure toNested foldLeft
     * @return Reduced value
     */
    public <T> T foldLeft(Monoid<T> monoid, Higher<CRE, T> ds);
    
    /**
     * Starting from the left combine each value in turn with an accumulator
     * 
     * @param identity Identity value &amp; default
     * @param semigroup Combining function
     * @param ds DataStructure toNested foldLeft
     * @return Reduced value
     */
    default <T>  T foldLeft(T identity, BinaryOperator<T> semigroup, Higher<CRE, T> ds){
        return foldLeft(Monoid.fromBiFunction(identity, semigroup),ds);
    }

    default <T, R> R foldMap(final Monoid<R> mb, final Function<? super T,? extends R> fn, Higher<CRE, T> nestedA) {
        return foldr( (T a) -> (R b) -> mb.apply(fn.apply(a), b), mb.zero(), nestedA);
    }

    default <T, R> R foldr(final Function< T, Function< R, R>> fn, R b, Higher<CRE, T> as) {

        return foldMap(Monoids.functionComposition(), fn, as).apply(b);
    }

}
