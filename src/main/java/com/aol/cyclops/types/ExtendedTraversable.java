package com.aol.cyclops.types;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;



public interface ExtendedTraversable<T> extends Traversable<T>,
                                                TransformerTraversable<T>,
                                                Foldable<T>, 
                                                Iterable<T>, 
                                                ConvertableSequence<T> {
	
	
	/**
	 * Generate the permutations based on values in the SequenceM Makes use of
	 * Streamable to store intermediate stages in a collection
	 * 
	 * 
	 * @return Permutations from this SequenceM
	 */
	 default ExtendedTraversable<ReactiveSeq<T>> permutations(){
		 return stream().permutations();
	 }


	/**
	 * <pre>
	 * {@code
	 *   ReactiveSeq.of(1,2,3).combinations(2)
	 *   
	 *   //SequenceM[SequenceM[1,2],SequenceM[1,3],SequenceM[2,3]]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param size
	 *            of combinations
	 * @return All combinations of the elements in this stream of the specified
	 *         size
	 */
	 default ExtendedTraversable<ReactiveSeq<T>> combinations(int size){
		 return stream().combinations(size);
	 }

	/**
	 * <pre>
	 * {@code
	 *   ReactiveSeq.of(1,2,3).combinations()
	 *   
	 *   //SequenceM[SequenceM[],SequenceM[1],SequenceM[2],SequenceM[3].SequenceM[1,2],SequenceM[1,3],SequenceM[2,3]
	 *   			,SequenceM[1,2,3]]
	 * }
	 * </pre>
	 * 
	 * 
	 * @return All combinations of the elements in this stream
	 */
	 default ExtendedTraversable<ReactiveSeq<T>> combinations(){
		 return stream().combinations();
	 }


    @Override
    default ReactiveSeq<T> stream() {
      
        return ConvertableSequence.super.stream();
    }

	
}
