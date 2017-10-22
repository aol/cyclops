package cyclops.companion;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.types.persistent.PersistentCollection;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.futurestream.SimpleReactStream;
import cyclops.async.Future;
import cyclops.async.SimpleReact;
import cyclops.collectionx.immutable.*;
import cyclops.collectionx.mutable.*;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Either;
import cyclops.data.Comparators;
import cyclops.function.Monoid;
import cyclops.reactive.FutureStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.typeclasses.NaturalTransformation;
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
public interface Monoids {

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
        return Monoid.of(identity, Semigroups.<T,C>collectionXConcat());
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
        return Monoid.of(identity, Semigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable lists
     */
    static <T> Monoid<List<T>> mutableListConcat() {
        return Monoid.of(ListX.empty(),Semigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable sets
     */
    static <T> Monoid<Set<T>> mutableSetConcat() {
        return Monoid.of(SetX.empty(),Semigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable SortedSets
     */
    static <T> Monoid<SortedSet<T>> mutableSortedSetConcat() {
        return Monoid.of(SortedSetX.empty(),Semigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable Queues
     */
    static <T> Monoid<Queue<T>> mutableQueueConcat() {
        return Monoid.of(QueueX.empty(),Semigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for mutable Deques
     */
    static <T> Monoid<Deque<T>> mutableDequeConcat() {
        return Monoid.of(DequeX.empty(),Semigroups.mutableCollectionConcat());
    }

    /**
     * @return A combiner for ListX (concatenates two ListX into a singleUnsafe ListX)
     */
    static <T> Monoid<ListX<T>> listXConcat() {
        return Monoid.of(ListX.empty(),Semigroups.collectionXConcat());
    }

    /**
     * @return A combiner for SetX (concatenates two SetX into a singleUnsafe SetX)
     */
    static <T> Monoid<SetX<T>> setXConcat() {
        return Monoid.of(SetX.empty(),Semigroups.collectionXConcat());
    }

    /**
     * @return A combiner for SortedSetX (concatenates two SortedSetX into a singleUnsafe SortedSetX)
     */
    static <T> Monoid<SortedSetX<T>> sortedSetXConcat() {
        return Monoid.of(SortedSetX.empty(),Semigroups.collectionXConcat());
    }

    /**
     * @return A combiner for QueueX (concatenates two QueueX into a singleUnsafe QueueX)
     */
    static <T> Monoid<QueueX<T>> queueXConcat() {
        return Monoid.of(QueueX.empty(),Semigroups.collectionXConcat());
    }

    /**
     * @return A combiner for DequeX (concatenates two DequeX into a singleUnsafe DequeX)
     */
    static <T> Monoid<DequeX<T>> dequeXConcat() {
        return Monoid.of(DequeX.empty(),Semigroups.collectionXConcat());
    }

    static <T, C extends PersistentCollection<T>> Monoid<C> pcollectionConcat(C empty) {
        return Monoid.of(empty,Semigroups.pcollectionConcat());
    }

    /**
     * @return A combiner for LinkedListX (concatenates two LinkedListX into a singleUnsafe LinkedListX)
     */
    static <T> Monoid<LinkedListX<T>> linkedListXConcat() {
        return Monoid.of(LinkedListX.empty(),Semigroups.linkedListXConcat());
    }

    /**
     * @return A combiner for VectorX (concatenates two VectorX into a singleUnsafe VectorX)
     */
    static <T> Monoid<VectorX<T>> vectorXConcat() {
        return Monoid.of(VectorX.empty(),Semigroups.collectionXConcat());
    }

    /**
     * @return A combiner for PersistentSetX (concatenates two PersistentSetX into a singleUnsafe PersistentSetX)
     */
    static <T> Monoid<PersistentSetX<T>> persistentSetXConcat() {
        return Monoid.of(PersistentSetX.empty(),Semigroups.collectionXConcat());
    }

    /**
     * @return A combiner for OrderedSetX (concatenates two OrderedSetX into a singleUnsafe OrderedSetX)
     */
    static <T> Monoid<OrderedSetX<T>> orderedSetXConcat() {
        return Monoid.of(OrderedSetX.empty(Comparators.naturalOrderIdentityComparator()),Semigroups.collectionXConcat());
    }

    /**
     * @return A combiner for PersistentQueueX (concatenates two PersistentQueueX into a singleUnsafe PersistentQueueX)
     */
    static <T> Monoid<PersistentQueueX<T>> persistentQueueXConcat() {
        return Monoid.of(PersistentQueueX.empty(),Semigroups.collectionXConcat());
    }

    /**
     * @return A combiner for BagX (concatenates two BagX into a singleUnsafe BagX)
     */
    static <T> Monoid<BagX<T>> bagXConcat() {
        return Monoid.of(BagX.empty(),Semigroups.collectionXConcat());
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
        return Monoid.of(zero, Semigroups.collectionConcat());
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
     *      Monoid<ListX<Integer>> sumLists = Monoids.combineZippables(ListX::of,Monoids.intSum);
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
     * @return Combination of two LazyFutureStreams Streams b is appended to a
     */
    static <T> Monoid<FutureStream<T>> combineFutureStream() {
        return Monoid.of(FutureStream.builder().of(),Semigroups.combineFutureStream());
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

    static <T> Monoid<SimpleReactStream<T>> firstOfSimpleReact() {
        return Monoid.of(new SimpleReact().of(),Semigroups.firstOfSimpleReact());
    }
    /**
     * @return Combine two Future's by taking the first successful
     */
    static <T> Monoid<Future<T>> firstSuccessfulFuture() {
        return Monoid.of(Future.future(), Semigroups.firstSuccessfulFuture());
    }
    /**
     * @return Combine two Xor's by taking the first lazyRight
     */
    static <ST,PT> Monoid<Either<ST,PT>> firstRightEither(ST zero) {
        return Monoid.of(Either.left(zero), Semigroups.firstPrimaryXor());
    }
    /**
     * @return Combine two Xor's by taking the first lazyLeft
     */
    static <ST,PT> Monoid<Either<ST,PT>> firstSecondaryXor(PT zero) {
        return Monoid.of(Either.right(zero), Semigroups.firstSecondaryXor());
    }
    /**
     * @return Combine two Xor's by taking the last lazyRight
     */
    static <ST,PT> Monoid<Either<ST,PT>> lastPrimaryXor(ST zero) {
        return Monoid.of(Either.left(zero), Semigroups.lastPrimaryXor());
    }
    /**
     * @return Combine two Xor's by taking the last lazyLeft
     */
    static <ST,PT> Monoid<Either<ST,PT>> lastSecondaryXor(PT zero) {
        return Monoid.of(Either.right(zero), Semigroups.lastSecondaryXor());
    }
    /**
     * @return Combine two Try's by taking the first lazyRight
     */
    static <T,X extends Throwable> Monoid<Try<T,X>> firstTrySuccess(X zero) {
        return Monoid.of(Try.failure(zero), Semigroups.firstTrySuccess());
    }
    /**
     * @return Combine two Try's by taking the first lazyLeft
     */
    static <T,X extends Throwable> Monoid<Try<T,X>> firstTryFailure(T zero) {
        return Monoid.of(Try.success(zero), Semigroups.firstTryFailure());
    }
    /**
     * @return Combine two Tryr's by taking the last lazyRight
     */
    static<T,X extends Throwable> Monoid<Try<T,X>> lastTrySuccess(X zero) {
        return Monoid.of(Try.failure(zero), Semigroups.lastTrySuccess());
    }
    /**
     * @return Combine two Try's by taking the last lazyLeft
     */
    static <T,X extends Throwable> Monoid<Try<T,X>>lastTryFailure(T zero) {
        return Monoid.of(Try.success(zero), Semigroups.lastTryFailure());
    }
    /**
     * @return Combine two Ior's by taking the first lazyRight
     */
    static <ST,PT> Monoid<Ior<ST,PT>> firstPrimaryIor(ST zero) {
        return Monoid.of(Ior.secondary(zero), Semigroups.firstPrimaryIor());
    }
    /**
     * @return Combine two Ior's by taking the first lazyLeft
     */
    static <ST,PT> Monoid<Ior<ST,PT>> firstSecondaryIor(PT zero) {
        return Monoid.of(Ior.primary(zero), Semigroups.firstSecondaryIor());
    }
    /**
     * @return Combine two Ior's by taking the last lazyRight
     */
    static <ST,PT> Monoid<Ior<ST,PT>> lastPrimaryIor(ST zero) {
        return Monoid.of(Ior.secondary(zero), Semigroups.lastPrimaryIor());
    }
    /**
     * @return Combine two Ior's by taking the last lazyLeft
     */
    static <ST,PT> Monoid<Ior<ST,PT>> lastSecondaryIor(PT zero) {
        return Monoid.of(Ior.primary(zero), Semigroups.lastSecondaryIor());
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
