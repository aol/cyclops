package cyclops.collections.persistent.anyM;

import com.oath.cyclops.types.anyM.AnyMSeq;
import cyclops.collections.AbstractAnyMSeqTest;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.companion.Reducers;
import cyclops.monads.AnyM;
import cyclops.monads.DataWitness;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class PersistentSetXTest extends AbstractAnyMSeqTest<Witness.persistentSetX> {

	@Override
	public <T> AnyMSeq<Witness.persistentSetX,T> of(T... values) {
		return AnyM.fromPersistentSetX(PersistentSetX.of(values));
	}

	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<Witness.persistentSetX,T> empty() {
		return AnyM.fromPersistentSetX(PersistentSetX.empty());
	}
	 /* (non-Javadoc)
     * @see com.oath.cyclops.function.collections.extensions.AbstractAnyMSeqTest#whenGreaterThan2()
     */
    @Override
    @Test
    public void whenGreaterThan2() {

    }

	@Test
	public void testRecover1(){
		assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(e->"hello").join(" "),equalTo("hello"));
	}
	@Test
	public void testRecover2(){
		assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(RuntimeException.class,e->"hello").join(" "),equalTo("hello"));
	}

    @Test
    public void testRetry(){
        of(1,2,3).retry(i->i+2).printOut();
    }
    @Test @Ignore
    public void testSorted() {



    }
	@Test
	public void testScanLeftStringConcatMonoid() {
		assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), hasItems("", "a", "ab", "abc"));
	}
}
