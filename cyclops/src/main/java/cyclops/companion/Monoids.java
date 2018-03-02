package cyclops.companion;


import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.Zippable;
import cyclops.control.Future;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Either;
import cyclops.data.*;
import cyclops.data.HashSet;
import cyclops.data.TreeSet;
import cyclops.function.Monoid;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;

import org.reactivestreams.Publisher;

import java.math.BigInteger;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * A static class with a large number of Monoids  or Combiners with identity elements.
 *
 * A Monoid is an Object that can be used to combine objects of the same type inconjunction with it's
 * identity element which leaves any element it is combined with unchanged.
 *
 * @author johnmcclean
 */
public interface Monoids {



    static <T, C extends PersistentCollection<T>> Monoid<C> concatPersistentCollection(C empty) {
        return Monoid.of(empty,Semigroups.persistentCollectionConcat());
    }
    static <T> Monoid<LazySeq<T>> lazySeqConcat() {
        return Monoid.of(LazySeq.empty(),Semigroups.immutableListConcat());
    }
    static <T> Monoid<Seq<T>> seqConcat() {
        return Monoid.of(Seq.empty(),Semigroups.immutableListConcat());
    }
    static <T> Monoid<Vector<T>> vectorConcat() {
        return Monoid.of(Vector.empty(),Semigroups.persistentCollectionConcat());
    }
    static <T> Monoid<IntMap<T>> intMapConcat() {
        return Monoid.of(IntMap.empty(),Semigroups.persistentCollectionConcat());
    }
    static <T> Monoid<HashSet<T>> hashSetConcat() {
        return Monoid.of(HashSet.empty(),Semigroups.persistentCollectionConcat());
    }
    static <T> Monoid<TrieSet<T>> trieSetConcat() {
        return Monoid.of(TrieSet.empty(),Semigroups.persistentCollectionConcat());
    }
    static <T> Monoid<TreeSet<T>> treeSetConcat(Comparator<T> c) {
        return Monoid.of(TreeSet.empty(c),Semigroups.persistentCollectionConcat());
    }
    static <T> Monoid<Bag<T>> bagConcat() {
        return Monoid.of(Bag.empty(),Semigroups.persistentCollectionConcat());
    }
    static <T> Monoid<BankersQueue<T>> bankersQueueConcat() {
        return Monoid.of(BankersQueue.empty(),Semigroups.persistentCollectionConcat());
    }
    static <T> Monoid<LazyString> lazyStringConcat() {
        return Monoid.of(LazyString.empty(),Semigroups.persistentCollectionConcat());
    }

    /**
     * Example sum integer Maybes
     * <pre>
     * {@code
     *     Monoid<Maybe<Integer>> sumMaybes = Monoids.combineScalarFunctors(Maybe::just,Monoids.intSum);
     * }
     * </pre>
     *
     * @param zeroFn Function zeoFn lift the Identity value into a Scalar Functor
     * @param monoid Monoid to combine the values inside the Scalar Functors
     * @return Combination of two Scalar Functors
     */
    static <T,A extends Zippable<T>> Monoid<A> combineScalarFunctors(Function<T,A> zeroFn,Monoid<T> monoid) {

        return Monoid.of(zeroFn.apply(monoid.zero()),Semigroups.combineScalarFunctors(monoid));
    }
    /**
     * Example sum integer Lists
     * <pre>
     * {@code
     *      Monoid<Seq<Integer>> sumLists = Monoids.combineZippables(Seq::of,Monoids.intSum);
     * }
     * </pre>
     *
     * @param zeroFn Function to lift the Identity value into a Zippable
     * @param monoid Monoid to combine the values inside the Zippables
     * @return Combination of two Applicatives
     */
    static <T,A extends Zippable<T>> Monoid<A> combineZippables(Function<T,A> zeroFn,Monoid<T> monoid) {

        return Monoid.of(zeroFn.apply(monoid.zero()),Semigroups.combineZippables(monoid));
    }

    /**
     * @return Combination of two ReactiveSeq Streams b is appended to a
     */
    static <T> Monoid<ReactiveSeq<T>> combineReactiveSeq() {
        return Monoid.of(ReactiveSeq.empty(), Semigroups.combineReactiveSeq());
    }
    static <T> Monoid<ReactiveSeq<T>> firstNonEmptyReactiveSeq() {
        return Monoid.of(ReactiveSeq.empty(), Semigroups.firstNonEmptyReactiveSeq());
    }

    static <T> Monoid<ReactiveSeq<T>> mergeLatestReactiveSeq () {
        return Monoid.of(Spouts.empty(),Semigroups.mergeLatestReactiveSeq());
    }
    static <T> Monoid<Publisher<T>> mergeLatest() {
        return Monoid.of(Spouts.empty(),Semigroups.mergeLatest());
    }
    static <T> Monoid<Publisher<T>> amb() {
        return Monoid.of(Spouts.empty(), Semigroups.amb());
    }
    static <T> Monoid<ReactiveSeq<T>> ambReactiveSeq() {
        return Monoid.of(Spouts.empty(), Semigroups.ambReactiveSeq());
    }


    /**
     * @return Combination of two Stream's : b is appended to a
     */
    static <T> Monoid<Stream<T>> combineStream() {
        return Monoid.of(Stream.empty(), Semigroups.combineStream());
    }
    /**
     * @param zero Empty Collection of same type
     * @return Combination of two Collection, first non-zero is returned
     */
    static <T,C extends Collection<T>> Monoid<C> firstNonEmpty(C zero) {
        return  Monoid.of(zero,Semigroups.firstNonEmpty());
    }
    /**
     * @param zero Empty Collection of same type
     * @return Combination of two Collection, last non-zero is returned
     */
    static <T,C extends Collection<T>> Monoid<C> lastNonEmpty(C zero) {
        return Monoid.of(zero,Semigroups.lastNonEmpty());
    }
    /**
     * @return Combination of two Objects of same type, first non-null is returned
     */
    static <T> Monoid<T> firstNonNull() {
         return Monoid.of(null, Semigroups.firstNonNull());
    }
    /**
     * @return Combine two CompletableFuture's by taking the first present
     */
    static <T> Monoid<CompletableFuture<T>> firstCompleteCompletableFuture() {
        return Monoid.of(new CompletableFuture<T>(), Semigroups.firstCompleteCompletableFuture());
    }
    /**
     * @return Combine two Future's by taking the first result
     */
    static <T> Monoid<Future<T>> firstCompleteFuture() {
       return Monoid.of(Future.future(), Semigroups.firstCompleteFuture());
    }


    /**
     * @return Combine two Future's by taking the first successful
     */
    static <T> Monoid<Future<T>> firstSuccessfulFuture() {
        return Monoid.of(Future.future(), Semigroups.firstSuccessfulFuture());
    }
    /**
     * @return Combine two Eithers by taking the first right
     */
    static <ST,PT> Monoid<Either<ST,PT>> firstRightEither(ST zero) {
        return Monoid.of(Either.left(zero), Semigroups.firstRightEither());
    }
    /**
     * @return Combine two Eithers by taking the first left
     */
    static <ST,PT> Monoid<Either<ST,PT>> firstLeftEither(PT zero) {
        return Monoid.of(Either.right(zero), Semigroups.firstLeftEither());
    }
    /**
     * @return Combine two Eithers by taking the last right
     */
    static <ST,PT> Monoid<Either<ST,PT>> lastRightEither(ST zero) {
        return Monoid.of(Either.left(zero), Semigroups.lastRightEither());
    }
    /**
     * @return Combine two Eithers by taking the last left
     */
    static <ST,PT> Monoid<Either<ST,PT>> lastLeftEither(PT zero) {
        return Monoid.of(Either.right(zero), Semigroups.lastLeftEither());
    }
    /**
     * @return Combine two Try's by taking the first right
     */
    static <T,X extends Throwable> Monoid<Try<T,X>> firstTrySuccess(X zero) {
        return Monoid.of(Try.failure(zero), Semigroups.firstTrySuccess());
    }
    /**
     * @return Combine two Try's by taking the first left
     */
    static <T,X extends Throwable> Monoid<Try<T,X>> firstTryFailure(T zero) {
        return Monoid.of(Try.success(zero), Semigroups.firstTryFailure());
    }
    /**
     * @return Combine two Tryr's by taking the last right
     */
    static<T,X extends Throwable> Monoid<Try<T,X>> lastTrySuccess(X zero) {
        return Monoid.of(Try.failure(zero), Semigroups.lastTrySuccess());
    }
    /**
     * @return Combine two Try's by taking the last left
     */
    static <T,X extends Throwable> Monoid<Try<T,X>>lastTryFailure(T zero) {
        return Monoid.of(Try.success(zero), Semigroups.lastTryFailure());
    }
    /**
     * @return Combine two Ior's by taking the first right
     */
    static <ST,PT> Monoid<Ior<ST,PT>> firstPrimaryIor(ST zero) {
        return Monoid.of(Ior.left(zero), Semigroups.firstPrimaryIor());
    }
    /**
     * @return Combine two Ior's by taking the first left
     */
    static <ST,PT> Monoid<Ior<ST,PT>> firstSecondaryIor(PT zero) {
        return Monoid.of(Ior.right(zero), Semigroups.firstSecondaryIor());
    }
    /**
     * @return Combine two Ior's by taking the last right
     */
    static <ST,PT> Monoid<Ior<ST,PT>> lastPrimaryIor(ST zero) {
        return Monoid.of(Ior.left(zero), Semigroups.lastPrimaryIor());
    }
    /**
     * @return Combine two Ior's by taking the last left
     */
    static <ST,PT> Monoid<Ior<ST,PT>> lastSecondaryIor(PT zero) {
        return Monoid.of(Ior.right(zero), Semigroups.lastSecondaryIor());
    }
    /**
     * @return Combine two Maybe's by taking the first present
     */
    static <T> Monoid<Maybe<T>> firstPresentMaybe() {
        return Monoid.of(Maybe.nothing(), Semigroups.firstPresentMaybe());
    }

    /**
     * @return Combine two optionals by taking the first present
     */
    static <T> Monoid<Optional<T>> firstPresentOptional() {
        return Monoid.of(Optional.empty(), Semigroups.firstPresentOptional());
    }

    /**
     * @return Combine two Maybes by taking the last present
     */
    static <T> Monoid<Maybe<T>> lastPresentMaybe() {
        return Monoid.of(Maybe.nothing(), Semigroups.lastPresentMaybe());
    }

    /**
     * @return Combine two optionals by taking the last present
     */
    static <T> Monoid<Optional<T>> lastPresentOptional() {
        return Monoid.of(Optional.empty(), Semigroups.lastPresentOptional());
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two strings separated by the supplied joiner
     */
    static Monoid<String> stringJoin(final String joiner) {
        return Monoid.of("", Semigroups.stringJoin(joiner));
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two StringBuilders separated by the supplied joiner
     */
    static Monoid<StringBuilder> stringBuilderJoin(final String joiner) {
        return Monoid.of(new StringBuilder(""), Semigroups.stringBuilderJoin(joiner));
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two StringBuffers separated by the supplied joiner
     */
    static Monoid<StringBuffer> stringBufferJoin(final String joiner) {
        return Monoid.of(new StringBuffer(""), Semigroups.stringBufferJoin(joiner));
    }

    /**
     * @return Combine two Comparables taking the lowest each time
     */
    static <T, T2 extends Comparable<T>> Monoid<T2> minComparable(T2 max) {
        return Monoid.of(max, Semigroups.minComparable());
    }

    /**
     * @return Combine two Comparables taking the highest each time
     */
    static <T, T2 extends Comparable<T>> Monoid<T2> maxComparable(T2 min) {
        return Monoid.of(min, Semigroups.maxComparable());
    }

    /**
     * Combine two Integers by summing them
     */
    static Monoid<Integer> intSum =  Monoid.of(0, Semigroups.intSum);
    /**
     * Combine two Longs by summing them
     */
    static Monoid<Long> longSum =  Monoid.of(0l, Semigroups.longSum);
    /**
     * Combine two Doubles by summing them
     */
    static Monoid<Double> doubleSum =  Monoid.of(0d, Semigroups.doubleSum);
    /**
     * Combine two BigIngegers by summing them
     */
    static Monoid<BigInteger> bigIntSum =  Monoid.of(BigInteger.ZERO, Semigroups.bigIntSum);
    /**
     * Combine two Integers by multiplying them
     */
    static Monoid<Integer> intMult =  Monoid.of(1, Semigroups.intMult);
    /**
     * Combine two Longs by multiplying them
     */
    static Monoid<Long> longMult =  Monoid.of(0l, Semigroups.longMult);
    /**
     * Combine two Doubles by multiplying them
     */
    static Monoid<Double> doubleMult = Monoid.of(0d, Semigroups.doubleMult);
    /**
     * Combine two BigIntegers by multiplying them
     */
    static Monoid<BigInteger> bigIntMult = Monoid.of(BigInteger.ZERO, Semigroups.bigIntMult);
    /**
     * Combine two Integers by selecting the max
     */
    static Monoid<Integer> intMax = Monoid.of(Integer.MIN_VALUE, Semigroups.intMax);
    /**
     * Combine two Longs by selecting the max
     */
    static Monoid<Long> longMax = Monoid.of(Long.MIN_VALUE, Semigroups.longMax);
    /**
     * Combine two Doubles by selecting the max
     */
    static Monoid<Double> doubleMax = Monoid.of(Double.MIN_VALUE, Semigroups.doubleMax);
    /**
     * Combine two BigIntegers by selecting the max
     */
    static Monoid<BigInteger> bigIntMax = Monoid.of(BigInteger.valueOf(Long.MIN_VALUE), Semigroups.bigIntMax);
    /**
     * Combine two Integers by selecting the min
     */
    static Monoid<Integer> intMin = Monoid.of(Integer.MAX_VALUE, Semigroups.intMin);
    /**
     * Combine two Longs by selecting the min
     */
    static Monoid<Long> longMin = Monoid.of(Long.MAX_VALUE, Semigroups.longMin);
    /**
     * Combine two Doubles by selecting the min
     */
    static Monoid<Double> doubleMin = Monoid.of(Double.MAX_VALUE, Semigroups.doubleMin);
    /**
     * Combine two BigIntegers by selecting the min
     */
    static Monoid<BigInteger> bigIntMin = Monoid.of(BigInteger.valueOf(Long.MAX_VALUE), Semigroups.bigIntMin);
    /**
     * String concatenation
     */
    static Monoid<String> stringConcat = Monoid.of("", Semigroups.stringConcat);
    /**
     * StringBuffer concatenation
     */
    static Monoid<StringBuffer> stringBufferConcat = Monoid.of(new StringBuffer(""), Semigroups.stringBufferConcat);
    /**
     * StringBuilder concatenation
     */
    static Monoid<StringBuilder> stringBuilderConcat = Monoid.of(new StringBuilder(""), Semigroups.stringBuilderConcat);
    /**
     * Combine two booleans by OR'ing them (disjunction)
     */
    static Monoid<Boolean> booleanDisjunction = Monoid.of(false, Semigroups.booleanDisjunction);
    /**
     * Combine two booleans by XOR'ing them (exclusive disjunction)
     */
    static Monoid<Boolean> booleanXDisjunction = Monoid.of(false, Semigroups.booleanXDisjunction);
    /**
     * Combine two booleans by AND'ing them (conjunction)
     */
    static Monoid<Boolean> booleanConjunction = Monoid.of(true, Semigroups.booleanConjunction);

    static <A> Monoid<NaturalTransformation<A,A>> naturalTransformationComposition(){
        return Monoid.of(NaturalTransformation.identity(), Semigroups.naturalTransformationComposition());
    }

    /**
     * @return Monoid for composing function
     */
    static <A> Monoid<Function<A,A>> functionComposition(){
        return Monoid.of(Function.identity(), Semigroups.functionComposition());
    }

}
