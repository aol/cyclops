package cyclops.futurestream.react.lazy.sequence;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.futurestream.LazyReact;
import org.junit.Assert;
import org.junit.Test;

import cyclops.control.Maybe;
import cyclops.reactive.ReactiveSeq;
import cyclops.companion.Streams;
import cyclops.companion.Streamable;
import com.oath.cyclops.data.collections.extensions.CollectionX;

public class SequentialTest {
	@Test
	public void startsWith(){
		assertTrue(LazyReact.sequentialBuilder().of(1,2,3,4)
						.startsWith(Arrays.asList(1,2,3)));
	}

	@Test
	public void onEmptySwitchEmpty(){
		assertThat(LazyReact.sequentialBuilder().of()
							.onEmptySwitch(()-> LazyReact.sequentialBuilder().of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));

	}
	@Test
	public void onEmptySwitch(){
		assertThat(LazyReact.sequentialBuilder().of(4,5,6)
							.onEmptySwitch(()-> LazyReact.sequentialBuilder().of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));

	}

	@Test
	public void elapsedIsPositive(){


		assertTrue(LazyReact.sequentialBuilder().of(1,2,3,4,5).elapsed().noneMatch(t->t._2()<0));
	}
	@Test
	public void timeStamp(){


		assertTrue(LazyReact.sequentialBuilder().of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t._2() <= System.currentTimeMillis()));


	}
	@Test
	public void get0(){
		assertThat(LazyReact.sequentialBuilder().of(1).elementAtAndStream(0)._1(),equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5).elementAtAndStream(2)._1(),equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5).elementAtAndStream(2)._2().toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		LazyReact.sequentialBuilder().of(1).elementAtAndStream(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		LazyReact.sequentialBuilder().of().elementAtAndStream(0);
	}
	@Test
	public void elementAt0(){
		assertTrue(LazyReact.sequentialBuilder().of(1).elementAt(0).isPresent());
	}
	@Test
	public void elementAtMultple(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5).elementAt(2).toOptional().get(),equalTo(3));
	}
	@Test
	public void elementAt1(){
		assertFalse(LazyReact.sequentialBuilder().of(1).elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(LazyReact.sequentialBuilder().of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(LazyReact.sequentialBuilder().of(1).singleOrElse(null),equalTo(1));
	}
	@Test
	public void singleEmpty(){
		Assert.assertNull(LazyReact.sequentialBuilder().of().singleOrElse(null));
	}
	@Test
	public void single2(){

		Assert.assertNull(LazyReact.sequentialBuilder().of(1,2).singleOrElse(null));
	}
	@Test
	public void limitTime(){
		List<Integer> result = LazyReact.sequentialBuilder().of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.take(900,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,hasItems(1,2));
        assertThat(result,not(hasItems(4,5,6)));
	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.take(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
	    sleep(500);
		List<Integer> result = LazyReact.sequentialBuilder().of(0,1,2,3,4,5,6)
										.peek(i->sleep(i*300))
										.drop(700,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,hasItems(6));
		assertThat(result,not(hasItems(0)));
	}
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.drop(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}
	private int sleep(Integer i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {

		}
		return i;
	}
	@Test
	public void testSkipLast(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5)
							.dropRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(LazyReact.sequentialBuilder().of()
							.dropRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,4,5)
							.takeRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(LazyReact.sequentialBuilder().of()
							.takeRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(LazyReact.sequentialBuilder().of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(LazyReact.sequentialBuilder().of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(LazyReact.sequentialBuilder().of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(LazyReact.sequentialBuilder().of(1,2,3,4,5,6)
				.endsWith(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(LazyReact.sequentialBuilder().of()
				.endsWith(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWith(Arrays.asList()));
	}

	@Test
	public void streamable(){
		Streamable<Integer> repeat = LazyReact.sequentialBuilder().of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.streamable();

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}


	@Test
	public void splitBy(){
		assertThat( LazyReact.sequentialBuilder().of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._1().toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( LazyReact.sequentialBuilder().of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._2().toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		CollectionX<Integer> col = CollectionX.fromCollection(LazyReact.sequentialBuilder().of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection());


		assertThat(col.map(i->"hello"+i).toList().size(),equalTo(5));
		System.out.println("takeOne!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}

	@Test
	public void headAndTailTest(){
		Stream<String> s = Stream.of("hello","world");
		Iterator<String> it = s.iterator();
		String head = it.next();
		Stream<String> tail = Streams.stream(it);
		tail.forEach(System.out::println);
	}
	@Test
	public void testOfType() {



		assertThat(LazyReact.sequentialBuilder().of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(LazyReact.sequentialBuilder().of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(LazyReact.sequentialBuilder().of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test
	public void testCastPast() {
		LazyReact.sequentialBuilder().of(1, "a", 2, "b", 3, null).cast(Date.class).map(d -> d.getTime());




	}

	@Test
	public void flatMapCompletableFuture(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3).flatMapCompletableFuture(i->CompletableFuture.completedFuture(i+2))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapOptional(){
		assertThat(LazyReact.sequentialBuilder().of(1,2,3,null).concatMap(Maybe::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testIntersperse() {

		assertThat(LazyReact.sequentialBuilder().of(1,2,3)
						.intersperse(0)
						.toList(),
						equalTo(Arrays.asList(1,0,2,0,3)));




	}
	@Test(expected = ClassCastException.class)
	public void cast(){
		LazyReact.sequentialBuilder().of(1,2,3).cast(String.class).collect(Collectors.toList())
		.stream().map(i->i.getClass())
		.allMatch(c->Integer.class.equals(c));
	}
	@Test
	public void xMatch(){
		assertTrue(LazyReact.sequentialBuilder().of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}

}
