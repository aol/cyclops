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
import io.reactivex.Observable;

/*
    Factory methods for asynchronously populating Reactive Collections via Observables
 */
public interface ObservableCollections {

    static <T> LinkedListX<T> linkedListX(Observable<T> flowable){
        return LinkedListX.linkedListX(ObservableReactiveSeq.reactiveSeq(flowable));
    }

    static <T> VectorX<T> vectorX(Observable<T> flowable){
        return VectorX.vectorX(ObservableReactiveSeq.reactiveSeq(flowable));
    }

    static <T> PersistentSetX<T> persistentSetX(Observable<T> flowable){
        return PersistentSetX.persistentSetX(ObservableReactiveSeq.reactiveSeq(flowable));
    }
    static <T> OrderedSetX<T> orderedSetX(Observable<T> flowable){
        return OrderedSetX.orderedSetX(ObservableReactiveSeq.reactiveSeq(flowable));
    }
    static <T> BagX<T> bagX(Observable<T> flowable){
        return BagX.bagX(ObservableReactiveSeq.reactiveSeq(flowable));
    }

    static <T> PersistentQueueX<T> persistentQueueX(Observable<T> flowable){
        return PersistentQueueX.persistentQueueX(ObservableReactiveSeq.reactiveSeq(flowable));
    }

    static <T> ListX<T> listX(Observable<T> flowable){
        return ListX.listX(ObservableReactiveSeq.reactiveSeq(flowable));
    }

    static <T> DequeX<T> dequeX(Observable<T> flowable){
        return DequeX.dequeX(ObservableReactiveSeq.reactiveSeq(flowable));
    }

    static <T> SetX<T> setX(Observable<T> flowable){
        return SetX.setX(ObservableReactiveSeq.reactiveSeq(flowable));
    }

    static <T> SortedSetX<T> sortedSetX(Observable<T> flowable){
        return SortedSetX.sortedSetX(ObservableReactiveSeq.reactiveSeq(flowable));
    }

    static <T> QueueX<T> queueX(Observable<T> flowable){
        return QueueX.queueX(ObservableReactiveSeq.reactiveSeq(flowable));
    }
}
