package cyclops.monads.data;

import com.oath.cyclops.anym.AnyMSeq;
import cyclops.companion.Reducers;
import cyclops.data.LazySeq;
import cyclops.monads.AnyM;
import cyclops.monads.Witness.lazySeq;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LazySeqAnyMTest extends AbstractAnyMSeqOrderedDependentTest<lazySeq> {

	@Override
	public <T> AnyMSeq<lazySeq,T> of(T... values) {
		return AnyM.fromLazySeq(LazySeq.of(values));
	}

	@Override
	public <T> AnyMSeq<lazySeq,T> empty() {
		return AnyM.fromLazySeq(LazySeq.empty());
	}



    @Test @Ignore //only works for lazy data types
    public void testRecover1(){

     }
    @Test  @Ignore //only works for lazy data types
    public void testRecover2(){
    }
    @Test
    public void testScanRightSumMonoid() {
      assertThat(of("a", "ab", "abc").peek(System.out::println)
        .map(str -> str.length())
        .peek(System.out::println)
        .scanRight(Reducers.toTotalInt()).toList(), is(asList(6,5,3,0)));

    }
}

