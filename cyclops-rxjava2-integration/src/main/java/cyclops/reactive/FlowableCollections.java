package cyclops.reactive;

import cyclops.reactive.collections.immutable.BagX;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.reactive.collections.immutable.OrderedSetX;
import cyclops.reactive.collections.immutable.PersistentQueueX;
import cyclops.reactive.collections.immutable.PersistentSetX;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.QueueX;
import cyclops.reactive.collections.mutable.SetX;
import cyclops.reactive.collections.mutable.SortedSetX;
import io.reactivex.Flowable;

/*
    Factory methods for asynchronously populating Reactive Collections via Flowables
 */
public interface FlowableCollections {

    static <T> LinkedListX<T> linkedListX(Flowable<T> flowable){
        return LinkedListX.linkedListX(FlowableReactiveSeq.reactiveSeq(flowable))
                          .materialize();
    }

    static <T> VectorX<T> vectorX(Flowable<T> flowable){
        return VectorX.vectorX(FlowableReactiveSeq.reactiveSeq(flowable))
                      .materialize();
    }

    static <T> PersistentSetX<T> persistentSetX(Flowable<T> flowable){
        return PersistentSetX.persistentSetX(FlowableReactiveSeq.reactiveSeq(flowable))
                             .materialize();
    }
    static <T> OrderedSetX<T> orderedSetX(Flowable<T> flowable){
        return OrderedSetX.orderedSetX(FlowableReactiveSeq.reactiveSeq(flowable))
                          .materialize();
    }
    static <T> BagX<T> bagX(Flowable<T> flowable){
        return BagX.bagX(FlowableReactiveSeq.reactiveSeq(flowable))
                    .materialize();
    }

    static <T> PersistentQueueX<T> persistentQueueX(Flowable<T> flowable){
        return PersistentQueueX.persistentQueueX(FlowableReactiveSeq.reactiveSeq(flowable))
            .materialize();
    }

    static <T> ListX<T> listX(Flowable<T> flowable){
        return ListX.listX(FlowableReactiveSeq.reactiveSeq(flowable))
                    .materialize();
    }

    static <T> DequeX<T> dequeX(Flowable<T> flowable){
        return DequeX.dequeX(FlowableReactiveSeq.reactiveSeq(flowable))
                     .materialize();
    }

    static <T> SetX<T> setX(Flowable<T> flowable){
        return SetX.setX(FlowableReactiveSeq.reactiveSeq(flowable))
                    .materialize();
    }

    static <T> SortedSetX<T> sortedSetX(Flowable<T> flowable){
        return SortedSetX.sortedSetX(FlowableReactiveSeq.reactiveSeq(flowable))
                         .materialize();
    }

    static <T> QueueX<T> queueX(Flowable<T> flowable){
        return QueueX.queueX(FlowableReactiveSeq.reactiveSeq(flowable))
                        .materialize();
    }
}
