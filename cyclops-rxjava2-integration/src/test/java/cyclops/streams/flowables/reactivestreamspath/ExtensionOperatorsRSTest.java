package cyclops.streams.flowables.reactivestreamspath;


import cyclops.collections.mutable.ListX;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.companion.rx2.Flowables;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.control.Option.some;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ExtensionOperatorsRSTest {

	@Test
	public void flatMapStreamFilterSimple(){
		assertThat(Flowables.of(1,2).flatMap(i-> ReactiveSeq.of(i).filter(x->x<2))
						.to(Streamable::fromStream).collect(Collectors.toList()),
				Matchers.equalTo(Arrays.asList(1)));
	}
    @Test
    public void combine(){
        assertThat(Flowables.of(1,1,2,3)
                   .combine((a, b)->a.equals(b), Semigroups.intSum)
                   .to(Streamable::fromStream).toListX(),equalTo(ListX.of(4,3)));

    }
	@Test
	public void subStream(){
		List<Integer> list = Flowables.of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(Flowables.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(Flowables.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(Flowables.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(Flowables.of(Flowables.of(1, 2, 3),
        		Flowables.of(1, 3, 2), Flowables.of(2, 1, 3), Flowables.of(2, 3, 1), Flowables.of(3, 1, 2), Flowables.of(3, 2, 1)).map(s->s.toList()).toList()));
    }

    @Test
    public void emptyAllCombinations() {
        assertThat(Flowables.of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(Flowables.of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }



    @Test
    public void emptyCombinations() {
        assertThat(Flowables.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(Flowables.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(Flowables.of()
							.onEmptySwitch(()-> Flowables.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));

	}
	@Test
	public void onEmptySwitch(){
		assertThat(Flowables.of(4,5,6)
							.onEmptySwitch(()-> Flowables.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));

	}

	@Test
	public void elapsedIsPositive(){


		assertTrue(Flowables.of(1,2,3,4,5).elapsed().noneMatch(t->t._2()<0));
	}
	@Test
	public void timeStamp(){


		assertTrue(Flowables.of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t._2() <= System.currentTimeMillis()));


	}
	@Test
	public void elementAt0(){
		assertThat(Flowables.of(1).elementAt(0),equalTo(some(1)));
	}
	@Test
	public void getMultple(){
		assertThat(Flowables.of(1,2,3,4,5).elementAt(2),equalTo(some(3)));
	}

	@Test
	public void getMultiple1(){
		assertFalse(Flowables.of(1).elementAt(1).isPresent());
	}
	@Test
	public void getEmpty(){
		assertFalse(Flowables.of().elementAt(0).isPresent());
	}
	@Test
	public void get0(){
		assertTrue(Flowables.of(1).elementAt(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(Flowables.of(1,2,3,4,5).elementAt(2).orElse(-1),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(Flowables.of(1).elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(Flowables.of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(Flowables.of(1).single().orElse(null),equalTo(1));
	}
	@Test
	public void singleEmpty(){
		assertTrue(Flowables.of().single().orElse(null)==null);
	}
	@Test
	public void single2(){
		assertTrue(Flowables.of(1,2).single().orElse(null)==null);
	}
	@Test
	public void singleOptionalTest(){
		assertThat(Flowables.of(1).single().orElse(null),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(Flowables.of().single().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(Flowables.of(1,2).single().isPresent());
	}
	@Test
	public void limitTime(){
		List<Integer> result = Flowables.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = Flowables.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = Flowables.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
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
		assertThat(Flowables.of(1,2,3,4,5)
							.skipLast(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(Flowables.of()
							.skipLast(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(Flowables.of(1,2,3,4,5)
							.limitLast(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLast1(){
		assertThat(Flowables.of(1,2,3,4,5)
				.limitLast(1)
				.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(Flowables.of()
							.limitLast(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(Flowables.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(Flowables.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(Flowables.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(Flowables.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(Flowables.of()
				.endsWithIterable(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(Flowables.<Integer>of()
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(Flowables.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(Flowables.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(Flowables.of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(Flowables.of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(Flowables.of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(Flowables.<Integer>of()
				.endsWith(Stream.of()));
	}
	@Test
	public void anyMTest(){
		List<Integer> list = Flowables.of(1,2,3,4,5,6)
								.anyM().filter(i->i>3).stream().toList();

		assertThat(list,equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void streamable(){
		Streamable<Integer> repeat = Flowables.of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.streamable();

		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}

	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = Flowables.of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.lazyStreamableSynchronized();

		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	@Test
	public void splitBy(){
		assertThat( Flowables.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._1().toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( Flowables.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._2().toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = Flowables.of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = Flowables.of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollectionSynchronized();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c-> AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.to(Streamable::fromStream).collect(Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c-> AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.to(Streamable::fromStream).collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
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



		assertThat(Flowables.of(1, "a", 2, "b", 3).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(Flowables.of(1, "a", 2, "b", 3).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(Flowables.of(1, "a", 2, "b", 3)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}


	@Test
	public void flatMapCompletableFuture(){
		assertThat(Flowables.of(1,2,3).flatMapAnyM(i-> AnyM.fromArray(i+2))
				  								.to(Streamable::fromStream).collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	}

	@Test
	public void testIntersperse() {

		assertThat(Flowables.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));




	}

	@Test
	public void xMatch(){
		assertTrue(Flowables.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}


}
