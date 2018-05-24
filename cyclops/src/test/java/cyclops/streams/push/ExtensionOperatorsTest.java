package cyclops.streams.push;


import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.Future;

import cyclops.control.Maybe;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.companion.Streamable;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ExtensionOperatorsTest {

	@Test
	public void flatMapStreamFilterSimple(){
		assertThat(Spouts.of(1,null).flatMap(i->Spouts.of(i).filter(Objects::nonNull))
						.collect(Collectors.toList()),
				Matchers.equalTo(Arrays.asList(1)));
	}
    @Test
    public void combine(){
        assertThat(Spouts.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toList(),equalTo(Arrays.asList(4,3)));

    }
	@Test
	public void subStream(){
		List<Integer> list = Spouts.of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(Spouts.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(Spouts.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(Spouts.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(Spouts.of(Spouts.of(1, 2, 3),
        		Spouts.of(1, 3, 2), Spouts.of(2, 1, 3), Spouts.of(2, 3, 1), Spouts.of(3, 1, 2), Spouts.of(3, 2, 1)).map(s->s.toList()).toList()));
    }

    @Test
    public void emptyAllCombinations() {
        assertThat(Spouts.of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(Spouts.of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }



    @Test
    public void emptyCombinations() {
        assertThat(Spouts.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(Spouts.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(Spouts.of()
							.onEmptySwitch(()->Spouts.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));

	}
	@Test
	public void onEmptySwitch(){
		assertThat(Spouts.of(4,5,6)
							.onEmptySwitch(()->Spouts.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));

	}

	@Test
	public void elapsedIsPositive(){


		assertTrue(Spouts.of(1,2,3,4,5).elapsed().noneMatch(t->t._2()<0));
	}
	@Test
	public void timeStamp(){


		assertTrue(Spouts.of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t._2() <= System.currentTimeMillis()));


	}
	@Test
	public void elementAt0(){
		assertThat(Spouts.of(1).elementAtAndStream(0)._1(),equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(Spouts.of(1,2,3,4,5).elementAtAndStream(2)._1(),equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(Spouts.of(1,2,3,4,5).elementAtAndStream(2)._2().toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		Spouts.of(1).elementAtAndStream(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		Spouts.of().elementAtAndStream(0);
	}
	@Test
	public void get0(){
		assertTrue(Spouts.of(1).elementAt(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(Spouts.of(1,2,3,4,5).elementAt(2).toOptional().get(),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(Spouts.of(1).elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(Spouts.of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(Spouts.of(1).singleOrElse(null),equalTo(1));
	}
	@Test
	public void singleEmpty(){
		assertNull(Spouts.of().singleOrElse(null));
	}
	@Test
	public void single2(){
		assertNull(Spouts.of(1,2).singleOrElse(null));
	}
	@Test
	public void singleOptionalTest(){
		assertThat(Spouts.of(1).single().toOptional().get(),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(Spouts.of().single().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(Spouts.of(1,2).single().isPresent());
	}
	@Test
	public void limitTime(){
		List<Integer> result = Spouts.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = Spouts.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = Spouts.of(1,2,3,4,5,6)
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
		assertThat(Spouts.of(1,2,3,4,5)
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(Spouts.of()
							.skipLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(Spouts.of(1,2,3,4,5)
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLast1(){
	    ReactiveSeq.of(1,2,3).forEach(2,System.out::println);
	    System.out.println("Hello world!");
        Future result = Future.future();

        Spouts.of(1,2,3,4,5).limitLast(1).collectAll(Collectors.toList()).forEachSubscribe(e -> {
            System.out.println("Value recieved " + e);
            result.complete(e);
          //  sub[0].cancel();


        },e->{
            result.completeExceptionally(e);
          //  sub[0].cancel();

        },()->{
            if(!result.isDone()) {
                result.complete(null);
            }
        }).request(1l);

        assertThat(result.orElse(null),equalTo(Arrays.asList(5)));
        System.out.println(Spouts.of(1,2,3,4,5).limitLast(1).collectAll(Collectors.toList()).findFirst());


		assertThat(Spouts.of(1,2,3,4,5)
				.limitLast(1)
				.collect(Collectors.toList()),equalTo(Arrays.asList(5)));

	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(Spouts.of()
							.limitLast(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(Spouts.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(Spouts.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(Spouts.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(Spouts.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(Spouts.of()
				.endsWithIterable(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(Spouts.<Integer>of()
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(Spouts.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(Spouts.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(Spouts.of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(Spouts.of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(Spouts.of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(Spouts.<Integer>of()
				.endsWith(Stream.of()));
	}

	@Test
	public void streamable(){
		Streamable<Integer> repeat = Streamable.fromStream(Spouts.of(1,2,3,4,5,6)
												.map(i->i*2));

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}

	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = Spouts.of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.streamable();

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	@Test
	public void splitBy(){
		assertThat( Spouts.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._1().toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( Spouts.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._2().toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = Spouts.of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
		System.out.println("takeOne!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = Spouts.of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
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



		assertThat(Spouts.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(Spouts.of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(Spouts.of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}


	@Test
	public void flatMapMaybe(){
		assertThat(Spouts.of(1,2,3,null).concatMap(Maybe::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testIntersperse() {

		assertThat(Spouts.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));




	}

	@Test
	public void xMatch(){
		assertTrue(Spouts.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}


}
