package cyclops.monads.collections.persistent;


import com.oath.cyclops.anym.AnyMSeq;
import cyclops.reactive.collections.immutable.BagX;
import cyclops.companion.Reducers;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.bagX;
import cyclops.monads.collections.AbstractAnyMSeqTest;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Comparator.comparing;
import static cyclops.data.tuple.Tuple.tuple;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BagXTest extends AbstractAnyMSeqTest<bagX> {

	@Override
	public <T> AnyMSeq<bagX,T> of(T... values) {
		return AnyM.fromBagX(BagX.of(values));
	}

	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<bagX,T> empty() {
		return AnyM.fromBagX(BagX.empty());
	}
	 /* (non-Javadoc)
     * @see com.oath.cyclops.function.collections.extensions.AbstractAnyMSeqTest#whenGreaterThan2()
     */
    @Override
    @Test
    public void whenGreaterThan2() {

    }


	@Test @Ignore
	public void testSorted() {



	}
	@Test
	public void testScanLeftStringConcatMonoid() {
		assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), hasItems("", "a", "ab", "abc"));
	}
}
