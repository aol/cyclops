package cyclops.streams.reactivestreamspath;


import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.reactive.FluxReactiveSeq;
import cyclops.reactive.ReactiveSeq;
import cyclops.companion.Streamable;
import cyclops.reactive.collections.mutable.ListX;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ExtensionOperatorsRSTest {

	@Test
	public void flatMapStreamFilterSimple(){
		assertThat(FluxReactiveSeq.of(1,2).flatMap(i-> ReactiveSeq.of(i).filter(x->x<2))
						.to(Streamable::fromStream).collect(Collectors.toList()),
				Matchers.equalTo(Arrays.asList(1)));
	}
    @Test
    public void combine(){
        assertThat(FluxReactiveSeq.of(1,1,2,3)
                   .combine((a, b)->a.equals(b), Semigroups.intSum)
                   .to(Streamable::fromStream).to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of(4,3)));

    }
	@Test
	public void subStream(){
		List<Integer> list = FluxReactiveSeq.of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(FluxReactiveSeq.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(FluxReactiveSeq.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(FluxReactiveSeq.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(FluxReactiveSeq.of(FluxReactiveSeq.of(1, 2, 3),
        		FluxReactiveSeq.of(1, 3, 2), FluxReactiveSeq.of(2, 1, 3), FluxReactiveSeq.of(2, 3, 1), FluxReactiveSeq.of(3, 1, 2), FluxReactiveSeq.of(3, 2, 1)).map(s->s.toList()).toList()));
    }

    @Test
    public void emptyAllCombinations() {
        assertThat(FluxReactiveSeq.of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(FluxReactiveSeq.of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }



    @Test
    public void emptyCombinations() {
        assertThat(FluxReactiveSeq.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(FluxReactiveSeq.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(FluxReactiveSeq.of()
							.onEmptySwitch(()-> FluxReactiveSeq.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));

	}
	@Test
	public void onEmptySwitch(){
		assertThat(FluxReactiveSeq.of(4,5,6)
							.onEmptySwitch(()-> FluxReactiveSeq.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));

	}

	@Test
	public void elapsedIsPositive(){


		assertTrue(FluxReactiveSeq.of(1,2,3,4,5).elapsed().noneMatch(t->t._2()<0));
	}
	@Test
	public void timeStamp(){


		assertTrue(FluxReactiveSeq.of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t._2() <= System.currentTimeMillis()));


	}
	@Test
	public void elementAt0(){
		assertThat(FluxReactiveSeq.of(1).elementAt(0),equalTo(Option.some(1)));
	}
	@Test
	public void getMultple(){
		assertThat(FluxReactiveSeq.of(1,2,3,4,5).elementAt(2),equalTo(Maybe.just(3)));
	}

	@Test
	public void getMultiple1(){
		assertFalse(FluxReactiveSeq.of(1).elementAt(1).isPresent());
	}
	@Test
	public void getEmpty(){
    assertFalse(FluxReactiveSeq.of().elementAt(0).isPresent());
	}
	@Test
	public void get0(){
		assertTrue(FluxReactiveSeq.of(1).elementAt(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(FluxReactiveSeq.of(1,2,3,4,5).elementAt(2).orElse(-1),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(FluxReactiveSeq.of(1).elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(FluxReactiveSeq.of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(FluxReactiveSeq.of(1).single().orElse(null),equalTo(1));
	}
	@Test
	public void singleEmpty(){
		assertNull(FluxReactiveSeq.of().single().orElse(null));
	}
	@Test
	public void single2(){
		assertNull(FluxReactiveSeq.of(1,2).single().orElse(null));
	}
	@Test
	public void singleOptionalTest(){
		assertThat(FluxReactiveSeq.of(1).single().orElse(null),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(FluxReactiveSeq.of().single().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(FluxReactiveSeq.of(1,2).single().isPresent());
	}
	@Test
	public void limitTime(){
		List<Integer> result = FluxReactiveSeq.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = FluxReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = FluxReactiveSeq.of(1,2,3,4,5,6)
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
		assertThat(FluxReactiveSeq.of(1,2,3,4,5)
							.skipLast(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(FluxReactiveSeq.of()
							.skipLast(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(FluxReactiveSeq.of(1,2,3,4,5)
							.limitLast(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLast1(){
		assertThat(FluxReactiveSeq.of(1,2,3,4,5)
				.limitLast(1)
				.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(FluxReactiveSeq.of()
							.limitLast(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(FluxReactiveSeq.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(FluxReactiveSeq.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(FluxReactiveSeq.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(FluxReactiveSeq.of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(FluxReactiveSeq.of()
				.endsWithIterable(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(FluxReactiveSeq.<Integer>of()
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(FluxReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(FluxReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(FluxReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(FluxReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(FluxReactiveSeq.of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(FluxReactiveSeq.<Integer>of()
				.endsWith(Stream.of()));
	}

	@Test
	public void streamable(){
		Streamable<Integer> repeat = FluxReactiveSeq.of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.streamable();

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}


	@Test
	public void splitBy(){
		assertThat( FluxReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._1().toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( FluxReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._2().toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = FluxReactiveSeq.of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
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



		assertThat(FluxReactiveSeq.of(1, "a", 2, "b", 3).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(FluxReactiveSeq.of(1, "a", 2, "b", 3).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(FluxReactiveSeq.of(1, "a", 2, "b", 3)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}



	@Test
	public void testIntersperse() {

		assertThat(FluxReactiveSeq.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));




	}

	@Test
	public void xMatch(){
		assertTrue(FluxReactiveSeq.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}


}
