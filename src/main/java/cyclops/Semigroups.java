package cyclops;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.internal.stream.spliterators.push.StreamSubscription;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.futurestream.EagerFutureStreamFunctions;
import com.aol.cyclops2.types.futurestream.SimpleReactStream;
import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.async.Future;
import cyclops.collections.*;
import cyclops.collections.immutable.*;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Xor;
import cyclops.function.Semigroup;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.Seq;
import org.pcollections.PCollection;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 
 * A static class with a large number of Semigroups  or Combiners.
 * 
 * A semigroup is an Object that can be used to combine objects of the same type.
 *
 *  @author johnmcclean
 */
public interface Semigroups {

    /**
     * To manage javac type inference first assign the semigroup
     * <pre>
     * {@code
     *    
     *    Semigroup<ListX<Integer>> listX = Semigroups.collectionXConcat();
     *    Semigroup<SetX<Integer>> setX = Semigroups.collectionXConcat();
     *    
     *    
     * 
     * }
     * </pre>
     * @return A Semigroup that can combine any cyclops2-react extended Collection type
     */
    static <T, C extends FluentCollectionX<T>> Semigroup<C> collectionXConcat() {
        return (a, b) -> (C) a.plusAll(b);
    }

    /**
     * Concatenate mutable collections
     * 
     * To manage javac type inference first assign the semigroup
     * <pre>
     * {@code
     *    
     *    Semigroup<List<Integer>> list = Semigroups.collectionConcat();
     *    Semigroup<Set<Integer>> set = Semigroups.collectionConcat();
     *    
     *    
     * 
     * }
     * </pre> 
     * @return A Semigroup that can combine any mutable collection type
     */
    static <T, C extends Collection<T>> Semigroup<C> mutableCollectionConcat() {
        return (a, b) -> {

            a.addAll(b);
            return a;
        };
    }

    /**
     * @return A combiner for mutable lists
     */
    static <T> Semigroup<List<T>> mutableListConcat() {
        return Semigroups.mutableCollectionConcat();
    }

    /**
     * @return A combiner for mutable sets
     */
    static <T> Semigroup<Set<T>> mutableSetConcat() {
        return Semigroups.mutableCollectionConcat();
    }

    /**
     * @return A combiner for mutable SortedSets
     */
    static <T> Semigroup<SortedSet<T>> mutableSortedSetConcat() {
        return Semigroups.mutableCollectionConcat();
    }

    /**
     * @return A combiner for mutable Queues
     */
    static <T> Semigroup<Queue<T>> mutableQueueConcat() {
        return Semigroups.mutableCollectionConcat();
    }

    /**
     * @return A combiner for mutable Deques
     */
    static <T> Semigroup<Deque<T>> mutableDequeConcat() {
        return Semigroups.mutableCollectionConcat();
    }

    /**
     * @return A combiner for ListX (concatenates two ListX into a single ListX)
     */
    static <T> Semigroup<ListX<T>> listXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for SetX (concatenates two SetX into a single SetX)
     */
    static <T> Semigroup<SetX<T>> setXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for SortedSetX (concatenates two SortedSetX into a single SortedSetX)
     */
    static <T> Semigroup<SortedSetX<T>> sortedSetXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for QueueX (concatenates two QueueX into a single QueueX)
     */
    static <T> Semigroup<QueueX<T>> queueXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for DequeX (concatenates two DequeX into a single DequeX)
     */
    static <T> Semigroup<DequeX<T>> dequeXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for PStackX (concatenates two PStackX into a single PStackX)
     */
    static <T> Semigroup<PStackX<T>> pStackXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for PVectorX (concatenates two PVectorX into a single PVectorX)
     */
    static <T> Semigroup<PVectorX<T>> pVectorXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for PSetX (concatenates two PSetX into a single PSetX)
     */
    static <T> Semigroup<PSetX<T>> pSetXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for POrderedSetX (concatenates two POrderedSetX into a single POrderedSetX)
     */
    static <T> Semigroup<POrderedSetX<T>> pOrderedSetXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for PQueueX (concatenates two PQueueX into a single PQueueX)
     */
    static <T> Semigroup<PQueueX<T>> pQueueXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * @return A combiner for PBagX (concatenates two PBagX into a single PBagX)
     */
    static <T> Semigroup<PBagX<T>> pBagXConcat() {
        return Semigroups.collectionXConcat();
    }

    /**
     * This Semigroup will attempt to combine JDK Collections. If the Supplied are instances of cyclops2-react extended Collections
     * or a pCollection persisent collection a new Collection type is created that contains the entries from both supplied collections.
     * If the supplied Collections are standard JDK mutable collections Colleciton b is appended to Collection a and a is returned.
     * 
     * 
     * To manage javac type inference first assign the semigroup
     * <pre>
     * {@code
     *    
     *    Semigroup<List<Integer>> list = Semigroups.collectionConcat();
     *    Semigroup<Set<Integer>> set = Semigroups.collectionConcat();
     *    
     *    
     * 
     * }
     * </pre>
     * @return A Semigroup that attempts to combine the supplied Collections
     */
    static <T, C extends Collection<T>> Semigroup<C> collectionConcat() {
        return (a, b) -> {
            if (a instanceof FluentCollectionX) {
                return (C) ((FluentCollectionX) a).plusAll(b);
            }
            if (a instanceof PCollection) {
                return (C) ((PCollection) a).plusAll(b);
            }
            if (b instanceof FluentCollectionX) {
                return (C) ((FluentCollectionX) b).plusAll(a);
            }
            if (b instanceof PCollection) {
                return (C) ((PCollection) b).plusAll(a);
            }
            a.addAll(b);
            return a;
        };
    }
    /**
     * <pre>
     * {@code 
     *  BinaryOperator<Zippable<Integer>> sumInts = Semigroups.combineZippables(Semigroups.intSum);
        sumInts.apply(ListX.of(1,2,3), ListX.of(4,5,6));
        
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
     *  BinaryOperator<Combiner<Integer>> sumMaybes = Semigroups.combineScalarFunctors(Semigroups.intSum);
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
     * @return Combination of two LazyFutureStreams Streams b is appended to a
     */
    static <T> Semigroup<FutureStream<T>> combineFutureStream() {
        return (a, b) -> a.appendS(b);
    }


    /**
     * @return Combination of two ReactiveSeq Streams b is appended to a
     */
    static <T> Semigroup<ReactiveSeq<T>> combineReactiveSeq() {
        return (a, b) -> a.appendS(b);
    }

    static <T> Semigroup<ReactiveSeq<T>> firstNonEmptyReactiveSeq() {
        return (a, b) -> a.onEmptySwitch(()->b);
    }
    static <T> Semigroup<ReactiveSeq<T>> ambReactiveSeq() {
        return (a,b)->(ReactiveSeq<T>)Semigroups.<T>amb().apply(a,b);
    }
    static <T> Semigroup<Publisher<T>> amb() {
        return (a, b) -> {
            ReactiveSubscriber<T> res = Spouts.reactiveSubscriber();

            AtomicInteger first = new AtomicInteger(0);
            AtomicBoolean aComplete = new AtomicBoolean(false);
            AtomicBoolean bComplete = new AtomicBoolean(false);

            ReactiveSubscriber<T> sub = Spouts.reactiveSubscriber();


            Subscription subA[] ={null};
            Subscription subB[] ={null};
            Subscription winner[] ={null};
                a.subscribe(new Subscriber<T>() {
                    boolean won= false;
                    @Override
                    public void onSubscribe(Subscription s) {
                        subA[0]=s;

                    }

                    @Override
                    public void onNext(T t) {
                        if(won) {
                            sub.onNext(t);
                        }else if (first.compareAndSet(0, 1)) {
                            winner[0]=subA[0];
                            sub.onNext(t);
                            won =true;
                        }

                    }

                    @Override
                    public void onError(Throwable t) {
                        aComplete.set(true);
                        if (won || bComplete.get())
                            sub.onError(t);
                    }

                    @Override
                    public void onComplete() {

                        aComplete.set(true);
                        if (won || bComplete.get()) {
                            sub.onComplete();
                        }
                    }
                });
                b.subscribe(new Subscriber<T>() {
                    boolean won = false;
                    @Override
                    public void onSubscribe(Subscription s) {
                        subB[0]=s;
                    }

                    @Override
                    public void onNext(T t) {
                        if(won) {
                            sub.onNext(t);
                        }
                        if (first.get()==2 || first.compareAndSet(0, 2)) {
                            winner[0]=subB[0];
                            sub.onNext(t);
                            won=true;
                        }

                    }

                    @Override
                    public void onError(Throwable t) {
                        bComplete.set(true);
                        if (won || aComplete.get())
                            sub.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        bComplete.set(true);
                        if (won || aComplete.get()) {
                            sub.onComplete();
                        }
                    }
                });

                sub.onSubscribe(new StreamSubscription() {
                    int count = 0;

                    @Override
                    public void request(long n) {
                        if(count==0) {
                            subA[0].request(1l);
                            subB[0].request(1l);

                            if(n-1>0)
                                super.request(n-1);
                            if(first.get()!=0){
                                count=2;
                            }else
                                count=1;
                        }else if(count<2){
                            if(first.get()!=0){
                                count=2;
                            }
                            super.request(n);
                        }
                        else if(count==2){
                            if(requested.get()>0)
                                winner[0].request(requested.get());
                            winner[0].request(n);
                            count=2;
                        }
                        else{
                            winner[0].request(n);
                        }
                    }

                    @Override
                    public void cancel() {
                        winner[0].cancel();
                    }
                });
                return sub.reactiveStream();


        };
    }

    /**
     * @return Combination of two Seq's : b is appended to a
     */
    static <T> Semigroup<Seq<T>> combineSeq() {
        return (a, b) -> Seq.concat(a, b);
    }

    /**
     * @return Combination of two Stream's : b is appended to a
     */
    static <T> Semigroup<Stream<T>> combineStream() {
        return (a, b) -> Stream.concat(a, b);
    }
    
    /**
     * @return Combination of two Collection, first non-empty is returned
     */
    static <T,C extends Collection<T>> Semigroup<C> firstNonEmpty() {
        return (a, b) -> a.isEmpty() ? b: a;
    }
    /**
     * @return Combination of two Collection, last non-empty is returned
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

    static <T> Semigroup<SimpleReactStream<T>> firstOfSimpleReact() {
        return (a, b) -> EagerFutureStreamFunctions.firstOf(a,b);
    }
    /**
     * @return Combine two Future's by taking the first successful
     */
    static <T> Semigroup<Future<T>> firstSuccessfulFuture() {
        return (a, b) -> Future.firstSuccess(a,b);
    }
    /**
     * @return Combine two Xor's by taking the first primary
     */
    static <ST,PT> Semigroup<Xor<ST,PT>> firstPrimaryXor() {
        return  (a, b) -> a.isPrimary() ? a : b;
    }
    /**
     * @return Combine two Xor's by taking the first secondary
     */
    static <ST,PT> Semigroup<Xor<ST,PT>> firstSecondaryXor() {
        return  (a, b) -> a.isSecondary() ? a : b;
    }
    /**
     * @return Combine two Xor's by taking the last primary
     */
    static <ST,PT> Semigroup<Xor<ST,PT>> lastPrimaryXor() {
        return  (a, b) -> b.isPrimary() ? b : a;
    }
    /**
     * @return Combine two Xor's by taking the last secondary
     */
    static <ST,PT> Semigroup<Xor<ST,PT>> lastSecondaryXor() {
        return  (a, b) -> b.isSecondary() ? b : a;
    }
    /**
     * @return Combine two Try's by taking the first primary
     */
    static <T,X extends Throwable> Semigroup<Try<T,X>> firstTrySuccess() {
        return  (a, b) -> a.isSuccess() ? a : b;
    }
    /**
     * @return Combine two Try's by taking the first secondary
     */
    static <T,X extends Throwable> Semigroup<Try<T,X>> firstTryFailure() {
        return  (a, b) -> a.isFailure() ? a : b;
    }
    /**
     * @return Combine two Tryr's by taking the last primary
     */
    static<T,X extends Throwable> Semigroup<Try<T,X>> lastTrySuccess() {
        return  (a, b) -> b.isSuccess() ? b : a;
    }
    /**
     * @return Combine two Try's by taking the last secondary
     */
    static <T,X extends Throwable> Semigroup<Try<T,X>>lastTryFailure() {
        return  (a, b) -> b.isFailure() ? b : a;
    }
    /**
     * @return Combine two Ior's by taking the first primary
     */
    static <ST,PT> Semigroup<Ior<ST,PT>> firstPrimaryIor() {
        return  (a, b) -> a.isPrimary() ? a : b;
    }
    /**
     * @return Combine two Ior's by taking the first secondary
     */
    static <ST,PT> Semigroup<Ior<ST,PT>> firstSecondaryIor() {
        return  (a, b) -> a.isSecondary() ? a : b;
    }
    /**
     * @return Combine two Ior's by taking the last primary
     */
    static <ST,PT> Semigroup<Ior<ST,PT>> lastPrimaryIor() {
        return  (a, b) -> b.isPrimary() ? b : a;
    }
    /**
     * @return Combine two Ior's by taking the last secondary
     */
    static <ST,PT> Semigroup<Ior<ST,PT>> lastSecondaryIor() {
        return  (a, b) -> b.isSecondary() ? b : a;
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
     * Combine two BigIntegers by adding one to a (can be used to count BigIntegers in a Collection or Stream)
     */
    static Semigroup<BigInteger> bigIntCount = (a, b) -> a.add(BigInteger.ONE);
    /**
     * Combine two Integers by adding one to a (can be used to count Integers in a Collection or Stream)
     */
    static Semigroup<Integer> intCount = (a, b) -> a + 1;
    /**
     * Combine two Longs by adding one to a (can be used to count Integers in a Collection or Stream)
     */
    static Semigroup<Long> longCount = (a, b) -> a + 1;
    /**
     * Combine two Double by adding one to a (can be used to count Double in a Collection or Stream)
     */
    static Semigroup<Double> doubleCount = (a, b) -> a + 1;
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

}
