package cyclops;

import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.persistent.PersistentCollection;
import cyclops.control.*;
import cyclops.data.Comparators;
import cyclops.data.NaturalTransformation;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;

import java.math.BigInteger;
import java.util.*;
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
public interface ReactiveMonoids {

    /**
     * To manage javac type inference first assign the monoid
     * <pre>
     * {@code
     *
     *    Monoid<ListX<Integer>> listX = Monoid.of(identity,Semigroups.collectionXConcat(ListX.zero());
     *    Monoid<SetX<Integer>> setX = Monoid.of(identity,Semigroups.collectionXConcat(SetX.zero());
     *
     *
     *
     * }
     * </pre>
     * @return A Monoid that can combine any cyclops2-react extended Collection type
     */
    static <T, C extends FluentCollectionX<T>> Monoid<C> collectionXConcat(C identity) {
        return Monoid.of(identity, ReactiveSemigroups.<T,C>collectionXConcat());
    }

    /**
     * Concatenate mutable collections
     *
     * To manage javac type inference first assign the Monoid
     * <pre>
     * {@code
     *
     *    Monoid<List<Integer>> list =  Monoid.of(identity,Semigroups.collectionConcat(Arrays.asList());
     *    Monoid<Set<Integer>> set =  Monoid.of(identity,Semigroups.collectionConcat(new HashSet());
     *
     *
     *
     * }
     * </pre>
     * @return A  Monoid that can combine any mutable toX type
     */
    static <T, C extends Collection<T>> Monoid<C> mutableCollectionConcat(C identity) {
        return Monoid.of(identity, ReactiveSemigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable lists
     */
    static <T> Monoid<List<T>> mutableListConcat() {
        return Monoid.of(ListX.empty(), ReactiveSemigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable sets
     */
    static <T> Monoid<Set<T>> mutableSetConcat() {
        return Monoid.of(SetX.empty(), ReactiveSemigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable SortedSets
     */
    static <T> Monoid<SortedSet<T>> mutableSortedSetConcat() {
        return Monoid.of(SortedSetX.empty(), ReactiveSemigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable Queues
     */
    static <T> Monoid<Queue<T>> mutableQueueConcat() {
        return Monoid.of(QueueX.empty(), ReactiveSemigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable Deques
     */
    static <T> Monoid<Deque<T>> mutableDequeConcat() {
        return Monoid.of(DequeX.empty(), ReactiveSemigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for ListX (concatenates two ListX into a single ListX)
     */
    static <T> Monoid<ListX<T>> listXConcat() {
        return Monoid.of(ListX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * @return A combiner for SetX (concatenates two SetX into a single SetX)
     */
    static <T> Monoid<SetX<T>> setXConcat() {
        return Monoid.of(SetX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * @return A combiner for SortedSetX (concatenates two SortedSetX into a single SortedSetX)
     */
    static <T> Monoid<SortedSetX<T>> sortedSetXConcat() {
        return Monoid.of(SortedSetX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * @return A combiner for QueueX (concatenates two QueueX into a single QueueX)
     */
    static <T> Monoid<QueueX<T>> queueXConcat() {
        return Monoid.of(QueueX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * @return A combiner for DequeX (concatenates two DequeX into a single DequeX)
     */
    static <T> Monoid<DequeX<T>> dequeXConcat() {
        return Monoid.of(DequeX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    static <T, C extends PersistentCollection<T>> Monoid<C> pcollectionConcat(C empty) {
        return Monoid.of(empty, ReactiveSemigroups.pcollectionConcat());
    }

    /**
     * @return A combiner for LinkedListX (concatenates two LinkedListX into a single LinkedListX)
     */
    static <T> Monoid<LinkedListX<T>> linkedListXConcat() {
        return Monoid.of(LinkedListX.empty(), ReactiveSemigroups.linkedListXConcat());
    }

    /**
     * @return A combiner for VectorX (concatenates two VectorX into a single VectorX)
     */
    static <T> Monoid<VectorX<T>> vectorXConcat() {
        return Monoid.of(VectorX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * @return A combiner for PersistentSetX (concatenates two PersistentSetX into a single PersistentSetX)
     */
    static <T> Monoid<PersistentSetX<T>> persistentSetXConcat() {
        return Monoid.of(PersistentSetX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * @return A combiner for OrderedSetX (concatenates two OrderedSetX into a single OrderedSetX)
     */
    static <T> Monoid<OrderedSetX<T>> orderedSetXConcat() {
        return Monoid.of(OrderedSetX.empty(Comparators.naturalOrderIdentityComparator()), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * @return A combiner for PersistentQueueX (concatenates two PersistentQueueX into a single PersistentQueueX)
     */
    static <T> Monoid<PersistentQueueX<T>> persistentQueueXConcat() {
        return Monoid.of(PersistentQueueX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * @return A combiner for BagX (concatenates two BagX into a single BagX)
     */
    static <T> Monoid<BagX<T>> bagXConcat() {
        return Monoid.of(BagX.empty(), ReactiveSemigroups.collectionXConcat());
    }

    /**
     * This Semigroup will recover to combine JDK Collections. If the Supplied are instances of cyclops2-react extended Collections
     * or a pCollection persisent toX a new Collection type is created that contains the entries from both supplied collections.
     * If the supplied Collections are standard JDK mutable collections Colleciton b is appended to Collection a and a is returned.
     *
     *
     * To manage javac type inference to assign the semigroup
     * <pre>
     * {@code
     *
     *    Monoid<List<Integer>> list = Monoids.collectionConcat();
     *    Monoid<Set<Integer>> set = Monoids.collectionConcat();
     *
     *
     *
     * }
     * </pre>
     * @return A Semigroup that attempts to combine the supplied Collections
     */
    static <T, C extends Collection<T>> Monoid<C> collectionConcat(C zero) {
        return Monoid.of(zero, ReactiveSemigroups.collectionConcat());
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
    static <T,A extends Zippable<T>> Monoid<A> combineScalarFunctors(Function<T, A> zeroFn, Monoid<T> monoid) {

        return Monoid.of(zeroFn.apply(monoid.zero()), ReactiveSemigroups.combineScalarFunctors(monoid));
    }
    /**
     * Example sum integer Lists
     * <pre>
     * {@code
     *      Monoid<ListX<Integer>> sumLists = Monoids.combineZippables(ListX::of,Monoids.intSum);
     * }
     * </pre>
     *
     * @param zeroFn Function to lift the Identity value into a Zippable
     * @param monoid Monoid to combine the values inside the Zippables
     * @return Combination of two Applicatives
     */
    static <T,A extends Zippable<T>> Monoid<A> combineZippables(Function<T, A> zeroFn, Monoid<T> monoid) {

        return Monoid.of(zeroFn.apply(monoid.zero()), ReactiveSemigroups.combineZippables(monoid));
    }

    /**
     * @return Combination of two ReactiveSeq Streams b is appended to a
     */
    static <T> Monoid<ReactiveSeq<T>> combineReactiveSeq() {
        return Monoid.of(ReactiveSeq.empty(), ReactiveSemigroups.combineReactiveSeq());
    }
    static <T> Monoid<ReactiveSeq<T>> firstNonEmptyReactiveSeq() {
        return Monoid.of(ReactiveSeq.empty(), ReactiveSemigroups.firstNonEmptyReactiveSeq());
    }

    static <T> Monoid<ReactiveSeq<T>> mergeLatestReactiveSeq() {
        return Monoid.of(Spouts.empty(), ReactiveSemigroups.mergeLatestReactiveSeq());
    }
    static <T> Monoid<Publisher<T>> mergeLatest() {
        return Monoid.of(Spouts.empty(), ReactiveSemigroups.mergeLatest());
    }
    static <T> Monoid<Publisher<T>> amb() {
        return Monoid.of(Spouts.empty(), ReactiveSemigroups.amb());
    }
    static <T> Monoid<ReactiveSeq<T>> ambReactiveSeq() {
        return Monoid.of(Spouts.empty(), ReactiveSemigroups.ambReactiveSeq());
    }


    /**
     * @return Combination of two Stream's : b is appended to a
     */
    static <T> Monoid<Stream<T>> combineStream() {
        return Monoid.of(Stream.empty(), ReactiveSemigroups.combineStream());
    }
    /**
     * @param zero Empty Collection of same type
     * @return Combination of two Collection, first non-zero is returned
     */
    static <T,C extends Collection<T>> Monoid<C> firstNonEmpty(C zero) {
        return  Monoid.of(zero, ReactiveSemigroups.firstNonEmpty());
    }
    /**
     * @param zero Empty Collection of same type
     * @return Combination of two Collection, last non-zero is returned
     */
    static <T,C extends Collection<T>> Monoid<C> lastNonEmpty(C zero) {
        return Monoid.of(zero, ReactiveSemigroups.lastNonEmpty());
    }
    /**
     * @return Combination of two Objects of same type, first non-null is returned
     */
    static <T> Monoid<T> firstNonNull() {
         return Monoid.of(null, ReactiveSemigroups.firstNonNull());
    }
    /**
     * @return Combine two CompletableFuture's by taking the first present
     */
    static <T> Monoid<CompletableFuture<T>> firstCompleteCompletableFuture() {
        return Monoid.of(new CompletableFuture<T>(), ReactiveSemigroups.firstCompleteCompletableFuture());
    }
    /**
     * @return Combine two Future's by taking the first result
     */
    static <T> Monoid<Future<T>> firstCompleteFuture() {
       return Monoid.of(Future.future(), ReactiveSemigroups.firstCompleteFuture());
    }


    /**
     * @return Combine two Future's by taking the first successful
     */
    static <T> Monoid<Future<T>> firstSuccessfulFuture() {
        return Monoid.of(Future.future(), ReactiveSemigroups.firstSuccessfulFuture());
    }
    /**
     * @return Combine two Xor's by taking the first right
     */
    static <ST,PT> Monoid<Either<ST,PT>> firstRightEither(ST zero) {
        return Monoid.of(Either.left(zero), ReactiveSemigroups.firstPrimaryXor());
    }
    /**
     * @return Combine two Xor's by taking the first left
     */
    static <ST,PT> Monoid<Either<ST,PT>> firstSecondaryXor(PT zero) {
        return Monoid.of(Either.right(zero), ReactiveSemigroups.firstSecondaryXor());
    }
    /**
     * @return Combine two Xor's by taking the last right
     */
    static <ST,PT> Monoid<Either<ST,PT>> lastPrimaryXor(ST zero) {
        return Monoid.of(Either.left(zero), ReactiveSemigroups.lastPrimaryXor());
    }
    /**
     * @return Combine two Xor's by taking the last left
     */
    static <ST,PT> Monoid<Either<ST,PT>> lastSecondaryXor(PT zero) {
        return Monoid.of(Either.right(zero), ReactiveSemigroups.lastSecondaryXor());
    }
    /**
     * @return Combine two Try's by taking the first right
     */
    static <T,X extends Throwable> Monoid<Try<T,X>> firstTrySuccess(X zero) {
        return Monoid.of(Try.failure(zero), ReactiveSemigroups.firstTrySuccess());
    }
    /**
     * @return Combine two Try's by taking the first left
     */
    static <T,X extends Throwable> Monoid<Try<T,X>> firstTryFailure(T zero) {
        return Monoid.of(Try.success(zero), ReactiveSemigroups.firstTryFailure());
    }
    /**
     * @return Combine two Tryr's by taking the last right
     */
    static<T,X extends Throwable> Monoid<Try<T,X>> lastTrySuccess(X zero) {
        return Monoid.of(Try.failure(zero), ReactiveSemigroups.lastTrySuccess());
    }
    /**
     * @return Combine two Try's by taking the last left
     */
    static <T,X extends Throwable> Monoid<Try<T,X>>lastTryFailure(T zero) {
        return Monoid.of(Try.success(zero), ReactiveSemigroups.lastTryFailure());
    }
    /**
     * @return Combine two Ior's by taking the first right
     */
    static <ST,PT> Monoid<Ior<ST,PT>> firstPrimaryIor(ST zero) {
        return Monoid.of(Ior.left(zero), ReactiveSemigroups.firstPrimaryIor());
    }
    /**
     * @return Combine two Ior's by taking the first left
     */
    static <ST,PT> Monoid<Ior<ST,PT>> firstSecondaryIor(PT zero) {
        return Monoid.of(Ior.right(zero), ReactiveSemigroups.firstSecondaryIor());
    }
    /**
     * @return Combine two Ior's by taking the last right
     */
    static <ST,PT> Monoid<Ior<ST,PT>> lastPrimaryIor(ST zero) {
        return Monoid.of(Ior.left(zero), ReactiveSemigroups.lastPrimaryIor());
    }
    /**
     * @return Combine two Ior's by taking the last left
     */
    static <ST,PT> Monoid<Ior<ST,PT>> lastSecondaryIor(PT zero) {
        return Monoid.of(Ior.right(zero), ReactiveSemigroups.lastSecondaryIor());
    }
    /**
     * @return Combine two Maybe's by taking the first present
     */
    static <T> Monoid<Maybe<T>> firstPresentMaybe() {
        return Monoid.of(Maybe.nothing(), ReactiveSemigroups.firstPresentMaybe());
    }

    /**
     * @return Combine two optionals by taking the first present
     */
    static <T> Monoid<Optional<T>> firstPresentOptional() {
        return Monoid.of(Optional.empty(), ReactiveSemigroups.firstPresentOptional());
    }

    /**
     * @return Combine two Maybes by taking the last present
     */
    static <T> Monoid<Maybe<T>> lastPresentMaybe() {
        return Monoid.of(Maybe.nothing(), ReactiveSemigroups.lastPresentMaybe());
    }

    /**
     * @return Combine two optionals by taking the last present
     */
    static <T> Monoid<Optional<T>> lastPresentOptional() {
        return Monoid.of(Optional.empty(), ReactiveSemigroups.lastPresentOptional());
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two strings separated by the supplied joiner
     */
    static Monoid<String> stringJoin(final String joiner) {
        return Monoid.of("", ReactiveSemigroups.stringJoin(joiner));
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two StringBuilders separated by the supplied joiner
     */
    static Monoid<StringBuilder> stringBuilderJoin(final String joiner) {
        return Monoid.of(new StringBuilder(""), ReactiveSemigroups.stringBuilderJoin(joiner));
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two StringBuffers separated by the supplied joiner
     */
    static Monoid<StringBuffer> stringBufferJoin(final String joiner) {
        return Monoid.of(new StringBuffer(""), ReactiveSemigroups.stringBufferJoin(joiner));
    }

    /**
     * @return Combine two Comparables taking the lowest each time
     */
    static <T, T2 extends Comparable<T>> Monoid<T2> minComparable(T2 max) {
        return Monoid.of(max, ReactiveSemigroups.minComparable());
    }

    /**
     * @return Combine two Comparables taking the highest each time
     */
    static <T, T2 extends Comparable<T>> Monoid<T2> maxComparable(T2 min) {
        return Monoid.of(min, ReactiveSemigroups.maxComparable());
    }

    /**
     * Combine two Integers by summing them
     */
    static Monoid<Integer> intSum =  Monoid.of(0, ReactiveSemigroups.intSum);
    /**
     * Combine two Longs by summing them
     */
    static Monoid<Long> longSum =  Monoid.of(0l, ReactiveSemigroups.longSum);
    /**
     * Combine two Doubles by summing them
     */
    static Monoid<Double> doubleSum =  Monoid.of(0d, ReactiveSemigroups.doubleSum);
    /**
     * Combine two BigIngegers by summing them
     */
    static Monoid<BigInteger> bigIntSum =  Monoid.of(BigInteger.ZERO, ReactiveSemigroups.bigIntSum);
    /**
     * Combine two Integers by multiplying them
     */
    static Monoid<Integer> intMult =  Monoid.of(1, ReactiveSemigroups.intMult);
    /**
     * Combine two Longs by multiplying them
     */
    static Monoid<Long> longMult =  Monoid.of(0l, ReactiveSemigroups.longMult);
    /**
     * Combine two Doubles by multiplying them
     */
    static Monoid<Double> doubleMult = Monoid.of(0d, ReactiveSemigroups.doubleMult);
    /**
     * Combine two BigIntegers by multiplying them
     */
    static Monoid<BigInteger> bigIntMult = Monoid.of(BigInteger.ZERO, ReactiveSemigroups.bigIntMult);
    /**
     * Combine two Integers by selecting the max
     */
    static Monoid<Integer> intMax = Monoid.of(Integer.MIN_VALUE, ReactiveSemigroups.intMax);
    /**
     * Combine two Longs by selecting the max
     */
    static Monoid<Long> longMax = Monoid.of(Long.MIN_VALUE, ReactiveSemigroups.longMax);
    /**
     * Combine two Doubles by selecting the max
     */
    static Monoid<Double> doubleMax = Monoid.of(Double.MIN_VALUE, ReactiveSemigroups.doubleMax);
    /**
     * Combine two BigIntegers by selecting the max
     */
    static Monoid<BigInteger> bigIntMax = Monoid.of(BigInteger.valueOf(Long.MIN_VALUE), ReactiveSemigroups.bigIntMax);
    /**
     * Combine two Integers by selecting the min
     */
    static Monoid<Integer> intMin = Monoid.of(Integer.MAX_VALUE, ReactiveSemigroups.intMin);
    /**
     * Combine two Longs by selecting the min
     */
    static Monoid<Long> longMin = Monoid.of(Long.MAX_VALUE, ReactiveSemigroups.longMin);
    /**
     * Combine two Doubles by selecting the min
     */
    static Monoid<Double> doubleMin = Monoid.of(Double.MAX_VALUE, ReactiveSemigroups.doubleMin);
    /**
     * Combine two BigIntegers by selecting the min
     */
    static Monoid<BigInteger> bigIntMin = Monoid.of(BigInteger.valueOf(Long.MAX_VALUE), ReactiveSemigroups.bigIntMin);
    /**
     * String concatenation
     */
    static Monoid<String> stringConcat = Monoid.of("", ReactiveSemigroups.stringConcat);
    /**
     * StringBuffer concatenation
     */
    static Monoid<StringBuffer> stringBufferConcat = Monoid.of(new StringBuffer(""), ReactiveSemigroups.stringBufferConcat);
    /**
     * StringBuilder concatenation
     */
    static Monoid<StringBuilder> stringBuilderConcat = Monoid.of(new StringBuilder(""), ReactiveSemigroups.stringBuilderConcat);
    /**
     * Combine two booleans by OR'ing them (disjunction)
     */
    static Monoid<Boolean> booleanDisjunction = Monoid.of(false, ReactiveSemigroups.booleanDisjunction);
    /**
     * Combine two booleans by XOR'ing them (exclusive disjunction)
     */
    static Monoid<Boolean> booleanXDisjunction = Monoid.of(false, ReactiveSemigroups.booleanXDisjunction);
    /**
     * Combine two booleans by AND'ing them (conjunction)
     */
    static Monoid<Boolean> booleanConjunction = Monoid.of(true, ReactiveSemigroups.booleanConjunction);

    static <A> Monoid<NaturalTransformation<A,A>> naturalTransformationComposition(){
        return Monoid.of(NaturalTransformation.identity(), ReactiveSemigroups.naturalTransformationComposition());
    }

    /**
     * @return Monoid for composing function
     */
    static <A> Monoid<Function<A,A>> functionComposition(){
        return Monoid.of(Function.identity(), ReactiveSemigroups.functionComposition());
    }

}
