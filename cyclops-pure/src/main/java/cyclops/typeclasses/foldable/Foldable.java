package cyclops.typeclasses.foldable;

import cyclops.control.Option;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.companion.Monoids;
import cyclops.function.Monoid;
import com.oath.cyclops.hkt.Higher;
import cyclops.function.Ordering;
import cyclops.reactive.ReactiveSeq;
import cyclops.arrow.MonoidK;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;


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
        return foldRight(Monoid.fromBiFunction(identity, semigroup),ds);
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

    <T, R> R foldMap(final Monoid<R> mb, final Function<? super T,? extends R> fn, Higher<CRE, T> nestedA);

    default <T, R> R foldr(final Function< T, Function< R, R>> fn, R b, Higher<CRE, T> ds) {

        return foldMap(Monoids.functionComposition(), fn, ds).apply(b);
    }

    default <C2,T,R> Higher<C2,T> foldK(MonoidK<C2> monoid, Higher<CRE,Higher<C2,T>> ds) {
        return foldLeft(monoid.asMonoid(), ds);
    }
    

    default <T> long size(Higher<CRE, T> ds) {
        return foldMap(Monoids.longSum, __ -> 1l, ds);
    }
    default  <T> Seq<T> seq(Higher<CRE, T> ds){
        return foldMap(Monoids.seqConcat(), t->Seq.of(t),ds);
    }
    default  <T> LazySeq<T> lazySeq(Higher<CRE, T> ds){
        return foldMap(Monoids.lazySeqConcat(), t->LazySeq.of(t),ds);
    }
    default  <T> ReactiveSeq<T> stream(Higher<CRE, T> ds){
        return lazySeq(ds).stream();
    }

    default <T> T intercalate(Monoid<T> monoid, T value, Higher<CRE, T> ds ){
        return seq(ds).intersperse(value).foldLeft(monoid);
    }

    default <T> Option<T> getAt(Higher<CRE, T> ds, int index){
        return seq(ds).get(index);
    }

    default<T> boolean anyMatch(Predicate<? super T> pred, Higher<CRE, T> ds){
        return foldMap(Monoids.booleanDisjunction,i->pred.test(i),ds);
    }
    default<T> boolean allMatch(Predicate<? super T> pred, Higher<CRE, T> ds){
        return foldMap(Monoids.booleanConjunction,i->pred.test(i),ds);
    }

}
