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
import reactor.core.publisher.Flux;

/*
    Factory methods for asynchronously populating Reactive Collections via Fluxs
 */
public interface FluxCollections {

    static <T> LinkedListX<T> linkedListX(Flux<T> flux){
        return LinkedListX.linkedListX(FluxReactiveSeq.reactiveSeq(flux))
                          .materialize();
    }

    static <T> VectorX<T> vectorX(Flux<T> flux){
        return VectorX.vectorX(FluxReactiveSeq.reactiveSeq(flux))
                      .materialize();
    }

    static <T> PersistentSetX<T> persistentSetX(Flux<T> flux){
        return PersistentSetX.persistentSetX(FluxReactiveSeq.reactiveSeq(flux))
                             .materialize();
    }
    static <T> OrderedSetX<T> orderedSetX(Flux<T> flux){
        return OrderedSetX.orderedSetX(FluxReactiveSeq.reactiveSeq(flux))
                          .materialize();
    }
    static <T> BagX<T> bagX(Flux<T> flux){
        return BagX.bagX(FluxReactiveSeq.reactiveSeq(flux))
                    .materialize();
    }

    static <T> PersistentQueueX<T> persistentQueueX(Flux<T> flux){
        return PersistentQueueX.persistentQueueX(FluxReactiveSeq.reactiveSeq(flux))
            .materialize();
    }

    static <T> ListX<T> listX(Flux<T> flux){
        return ListX.listX(FluxReactiveSeq.reactiveSeq(flux))
                    .materialize();
    }

    static <T> DequeX<T> dequeX(Flux<T> flux){
        return DequeX.dequeX(FluxReactiveSeq.reactiveSeq(flux))
                     .materialize();
    }

    static <T> SetX<T> setX(Flux<T> flux){
        return SetX.setX(FluxReactiveSeq.reactiveSeq(flux))
                    .materialize();
    }

    static <T> SortedSetX<T> sortedSetX(Flux<T> flux){
        return SortedSetX.sortedSetX(FluxReactiveSeq.reactiveSeq(flux))
                         .materialize();
    }

    static <T> QueueX<T> queueX(Flux<T> flux){
        return QueueX.queueX(FluxReactiveSeq.reactiveSeq(flux))
                        .materialize();
    }
}
