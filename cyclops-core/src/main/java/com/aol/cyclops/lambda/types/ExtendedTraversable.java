package com.aol.cyclops.lambda.types;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.traits.ConvertableSequence;



public interface ExtendedTraversable<T> extends Traversable<T>,Foldable<T>, Iterable<T>, ConvertableSequence<T> {
	
	
	/**
	 * Generate the permutations based on values in the SequenceM Makes use of
	 * Streamable to store intermediate stages in a collection
	 * 
	 * 
	 * @return Permutations from this SequenceM
	 */
	 default ExtendedTraversable<SequenceM<T>> permutations(){
		 return stream().permutations();
	 }


	/**
	 * <pre>
	 * {@code
	 *   SequenceM.of(1,2,3).combinations(2)
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
	 default ExtendedTraversable<SequenceM<T>> combinations(int size){
		 return stream().combinations();
	 }

	/**
	 * <pre>
	 * {@code
	 *   SequenceM.of(1,2,3).combinations()
	 *   
	 *   //SequenceM[SequenceM[],SequenceM[1],SequenceM[2],SequenceM[3].SequenceM[1,2],SequenceM[1,3],SequenceM[2,3]
	 *   			,SequenceM[1,2,3]]
	 * }
	 * </pre>
	 * 
	 * 
	 * @return All combinations of the elements in this stream
	 */
	 default ExtendedTraversable<SequenceM<T>> combinations(){
		 return stream().combinations();
	 }

	
}
