package cyclops.monads.data;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.companion.Reducers;
import cyclops.data.HashSet;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.hashSet;
import cyclops.monads.collections.AbstractAnyMSeqTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class HashSetAnyMTest extends AbstractAnyMSeqTest<hashSet> {

	@Override
	public <T> AnyMSeq<hashSet,T> of(T... values) {
		return AnyM.fromHashSet(HashSet.of(values));
	}

	@Override
	public <T> AnyMSeq<Witness.hashSet,T> empty() {
		return AnyM.fromHashSet(HashSet.empty());
	}


	@Test @Ignore //lazy data structures only
	public void testRecover1(){
		assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(e->"hello").join(" "),equalTo("hello"));
	}
	@Test @Ignore //lazy data structures only
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
