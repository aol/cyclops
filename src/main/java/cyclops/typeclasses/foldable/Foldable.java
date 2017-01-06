package cyclops.typeclasses.foldable;

import cyclops.function.Monoid;
import com.aol.cyclops2.hkt.Higher;

import java.util.function.BinaryOperator;


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
     * @param monoid Monoid to combine values
     * @param ds DataStructure to foldRight
     * @return Reduced value
     */
    public <T> T foldRight(Monoid<T> monoid, Higher<CRE, T> ds);
    
    /**
     * Starting from the right combine each value in turn with an accumulator
     * 
     * @param identity Identity value &amp; default
     * @param semigroup Combining function
     * @param ds DataStructure to foldRight
     * @return reduced value
     */
    default <T>  T foldRight(T identity, BinaryOperator<T> semigroup, Higher<CRE, T> ds){
        return foldLeft(Monoid.fromBiFunction(identity, semigroup),ds);
    }
    /**
     * Starting from the left combine each value in turn with an accumulator
     * 
     * @param monoid  Monoid to combine values
     * @param ds DataStructure to foldLeft
     * @return Reduced value
     */
    public <T> T foldLeft(Monoid<T> monoid, Higher<CRE, T> ds);
    
    /**
     * Starting from the left combine each value in turn with an accumulator
     * 
     * @param identity Identity value &amp; default
     * @param semigroup Combining function
     * @param ds DataStructure to foldLeft
     * @return Reduced value
     */
    default <T>  T foldLeft(T identity, BinaryOperator<T> semigroup, Higher<CRE, T> ds){
        return foldLeft(Monoid.fromBiFunction(identity, semigroup),ds);
    }
}
