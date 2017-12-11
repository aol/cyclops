package cyclops.futurestream.react.lazy;

import java.util.function.Supplier;

import com.oath.cyclops.react.ThreadPools;
import cyclops.futurestream.react.base.BaseNumberOperationsTest;
import com.oath.cyclops.async.LazyReact;
import cyclops.reactive.FutureStream;

public class LazyFutureNumberOperationsTest extends BaseNumberOperationsTest {
	@Override
	protected <U> FutureStream<U> of(U... array) {
		return new LazyReact().of(array);
	}
	@Override
	protected <U> FutureStream<U> ofThread(U... array) {
		return new LazyReact(ThreadPools.getCommonFreeThread()).of(array);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return LazyReact.parallelBuilder().ofAsync(array);

	}


}
