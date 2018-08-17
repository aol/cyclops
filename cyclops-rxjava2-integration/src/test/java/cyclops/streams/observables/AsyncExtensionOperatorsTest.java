package cyclops.streams.observables;


import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.async.adapters.Queue;

import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.companion.rx2.Observables;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.monads.AnyM;
import cyclops.reactive.ObservableReactiveSeq;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.companion.Streamable;
import cyclops.reactive.collections.mutable.ListX;
import org.hamcrest.Matchers;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
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

public class AsyncExtensionOperatorsTest {
	protected <U> ReactiveSeq<U> of(U... array){

		ReactiveSeq<U> seq = Spouts.async(s->{
			Thread t = new Thread(()-> {
				for (U next : array) {
					s.onNext(next);
				}
				s.onComplete();
			});
			t.start();
		});
		return ObservableReactiveSeq.reactiveSeq(Observables.observableFrom(seq));
	}

	@Test
    public void queueTest(){
	    Queue<Integer> q = new Queue<>();
	    q.add(1);
	    q.add(2);
	    q.add(3);
	    q.stream().limit(3).forEach(System.out::println);
	    q.add(4);
	    q.add(5);
        q.stream().limit(2).forEach(System.out::println);

    }
	@Test
	public void flatMapStreamFilterSimple(){
		assertThat(of(1).flatMap(i->of(i).filter(Objects::nonNull))
						.collect(Collectors.toList()),
				Matchers.equalTo(Arrays.asList(1)));
	}

	@Test
    public void forEachOrdered(){
	    Spouts.of(1,2,3).forEachOrdered(System.out::println);
    }
    @Test
    public void combine(){
        assertThat(of(1,1,2,3)
                   .combine((a, b)->a.equals(b), Semigroups.intSum)
                   .to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of(4,3)));

    }
	@Test
	public void subStream(){
		List<Integer> list = of(1,2,3,4,5,6).subStream(1,3).toList();
		assertThat(list,equalTo(Arrays.asList(2,3)));
	}
	@Test
    public void emptyPermutations() {
        assertThat(of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(of(of(1, 2, 3),
        		of(1, 3, 2), of(2, 1, 3), of(2, 3, 1), of(3, 1, 2), of(3, 2, 1)).map(s->s.toList()).toList()));
    }

    @Test
    public void emptyAllCombinations() {
        assertThat(of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }



    @Test
    public void emptyCombinations() {
        assertThat(of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(of()
							.onEmptySwitch(()->of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));

	}
	@Test
	public void onEmptySwitch(){
		assertThat(of(4,5,6)
							.onEmptySwitch(()->of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));

	}

	@Test
	public void elapsedIsPositive(){


		assertTrue(of(1,2,3,4,5).elapsed().noneMatch(t->t._2()<0));
	}
	@Test
	public void timeStamp(){


		assertTrue(of(1,2,3,4,5)
							.timestamp()
							.allMatch(t-> t._2() <= System.currentTimeMillis()));


	}

	@Test
    public void duplicateFindOne(){
        Tuple2<Integer, Long> v2 = of(1).duplicate()._1().zipWithIndex().takeOne().orElse(null);
        assertThat(v2,equalTo(Tuple.tuple(1,0l)));
    }
	@Test
	public void elementAt0(){



		assertThat(of(1).elementAt(0),equalTo(some(1)));
	}
	@Test
	public void getMultple(){
		assertThat(of(1,2,3,4,5).elementAt(2),equalTo(Option.some(3)));
	}

	@Test
	public void getMultiple1(){
		assertFalse(of(1).elementAt(1).isPresent());
	}
	@Test
	public void getEmpty(){
		assertFalse(of().elementAt(0).isPresent());
	}
	@Test
	public void get0(){
		assertTrue(of(1).elementAt(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(of(1,2,3,4,5).elementAt(2).orElse(-1),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(of(1).elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(of(1).single().orElse(null),equalTo(1));
	}
	@Test
	public void singleEmpty(){
    assertTrue(of().single().orElse(null)==null);
	}
	@Test
	public void single2(){
		assertTrue(of(1,2).single().orElse(null)==null);
	}
	@Test
	public void singleOptionalTest(){
		assertThat(of(1).single().orElse(null),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(of().single().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(of(1,2).single().isPresent());
	}
	@Test
	public void limitTime(){
		List<Integer> result = of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.take(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,hasItems(1,2,3));

	}
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = Spouts.<Integer>of()
										.peek(i->sleep(i*100))
										.take(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}
	@Test
	public void skipTime(){
		List<Integer> result = of(1,2,3,4,5,6)
										.peek(i->sleep(i*100))
										.drop(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,not(hasItems(1,2,3)));
        assertThat(result.size(),lessThanOrEqualTo(3));
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
		assertThat(of(1,2,3,4,5)
							.dropRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(of()
							.dropRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
    @Test
    public void testSkipLast1Empty(){
        assertThat(of()
                .dropRight(1)
                .collect(Collectors.toList()),equalTo(Arrays.asList()));
    }
	@Test
	public void testLimitLast(){
		assertThat(of(1,2,3,4,5)
							.takeRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLast1(){
		assertThat(of(1,2,3,4,5)
				.takeRight(1)
				.collect(Collectors.toList()),equalTo(Arrays.asList(5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(of()
							.takeRight(2)
							.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
    @Test
    public void testLimitLast1Empty(){
        assertThat(of()
                .takeRight(1)
                .collect(Collectors.toList()),equalTo(Arrays.asList()));
    }
	@Test
	public void endsWith(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(of()
				.endsWith(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(Spouts.<Integer>of()
				.endsWith(Arrays.asList()));
	}

	@Test
	public void streamable(){
		Streamable<Integer> repeat = of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.streamable();

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}


	@Test
	public void splitBy(){

		assertThat( of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._1().toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)._2().toList(),equalTo(Arrays.asList(4,5,6)));
	}
	@Test
	public void testLazy(){
		Collection<Integer> col = of(1,2,3,4,5)
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
				  				.collect(Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c-> AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
    @Test
    public void flatMap(){
        for(int i=0;i<1000;i++){
            System.out.println("Iteration " + i);
            assertThat(of(1)
                            .flatMap(in -> of(1, 2, 3))
                            .toList(),
                    equalTo(Arrays.asList(1, 2, 3)));
        }

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



		assertThat(of(1, "a", 2, "b", 3).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

		assertThat(of(1, "a", 2, "b", 3).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

		assertThat(of(1, "a", 2, "b", 3)

				.ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

	}



	@Test
	public void flatMapMaybe(){
		assertThat(of(1,2,3).concatMap(Maybe::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testIntersperse() {

		assertThat(of(1,2,3).intersperse(0).toList(),equalTo(Arrays.asList(1,0,2,0,3)));




	}

	@Test
	public void xMatch(){
		assertTrue(of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}


}
