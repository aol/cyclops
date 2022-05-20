package cyclops.futurestream.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.futurestream.react.base.BaseSeqTest;
import cyclops.futurestream.FutureStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;

import cyclops.futurestream.LazyReact;
import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.Signal;
import com.oath.cyclops.react.ThreadPools;

public class LazySeqObjectPoolingTest extends BaseSeqTest {
	volatile Throwable error;
	@Test
	public void iterate(){
		error= null;
		 new LazyReact()
		 		.objectPoolingOn()
		 		.iterate(1,i->i+1)
		 		.limit(1_000_000)
		 		.map(x->x+2)
		 		.capture(e->{error=e; e.printStackTrace();})
		 		.forEach(a->{});
		 if(error!=null)
			 error.printStackTrace();

		 assertNull(error);
	}
	@Test
	public void copy(){
		of(1,2,3,4,5,6)
				.map(i->i+2)
				.copy(5)
				.forEach(s -> System.out.println(s.toList()));
	}
	@Test
    public void testCycle() {
    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().limit(6).toList());

    }
	@Test
	public void lazyCollection(){
		Collection<Integer> col = of(1,2,3,4,5,6)
				.map(i->i+2).to()
				.lazyCollection();

		assertThat(col.size(),equalTo(6));
	}
	@Test
	public void switchOnNextMultiple(){
		assertThat(react(()->1,()->2).mergeLatest( react(()->'a',()->'b'),
						react(()->100,()->200)).toList().size(),equalTo(6));
	}

	@Test
	public void batchByTime2(){
		for(int i=0;i<5;i++)
	    {
		    System.out.println(i);
			assertThat(react(()->1,()->2,()->3,()->4,()->5,()->{sleep(150);return 6;})

							.groupedByTime(10,TimeUnit.MICROSECONDS)
								.peek(System.out::println)
							.toList()
							.get(0)
							,not(hasItem(6)));
		}
	}

	@Test
	public void testZipWithFutures(){
		FutureStream stream = of("a","b");
		FutureStream<Tuple2<Integer,String>> seq = of(1,2).actOnFutures().zip(stream);
		List<Tuple2<Integer,String>> result = seq.block().listView();//.map(tuple -> Tuple.tuple(tuple.v1.join(),tuple.v2)).collect(CyclopsCollectors.toList());
		assertThat(result.size(),is(asList(tuple(1,"a"),tuple(2,"b")).size()));
	}

	@Test
	public void testZipWithFuturesStream(){
		Stream stream = of("a","b");
		FutureStream<Tuple2<Integer,String>> seq = of(1,2).actOnFutures().zip(stream);
		List<Tuple2<Integer,String>> result = seq.block().listView();//.map(tuple -> Tuple.tuple(tuple.v1.join(),tuple.v2)).collect(CyclopsCollectors.toList());
		assertThat(result.size(),is(asList(tuple(1,"a"),tuple(2,"b")).size()));
	}
	@Test
	public void testZipWithFuturesCoreStream(){
		Stream stream = Stream.of("a","b");
		FutureStream<Tuple2<Integer,String>> seq = of(1,2).actOnFutures().zip(stream);
		List<Tuple2<Integer,String>> result = seq.block().listView();//.map(tuple -> Tuple.tuple(tuple.v1.join(),tuple.v2)).collect(CyclopsCollectors.toList());
		assertThat(result.size(),is(asList(tuple(1,"a"),tuple(2,"b")).size()));
	}


	@Test
	public void testZipFuturesWithIndex(){

		 FutureStream<Tuple2<String,Long>> seq = of("a","b").actOnFutures().zipWithIndex();
		List<Tuple2<String,Long>> result = seq.block().listView();//.map(tuple -> Tuple.tuple(tuple.v1.join(),tuple.v2)).collect(CyclopsCollectors.toList());
		assertThat(result.size(),is(asList(tuple("a",0l),tuple("b",1l)).size()));
	}
	@Test
	public void duplicateFutures(){
		List<String> list = of("a","b").actOnFutures().duplicate()._1().block().listView();
		assertThat(sortedList(list),is(asList("a","b")));
	}
	private <T> List<T> sortedList(List<T> list) {
		return list.stream().sorted().collect(Collectors.toList());
	}

	@Test
	public void duplicateFutures2(){
		List<String> list = of("a","b").actOnFutures().duplicate()._2().block().listView();
		assertThat(sortedList(list),is(asList("a","b")));
	}



	@Test
	public void batchSinceLastReadIterator() throws InterruptedException{

			Iterator<Collection<Integer>> it = of(1,2,3,4,5,6).chunkLastReadIterator();


			List<Integer> list = new ArrayList<>();
			while(it.hasNext())
				list.addAll(it.next());



			assertThat(list.size(),equalTo(6));





	}
	@Test
	public void batchSinceLastRead() throws InterruptedException{
		List<Collection> cols = of(1,2,3,4,5,6).chunkSinceLastRead().peek(System.out::println).peek(it->{sleep(50);}).collect(Collectors.toList());

		System.out.println(cols.get(0));
		assertThat(cols.get(0).size(),is(1));
		assertThat(cols.size(),greaterThan(0));




	}

	@Test
	public void zipFastSlow() {
		Queue q = new Queue();
		LazyReact.parallelBuilder().generate(() -> sleep(100))
				.then(it -> q.add("100")).runThread(new Thread());
		new LazyReact().of(1, 2, 3, 4, 5, 6).zip(q.stream())
				.peek(it -> System.out.println(it))
				.collect(Collectors.toList());

	}


	@Test
	public void reactInfinitely(){
		 assertThat(LazyReact.sequentialBuilder().generate(() -> "100")
		 	.limit(100)
		 	.toList().size(),equalTo(100));
	}
	@Test
	public void streamFromQueue() {
		assertThat( LazyReact.sequentialBuilder().generate(() -> "100")
			.limit(100)
			.withQueueFactory(QueueFactories.boundedQueue(10)).toQueue()
			.stream().collect(Collectors.toList()).size(),equalTo(100));

	}
	@Test
	public void testBackPressureWhenZippingUnevenStreams2() {

		Queue fast = LazyReact.parallelBuilder().withExecutor(new ForkJoinPool(2)).generateAsync(() -> "100")
				.peek(System.out::println)
				.withQueueFactory(QueueFactories.boundedQueue(10)).toQueue();

		new Thread(() -> {


			LazyReact.parallelBuilder().withExecutor(new ForkJoinPool(2)).range(0,1000)
			.peek(System.out::println)
			.peek(c -> sleep(10))
					.zip(fast.stream()).forEach(it -> {

					});
		}).start();
		;
		fast.setSizeSignal(Signal.queueBackedSignal());
		int max = fast.getSizeSignal().getContinuous().stream()
				.mapToInt(it -> (int) it).limit(50).max().getAsInt();

		assertThat(max, lessThan(11));
	}



	@Test
	public void testOfType() {

		assertThat(of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));
		assertThat(of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));
		assertThat(of(1, "a", 2, "b", 3, null)
				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));
	}
	@Test @Ignore
	public void shouldZipTwoInfiniteSequences() throws Exception {

		final FutureStream<Integer> units = new LazyReact(ThreadPools.getCommonFreeThread()).iterate(1, n -> n+1);
		final FutureStream<Integer> hundreds = new LazyReact(ThreadPools.getCommonFreeThread()).iterate(100, n-> n+100);
		final FutureStream<String> zipped = units.zip(hundreds, (n, p) -> n + ": " + p);


		assertThat(zipped.limit(5).join(),equalTo(of("1: 100", "2: 200", "3: 300", "4: 400", "5: 500").join()));
	}

	@Test
	public void shouldZipFiniteWithInfiniteSeq() throws Exception {
		ThreadPools.setUseCommon(false);
		final FutureStream<Integer> units = new LazyReact(ThreadPools.getCommonFreeThread()).iterate(1, n -> n+1).limit(5);
		final FutureStream<Integer> hundreds = new LazyReact(ThreadPools.getCommonFreeThread()).iterate(100, n-> n+100); // <-- MEMORY LEAK! - no auto-closing yet, so writes infinetely to it's async queue
		final FutureStream<String> zipped = units.zip(hundreds, (n, p) -> n + ": " + p);

		assertThat(zipped.limit(5).join(),equalTo(of("1: 100", "2: 200", "3: 300", "4: 400", "5: 500").join()));
		ThreadPools.setUseCommon(true);
	}

	@Test
	public void shouldZipInfiniteWithFiniteSeq() throws Exception {
		ThreadPools.setUseCommon(false);
		final FutureStream<Integer> units = new LazyReact(ThreadPools.getCommonFreeThread()).iterate(1, n -> n+1); // <-- MEMORY LEAK!- no auto-closing yet, so writes infinetely to it's async queue
		final FutureStream<Integer> hundreds = new LazyReact(ThreadPools.getCommonFreeThread()).iterate(100, n-> n+100).limit(5);
		final FutureStream<String> zipped = units.zip(hundreds, (n, p) -> n + ": " + p);
		assertThat(zipped.limit(5).join(),equalTo(of("1: 100", "2: 200", "3: 300", "4: 400", "5: 500").join()));
		ThreadPools.setUseCommon(true);
	}


	@Test
	public void testCastPast() {
		assertThat(
				of(1, "a", 2, "b", 3, null).capture(e -> e.printStackTrace())
						.cast(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3, null));

	}

	@Override
	protected <U> FutureStream<U> of(U... array) {
		return new LazyReact()
							.objectPoolingOn()
							.of(array);
	}
	@Override
	protected <U> FutureStream<U> ofThread(U... array) {
		return new LazyReact()
							.objectPoolingOn()
							.of(array);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return new LazyReact().objectPoolingOn()
								.ofAsync(array);
	}
	protected Object sleep(int i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
    @Override
    public void testSkipUntilWithNullsInclusive() {
        Supplier<FutureStream<Integer>> s = () -> of(1, 2, null, 3, 4, 5);

        assertTrue(s.get().dropUntilInclusive(i -> true).toList().size()==5);
    }
    @Test
    public void testSkipUntilInclusive() {
        Supplier<FutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

        assertEquals(asList(), s.get().dropUntil(i -> false).toList());
        assertTrue(s.get().dropUntilInclusive(i -> true).toList().size()==4);
    }

}
