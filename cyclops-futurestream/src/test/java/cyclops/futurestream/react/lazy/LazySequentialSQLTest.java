package cyclops.futurestream.react.lazy;

import static cyclops.data.tuple.Tuple.tuple;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import cyclops.futurestream.LazyReact;
import cyclops.companion.Streams;
import cyclops.futurestream.react.base.BaseSequentialSQLTest;
import cyclops.futurestream.FutureStream;
import org.junit.Test;

public class LazySequentialSQLTest extends BaseSequentialSQLTest {

	@Override
	protected <U> FutureStream<U> of(U... array) {
		return LazyReact.sequentialBuilder().of(array);
	}

	@Override
	protected <U> FutureStream<U> ofThread(U... array) {
		return LazyReact.sequentialCommonBuilder().of(array);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return LazyReact.sequentialCommonBuilder().react(Arrays.asList(array));
	}

	Throwable ex;

	@Test
    public void futureStreamCJ(){
        assertEquals(asList(
                tuple("A", 1),
                tuple("B", 1)),
                Streams.oneShotStream(Stream.of("A", "B")).crossJoin(Streams.oneShotStream(Stream.of(1))).toList());
    }

	@Test(expected=X.class)
	public void testOnEmptyThrows() {

		ex = null;
		of().capture(e -> ex = e).onEmptyError(() -> new X()).toList();

		fail("Exception expected");
	}
}
