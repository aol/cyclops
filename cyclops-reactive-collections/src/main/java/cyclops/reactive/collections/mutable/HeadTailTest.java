package cyclops.reactive.collections.mutable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cyclops.reactive.collections.mutable.ListX;

public class HeadTailTest {



	@Test
	public void empty(){

		Assert.assertFalse(ListX.empty().headAndTail().headMaybe().isPresent());
		Assert.assertFalse(ListX.empty().headAndTail().headOptional().isPresent());
		Assert.assertTrue(ListX.empty().headAndTail().headStream().size()==0);


	}
}
