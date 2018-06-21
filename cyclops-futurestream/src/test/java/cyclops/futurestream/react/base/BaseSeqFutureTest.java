package cyclops.futurestream.react.base;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.function.Supplier;

import cyclops.control.Option;
import cyclops.futurestream.FutureStream;
import org.junit.Before;
import org.junit.Test;


//see BaseSequentialSeqTest for in order tests
public abstract class BaseSeqFutureTest {
	abstract protected <U> FutureStream<U> of(U... array);
	abstract protected <U> FutureStream<U> ofThread(U... array);
	abstract protected <U> FutureStream<U> react(Supplier<U>... array);
	FutureStream<Integer> empty;
	FutureStream<Integer> nonEmpty;


	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);

	}
	@Test
	public void testMax(){
		assertThat(of(1,2,3,4,5).foldFuture(s->s.maximum((t1, t2) -> t1-t2)).orElse(Option.none()),is(Option.of(5)));
	}

}
