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

import cyclops.futurestream.react.base.BaseSeqLazyTest;
import cyclops.async.LazyReact;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import cyclops.reactive.FutureStream;

public class LazySeqLazyTest extends BaseSeqLazyTest {

	  @Test
	    public void testZipDifferingLength() {
	        List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d"))
	        		.foldLazy(s->s.toList())
	        		.get();

	        assertEquals(2, list.size());
	        assertTrue(asList(1,2).contains( list.get(0)._1()));
	        assertTrue(""+list.get(1)._2(),asList(1,2).contains( list.get(1)._1()));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(0)._2()));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(1)._2()));


	    }


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





}
