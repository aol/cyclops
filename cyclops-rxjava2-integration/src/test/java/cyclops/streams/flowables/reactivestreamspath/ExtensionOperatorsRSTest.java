package cyclops.streams.flowables.reactivestreamspath;


import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.monads.AnyM;
import cyclops.reactive.FlowableReactiveSeq;
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

import static cyclops.control.Option.some;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ExtensionOperatorsRSTest {

	@Test
	public void flatMapStreamFilterSimple(){
		assertThat(FlowableReactiveSeq.of(1,2).flatMap(i-> ReactiveSeq.of(i).filter(x->x<2))
						.to(Streamable::fromStream).collect(Collectors.toList()),
				Matchers.equalTo(Arrays.asList(1)));
	}
    @Test
    public void combine(){
        assertThat(FlowableReactiveSeq.of(1,1,2,3)
                   .combine((a, b)->a.equals(b), Semigroups.intSum)
                   .to(Streamable::fromStream).to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of(4,3)));

    }
	@Test
	public void subStream(){
		List<Integer> list = FlowableReactiveSeq.of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(FlowableReactiveSeq.of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(FlowableReactiveSeq.of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(FlowableReactiveSeq.of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(FlowableReactiveSeq.of(FlowableReactiveSeq.of(1, 2, 3),
        		FlowableReactiveSeq.of(1, 3, 2), FlowableReactiveSeq.of(2, 1, 3), FlowableReactiveSeq.of(2, 3, 1), FlowableReactiveSeq.of(3, 1, 2), FlowableReactiveSeq.of(3, 2, 1)).map(s->s.toList()).toList()));
    }

    @Test
    public void emptyAllCombinations() {
        assertThat(FlowableReactiveSeq.of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(FlowableReactiveSeq.of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }



    @Test
    public void emptyCombinations() {
        assertThat(FlowableReactiveSeq.of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(FlowableReactiveSeq.of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(FlowableReactiveSeq.of()
							.onEmptySwitch(()-> FlowableReactiveSeq.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));

	}
	@Test
	public void onEmptySwitch(){
		assertThat(FlowableReactiveSeq.of(4,5,6)
							.onEmptySwitch(()-> FlowableReactiveSeq.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));

	}

	@Test
	public void elapsedIsPositive(){


		assertTrue(FlowableReactiveSeq.of(1,2,3,4,5).elapsed().noneMatch(t->t._2()<0));
	}
	@Test
	public void timeStamp(){


		assertTrue(FlowableReactiveSeq.of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t._2() <= System.currentTimeMillis()));


	}
	@Test
	public void elementAt0(){
		assertThat(FlowableReactiveSeq.of(1).elementAt(0),equalTo(some(1)));
	}
	@Test
	public void getMultple(){
		assertThat(FlowableReactiveSeq.of(1,2,3,4,5).elementAt(2),equalTo(some(3)));
	}

	@Test
	public void getMultiple1(){
		assertFalse(FlowableReactiveSeq.of(1).elementAt(1).isPresent());
	}
	@Test
	public void getEmpty(){
		assertFalse(FlowableReactiveSeq.of().elementAt(0).isPresent());
	}
	@Test
	public void get0(){
		assertTrue(FlowableReactiveSeq.of(1).elementAt(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(FlowableReactiveSeq.of(1,2,3,4,5).elementAt(2).orElse(-1),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(FlowableReactiveSeq.of(1).elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(FlowableReactiveSeq.of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(FlowableReactiveSeq.of(1).single().orElse(null),equalTo(1));
	}
	@Test
	public void singleEmpty(){
		assertTrue(FlowableReactiveSeq.of().single().orElse(null)==null);
	}
	@Test
	public void single2(){
		assertTrue(FlowableReactiveSeq.of(1,2).single().orElse(null)==null);
	}
	@Test
	public void singleOptionalTest(){
		assertThat(FlowableReactiveSeq.of(1).single().orElse(null),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(FlowableReactiveSeq.of().single().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(FlowableReactiveSeq.of(1,2).single().isPresent());
	}
	@Test
	public void limitTime(){
		List<Integer> result = FlowableReactiveSeq.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.take(1000,TimeUnit.MILLISECONDS)
										.toList();


        assertThat(result,hasItems(1,2,3));
        assertThat(result.size(),lessThan(5));
    }
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = FlowableReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.take(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = FlowableReactiveSeq.of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.drop(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList(4,5,6)));
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
		assertThat(FlowableReactiveSeq.of(1,2,3,4,5)
							.dropRight(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(FlowableReactiveSeq.of()
							.dropRight(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(FlowableReactiveSeq.of(1,2,3,4,5)
							.takeRight(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLast1(){
		assertThat(FlowableReactiveSeq.of(1,2,3,4,5)
				.takeRight(1)
				.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList(5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(FlowableReactiveSeq.of()
							.takeRight(2)
							.to(Streamable::fromStream).collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(FlowableReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(FlowableReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(FlowableReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(FlowableReactiveSeq.of(1,2,3,4,5,6)
				.endsWith(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(FlowableReactiveSeq.of()
				.endsWith(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(FlowableReactiveSeq.<Integer>of()
				.endsWith(Arrays.asList()));
	}


	@Test
	public void streamable(){
		Streamable<Integer> repeat = FlowableReactiveSeq.of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.streamable();

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}


	@Test
	public void splitBy(){
		assertThat( FlowableReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._1().toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( FlowableReactiveSeq.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._2().toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = FlowableReactiveSeq.of(1,2,3,4,5)
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



		assertThat(FlowableReactiveSeq.of(1, "a", 2, "b", 3).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(FlowableReactiveSeq.of(1, "a", 2, "b", 3).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(FlowableReactiveSeq.of(1, "a", 2, "b", 3)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}


	@Test
	public void testIntersperse() {

		assertThat(FlowableReactiveSeq.of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));




	}

	@Test
	public void xMatch(){
		assertTrue(FlowableReactiveSeq.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}


}
