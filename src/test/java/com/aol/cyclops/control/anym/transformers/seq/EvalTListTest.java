package com.aol.cyclops.control.anym.transformers.seq;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.control.monads.transformers.seq.EvalTSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops.types.anyM.AnyMSeq;
public class EvalTListTest extends AbstractAnyMSeqOrderedDependentTest{

	@Override
	public <T> AnyMSeq<T> of(T... values) {
		return AnyM.fromEvalTSeq(EvalT.fromIterable(ListX.of(values).map(Eval::now)));
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<T> empty() {
		return AnyM.fromIterable(EvalTSeq.emptyList());
	}
	
	@Test
    public void sliding() {
	   // of(1, 2, 3, 4, 5, 6).printOut();
	    of(1, 2, 3, 4, 5, 6).forEach(System.out::println);
        List<List<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());
        System.out.println(list);

        assertThat(list.get(0), hasItems(1, 2));
        assertThat(list.get(1), hasItems(2, 3));
    }
	

}

