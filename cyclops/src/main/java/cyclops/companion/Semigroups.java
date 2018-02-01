package cyclops.companion;

import com.oath.cyclops.types.Zippable;
import cyclops.control.Future;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Either;
import cyclops.data.*;
import cyclops.data.HashSet;
import cyclops.data.Seq;
import cyclops.data.TreeSet;
import cyclops.data.Vector;
import cyclops.function.Semigroup;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.NaturalTransformation;
import com.oath.cyclops.types.persistent.PersistentCollection;
import org.reactivestreams.Publisher;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * A static class with a large number of SemigroupK  or Combiners.
 *
 * A semigroup is an Object that can be used to combine objects of the same type.
 *
 * Using raw Semigroups with container types
 * <pre>
 *     {@code
 *       Semigroup<Maybe<Integer>> m = Semigroups.combineZippables(Semigroups.intMax);
 *       Semigroup<ReactiveSeq<Integer>> m = Semigroups.combineZippables(Semigroups.intSum);
 *     }
 * </pre>
 *
 *
 *  @author johnmcclean
 */
public interface Semigroups {



    static <T, C extends PersistentCollection<T>> Semigroup<C> persistentCollectionConcat() {

        return (C a, C b) -> (C)a.plusAll(b);
    }
    static <T, C extends ImmutableList<T>> Semigroup<C> immutableListConcat() {

        return (C a, C b) -> (C)a.appendAll(b);
    }

    static <T> Semigroup<LazySeq<T>> lazySeqConcat() {
        return Semigroups.immutableListConcat();
    }
    static <T> Semigroup<Seq<T>> seqConcat() {
      return Semigroups.immutableListConcat();
    }
    static <T> Semigroup<Vector<T>> vectorConcat() {
      return Semigroups.persistentCollectionConcat();
    }
    static <T> Semigroup<IntMap<T>> intMapConcat() {
      return Semigroups.persistentCollectionConcat();
    }
    static <T> Semigroup<HashSet<T>> hashSetConcat() {
      return Semigroups.persistentCollectionConcat();
    }
    static <T> Semigroup<HashSet<T>> trieSetConcat() {
      return Semigroups.persistentCollectionConcat();
    }
    static <T> Semigroup<TreeSet<T>> treeSetConcat() {
      return Semigroups.persistentCollectionConcat();
    }
    static <T> Semigroup<Bag<T>> bagConcat() {
      return Semigroups.persistentCollectionConcat();
    }
    static <T> Semigroup<BankersQueue<T>> bankersQueueConcat() {
      return Semigroups.persistentCollectionConcat();
    }
    static <T> Semigroup<LazyString> lazyStringConcat() {
      return Semigroups.persistentCollectionConcat();
    }


    /**
     * <pre>
     * {@code
     *  BinaryOperator<Seq<Integer>> sumInts = Semigroups.combineZippables(Semigroups.intSum);

        sumInts.apply(Seq.of(1,2,3), Seq.of(4,5,6));

        //List[5,7,9];
     *
     * }
     * </pre>
     *
     * @param semigroup Semigroup to combine the values inside the zippables
     * @return Combination of two Zippables
     */
    static <T,A extends Zippable<T>> Semigroup<A> combineZippables(BiFunction<T,T,T> semigroup) {
        return (a, b) -> (A) a.zip(b, semigroup);
    }
    /**
     *
     * <pre>
     * {@code
     *
     *  BinaryOperator<Maybe<Integer>> sumMaybes = Semigroups.combineScalarFunctors(Semigroups.intSum);
     *  Maybe.just(1)
     *       .combine(sumMaybes, Maybe.just(5))
     *
     *  //Maybe[6]
     * }
     * </pre>
     *
     *
     * @param semigroup Semigroup to combine the values inside the Scalar Functors (Maybe, Xor, Ior, Try, Eva, FeatureToggle etc)
     * @return Combination of two Scalar Functors
     */
    static <T,A extends Zippable<T>> Semigroup<A> combineScalarFunctors(BiFunction<T,T,T> semigroup) {
        return (a, b) -> (A) a.zip(b, semigroup);
    }


    /**
     * @return Combination of two ReactiveSeq Streams b is appended to a
     */
    static <T> Semigroup<ReactiveSeq<T>> combineReactiveSeq() {
        return (a, b) -> a.appendStream(b);
    }

    static <T> Semigroup<ReactiveSeq<T>> firstNonEmptyReactiveSeq() {
        return (a, b) -> a.onEmptySwitch(()->b);
    }
    static <T> Semigroup<ReactiveSeq<T>> ambReactiveSeq() {
        return (a,b)->(ReactiveSeq<T>)Semigroups.<T>amb().apply(a,b);
    }

    static <T> Semigroup<ReactiveSeq<T>> mergeLatestReactiveSeq() {
        return (a,b) -> Spouts.mergeLatest(a,b);
    }
    static <T> Semigroup<Publisher<T>> mergeLatest() {
        return (a,b) -> Spouts.mergeLatest(a,b);
    }
    static <T> Semigroup<Publisher<T>> amb() {
        return (a, b) -> Spouts.amb(a,b);

    }



    /**
     * @return Combination of two Stream's : b is appended to a
     */
    static <T> Semigroup<Stream<T>> combineStream() {
        return (a, b) -> Stream.concat(a, b);
    }

    /**
     * @return Combination of two Collection, first non-zero is returned
     */
    static <T,C extends Collection<T>> Semigroup<C> firstNonEmpty() {
        return (a, b) -> a.isEmpty() ? b: a;
    }
    /**
     * @return Combination of two Collection, last non-zero is returned
     */
    static <T,C extends Collection<T>> Semigroup<C> lastNonEmpty() {
        return (a, b) -> b.isEmpty() ? a: b;
    }

    /**
     * @return Combination of two Objects of same type, first non-null is returned
     */
    static <T> Semigroup<T> firstNonNull() {
        return (a, b) -> a != null ? a : b;
    }

    /**
     * @return Combine two CompletableFuture's by taking the first present
     */
    static <T> Semigroup<CompletableFuture<T>> firstCompleteCompletableFuture() {
        return (a, b) -> (CompletableFuture<T>)CompletableFuture.<T>anyOf(a,b);
    }
    /**
     * @return Combine two Future's by taking the first result
     */
    static <T> Semigroup<Future<T>> firstCompleteFuture() {
        return (a, b) -> Future.anyOf(a,b);
    }


    /**
     * @return Combine two Future's by taking the first successful
     */
    static <T> Semigroup<Future<T>> firstSuccessfulFuture() {
        return (a, b) -> Future.firstSuccess(a,b);
    }
    /**
     * @return Combine two Xor's by taking the first right
     */
    static <ST,PT> Semigroup<Either<ST,PT>> firstPrimaryXor() {
        return  (a, b) -> a.isRight() ? a : b;
    }
    /**
     * @return Combine two Xor's by taking the first left
     */
    static <ST,PT> Semigroup<Either<ST,PT>> firstSecondaryXor() {
        return  (a, b) -> a.isLeft() ? a : b;
    }
    /**
     * @return Combine two Xor's by taking the last right
     */
    static <ST,PT> Semigroup<Either<ST,PT>> lastPrimaryXor() {
        return  (a, b) -> b.isRight() ? b : a;
    }
    /**
     * @return Combine two Xor's by taking the last left
     */
    static <ST,PT> Semigroup<Either<ST,PT>> lastSecondaryXor() {
        return  (a, b) -> b.isLeft() ? b : a;
    }
    /**
     * @return Combine two Try's by taking the first right
     */
    static <T,X extends Throwable> Semigroup<Try<T,X>> firstTrySuccess() {
        return  (a, b) -> a.isSuccess() ? a : b;
    }
    /**
     * @return Combine two Try's by taking the first left
     */
    static <T,X extends Throwable> Semigroup<Try<T,X>> firstTryFailure() {
        return  (a, b) -> a.isFailure() ? a : b;
    }
    /**
     * @return Combine two Tryr's by taking the last right
     */
    static<T,X extends Throwable> Semigroup<Try<T,X>> lastTrySuccess() {
        return  (a, b) -> b.isSuccess() ? b : a;
    }
    /**
     * @return Combine two Try's by taking the last left
     */
    static <T,X extends Throwable> Semigroup<Try<T,X>>lastTryFailure() {
        return  (a, b) -> b.isFailure() ? b : a;
    }
    /**
     * @return Combine two Ior's by taking the first right
     */
    static <ST,PT> Semigroup<Ior<ST,PT>> firstPrimaryIor() {
        return  (a, b) -> a.isRight() ? a : b;
    }
    /**
     * @return Combine two Ior's by taking the first left
     */
    static <ST,PT> Semigroup<Ior<ST,PT>> firstSecondaryIor() {
        return  (a, b) -> a.isLeft() ? a : b;
    }
    /**
     * @return Combine two Ior's by taking the last right
     */
    static <ST,PT> Semigroup<Ior<ST,PT>> lastPrimaryIor() {
        return  (a, b) -> b.isRight() ? b : a;
    }
    /**
     * @return Combine two Ior's by taking the last left
     */
    static <ST,PT> Semigroup<Ior<ST,PT>> lastSecondaryIor() {
        return  (a, b) -> b.isLeft() ? b : a;
    }

    /**
     * @return Combine two Maybe's by taking the first present
     */
    static <T> Semigroup<Maybe<T>> firstPresentMaybe() {
        return (a, b) -> a.isPresent() ? a : b;
    }

    /**
     * @return Combine two optionals by taking the first present
     */
    static <T> Semigroup<Optional<T>> firstPresentOptional() {
        return (a, b) -> a.isPresent() ? a : b;
    }

    /**
     * @return Combine two Maybes by taking the last present
     */
    static <T> Semigroup<Maybe<T>> lastPresentMaybe() {
        return (a, b) -> b.isPresent() ? b : a;
    }

    /**
     * @return Combine two optionals by taking the last present
     */
    static <T> Semigroup<Optional<T>> lastPresentOptional() {
        return (a, b) -> b.isPresent() ? b : a;
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two strings separated by the supplied joiner
     */
    static Semigroup<String> stringJoin(final String joiner) {
        return (a, b) -> a + joiner + b;
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two StringBuilders separated by the supplied joiner
     */
    static Semigroup<StringBuilder> stringBuilderJoin(final String joiner) {
        return (a, b) -> a.append(joiner)
                          .append(b);
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two StringBuffers separated by the supplied joiner
     */
    static Semigroup<StringBuffer> stringBufferJoin(final String joiner) {
        return (a, b) -> a.append(joiner)
                          .append(b);
    }

    /**
     * @return Combine two Comparables taking the lowest each time
     */
    static <T, T2 extends Comparable<T>> Semigroup<T2> minComparable() {
        return (a, b) -> a.compareTo((T) b) > 0 ? b : a;
    }

    /**
     * @return Combine two Comparables taking the highest each time
     */
    static <T, T2 extends Comparable<T>> Semigroup<T2> maxComparable() {
        return (a, b) -> a.compareTo((T) b) > 0 ? a : b;
    }


    /**
     * Combine two Integers by summing them
     */
    static Semigroup<Integer> intSum = (a, b) -> a + b;
    /**
     * Combine two Longs by summing them
     */
    static Semigroup<Long> longSum = (a, b) -> a + b;
    /**
     * Combine two Doubles by summing them
     */
    static Semigroup<Double> doubleSum = (a, b) -> a + b;
    /**
     * Combine two BigIngegers by summing them
     */
    static Semigroup<BigInteger> bigIntSum = (a, b) -> a.add(b);
    /**
     * Combine two Integers by multiplying them
     */
    static Semigroup<Integer> intMult = (a, b) -> a * b;
    /**
     * Combine two Longs by multiplying them
     */
    static Semigroup<Long> longMult = (a, b) -> a * b;
    /**
     * Combine two Doubles by multiplying them
     */
    static Semigroup<Double> doubleMult = (a, b) -> a * b;
    /**
     * Combine two BigIntegers by multiplying them
     */
    static Semigroup<BigInteger> bigIntMult = (a, b) -> a.multiply(b);
    /**
     * Combine two Integers by selecting the max
     */
    static Semigroup<Integer> intMax = (a, b) -> b > a ? b : a;
    /**
     * Combine two Longs by selecting the max
     */
    static Semigroup<Long> longMax = (a, b) -> b > a ? b : a;
    /**
     * Combine two Doubles by selecting the max
     */
    static Semigroup<Double> doubleMax = (a, b) -> b > a ? b : a;
    /**
     * Combine two BigIntegers by selecting the max
     */
    static Semigroup<BigInteger> bigIntMax = (a, b) -> a.max(b);
    /**
     * Combine two Integers by selecting the min
     */
    static Semigroup<Integer> intMin = (a, b) -> a < b ? a : b;
    /**
     * Combine two Longs by selecting the min
     */
    static Semigroup<Long> longMin = (a, b) -> a < b ? a : b;
    /**
     * Combine two Doubles by selecting the min
     */
    static Semigroup<Double> doubleMin = (a, b) -> a < b ? a : b;
    /**
     * Combine two BigIntegers by selecting the min
     */
    static Semigroup<BigInteger> bigIntMin = (a, b) -> a.min(b);
    /**
     * String concatenation
     */
    static Semigroup<String> stringConcat = (a, b) -> a + b;
    /**
     * StringBuffer concatenation
     */
    static Semigroup<StringBuffer> stringBufferConcat = (a, b) -> a.append(b);
    /**
     * StringBuilder concatenation
     */
    static Semigroup<StringBuilder> stringBuilderConcat = (a, b) -> a.append(b);
    /**
     * Combine two booleans by OR'ing them (disjunction)
     */
    static Semigroup<Boolean> booleanDisjunction = (a, b) -> a || b;
    /**
     * Combine two booleans by XOR'ing them (exclusive disjunction)
     */
    static Semigroup<Boolean> booleanXDisjunction = (a, b) -> a && !b || b && !a;
    /**
     * Combine two booleans by AND'ing them (conjunction)
     */
    static Semigroup<Boolean> booleanConjunction = (a, b) -> a && b;

    /**
     * @return Combine  function
     */
    static <A> Semigroup<Function<A,A>> functionComposition(){
        return  (a,b)->a.andThen(b);
    }
    static <A> Semigroup<NaturalTransformation<A,A>> naturalTransformationComposition(){
        return  (a,b)->a.andThen(b);
    }


}
