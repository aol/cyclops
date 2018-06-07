package cyclops.monads.collections.persistent;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.reactive.collections.immutable.PersistentSetX;
import cyclops.companion.Reducers;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.collections.AbstractAnyMSeqTest;
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

	@Override
	public <T> AnyMSeq<Witness.persistentSetX,T> empty() {
		return AnyM.fromPersistentSetX(PersistentSetX.empty());
	}


	@Test
	public void testRecover1(){
		assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(e->"hello").join(" "),equalTo("hello"));
	}
	@Test
	public void testRecover2(){
		assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(RuntimeException.class,e->"hello").join(" "),equalTo("hello"));
	}


    @Test @Ignore
    public void testSorted() {



    }
	@Test
	public void testScanLeftStringConcatMonoid() {
		assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), hasItems("", "a", "ab", "abc"));
	}
}
