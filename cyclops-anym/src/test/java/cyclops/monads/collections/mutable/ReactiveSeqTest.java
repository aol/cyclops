package cyclops.monads.collections.mutable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


import com.oath.cyclops.anym.AnyMSeq;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.data.Vector;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Test;

import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;

public class ReactiveSeqTest extends AbstractAnyMSeqOrderedDependentTest<reactiveSeq> {

	@Override
	public <T> AnyMSeq<reactiveSeq,T> of(T... values) {
		return AnyM.fromStream(ReactiveSeq.of(values));
	}
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<reactiveSeq,T> empty() {
		return AnyM.fromStream(ReactiveSeq.empty());
	}


    @Test
  public void vector(){


      //cyclopsX fast eager persistent Vector (bitmapped vector trie)
      Vector<Integer> vec = Vector.of(1,2,3,4);
      vec.map(i->i*2)
         .filter(i->i<3)
         .peek(i->System.out.println("hello"));
      //prints hello


      //cyclopsX lazy persistent Vector
      VectorX<Integer> vecX = VectorX.of(1,2,3,4);
      VectorX<Integer> filteredX = vecX.map(i->i*2)
                                       .filter(i->i<3)
                                       .peek(i->System.out.println("hello"));
      //doesn't print until the vector is used

      filteredX.get(5); //will print "hello"
    }


}

