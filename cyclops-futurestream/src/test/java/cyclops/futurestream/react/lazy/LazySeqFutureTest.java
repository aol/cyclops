package cyclops.futurestream.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.oath.cyclops.react.ThreadPools;
import cyclops.futurestream.react.base.BaseSeqFutureTest;
import cyclops.async.LazyReact;
import cyclops.reactive.FutureStream;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

public class LazySeqFutureTest extends BaseSeqFutureTest {

	  @Test
	    public void testZipDifferingLength() {
	        List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d"))
	        		.foldFuture(s->s
	        		.toList()).toOptional()
	        		.get();

	        assertEquals(2, list.size());
	        assertTrue(asList(1,2).contains( list.get(0)._1()));
	        assertTrue(""+list.get(1)._2(),asList(1,2).contains( list.get(1)._1()));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(0)._2()));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(1)._2()));


	    }



		@Override
		protected <U> FutureStream<U> of(U... array) {
			return new LazyReact(ThreadPools.getCurrentThreadExecutor()).of(array);
		}

		@Override
		protected <U> FutureStream<U> ofThread(U... array) {
			return new LazyReact(ThreadPools.getCurrentThreadExecutor()).of(array);
		}

		@Override
		protected <U> FutureStream<U> react(Supplier<U>... array) {
			return new LazyReact().react(Arrays.asList(array));
		}





}
