package cyclops.monads.collections.persistent;


import com.oath.cyclops.anym.AnyMSeq;
import cyclops.reactive.collections.immutable.OrderedSetX;
import cyclops.monads.AnyM;
import cyclops.companion.Comparators;
import cyclops.monads.Witness.orderedSetX;
import cyclops.monads.collections.AbstractAnyMSeqTest;
import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class OrderedSetXTest extends AbstractAnyMSeqTest<orderedSetX> {

	@Override
	public <T> AnyMSeq<orderedSetX,T> of(T... values) {
		return AnyM.fromOrderedSetX(OrderedSetX.of(Comparators.naturalOrderIdentityComparator(),values));
	}


	@Override
	public <T> AnyMSeq<orderedSetX,T> empty() {
		return AnyM.fromOrderedSetX(OrderedSetX.empty(Comparators.naturalOrderIdentityComparator()));
	}

	@Test
	public void prependAppend(){
		assertThat(of(1).prependStream(Stream.of(2)).append(3).prepend(4).appendAll(5,6).prependAll(7,8).insertAt(4,9).deleteBetween(1,2)
				.insertStreamAt(5,Stream.of(11,12)).stream().count(),equalTo(10L));
	}
	@Test
	public void testRecover1(){
		assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(e->"hello").join(" "),equalTo("hello"));
	}
    @Test
    public void testRecover2(){
        assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(RuntimeException.class,e->"hello").join(" "),equalTo("hello"));
    }

}
