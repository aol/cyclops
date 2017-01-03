package com.aol.cyclops2.types;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import cyclops.stream.ReactiveSeq;
import cyclops.monads.transformers.ListT;
import cyclops.collections.ListX;
import cyclops.monads.Witness.stream;

/**
 * Represents a Traversable Monad Transformer, the monad transformer instance manipulates a nested non-scalar data type
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of the elements stored inside the traversable manipulated by this monad transformer
 */
public interface TransformerTraversable<T>{

    /**
     * Group the elements in this Traversable into batches of the provided size.
     * A List Transformer view into the data will be provided, ListTransformers allow nested Lists to be manipulated directly. 
     * The data will remain nested (as List's inside the current
     * Traversable), but the ListTransformer will be able to operate aggregate operations across all nested Lists, each operation 
     * on the List Transformer is applied, by the transformer to all Lists.
     * 
     * <pre>
     * {@code
     *   ListTSeq<Integer> transformer = ReactiveSeq.of(2,1,3,4,5,7,6,8,9,10)
     *                                              .groupedT(4);
     *   
     *   //ReactiveSeq[List[2,1,3,4],List[5,7,6,8],List[9,10]]
     *   
     *  ListTSeq<ListX<Integer>> nested = transformer.map(i->i*2)
     *                                               .sorted()
     *                                               .grouped(2);   
     *   
     *  //ReactiveSeq[List[List[2,4],List[6,8]],List[List[10,14],List[12,16]],List[List[18,20]]]
       }
       </pre>
      
       
     * 
     * @param groupSize Size of each batch of elements to be grouped into Lists 
     * @return List Transformer view into batched / grouped data
     */
    default ListT<stream,T> groupedT(final int groupSize) {
        return ListT.fromStream(stream().grouped(groupSize));
    }

    /**
     * Create a Sliding window into the data in this traversble.
     * A List Transformer view into the data will be provided, ListTransformers allow nested Lists to be manipulated directly. 
     * The data will remain nested (as List's inside the current
     * Traversable), but the ListTransformer will be able to operate aggregate operations across all nested Lists, each operation 
     * on the List Transformer is applied, by the transformer to all Lists.
     * 
     * <pre>
     * {@code 
     *  ListTSeq<Integer> sliding = ListX.of(1,2,3,4,5,6,7)
     *                                   .sliding(3,2);
     * 
     *  //ReactiveSeq[List[1,2,3],List[3,4,5],List[5,6,7],List[7]]
     * }
     * </pre>
     * 
     * 
     * @param windowSize Size of sliding window
     * @param increment Increment between windows
     * @return List Transformer view into the Sliding windows
     */
    default ListT<stream,T> slidingT(final int windowSize, final int increment) {
        return ListT.fromStream(stream().sliding(windowSize, increment));
    }

    /**
     * Create a Sliding window into the data in this traversble.
     * A List Transformer view into the data will be provided, ListTransformers allow nested Lists to be manipulated directly. 
     * The data will remain nested (as List's inside the current
     * Traversable), but the ListTransformer will be able to operate aggregate operations across all nested Lists, each operation 
     * on the List Transformer is applied, by the transformer to all Lists. ( @see TransformerTraversable#grouped(int) )
     
       <pre>
       {@code 
        ListTSeq<Integer> sliding = ListX.of(1,2,3,4,5,6,7)
                                         .sliding(3);
       
       //ReactiveSeq[List[1,2,3],List[2,3,4],List[4,5,6],List[6,7]]
       }
       </pre>
     * 
     * 
     * @param windowSize Size of sliding window
     * @return List Transformer view into the Sliding windows
     */
    default ListT<stream,T> slidingT(final int windowSize) {
        return ListT.fromStream(stream().sliding(windowSize));
    }

    /**
     * Create a Traversable batched by List, where each batch is populated until
     * the predicate holds.
     * A List Transformer view into the data will be provided, ListTransformers allow nested Lists to be manipulated directly. 
     * The data will remain nested (as List's inside the current
     * Traversable), but the ListTransformer will be able to operate aggregate operations across all nested Lists, each operation 
     * on the List Transformer is applied, by the transformer to all Lists.
     * 
     * <pre>
     * {@code 
     *  ListTSeq<Integer> grouped = ReactiveSeq.of(1,2,3,4,5,6)
     *                                         .groupedWhile(i->i%3!=0)
     *                                         .toList();
     *                                         
     *  //ReactiveSeq[List[1,2,3],List[4,5,6]]                                       
     *  
     * }
     * </pre>
     * 
     * @param predicate Predicate to determine batch termination point
     * @return List Transformer view into batched Traversable
     */
    default ListT<stream,T> groupedUntilT(final Predicate<? super T> predicate) {
        return ListT.fromStream(stream().groupedUntil(predicate));
    }

    /**
     * List Transformer view into batched Lists where
     * each List is populated while the supplied bipredicate holds. The
     * bipredicate recieves the List from the last window as well as the
     * current value and can choose to aggregate the current value or create a
     * new window
     * 
    
     * 
     * 
     * @param predicate Predicate to determine batch
     * @return List Transformer view into batched Traversable
     */
    default ListT<stream,T> groupedStatefullyUntilT(final BiPredicate<ListX<? super T>, ? super T> predicate) {
        return ListT.fromStream(stream().groupedStatefullyUntil(predicate));
    }

    /**
     * Create a Traversable batched by List, where each batch is populated while
     * the predicate holds.
     * A List Transformer view into the data will be provided, ListTransformers allow nested Lists to be manipulated directly. 
     * The data will remain nested (as List's inside the current
     * Traversable), but the ListTransformer will be able to operate aggregate operations across all nested Lists, each operation 
     * on the List Transformer is applied, by the transformer to all Lists. 
     * <pre>
     * {@code 
     *  ListTSeq<Integer> grouped = ReactiveSeq.of(1,2,3,4,5,6,7,8,9,5)
     *                                         .groupedUntil(i->i==5)
     *                                         .toList();
     *                                         
     *  //ReactiveSeq[List[1,2,3,4,5],List[6,7,8,9,5]]                                       
     *  
     * }
     * </pre>
     * @param predicate Predicate to determine batch termination point
     * @return List Transformer view into batched Traversable
     */
    default ListT<stream,T> groupedWhileT(final Predicate<? super T> predicate) {
        return ListT.fromStream(stream().groupedUntil(predicate));
    }

    public ReactiveSeq<T> stream();
}
