package cyclops.collections.persistent.anyM;

import com.oath.cyclops.types.anyM.AnyMSeq;
import cyclops.collections.AbstractAnyMSeqTest;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.data.Comparators;
import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class OrderedSetXTest extends AbstractAnyMSeqTest<Witness.orderedSetX> {

	@Override
	public <T> AnyMSeq<Witness.orderedSetX,T> of(T... values) {
		return AnyM.fromOrderedSetX(OrderedSetX.of(Comparators.naturalOrderIdentityComparator(),values));
	}

	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> AnyMSeq<Witness.orderedSetX,T> empty() {
		return AnyM.fromOrderedSetX(OrderedSetX.empty(Comparators.naturalOrderIdentityComparator()));
	}
	 /* (non-Javadoc)
     * @see com.oath.cyclops.function.collections.extensions.AbstractAnyMSeqTest#whenGreaterThan2()
     */
    @Override
    @Test
    public void whenGreaterThan2() {

    }
	@Test
	public void prependAppend(){
		assertThat(of(1).prependS(Stream.of(2)).append(3).prepend(4).append(5,6).prependAll(7,8).insertAt(4,9).deleteBetween(1,2)
				.insertAtS(5,Stream.of(11,12)).stream().count(),equalTo(10L));
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
}
