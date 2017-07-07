package cyclops.companion;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.futurestream.SimpleReactStream;
import cyclops.async.Future;
import cyclops.async.SimpleReact;
import cyclops.collections.box.Mutable;
import cyclops.collections.immutable.*;
import cyclops.collections.mutable.*;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Try;
import cyclops.control.Xor;
import cyclops.function.Group;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import cyclops.typeclasses.NaturalTransformation;
import org.jooq.lambda.Seq;
import org.reactivestreams.Publisher;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;


public interface Groups {



    /**
     * @return A combiner for mutable lists
     */
    static <T> Group<List<T>> mutableListConcat() {
        return Group.<List<T>>of(l->ListX.fromIterable(l).reverse(),Monoids.mutableListConcat());
    }



    /**
     * @return A combiner for mutable SortedSets
     */
    static <T> Group<SortedSet<T>> mutableSortedSetConcat() {
        return Group.of(s->SortedSetX.fromIterable(s).reverse(),Monoids.mutableSortedSetConcat());
    }

    /**
     * @return A combiner for mutable Queues
     */
    static <T> Group<Queue<T>> mutableQueueConcat() {
        return Group.of(l->QueueX.fromIterable(l).reverse(),Monoids.mutableQueueConcat());
    }

    /**
     * @return A combiner for mutable Deques
     */
    static <T> Group<Deque<T>> mutableDequeConcat() {
        return Group.of(d->DequeX.fromIterable(d).reverse(),Monoids.mutableDequeConcat());
    }

    /**
     * @return A combiner for ListX (concatenates two ListX into a singleUnsafe ListX)
     */
    static <T> Group<ListX<T>> listXConcat() {
        return Group.of(ListX::reverse,Monoids.listXConcat());
    }


    /**
     * @return A combiner for SortedSetX (concatenates two SortedSetX into a singleUnsafe SortedSetX)
     */
    static <T> Group<SortedSetX<T>> sortedSetXConcat() {
        return Group.of(SortedSetX::reverse,Monoids.sortedSetXConcat());
    }

    /**
     * @return A combiner for QueueX (concatenates two QueueX into a singleUnsafe QueueX)
     */
    static <T> Group<QueueX<T>> queueXConcat() {
        return Group.of(QueueX::reverse,Monoids.queueXConcat());
    }

    /**
     * @return A combiner for DequeX (concatenates two DequeX into a singleUnsafe DequeX)
     */
    static <T> Group<DequeX<T>> dequeXConcat() {
        return Group.of(DequeX::reverse,Monoids.dequeXConcat());
    }

    /**
     * @return A combiner for LinkedListX (concatenates two LinkedListX into a singleUnsafe LinkedListX)
     */
    static <T> Group<LinkedListX<T>> linkedListXConcat() {
        return Group.of(LinkedListX::reverse,Monoids.linkedListXConcat());
    }

    /**
     * @return A combiner for VectorX (concatenates two VectorX into a singleUnsafe VectorX)
     */
    static <T> Group<VectorX<T>> vectorXConcat() {
        return Group.of(VectorX::reverse,Monoids.vectorXConcat());
    }



    /**
     * @return A combiner for OrderedSetX (concatenates two OrderedSetX into a singleUnsafe OrderedSetX)
     */
    static <T> Group<OrderedSetX<T>> orderedSetXConcat() {
        return Group.of(OrderedSetX::reverse,Monoids.orderedSetXConcat());
    }

    /**
     * @return A combiner for PersistentQueueX (concatenates two PersistentQueueX into a singleUnsafe PersistentQueueX)
     */
    static <T> Group<PersistentQueueX<T>> persistentQueueXConcat() {
        return Group.of(PersistentQueueX::reverse,Monoids.persistentQueueXConcat());
    }



    /**
     * @return Combination of two LazyFutureStreams Streams b is appended toNested a
     */
    static <T> Group<FutureStream<T>> combineFutureStream() {
        return Group.of(FutureStream::reverse,Monoids.combineFutureStream());
    }
    /**
     * @return Combination of two ReactiveSeq Streams b is appended toNested a
     */
    static <T> Group<ReactiveSeq<T>> combineReactiveSeq() {
        return Group.of(ReactiveSeq::reverse,Monoids.combineReactiveSeq());
    }


    static <T> Group<ReactiveSeq<T>> mergeLatestReactiveSeq() {
        return Group.of(ReactiveSeq::reverse,Monoids.mergeLatestReactiveSeq());
    }
    static <T> Group<Publisher<T>> mergeLatest() {
        return Group.of(s->Spouts.from(s).reverse(),Monoids.mergeLatest());
    }


    /**
     * @return Combination of two Seq's : b is appended toNested a
     */
    static <T> Group<Seq<T>> combineSeq() {
        return Group.of(s->s.reverse(), Monoids.combineSeq());
    }

    /**
     * @return Combination of two Stream's : b is appended toNested a
     */
    static <T> Group<Stream<T>> combineStream() {
        return Group.of(s->ReactiveSeq.fromStream(s).reverse(), Monoids.combineStream());
    }




    /**
     * @param joiner Separator in joined String
     * @return Combine two strings separated by the supplied joiner
     */
    static Group<String> stringJoin(final String joiner) {
        return Group.of(s->new StringBuilder(s).reverse().toString(),Monoids.stringJoin(joiner));
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two StringBuilders separated by the supplied joiner
     */
    static Group<StringBuilder> stringBuilderJoin(final String joiner) {
        return Group.of(s->s.reverse(),Monoids.stringBuilderJoin(joiner));
    }

    /**
     * @param joiner Separator in joined String
     * @return Combine two StringBuffers separated by the supplied joiner
     */
    static Group<StringBuffer> stringBufferJoin(final String joiner) {
        return Group.of(StringBuffer::reverse, Monoids.stringBufferJoin(joiner));
    }


    /**
     * Combine two Integers by summing them
     */
    static Group<Integer> intSum =  Group.of(a->-a, Monoids.intSum);
    /**
     * Combine two Longs by summing them
     */
    static Group<Long> longSum =  Group.of(a->-a, Monoids.longSum);
    /**
     * Combine two Doubles by summing them
     */
    static Group<Double> doubleSum =  Group.of(a->-a, Monoids.doubleSum);
    /**
     * Combine two BigIngegers by summing them
     */
    static Group<BigInteger> bigIntSum =  Group.of(a->BigInteger.ZERO.subtract(a), Monoids.bigIntSum);

      /**
     * String concatenation
     */
    static Group<String> stringConcat = Group.of(s->new StringBuffer(s).reverse().toString(), Monoids.stringConcat);
    /**
     * StringBuffer concatenation
     */
    static Group<StringBuffer> stringBufferConcat = Group.of(StringBuffer::reverse, Monoids.stringBufferConcat);
    /**
     * StringBuilder concatenation
     */
    static Group<StringBuilder> stringBuilderConcat = Group.of(StringBuilder::reverse, Monoids.stringBuilderConcat);
    /**
     * Combine two booleans by OR'ing them (disjunction)
     */
    static Group<Boolean> booleanDisjunction = Group.of(a->!a,Monoids.booleanDisjunction);
    /**
     * Combine two booleans by XOR'ing them (exclusive disjunction)
     */
    static Group<Boolean> booleanXDisjunction = Group.of(a->!a,Monoids.booleanXDisjunction);
    /**
     * Combine two booleans by AND'ing them (conjunction)
     */
    static Group<Boolean> booleanConjunction = Group.of(a->!a,Monoids.booleanConjunction);



}
