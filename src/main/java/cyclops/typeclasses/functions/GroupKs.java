package cyclops.typeclasses.functions;

import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.collections.immutable.PersistentQueueX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.DequeX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.QueueX;
import cyclops.collections.mutable.SortedSetX;
import cyclops.companion.Monoids;
import cyclops.function.Group;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.Seq;
import org.reactivestreams.Publisher;

import java.math.BigInteger;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.SortedSet;
import java.util.stream.Stream;


public interface GroupKs {





    /**
     * @return A combiner for ListX (concatenates two ListX into a singleUnsafe ListX)
     */
    static <T> GroupK<ListX<T>> listXConcat() {
        return GroupK.of(ListX::reverse,Monoids.listXConcat());
    }


    /**
     * @return A combiner for SortedSetX (concatenates two SortedSetX into a singleUnsafe SortedSetX)
     */
    static <T> GroupK<SortedSetX<T>> sortedSetXConcat() {
        return GroupK.of(SortedSetX::reverse,Monoids.sortedSetXConcat());
    }

    /**
     * @return A combiner for QueueX (concatenates two QueueX into a singleUnsafe QueueX)
     */
    static <T> GroupK<QueueX<T>> queueXConcat() {
        return GroupK.of(QueueX::reverse,Monoids.queueXConcat());
    }

    /**
     * @return A combiner for DequeX (concatenates two DequeX into a singleUnsafe DequeX)
     */
    static <T> GroupK<DequeX<T>> dequeXConcat() {
        return GroupK.of(DequeX::reverse,Monoids.dequeXConcat());
    }

    /**
     * @return A combiner for LinkedListX (concatenates two LinkedListX into a singleUnsafe LinkedListX)
     */
    static <T> GroupK<LinkedListX<T>> linkedListXConcat() {
        return GroupK.of(LinkedListX::reverse,Monoids.linkedListXConcat());
    }

    /**
     * @return A combiner for VectorX (concatenates two VectorX into a singleUnsafe VectorX)
     */
    static <T> GroupK<vectorX,T>> vectorXConcat() {
        return GroupK.of(VectorX::reverse,Monoids.vectorXConcat());
    }



    /**
     * @return A combiner for OrderedSetX (concatenates two OrderedSetX into a singleUnsafe OrderedSetX)
     */
    static <T> GroupK<OrderedSetX<T>> orderedSetXConcat() {
        return GroupK.of(OrderedSetX::reverse,Monoids.orderedSetXConcat());
    }

    /**
     * @return A combiner for PersistentQueueX (concatenates two PersistentQueueX into a singleUnsafe PersistentQueueX)
     */
    static <T> GroupK<PersistentQueueX<T>> persistentQueueXConcat() {
        return GroupK.of(PersistentQueueX::reverse,Monoids.persistentQueueXConcat());
    }



    /**
     * @return Combination of two ReactiveSeq Streams b is appended toNested a
     */
    static <T> GroupK<reactiveSeq,T>> combineReactiveSeq() {
        return GroupK.of(ReactiveSeq::reverse,Monoids.combineReactiveSeq());
    }


    static <T> GroupK<reactiveSeq,T>> mergeLatestReactiveSeq() {
        return GroupK.of(ReactiveSeq::reverse,Monoids.mergeLatestReactiveSeq());
    }
    static <T> GroupK<Publisher<T>> mergeLatest() {
        return GroupK.of(s->Spouts.from(s).reverse(),Monoids.mergeLatest());
    }


    /**
     * @return Combination of two Seq's : b is appended toNested a
     */
    static <T> GroupK<Seq<T>> combineSeq() {
        return GroupK.of(s->s.reverse(), Monoids.combineSeq());
    }

    /**
     * @return Combination of two Stream's : b is appended toNested a
     */
    static <T> GroupK<stream,T>> combineStream() {
        return GroupK.of(s->ReactiveSeq.fromStream(s).reverse(), Monoids.combineStream());
    }





}
