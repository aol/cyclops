package cyclops.monads.jdk;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.oath.anym.internal.adapters.OptionalAdapter;
import com.oath.anym.internal.monads.AnyMSeqImpl;
import org.junit.Test;
import org.testng.Assert;

import cyclops.async.QueueFactories;
import cyclops.collections.mutable.ListX;
import cyclops.reactive.FutureStream;

public class AnyMSeqTest {

	@Test
	public void testMergeP() {
		List<Integer> expected = Arrays.asList(new Integer[] { 10, 40, 50, 60 });
		AnyMSeq seq = new AnyMSeqImpl(Optional.of(10), OptionalAdapter.optional);
		AnyMSeq merged = seq.mergeP(QueueFactories.unboundedQueue(), FutureStream.builder().from(ListX.of(40, 50, 60)));
		org.junit.Assert.assertEquals(4, merged.count());
		merged.traversable().forEach(x -> {
			Assert.assertTrue(expected.contains(x));
		});
	}

	@Test
	public void testMergeP2() {
		List<Integer> expected = Arrays.asList(new Integer[] { 10, 40, 50, 60 });
		AnyMSeq seq = new AnyMSeqImpl(Optional.of(10), OptionalAdapter.optional);
		AnyMSeq merged = seq.mergeP(FutureStream.builder().from(ListX.of(40, 50, 60)));
		org.junit.Assert.assertEquals(4, merged.count());
		merged.traversable().forEach(x -> {
			Assert.assertTrue(expected.contains(x));
		});
	}
}
