package cyclops.data.basetests;




import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.util.ExceptionSoftener;
import com.oath.cyclops.util.SimpleTimer;

import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.control.Try;
import cyclops.data.*;
import cyclops.data.HashMap;
import cyclops.data.TreeSet;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.companion.Streamable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static cyclops.data.tuple.Tuple.tuple;
import static cyclops.reactive.ReactiveSeq.fromIntStream;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public abstract class AbstractIterableXTest {
	public abstract <T> IterableX<T> empty();
	public abstract <T> IterableX<T> of(T... values);
	public abstract  IterableX<Integer> range(int start, int end);
	public abstract  IterableX<Long> rangeLong(long start, long end);
	public abstract <T> IterableX<T> iterate(int times, T seed, UnaryOperator<T> fn);
	public abstract <T> IterableX<T> generate(int times,Supplier<T> fn);
	public abstract <U,T> IterableX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder);


	int captured=-1;

	static Executor ex = Executors.newFixedThreadPool(1);
    boolean set = false;
    @Test
    public void deleteBetween(){
        List<String> result = 	of(1,2,3,4,5,6).deleteBetween(2,4)
                .map(it ->it+"!!").collect(Collectors.toList());

        assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
    }

    @Test
    public void indexOf(){
      assertThat(empty().indexOf(e->true),equalTo(Maybe.nothing()));
      assertThat(of(1).indexOf(e->true),equalTo(Maybe.just(0l)));
      assertThat(of(1).indexOf(e->false),equalTo(Maybe.nothing()));
      assertThat(of(1,2,3).indexOf(e->Objects.equals(2,e)),equalTo(Maybe.just(1l)));
    }
    @Test
    public void indexOfSlize(){
        assertThat(empty().indexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        assertThat(of(1,2,3).indexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(0l)));
        assertThat(of(1).indexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        assertThat(of(0,1,2,3).indexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(1l)));
    }
    @Test
    public void lastIndexOfSlize(){
        assertThat(empty().lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        assertThat(of(1,2,3).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(0l)));
        assertThat(of(1).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        assertThat(of(0,1,2,3,4,5,6,1,2,3).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(7l)));
    }
    @Test
    public void lastIndexOf(){
      assertThat(empty().lastIndexOf(e->true),equalTo(Maybe.nothing()));
      assertThat(of(1).lastIndexOf(e->true),equalTo(Maybe.just(0l)));
      assertThat(of(1).lastIndexOf(e->false),equalTo(Maybe.nothing()));
      assertThat(of(1,2,3).lastIndexOf(e->Objects.equals(2,e)),equalTo(Maybe.just(1l)));
      assertThat(of(1,2,3,2).lastIndexOf(e->Objects.equals(2,e)),equalTo(Maybe.just(3l)));
    }
    @Test
    public void insertAt(){
        IterableX<String> result = 	of(1,2,3)
                                .insertAt(1,100,200,300)
                .map(it ->it+"!!");

        assertThat(result,equalTo(of("1!!","100!!","200!!","300!!","2!!","3!!")));
    }
    @Test
    public void insertAtStream(){
        IterableX<String> result = 	of(1,2,3).insertStreamAt(1,ReactiveSeq.of(100,200,300))
                .map(it ->it+"!!");

        assertThat(result,equalTo(of("1!!","100!!","200!!","300!!","2!!","3!!")));
    }


    @Test
    public void unitIteratable(){
        assertThat(of(3).unitIterable(()->of().iterator()),equalTo(of()));
        assertThat(of().unitIterable(()->of(3).iterator()),equalTo(of(3)));
    }

    @Test
    public void sizeTest(){
        assertThat(of().size(),equalTo(0));
        assertThat(of(1).size(),equalTo(1));
        assertThat(of(1,2).size(),equalTo(2));
    }
    @Test
    public void emptyTest(){
        assertThat(of().isEmpty(),equalTo(true));
        assertThat(of(1).isEmpty(),equalTo(false));
        assertThat(of(2).isEmpty(),equalTo(false));
    }
    @Test
    public void foldFuture(){
        assertThat(of(1,2,3).foldFuture(ex, l->l.foldLeft(Monoids.intSum)).get(),equalTo(Try.success(6)));
    }
    @Test
    public void foldLazy(){
        assertThat(of(1,2,3).foldLazy(l->l.foldLeft(Monoids.intSum)).get(),equalTo(6));
    }
    @Test
    public void foldTry(){
        assertThat(of(1,2,3).foldTry(l->l.foldLeft(Monoids.intSum), Throwable.class).get(),equalTo(Option.some(6)));
    }

    @Test
    public void subscribeEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().forEachSubscribe(i->result.add(i));
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));

    }
    @Test
    public void subscribe(){
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i));
        s.request(1l);
        assertThat(result.size(),equalTo(1));
        s.request(1l);
        assertThat(result.size(),equalTo(2));
        s.request(1l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribe3(){
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i));
        s.request(3l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribeErrorEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));

    }
    @Test
    public void subscribeError(){
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(1l);
        assertThat(result.size(),equalTo(1));
        s.request(1l);
        assertThat(result.size(),equalTo(2));
        s.request(1l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribe3Error() throws InterruptedException {
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(3l);

        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribeErrorEmptyOnComplete(){
        List result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of().forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));
        s.request(1l);
        assertThat(onComplete.get(),equalTo(true));
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));

    }
    @Test
    public void subscribeErrorOnComplete(){
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));

        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(1));
        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(2));
        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
        s.request(1l);
        assertThat(onComplete.get(),equalTo(true));
    }
    @Test
    public void subscribe3ErrorOnComplete(){
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));
        assertThat(onComplete.get(),equalTo(false));
        s.request(4l);
        assertThat(onComplete.get(),equalTo(true));

        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
        s.request(1l);
        assertThat(onComplete.get(),equalTo(true));
    }
    @Test
    public void iterate(){
        Iterator<Integer> it = of(1,2,3).iterator();
        List<Integer> list2 = new ArrayList<>();
        while(it.hasNext())
            list2.add(it.next());
        assertThat(list2.size(),equalTo(3));
    }
    @Test
    public void iterateStream(){
        Iterator<Integer> it = of(1,2,3).stream().iterator();
        List<Integer> list2 = new ArrayList<>();
        while(it.hasNext())
            list2.add(it.next());
        assertThat(list2.size(),equalTo(3));
    }
	@Test
	public void testRange(){
	    assertThat(range(0,2).size(),equalTo(2));
	}
	@Test
    public void testRangeLong(){
        assertThat(rangeLong(0,2).size(),equalTo(2));
    }
	@Test
    public void testIterate(){
        assertThat(iterate(5,1,i->i+1).size(),equalTo(5));
    }

	@Test
    public void testGenerate(){
	    count = 0;
        assertThat(generate(5,()->"hello"+(count++)).size(),equalTo(5));
    }
	@Test
    public void testUnfold(){
	    Function<Integer,Option<Tuple2<Integer,Integer>>> fn= i-> i<=6 ? Option.of(Tuple.tuple(i,i+1)) : Option.none();

        assertThat(unfold(1,fn ).size(),equalTo(6));
    }


	@Test
	public void plusOne(){
	    assertThat(of().plus(1),hasItem(1));
	}
	@Test
    public void plusTwo(){
        assertThat(of().plus(1).plus(2),hasItems(1,2));
    }

	@Test
    public void plusAllOne(){
        assertThat(of().plusAll(of(1).toList()),hasItem(1));
    }
    @Test
    public void plusAllTwo(){
        assertThat(of().plusAll(of(1).toList()).plus(2),hasItems(1,2));
    }

	@Test
    public void minusOne(){
        assertThat(of().removeValue(1).size(),equalTo(0));
    }
	@Test
    public void minusOneNotEmpty(){
        assertThat(of(1).removeValue(1).size(),equalTo(0));
    }
	@Test
    public void minusOneTwoValues(){
        assertThat(of(1,2).removeValue(1),hasItem(2));
        assertThat(of(1,2).removeValue(1),not(hasItem(1)));
    }

    @Test
    public void removeAllTest(){
      assertThat(of(1,2,3).removeAll(of(1,5,6,7,2)),equalTo(of(3)));
    }
	@Test
    public void minusAllOne(){
        assertThat(of().removeAll(of(1).toList()).size(),equalTo(0));
    }
    @Test
    public void minusAllOneNotEmpty(){
        assertThat(of(1).removeAll(of(1).toList()).size(),equalTo(0));
    }
    @Test
    public void minusAllOneTwoValues(){
        assertThat(of(1,2).removeAll(of(1).toList()),hasItem(2));
        assertThat(of(1,2).removeAll(of(1).toList()),not(hasItem(1)));
    }


	@Test
    public void notNull(){
        assertThat(of(1,2,3,4,5).notNull(),hasItems(1,2,3,4,5));
    }
	@Test
	public void retainAll(){
	    assertThat(of(1,2,3,4,5).retainAll((Iterable<Integer>)of(1,2,3)),hasItems(1,2,3));
	}


	@Test
    public void retainAllStream(){
        assertThat(of(1,2,3,4,5).retainStream(Stream.of(1,2,3)),hasItems(1,2,3));
    }
	@Test
    public void retainAllValues(){
        assertThat(of(1,2,3,4,5).retainAll(1,2,3),hasItems(1,2,3));
    }
	@Test
    public void removeAll(){
        assertThat(of(1,2,3,4,5).removeAll((Iterable<Integer>)of(1,2,3)),hasItems(4,5));
    }

    @Test
    public void removeAllStream(){
        assertThat(of(1,2,3,4,5).removeStream(Stream.of(1,2,3)),hasItems(4,5));
    }
    @Test
    public void removeAllValues(){
        assertThat(of(1,2,3,4,5).removeAll(1,2,3),hasItems(4,5));
    }
	@Test
    public void testAnyMatch(){
        assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(3)),is(true));
    }
    @Test
    public void testAllMatch(){
        assertThat(of(1,2,3,4,5).allMatch(it-> it>0 && it <6),is(true));
    }
    @Test
    public void testNoneMatch(){
        assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),is(true));
    }


    @Test
    public void testAnyMatchFalse(){
        assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(8)),is(false));
    }
    @Test
    public void testAllMatchFalse(){
        assertThat(of(1,2,3,4,5).allMatch(it-> it<0 && it >6),is(false));
    }

    @Test
    public void testMapReduce(){
        assertThat(of(1,2,3,4,5).map(it -> it*100).stream().reduce( (acc,next) -> acc+next).get(),is(1500));
    }
    @Test
    public void testMapReduceSeed(){
        assertThat(of(1,2,3,4,5).map(it -> it*100).stream().reduce( 50,(acc,next) -> acc+next),is(1550));
    }


    @Test
    public void testMapReduceCombiner(){
        assertThat(of(1,2,3,4,5).map(it -> it*100).stream().reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum),is(1500));
    }
    /**
    @Test
    public void testFindFirst(){
        assertThat(of(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findFirst().get()));
    }
    @Test
    public void testFindAny(){
        assertThat(of(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findAny().get()));
    }
    **/
     @Test
    public void testDistinct(){
        assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()).size(),is(2));
        assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()),hasItem(1));
        assertThat(of(1,1,1,2,1).distinct().collect(Collectors.toList()),hasItem(2));
    }


    @Test
    public void testMax2() {
        assertThat(of(1, 2, 3, 4, 5).maximum((t1, t2) -> t1 - t2).orElse(-1), is(5));
    }
    @Test
    public void testMin2(){
        assertThat(of(1,2,3,4,5).minimum((t1, t2) -> t1-t2).orElse(-100),is(1));
    }





    @Test
    public void sorted() {
        assertThat(of(1,5,3,4,2).sorted(),is(of(1,2,3,4,5)));
    }
    @Test
    public void sortedComparator() {
        assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(Collectors.toList()).size(),is(5));
    }
    @Test
    public void forEach() {
        List<Integer> list = new ArrayList<>();
        of(1,5,3,4,2).forEach(it-> list.add(it));
        assertThat(list,hasItem(1));
        assertThat(list,hasItem(2));
        assertThat(list,hasItem(3));
        assertThat(list,hasItem(4));
        assertThat(list,hasItem(5));

    }





    @Test
    public void testCount(){
        assertThat(of(1,5,3,4,2).count(),is(5L));
    }


    @Test
    public void collect(){
        assertThat(of(1,2,3,4,5).collect(Collectors.toList()).size(),is(5));
        assertThat(of(1,1,1,2).collect(Collectors.toSet()).size(),is(2));
    }
    @Test
    public void testFilter(){
        assertThat(of(1,1,1,2).filter(it -> it==1).collect(Collectors.toList()),hasItem(1));
    }
    @Test
    public void testFilterNot(){
        assertThat(of(1,1,1,2).filterNot(it -> it==1).collect(Collectors.toList()),hasItem(2));
    }
    @Test
    public void testMap2(){
        assertThat(of(1).map(it->it+100).collect(Collectors.toList()).get(0),is(101));
    }
    Object val;
    @Test
    public void testPeek2(){
        val = null;
        List l = of(1).map(it->it+100)
                        .peek(it -> val=it)
                        .collect(Collectors.toList());
        System.out.println(l);
        assertThat(val,is(101));
    }

	@SuppressWarnings("serial")
    public class X extends Exception {
    }

	@Test
	public void flatMapEmpty(){
	    assertThat(empty().concatMap(i->of(1,2,3)).size(),equalTo(0));
	}
	@Test
    public void flatMap(){
        assertThat(of(1).concatMap(i->of(1,2,3)),hasItems(1,2,3));
    }
	@Test
	public void slice(){
	    assertThat(of(1,2,3).slice(0,3),hasItems(1,2,3));
	    assertThat(empty().slice(0,2).size(),equalTo(0));
	}
	@Test
    public void testLimit(){
        assertThat(of(1,2,3,4,5).limit(2).collect(Collectors.toList()).size(),is(2));
    }
	@Test
    public void testTake(){
        assertThat(of(1,2,3,4,5).take(2).collect(Collectors.toList()).size(),is(2));
        assertThat(of(1,2,3,4,5).take(5).collect(Collectors.toList()).size(),is(5));
        assertThat(of(1,2,3,4,5).take(50).collect(Collectors.toList()).size(),is(5));
        assertThat(of(1,2,3,4,5).take(0).collect(Collectors.toList()).size(),is(0));
        assertThat(of(1,2,3,4,5).take(-1).collect(Collectors.toList()).size(),is(0));
        assertThat(of(1,2,3,4,5).take(5).containsValue(2),is(true));
    }

    @Test
    public void testDrop() {
        assertThat(of(1, 2, 3, 4, 5).drop(2)
                                    .collect(Collectors.toList())
                                    .size(),
                   is(3));
        assertThat(of(1, 2, 3, 4, 5).drop(0)
                .collect(Collectors.toList())
                .size(),
            is(5));
        assertThat(of(1, 2, 3, 4, 5).drop(-1)
                .collect(Collectors.toList())
                .size(),
            is(5));
        assertThat(of(1, 2, 3, 4, 5).drop(5)
                .collect(Collectors.toList())
                .size(),
            is(0));
        assertThat(of(1, 2, 3, 4, 5).drop(50)
                .collect(Collectors.toList())
                .size(),
            is(0));
    }
    @Test
    public void testSkip(){
        assertThat(of(1,2,3,4,5).skip(2).collect(Collectors.toList()).size(),is(3));
    }
    @Test
    public void testMax(){
        assertThat(of(1,2,3,4,5).maximum((t1, t2) -> t1-t2).orElse(-100),is(5));
    }
    @Test
    public void testMin(){
        assertThat(of(1,2,3,4,5).minimum((t1, t2) -> t1-t2).orElse(-40),is(1));
    }

	@Test
    public void testOnEmpty() throws X {
        assertEquals(asList(1), of().onEmpty(1).toList());
        assertEquals(asList(1), of().onEmptyGet(() -> 1).toList());

        assertEquals(asList(2), of(2).onEmpty(1).toList());
        assertEquals(asList(2), of(2).onEmptyGet(() -> 1).toList());

    }



	@Test
	public void testCollectable(){
		assertThat(of(1,2,3).anyMatch(i->i==2),equalTo(true));
	}
	@Test
	public void dropRight(){

	    assertThat(of(1,2,3).dropRight(1).toList(),hasItems(1,2));
        assertThat(of(1,2,3).dropRight(1).toList().size(),equalTo(2));
	}
	@Test
	public void dropRightEmpty(){
		assertThat(of().dropRight(1),equalTo(of()));
	}

	@Test
	public void dropUntil(){
		assertThat(of(1,2,3,4,5).dropUntil(p->p==2).toList().size(),lessThan(5));
	}
	@Test
	public void dropUntilEmpty(){
		assertThat(of().dropUntil(p->true),equalTo(of()));
	}
	@Test
	public void dropWhile(){
		assertThat(of(1,2,3,4,5).dropWhile(p->p<6).toList().size(),lessThan(1));
	}
	@Test
	public void dropWhileEmpty(){
		assertThat(of().dropWhile(p->true),equalTo(of()));
	}
	@Test
    public void skipUntil(){
        assertThat(of(1,2,3,4,5).skipUntil(p->p==2).toList().size(),lessThan(5));
    }
    @Test
    public void skipUntilEmpty(){
        assertThat(of().skipUntil(p->true).toList(),equalTo(Arrays.asList()));
    }
    @Test
    public void skipWhile(){
        assertThat(of(1,2,3,4,5).skipWhile(p->p<6).toList().size(),lessThan(1));
    }
    @Test
    public void skipWhileEmpty(){
        assertThat(of().skipWhile(p->true),equalTo(of()));
    }
	@Test
	public void filter(){
		assertThat(of(1,2,3,4,5).filter(i->i<3).toList(),hasItems(1,2));
	}

	/**
	@Test
	public void findAny(){
		assertThat(of(1,2,3,4,5).findAny().get(),lessThan(6));
	}
	@Test
	public void findFirst(){
		assertThat(of(1,2,3,4,5).findFirst().get(),lessThan(6));
	}
**/

    Throwable error;

	IterableX<Integer> empty;
    IterableX<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
        MockitoAnnotations.initMocks(this);


        error = null;

	}


	protected Object value() {

		return "jello";
	}
	private int value2() {

		return 200;
	}


	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}



	@Test
	public void takeWhileTest(){

		List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).takeWhile(it -> it<4)
						.peek(it -> System.out.println(it)).collect(Collectors.toList());

		}
		assertThat(of(1,2,3,4,5,6),hasItem(list.get(0)));




	}
	@Test
    public void limitWhileTest(){

        List<Integer> list = new ArrayList<>();
        while(list.size()==0){
            list = of(1,2,3,4,5,6).limitWhile(it -> it<4)
                        .toList();

        }
        assertThat(of(1,2,3,4,5,6),hasItem(list.get(0)));




    }

    @Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
    }
    @Test
    public void testScanLeftSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanLeft(0, (u, t) -> u + t).toList().size(),
    			is(asList(0, 1, 3, 6).size()));
    }
    @Test
    public void testScanRightStringConcatMonoid() {
        System.out.println(of("a", "b", "c","d").scanRight(Monoid.of("", String::concat)).toList());
        assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").scanRight("", String::concat).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
            is(asList(0, 3, 5, 6).size()));


    }



    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).to().collection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }

	    @Test
	    public void testGroupByEager() {
	        HashMap<Integer, Vector<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);

	        assertThat(map1.getOrElse(0,Vector.empty()),hasItem(2));
	        assertThat(map1.getOrElse(0,Vector.empty()),hasItem(4));
	        assertThat(map1.getOrElse(1,Vector.empty()),hasItem(1));
	        assertThat(map1.getOrElse(1,Vector.empty()),hasItem(3));

	        assertEquals(2, map1.size());


	    }


	    @Test
	    public void testJoin() {
	        assertEquals("123".length(),of(1, 2, 3).join().length());
	        assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").length());
	        assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());

	    }



	    @Test
	    public void testSkipWhile() {
	        Supplier<IterableX<Integer>> s = () -> of(1, 2, 3, 4, 5);

            of(1, 2, 3, 4, 5).dropWhile(i -> false);
	        System.out.println(s.get().dropWhile(i -> false).toList());
	        assertTrue(s.get().dropWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));

	        assertEquals(asList(), s.get().dropWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<IterableX<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().dropUntil(i -> false).toList());
	        assertTrue(s.get().dropUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }



	    @Test
	    public void testLimitWhile() {
	        Supplier<IterableX<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().takeWhile(i -> false).toList());
	        assertTrue( s.get().takeWhile(i -> i < 3).toList().size()!=5);
	        assertTrue(s.get().takeWhile(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testTakeUntil() {


	        assertTrue(of(1, 2, 3, 4, 5).takeUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	        assertFalse(of(1, 2, 3, 4, 5).takeUntil(i -> i % 3 == 0).toList().size()==5);

	        assertEquals(asList(), of(1, 2, 3, 4, 5).takeUntil(i -> true).toList());
	    }

	    @Test
        public void testLimitUntil() {


            assertTrue(of(1, 2, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
            assertFalse(of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toList().size()==5);

            assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());
        }



	    @Test
	    public void testMinByMaxBy() {
	        Supplier<IterableX<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).orElse(-1));
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).orElse(-1));

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).orElse(-1));
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).orElse(-1));
	    }




		@Test
		public void onePer(){
			SimpleTimer timer = new SimpleTimer();
			System.out.println(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()));
			assertThat(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
			assertThat(timer.getElapsedNanoseconds(),greaterThan(600l));
		}
		@Test
		public void xPer(){
			SimpleTimer timer = new SimpleTimer();
			assertThat(of(1,2,3,4,5,6).xPer(6,100000000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
			assertThat(timer.getElapsedNanoseconds(),lessThan(60000000l));
		}


		@Test
		public void zip(){
			List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).zip(of(100,200,300,400))
													.peek(it -> System.out.println(it))

													.collect(Collectors.toList());
			System.out.println(list);

			List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());

			assertThat(right,hasItem(100));
			assertThat(right,hasItem(200));
			assertThat(right,hasItem(300));
			assertThat(right,hasItem(400));

			List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
			assertThat(of(1,2,3,4,5,6),hasItem(left.get(0)));


		}


		@Test
		public void testScanLeftStringConcatMonoid() {
			assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), is(asList("", "a", "ab", "abc")));
		}

		@Test
		public void testScanLeftSumMonoid() {

			assertThat(of("a", "ab", "abc").map(str -> str.length()).
								peek(System.out::println).scanLeft(Reducers.toTotalInt()).toList(), is(asList(0, 1, 3, 6)));
		}



		@Test
		public void testScanRightSumMonoid() {
			assertThat(of("a", "ab", "abc").peek(System.out::println)
										.map(str -> str.length())
										.peek(System.out::println)
										.scanRight(Reducers.toTotalInt()).toList(), is(asList(0, 3, 5, 6)));

            assertThat(of("a", "ab", "abc").peek(System.out::println)
                .map(str -> str.length())
                .peek(System.out::println)
                .scanRight(Reducers.toTotalInt().zero(),Reducers.toTotalInt()).toList(), is(asList(0, 3, 5, 6)));

		}

/**
    @Test
    public void recoverTest(){
		    assertThat(of(1,2,3).recover(i->10),equalTo(of(1,2,3)));
        assertThat(of(1,2,3).recover(Throwable.class,i->10),equalTo(of(1,2,3)));
    }
 **/
    @Test
    public void windowStatefullyUntil(){
        System.out.println(of(1,2,3,4,5,6)
                .groupedUntil((s, i)->s.containsValue(4) ? true : false).toList());
        System.out.println(ReactiveSeq.of(1,2,3,4,5,6)
                .groupedUntil((s, i)->s.containsValue(4) ? true : false).toList());
        System.out.println(Streamable.of(1,2,3,4,5,6)
                .groupedUntil((s, i)->s.containsValue(4) ? true : false).toList());
        assertThat(of(1,2,3,4,5,6)
                .groupedUntil((s, i)->s.containsValue(4) ? true : false)
                .toList().size(),equalTo(2));


    }
    @Test
    public void windowStatefullyWhileEmpty(){

        assertThat(of()
                .groupedUntil((s, i)->s.contains(4) ? true : false)
                .toList().size(),equalTo(0));

    }

	@Test
	public void get0(){
		assertTrue(of(1).elementAt(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(of(1,2,3,4,5).elementAt(2).toOptional().get(),equalTo(3));
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
		assertThat(of(1).singleOrElse(null),equalTo(1));
	}
	@Test
	public void singleEmpty(){
		assertNull(of().singleOrElse(null));
	}
	@Test
	public void single2(){
		assertNull(of(1,2).singleOrElse(null));
	}

	@Test
	public void singleOptionalTest(){
		assertThat(of(1).single().toOptional().get(),equalTo(1));
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
	public void testSkipLast(){
		assertThat(of(1,2,3,4,5)
							.skipLast(2),equalTo(of(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(of()
							.skipLast(2),equalTo(of()));
	}
	@Test
	public void testLimitLast(){
		assertThat(of(1,2,3,4,5)
							.limitLast(2),equalTo(of(4,5)));
	}
	@Test
    public void testTakeRight(){
        assertThat(of(1,2,3,4,5)
                            .takeRight(2),equalTo(of(4,5)));
    }
    @Test
    public void testTakeRight5(){
        assertThat(of(1,2)
                .takeRight(5),equalTo(of(1,2)));
    }
	@Test
	public void testLimitLastEmpty(){
		assertThat(of()
							.limitLast(2),equalTo(of()));
	}
	@Test
	public void endsWith(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(of(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(of(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(of()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(of()
				.endsWith(of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWith(of()));
	}


	@Test
	public void streamable(){
		Streamable<Integer> repeat = (of(1,2,3,4,5,6)
												.map(i->i*2)
												).to()
												.streamable();

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}

	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.streamable();

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	/**
	@Test
	public void splitBy(){
		assertThat( of(1, 2, 3, 4, 5, 6).stream().splitBy(i->i<4).v1.toList(),equalTo(of(1,2,3)));
		assertThat( of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v2.toList(),equalTo(of(4,5,6)));
	}
	**/
	@Test
	public void testLazy(){
		Collection<Integer> col = of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
		System.out.println("takeOne!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = of(1,2,3,4,5)
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
	public void xMatch(){
		assertTrue(of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}



	@Test
	public void zip2of(){

		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6)
											.zip(of(100,200,300,400).stream())
											.toList();


		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		assertThat(of(1,2,3,4,5,6),hasItem(left.get(0)));

	}
	@Test
	public void zipInOrder(){

		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6)
													.zip( of(100,200,300,400).stream())
													.toList();

		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0)._1()));
		assertThat(asList(100,200,300,400),hasItem(list.get(0)._2()));



	}

	@Test
	public void zipEmpty() throws Exception {


		final IterableX<Integer> zipped = this.<Integer>empty().zip(ReactiveSeq.<Integer>of(), (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {



		final IterableX<Integer> zipped = this.<Integer>empty().zip(of(1,2), (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

    @Test
    public void shouldReturnEmptySeqWhenZipNonEmptyWithEmptyStream() throws Exception {


        final IterableX<Integer> zipped = of(1,2,3).zipWithStream(ReactiveSeq.<Integer>empty(), (a, b) -> a + b);


        assertTrue(zipped.collect(Collectors.toList()).isEmpty());
    }
    @Test
    public void shouldReturnEmptySeqWhenZipNonEmptyWithEmptyPublisherWith() throws Exception {


        final IterableX<Tuple2<Integer,Integer>> zipped = of(1,2,3).zipWithPublisher(ReactiveSeq.empty());


        assertTrue(zipped.collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void shouldReturnEmptySeqWhenZipNonEmptyWithEmptyStreamWith() throws Exception {


        final IterableX<Tuple2<Integer,Integer>> zipped = of(1,2,3).zipWithStream(ReactiveSeq.empty());


        assertTrue(zipped.collect(Collectors.toList()).isEmpty());
    }
    @Test
    public void shouldReturnEmptySeqWhenZipNonEmptyWithEmptyPublisher() throws Exception {


        final IterableX<Integer> zipped = of(1,2,3).zip((a, b) -> a + b, ReactiveSeq.<Integer>empty());


        assertTrue(zipped.collect(Collectors.toList()).isEmpty());
    }

    @Test
	public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {


		final IterableX<Integer> zipped = of(1,2,3).zip(this.<Integer>empty(), (a, b) -> a + b);


		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {

		final IterableX<String> first = of("A", "B", "C");
		final IterableX<Integer> second = of(1, 2, 3);


		final IterableX<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}



	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final IterableX<String> first = of("A", "B", "C");
		final IterableX<Integer> second = of(1, 2, 3, 4);


		final IterableX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final IterableX<String> first = of("A", "B", "C","D");
		final IterableX<Integer> second = of(1, 2, 3);
		final IterableX<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLength() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(of(1, 2).containsValue(list.get(0)._1()));
        assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(of("a", "b", "c", "d").containsValue(list.get(0)._2()));
		assertTrue(of("a", "b", "c", "d").containsValue(list.get(1)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}
    @Test
    public void testZipPDifferingLength() {
        List<Tuple2<Integer, String>> list = of(1, 2).zipWithPublisher(Spouts.of("a", "b", "c", "d")).toList();

        assertEquals(2, list.size());
        assertTrue(of(1, 2).containsValue(list.get(0)._1()));
        assertTrue(asList(1, 2).contains(list.get(0)._1()));
        assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
        assertTrue(of("a", "b", "c", "d").containsValue(list.get(0)._2()));
        assertTrue(of("a", "b", "c", "d").containsValue(list.get(1)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

    }
    @Test
    public void testZipSDifferingLength() {
        List<Tuple2<Integer, String>> list = of(1, 2).zipWithStream(ReactiveSeq.of("a", "b", "c", "d")).toList();

        assertEquals(2, list.size());
        assertTrue(of(1, 2).containsValue(list.get(0)._1()));
        assertTrue(asList(1, 2).contains(list.get(0)._1()));
        assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
        assertTrue(of("a", "b", "c", "d").containsValue(list.get(0)._2()));
        assertTrue(of("a", "b", "c", "d").containsValue(list.get(1)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

    }
    @Test
    public void testZipPDifferingLengthT2() {
        List<Tuple2<Integer, String>> list = of(1, 2).zip(Tuple2::of, Spouts.of("a", "b", "c", "d")).toList();

        assertEquals(2, list.size());
        assertTrue(of(1, 2).containsValue(list.get(0)._1()));
        assertTrue(asList(1, 2).contains(list.get(0)._1()));
        assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
        assertTrue(of("a", "b", "c", "d").containsValue(list.get(0)._2()));
        assertTrue(of("a", "b", "c", "d").containsValue(list.get(1)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

    }
    @Test
    public void testZipSDifferingLengthT2() {
        List<Tuple2<Integer, String>> list = of(1, 2).zipWithStream(ReactiveSeq.of("a", "b", "c", "d"),Tuple2::of).toList();

        assertEquals(2, list.size());
        assertTrue(of(1, 2).containsValue(list.get(0)._1()));
        assertTrue(asList(1, 2).contains(list.get(0)._1()));
        assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
        assertTrue(of("a", "b", "c", "d").containsValue(list.get(0)._2()));
        assertTrue(of("a", "b", "c", "d").containsValue(list.get(1)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
        assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

    }




	@Test
	public void shouldTrimSecondFixedSeqIfLongerStream() throws Exception {
		final IterableX<String> first = of("A", "B", "C");
		final IterableX<Integer> second = of(1, 2, 3, 4);


		final IterableX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerStream() throws Exception {
		final IterableX<String> first = of("A", "B", "C","D");
		final IterableX<Integer> second = of(1, 2, 3);

		final IterableX<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLengthStream() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}

	@Test
	public void shouldTrimSecondFixedSeqIfLongerSequence() throws Exception {
		final IterableX<String> first = of("A", "B", "C");
		final IterableX<Integer> second = of(1, 2, 3, 4);


		final IterableX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerSequence() throws Exception {
		final IterableX<String> first = of("A", "B", "C","D");
		final IterableX<Integer> second = of(1, 2, 3);
		final IterableX<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}


	@Test
	public void testZipWithIndex() {
		assertEquals(asList(), of().zipWithIndex().toList());

        of("a").zipWithIndex().map(t -> t._2()).printOut();
		assertThat(of("a").zipWithIndex().map(t -> t._2()).headOption().orElse(-1l), is(0l));
		assertEquals(asList(Tuple.tuple("a", 0L)), of("a").zipWithIndex().toList());

	}




	@Test
	public void emptyConvert(){

		assertFalse(empty().to().option().isPresent());

        assertTrue(empty().nonEmptyList(()->10).size()>0);
        assertFalse(empty().seq().size()>0);
        assertFalse(empty().lazySeq().size()>0);
        assertFalse(empty().vector().size()>0);
        assertFalse(empty().bankersQueue().size()>0);
        assertFalse(empty().hashSet().size()>0);
        assertFalse(empty().treeSet((Comparator)Comparator.naturalOrder()).size()>0);
        assertFalse(empty().to().hashMap(t->t,t->t).size()>0);
        assertFalse(empty().to().streamable().size()>0);
        assertFalse(empty().to().seq().size()>0);
        assertFalse(empty().to().lazySeq().size()>0);
        assertFalse(empty().to().vector().size()>0);
        assertFalse(empty().to().bankersQueue().size()>0);
        assertFalse(empty().to().hashSet().size()>0);
        assertFalse(empty().to().treeSet((Comparator)Comparator.naturalOrder()).size()>0);


        assertFalse(empty().toSet().size()>0);
		assertFalse(empty().toList().size()>0);
		assertFalse(empty().to().streamable().size()>0);

		assertFalse(empty.toMap(a->a).size()>0);
        assertFalse(empty.toMap(a->a,a->a).size()>0);

        assertFalse(empty.toHashMap(a->a).size()>0);
        assertFalse(empty.toHashMap(a->a,a->a).size()>0);

	}

	@Test
    public void bagXPresent(){
	    System.out.println(of(1).to().bag());
	    assertTrue(of(1).to().bag().size()>0);

    }

	@Test
	public void presentConvert(){

        assertTrue(of(1).seq().size()>0);
        assertTrue(of(1).lazySeq().size()>0);
        assertTrue(of(1).bankersQueue().size()>0);
        assertTrue(of(1).vector().size()>0);
        assertTrue(of(1).hashSet().size()>0);
        assertThat(of(1).nonEmptyList(()->10),equalTo(NonEmptyList.of(1)));

        assertTrue(of(1).to().option().isPresent());
        assertTrue(of(1).toList().size()>0);
        assertTrue(of(1).to().seq().size()>0);
        assertTrue(of(1).to().lazySeq().size()>0);
        assertTrue(of(1).to().bankersQueue().size()>0);
        assertTrue(of(1).to().vector().size()>0);
        assertTrue(of(1).to().hashSet().size()>0);
        assertTrue(of(1).toSet().size()>0);
        assertTrue(of(1).to().treeSet(Comparator.naturalOrder()).size()>0);
        assertTrue(of(1).to().streamable().size()>0);
        assertTrue(of(1).to().bag().size()>0);
        assertTrue(of(1).to().hashMap(t->t, t->t).size()>0);


        assertTrue(of(1).toSet().size()>0);
        assertTrue(of(1).toList().size()>0);
        assertTrue(of(1).to().streamable().size()>0);
        assertTrue(of(1).toMap(a->a).size()>0);
        assertTrue(of(1).toMap(a->a,a->a).size()>0);

        assertTrue(of(1).toHashMap(a->a).size()>0);
        assertTrue(of(1).toHashMap(a->a,a->a).size()>0);


        assertThat(of(1).toMap(a->a).get(1),equalTo(1));
        assertThat(of(1).toMap(a->a+1,a->a+2).get(2),equalTo(3));
        assertThat(of(1).toHashMap(a->a).get(1),equalTo(Option.some(1)));
        assertThat(of(1).toHashMap(a->a+1,a->a+2).get(2),equalTo(Option.some(3)));



    }

    @Test
    public void presentConvert2(){

        assertTrue(of(1,2).to().option().isPresent());
        assertTrue(of(1,2).toList().size()==2);
        assertTrue(of(1,2).to().seq().size()==2);
        assertTrue(of(1,2).to().lazySeq().size()==2);
        assertTrue(of(1,2).to().bankersQueue().size()==2);
        assertTrue(of(1,2).to().vector().size()==2);
        assertTrue(of(1,2).to().hashSet().size()==2);
        assertTrue(of(1,2).toSet().size()==2);
        assertTrue(of(1,2).to().treeSet(Comparator.naturalOrder()).size()==2);
        assertTrue(of(1,2).to().streamable().size()==2);
        assertTrue(of(1,2).to().bag().size()==2);
        assertTrue(of(1,2).to().hashMap(t->t, t->t).size()==2);


        assertTrue(of(1,2).toSet().size()==2);
        assertTrue(of(1,2).toList().size()==2);
        assertTrue(of(1,2).to().streamable().size()==2);
        assertTrue(of(1,2).toMap(a->a).size()==2);
        assertTrue(of(1,2).toMap(a->a,a->a).size()==2);

        assertTrue(of(1,2).toHashMap(a->a).size()==2);
        assertTrue(of(1,2).toHashMap(a->a,a->a).size()==2);

    }


	    @Test
	    public void batchBySizeCollection(){


	        assertThat(of(1,2,3,4,5,6).grouped(3,()->Vector.empty()).elementAt(0).toOptional().get().size(),is(3));

	       // assertThat(of(1,1,1,1,1,1).grouped(3,()->new ListXImpl<>()).getValue(1).getValue().size(),is(1));
	    }
	    @Test
	    public void batchBySizeInternalSize(){
	        assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).get(0).size(),is(3));
	    }
	    @Test
	    public void fixedDelay(){
	        SimpleTimer timer = new SimpleTimer();

	        assertThat(of(1,2,3,4,5,6).fixedDelay(10000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
	        assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
	    }





	    @Test
	    public void testSorted() {

            IterableX<Tuple2<Integer, String>> t1 = of(tuple(2, "two"), tuple(1, "replaceWith"));

	        List<Tuple2<Integer, String>> s1 = t1.sorted().toList();
	        System.out.println(s1);
	        assertEquals(tuple(1, "replaceWith"), s1.get(0));
	        assertEquals(tuple(2, "two"), s1.get(1));

            IterableX<Tuple2<Integer, String>> t2 = of(tuple(2, "two"), tuple(1, "replaceWith"));
	        List<Tuple2<Integer, String>> s2 = t2.sorted(comparing(t -> t._1())).toList();
	        assertEquals(tuple(1, "replaceWith"), s2.get(0));
	        assertEquals(tuple(2, "two"), s2.get(1));

            IterableX<Tuple2<Integer, String>> t3 = of(tuple(2, "two"), tuple(1, "replaceWith"));
	        List<Tuple2<Integer, String>> s3 = t3.sorted(t -> t._1()).toList();
	        assertEquals(tuple(1, "replaceWith"), s3.get(0));
	        assertEquals(tuple(2, "two"), s3.get(1));
	    }

	    @Test
	    public void zip2(){
	        List<Tuple2<Integer,Integer>> list =
	                of(1,2,3,4,5,6).zipWithStream(Stream.of(100,200,300,400))
	                                                .peek(it -> System.out.println(it))

	                                                .collect(Collectors.toList());

	        List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
	        assertThat(right,hasItem(100));
	        assertThat(right,hasItem(200));
	        assertThat(right,hasItem(300));
	        assertThat(right,hasItem(400));

	        List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
	        assertThat(of(1,2,3,4,5,6),hasItem(left.get(0)));


	    }
    @Test
    public void zip3(){
        IterableX<Tuple3<Integer, Integer, Character>> list = of(1, 2, 3, 4, 5, 6).zip3(ReactiveSeq.of(100, 200, 300, 400), ReactiveSeq.of('a', 'b'));

        assertThat(list.size(),equalTo(2));

    }
    @Test
    public void zip4(){
        IterableX<Tuple4<Integer, Integer, Character, String>> list = of(1, 2, 3, 4, 5, 6).zip4(ReactiveSeq.of(100, 200, 300, 400), ReactiveSeq.of('a', 'b'), ReactiveSeq.of("hello"));

        assertThat(list.size(),equalTo(1));

    }



    @Test
    public void notEqualNull(){
        assertFalse(empty().equals(null));
    }


	    @Test
	    public void testReverse() {
	        assertThat( of(1, 2, 3).reverse().toList().size(), is(asList(3, 2, 1).size()));
	    }

	    @Test
	    public void testShuffle() {

	        Supplier<IterableX<Integer>> s = () ->of(1, 2, 3);

	        assertEquals(3, ((IterableX<Integer>)s.get().shuffle()).toList().size());
	        assertThat(((IterableX<Integer>)s.get().shuffle()).toList(), hasItems(1, 2, 3));


	    }
	    @Test
	    public void testShuffleRandom() {
	        Random r = new Random();
	        Supplier<IterableX<Integer>> s = () ->of(1, 2, 3);

	        assertEquals(3, ((IterableX<Integer>)s.get()).shuffle(r).toList().size());
	        assertThat(((IterableX<Integer>)s.get()).shuffle(r).toList(), hasItems(1, 2, 3));


	    }






	        @Test
	        public void testMinByMaxBy2() {
	            Supplier<IterableX<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	            assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).orElse(-1));
	            assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).orElse(-1));

	            assertEquals(6, (int) s.get().maxBy(t -> "" + t).orElse(-1));
	            assertEquals(1, (int) s.get().minBy(t -> "" + t).orElse(-1));
	        }




	        @Test
	        public void testFoldLeft() {
	            for(int i=0;i<100;i++){
	                Supplier<IterableX<String>> s = () -> of("a", "b", "c");

	                assertTrue(s.get().foldLeft("", String::concat).contains("a"));
	                assertTrue(s.get().foldLeft("", String::concat).contains("b"));
	                assertTrue(s.get().foldLeft("", String::concat).contains("c"));

	                assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));


	                assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));
	            }
	        }

	        @Test
	        public void testFoldRight(){
	                Supplier<IterableX<String>> s = () -> of("a", "b", "c");

	                assertTrue(s.get().foldRight("", String::concat).contains("a"));
	                assertTrue(s.get().foldRight("", String::concat).contains("b"));
	                assertTrue(s.get().foldRight("", String::concat).contains("c"));
	                assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
	        }

	        @Test
	        public void testFoldLeftStringBuilder() {
	            Supplier<IterableX<String>> s = () -> of("a", "b", "c");


	            assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("a"));
	            assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("b"));
	            assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("c"));
	            assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("-"));


	            assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));
                assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));


	        }

	        @Test
	        public void testFoldRighttringBuilder() {
	            Supplier<IterableX<String>> s = () -> of("a", "b", "c");


	            assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("a"));
	            assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("b"));
	            assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("c"));
	            assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("-"));


	        }

	        @Test
	        public void batchUntil(){
	            assertThat(of(1,2,3,4,5,6)
	                    .groupedUntil(i->false)
	                    .toList().size(),equalTo(1));

	        }
	        @Test
	        public void batchWhile(){
	            assertThat(of(1,2,3,4,5,6)
	                    .groupedWhile(i->true)
	                    .toList()
	                    .size(),equalTo(1));

	        }
	        @Test
            public void batchUntilSupplier(){
                assertThat(of(1,2,3,4,5,6)
                        .groupedUntil(i->false,()->Vector.empty())
                        .toList().size(),equalTo(1));

            }
            @Test
            public void batchWhileSupplier(){
                assertThat(of(1,2,3,4,5,6)
                        .groupedWhile(i->true,()->Vector.empty())
                        .toList()
                        .size(),equalTo(1));

            }

	        @Test
	        public void slidingNoOrder() {
	            List<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).toList();

	            System.out.println(list);
	            assertThat(list.get(0).size(), equalTo(2));
	            assertThat(list.get(1).size(), equalTo(2));
	        }

	        @Test
	        public void slidingIncrementNoOrder() {
	            List<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

	            System.out.println(list);

                assertThat(list.get(1).size(), greaterThan(1));
	        }

	        @Test
	        public void combineNoOrder(){
	            assertThat(of(1,2,3)
	                       .combine((a, b)->a.equals(b), Semigroups.intSum)
	                       .toList(),equalTo(Arrays.asList(1,2,3)));

	        }

	        @Test
	        public void zip3NoOrder(){
	            List<Tuple3<Integer,Integer,Character>> list =
	                    of(1,2,3,4).zip3(of(100,200,300,400).stream(),of('a','b','c','d').stream())
	                                                    .toList();

	            System.out.println(list);
	            List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
	            assertThat(right,hasItem(100));
	            assertThat(right,hasItem(200));
	            assertThat(right,hasItem(300));
	            assertThat(right,hasItem(400));

	            List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
	            assertThat(of(1,2,3,4),hasItem(left.get(0)));

	            List<Character> three = list.stream().map(t -> t._3()).collect(Collectors.toList());
	            assertThat(of('a','b','c','d'),hasItem(three.get(0)));


	        }
	        @Test
	        public void zip4NoOrder(){
	            List<Tuple4<Integer,Integer,Character,String>> list =
	                    of(1,2,3,4).zip4(of(100,200,300,400).stream(),of('a','b','c','d').stream(),of("hello","world","boo!","2").stream())
	                                                    .toList();
	            System.out.println(list);
	            List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
	            assertThat(right,hasItem(100));
	            assertThat(right,hasItem(200));
	            assertThat(right,hasItem(300));
	            assertThat(right,hasItem(400));

	            List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
	            assertThat(of(1,2,3,4),hasItem(left.get(0)));

	            List<Character> three = list.stream().map(t -> t._3()).collect(Collectors.toList());
	            assertThat(of('a','b','c','d'),hasItem(three.get(0)));

	            List<String> four = list.stream().map(t -> t._4()).collect(Collectors.toList());
	            assertThat(of("hello","world","boo!","2"),hasItem(four.get(0)));


	        }

	        @Test
	        public void testIntersperseNoOrder() {

	            assertThat(((IterableX<Integer>)of(1,2,3).intersperse(0)).toList(),hasItem(0));




	        }




	        @Test
	        public void allCombinations3NoOrder() {
	            System.out.println(of(1, 2, 3).combinations().map(s->s.toList()).toList());
	            assertThat(of(1, 2, 3).combinations().map(s->s.toList()).toList().size(),equalTo(8));
	        }

	        @Test
	        public void emptyAllCombinationsNoOrder() {
	            assertThat(of().combinations().map(s -> s.toList()), equalTo(of(Arrays.asList())));
	        }

	        @Test
	        public void emptyPermutationsNoOrder() {
	            assertThat(of().permutations().map(s->s.toList()),equalTo(of()));
	        }

	        @Test
	        public void permuations3NoOrder() {
	            System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
	            assertThat(of(1, 2, 3).permutations().map(s->s.toList()).toList().get(0).size(),
	                    equalTo(3));
	        }

	        @Test
	        public void emptyCombinationsNoOrder() {
	            assertThat(of().combinations(2).map(s -> s.toList()).toList(), equalTo(Arrays.asList()));
	        }

	         @Test
	        public void combinations2NoOrder() {

	                assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()).toList().get(0).size(),
	                        equalTo(2));
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

	    Trampoline<Long> fibonacci(int i){
	        return fibonacci(i,1,0);
	    }
	    Trampoline<Long> fibonacci(int n, long a, long b) {
	        return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
	    }
	    @Test
	    public void cycleMonoidNoOrder(){
	        assertThat(of(1,2,3)
	                    .cycle(Reducers.toCountInt(),3)
	                    .toList(),
	                    equalTo(Arrays.asList(3,3,3)));
	    }
	    @Test
	    public void testCycleNoOrder() {
	        assertEquals(6,of(1, 2).cycle(3).toList().size());
	        assertEquals(6, of(1, 2, 3).cycle(2).toList().size());
	    }
	    @Test
	    public void testCycleTimesNoOrder() {
	        assertEquals(6,of(1, 2).cycle(3).toList().size());

	    }

	    @Test
	    public void testCycleWhile() {
	        count =0;
	        assertEquals(6,of(1, 2, 3).cycleWhile(next->count++<6).toList().size());

	    }
	    @Test
	    public void testCycleUntil() {
	        count =0;
	        System.out.println("List " + of(1, 2, 3).peek(System.out::println).cycleUntil(next->count++==6).toList());
	        count =0;
	        assertEquals(6,of(1, 2, 3).cycleUntil(next->count++==6).toList().size());

	    }




    @Test
    public void sortedComparatorNoOrd() {
        assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1),is(of(5,4,3,2,1)));
    }
    @Test
    public void takeRightNoOrd(){
        assertThat(of(1,2,3).takeRight(1).toList(),hasItems(3));
    }
    @Test
    public void takeRightEmptyNoOrd(){
        assertThat(of().takeRight(1),equalTo(of()));
    }

    @Test
    public void takeUntilNoOrd(){
        assertThat(of(1,2,3,4,5).takeUntil(p->p==2).toList().size(),greaterThan(0));
    }
    @Test
    public void takeUntilEmptyNoOrd(){
        assertThat(of().takeUntil(p->true),equalTo(of()));
    }
    @Test
    public void takeWhileNoOrd(){
        assertThat(of(1,2,3,4,5).takeWhile(p->p<6).toList().size(),greaterThan(1));
    }
    @Test
    public void takeWhileEmptyNoOrd(){
        assertThat(of().takeWhile(p->true),equalTo(of()));
    }

    @Test
    public void testOnEmptyOrderedNoOrd()  {
        assertEquals(asList(1), of().onEmpty(1).toList());
        assertEquals(asList(1), of().onEmptyGet(() -> 1).toList());

        assertEquals(asList(2), of(2).onEmpty(1).toList());
        assertEquals(of(2), of(2).onEmptyGet(() -> 1));


        assertEquals(asList(2, 3), of(2, 3).onEmpty(1).toList());
        assertEquals(asList(2, 3), of(2, 3).onEmptyGet(() -> 1).toList());

    }
    @Test
    public void testCycleNoOrd() {
        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle(3).toList());
        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).cycle(2).toList());
    }
    /**
    @Test
    public void testCycleTimesNoOrd() {
        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle(3).toListX());
    }
**/
    int count =0;
    @Test
    public void testCycleWhileNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleWhile(next->count++<6).toList());

    }
    @Test
    public void testCycleUntilNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toList());

    }
    @Test
    public void slidingNoOrd() {
        List<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).toList();

        System.out.println(list);
        assertThat(list.get(0), hasItems(1, 2));
        assertThat(list.get(1), hasItems(2, 3));
    }

    @Test
    public void slidingIncrementNoOrd() {
        List<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

        System.out.println(list);
        assertThat(list.get(0), hasItems(1, 2, 3));
        assertThat(list.get(1), hasItems(3, 4, 5));
    }

    @Test
    public void combineNoOrd(){
        assertThat(of(1,1,2,3)
                .combine((a, b)->a.equals(b),Semigroups.intSum)
                .toList(),equalTo(Arrays.asList(4,3)));
    }

    @Test
    public void zip3NoOrd(){
        List<Tuple3<Integer,Integer,Character>> list =
                of(1,2,3,4,5,6).zip3(of(100,200,300,400).stream(),of('a','b','c').stream())
                        .toList();

        System.out.println(list);
        List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
        assertThat(right,hasItem(100));
        assertThat(right,hasItem(200));
        assertThat(right,hasItem(300));
        assertThat(right,not(hasItem(400)));

        List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
        assertThat(of(1,2,3,4,5,6),hasItem(left.get(0)));

        List<Character> three = list.stream().map(t -> t._3()).collect(Collectors.toList());
        assertThat(of('a','b','c'),hasItem(three.get(0)));


    }
    @Test
    public void zip4NoOrd(){
        List<Tuple4<Integer,Integer,Character,String>> list =
                of(1,2,3,4,5,6).zip4(of(100,200,300,400).stream(),of('a','b','c').stream(),of("hello","world").stream())
                        .toList();
        System.out.println(list);
        List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
        assertThat(right,hasItem(100));
        assertThat(right,hasItem(200));
        assertThat(right,not(hasItem(300)));
        assertThat(right,not(hasItem(400)));

        List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
        assertThat(of(1,2,3,4,5,6),hasItem(left.get(0)));

        List<Character> three = list.stream().map(t -> t._3()).collect(Collectors.toList());
        assertThat(of('a','b','c'),hasItem(three.get(0)));

        List<String> four = list.stream().map(t -> t._4()).collect(Collectors.toList());
        assertThat(of("hello","world"),hasItem(four.get(0)));


    }

    @Test
    public void testIntersperseNoOrd() {

        assertThat(((IterableX<Integer>)of(1,2,3).intersperse(0)),equalTo(of(1,0,2,0,3)));




    }




    private int addOne(Integer i){
        return i+1;
    }
    private int add(Integer a, Integer b){
        return a+b;
    }
    private String concat(String a, String b, String c){
        return a+b+c;
    }
    private String concat4(String a, String b, String c,String d){
        return a+b+c+d;
    }
    private String concat5(String a, String b, String c,String d,String e){
        return a+b+c+d+e;
    }



    @Test
    public void allCombinations3NoOrd() {
        assertThat(of(1, 2, 3).combinations().map(s->s.toList()),equalTo(of(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
                Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

    @Test
    public void emptyAllCombinationsNoOrd() {
        assertThat(of().combinations().map(s -> s.toList()).toList(), equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void emptyPermutationsNoOrd() {
        assertThat(of().permutations().map(s->s.toList()),equalTo(of()));
    }

    @Test
    public void permuations3NoOrd() {
        System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(of(1, 2, 3).permutations().map(s->s.toList()).toList(),
                equalTo(of(of(1, 2, 3),
                        of(1, 3, 2), of(2, 1, 3), of(2, 3, 1), of(3, 1, 2), of(3, 2, 1)).peek(i->System.out.println("peek - " + i)).map(s->s.toList()).toList()));
    }

    @Test
    public void emptyCombinationsNoOrd() {
        assertThat(of().combinations(2).map(s -> s.toList()).toList(), equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2NoOrd() {
        assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()),
                equalTo(of(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }




    @Test
    public void testScanLeftStringConcatNoOrd() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
                is(4));
    }
    @Test
    public void batchBySizeNoOrd(){
        System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
        assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
    }
    @Test
    public void testReverseNoOrd() {

        assertThat(of(1, 2, 3).reverse().toList(), containsInAnyOrder(3, 2, 1));
    }

    @Test
    public void testFoldRightNoOrd() {
        Supplier<IterableX<String>> s = () -> of("a", "b", "c");

        assertTrue(s.get().foldRight("", String::concat).contains("a"));
        assertTrue(s.get().foldRight("", String::concat).contains("b"));
        assertTrue(s.get().foldRight("", String::concat).contains("c"));

    }

    @Test
    public void testFoldLeftNoOrd() {
        for (int i = 0; i < 100; i++) {
            Supplier<IterableX<String>> s = () -> of("a", "b", "c");

            assertTrue(s.get().foldLeft("", String::concat).contains("a"));
            assertTrue(s.get().foldLeft("", String::concat).contains("b"));
            assertTrue(s.get().foldLeft("", String::concat).contains("c"));


        }
    }
    private Trampoline<Integer> sum(int times,int sum){
        return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
    }


    @Test
    public void plus(){
        IterableX<Integer> vec = this.<Integer>empty().plus(1).plus(2).plus(5);

        assertThat(vec,equalTo(of(1,2,5)));
    }
    @Test
    public void plusAll(){
        IterableX<Integer> vec = this.<Integer>empty().plusAll(of(1)).plusAll(of(2)).plusAll(of(5));

        assertThat(vec,equalTo(of(1,2,5)));
    }
    @Test
    public void insertAt0(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,1).insertAt(0,2).insertAt(0,5);

        assertThat(vec,equalTo(of(5,2,1)));
    }
    @Test
    public void insertAtMultiple0(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,Arrays.asList(1,2))
                .insertAt(0,Arrays.asList(2,2)).insertAt(0,Arrays.asList(5,2));

        assertThat(vec,equalTo(of(5,2,2,2,1,2)));
    }
    @Test
    public void insertAtSize(){
        IterableX<Integer> vec = this.<Integer>empty();
        vec = vec.insertAt(Math.max(0,vec.size()),1);
        vec = vec.insertAt(Math.max(0,vec.size()),2);

        assertThat(vec,equalTo(of(1,2)));
    }
    @Test
    public void insertAtAll0(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,of(1))
                                     .insertAt(0,of(2))
                                     .insertAt(0,of(5));


        assertThat(vec,equalTo(of(5,2,1)));
    }
    @Test
    public void plusAllSize(){

        IterableX<Integer> vec = this.<Integer>empty();
        vec = vec.insertAt(Math.max(0,vec.size()),of(1));
        System.out.println("Vec1 " + vec);
        vec = vec.insertAt(Math.max(0,vec.size()),of(2));

        System.out.println("Vec " + vec);
        assertThat(vec,equalTo(of(1,2)));
    }

    @Test
    public void withTest(){

        assertEquals(of("x", "b", "c"), of("a", "b", "c").updateAt(0, "x"));
        assertEquals(of("a", "x", "c"), of("a", "b", "c").updateAt(1, "x"));
        assertEquals(of("a", "b", "x"), of("a", "b", "c").updateAt(2, "x"));
    }
    @Test
    public void withLarge(){
        System.out.println(range(0,2000).insertAt(1010,-1).containsValue(-1));
        assertThat(range(0,2000).insertAt(1010,-1).containsValue(-1),equalTo(true));
    }
    @Test
    public void containsValueTests(){

        assertThat(of(0,1,20).containsValue(-1),equalTo(false));
        assertThat(range(0,64).insertAt(1010,-1).containsValue(-1),equalTo(true));
        assertThat(range(0,128).insertAt(1010,-1).containsValue(-1),equalTo(true));
        assertThat(range(0,256).insertAt(1010,-1).containsValue(-1),equalTo(true));
        assertThat(range(0,512).insertAt(1010,-1).containsValue(-1),equalTo(true));
        assertThat(range(0,1024).insertAt(1010,-1).containsValue(-1),equalTo(true));
        assertThat(range(0,2000).insertAt(1010,-1).containsValue(-1),equalTo(true));
        assertThat(range(0,20000).insertAt(10010,-1).containsValue(-1),equalTo(true));
        assertThat(range(0,2000).insertAt(1010,-10).containsValue(-1),equalTo(false));
        assertThat(range(0,20000).insertAt(10010,-10).containsValue(-1),equalTo(false));
    }
    @Test
    public void minus(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,1).insertAt(0,2).insertAt(0,5);

        assertThat(vec.removeValue(2),equalTo(of(5,1)));
    }

    @Test
    public void plusTests(){
        assertThat(of(1,2,3,4,5),equalTo(empty().plusAll(of(1,2,3,4,5))));
        assertThat(empty().plus(1).plus(2).plus(3)
                .plus(4).plus(5).size(),equalTo(5));
    }

    @Test
    public void append(){
        assertThat(of(1).append(2).size(),equalTo(2));
        assertThat(of().append(2).size(),equalTo(1));
    }
    @Test
    public void appendMultiple(){
        assertThat(of(1).appendAll(2,3).size(),equalTo(3));
        assertThat(of(1).appendAll(2,3,4).size(),equalTo(4));
        assertThat(of().appendAll(2,3,4).size(),equalTo(3));
    }
    @Test
    public void prependAppend(){
        System.out.println(of(1)
            .prependStream(Stream.of(2)).append(3).prepend(4).appendAll(5,6)
            .prependAll(7,8)
            .insertAt(4,9).deleteBetween(1,2)
            .insertStreamAt(5,Stream.of(11,12)));
        assertThat(of(1)
                    .prependStream(Stream.of(2)).append(3).prepend(4).appendAll(5,6)
                    .prependAll(7,8)
                    .insertAt(4,9).deleteBetween(1,2)
                .insertStreamAt(5,Stream.of(11,12)).stream().count(),equalTo(10L));
    }
    @Test
    public void insertAndRemove(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,1).insertAt(0,2,5);

        assertThat(vec.removeValue(2),equalTo(of(5,1)));
    }
    @Test
    public void insertAtChain(){
        assertThat(this.<Integer>empty().insertAt(0,1).insertAt(0,2),equalTo(of(2,1)));
    }
    @Test
    public void removeAt(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,1).insertAt(0,2).insertAt(0,5);


        assertThat(vec.removeAt(1),equalTo(of(5,1)));
    }
    @Test
    public void removeFirst(){
        IterableX<Integer> vec = this.of(1,2,2,2,3);

        assertThat(vec.removeFirst(i->i==2),equalTo(of(1,2,2,3)));
    }
    @Test
    public void minusAt(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,1).insertAt(0,2).insertAt(0,5);


        assertThat(vec.removeAt(1),equalTo(of(5,1)));
    }
    @Test
    public void minusAtOutOfRange(){
        IterableX<Integer> vec = this.<Integer>empty();
        vec = vec.insertAt(0,1)
                 .insertAt(0,2)
                 .insertAt(0,5);




        assertThat(vec.removeAt(-1),equalTo(of(5,2,1)));
        assertThat(vec.removeAt(500),equalTo(of(5,2,1)));

    }
    @Test
    public void updateAt(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,1).insertAt(0,2).insertAt(0,5);

        assertThat(vec.updateAt(1,10),equalTo(of(5,10,1)));
    }
    @Test
    public void updateAtEmpty(){
        IterableX<Integer> vec = this.<Integer>empty();

        assertThat(vec.updateAt(0,10),equalTo(of()));
    }
    @Test
    public void updateAtOutOfRange(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,1).insertAt(0,2).insertAt(0,5);

        assertThat(vec.updateAt(-1,10),equalTo(of(5,2,1)));
    }
    @Test
    public void updateAtOutOfRange2(){
        IterableX<Integer> vec = this.<Integer>empty().insertAt(0,1).insertAt(0,2).insertAt(0,5);

        assertThat(vec.updateAt(100,10),equalTo(of(5,2,1)));
    }

    @Test
    public void largePlusAll(){
        assertThat(range(0,2000).insertAt(1010,of(-1,-2,-3)).size(),equalTo(2003));
        assertThat(range(0,2000).insertAt(10010,of(-1,-2,-3)).size(),equalTo(2003));
    }
    @Test
    public void largePlus(){
        assertThat(range(0,2_000).insertAt(20000,-1).size(),equalTo(2_001));
        assertThat(range(0,2_000).insertAt(60000,-1).size(),equalTo(2_001));
        assertThat(range(0,2_000).insertAt(20000,-1),hasItem(-1));
    }

    @Test
    public void compareDifferentSizes(){
        assertThat(empty(),not(equalTo(of(1))));
        assertThat(of(1),not(equalTo(empty())));
        assertThat(of(1),not(equalTo(of(1,2,3))));
    }

    @Test
    public void isEmpty(){
        assertThat(empty().isEmpty(),equalTo(true));
        assertThat(of(1).isEmpty(),equalTo(false));
    }


    @Test
    public void appendAll(){
        assertThat(of(1,2,3,4).appendAll(Arrays.asList(10,20,30)),equalTo(of(1,2,3,4,10,20,30)));
        assertThat(empty().appendAll(Arrays.asList(10,20,30)),equalTo(of(10,20,30)));
    }

    @Test
    public void prependAll(){
        assertThat(of(1,2,3,4).prependAll(Arrays.asList(10,20,30)),equalTo(of(10,20,30,1,2,3,4)));
        assertThat(empty().prependAll(Arrays.asList(10,20,30)),equalTo(of(10,20,30)));
    }

    @Test
    public void batchUntilCollection(){
        assertThat(of(1,2,3,4,5,6)
                .groupedUntil(i->i%3==0,()->Vector.empty())
                .toList().size(),equalTo(2));
        assertThat(of(1,2,3,4,5,6)
                .groupedUntil(i->i%3==0,()->Vector.empty())
                .toList().get(0),equalTo(Vector.of(1,2,3)));
    }
    @Test
    public void batchWhileCollection(){
        assertThat(of(1,2,3,4,5,6)
                .groupedWhile(i->i%3!=0,()->Vector.empty())
                .toList().size(),equalTo(2));
        assertThat(of(1,2,3,4,5,6)
                .groupedWhile(i->i%3!=0,()->Vector.empty()),equalTo(of(Vector.of(1,2,3),Vector.of(4,5,6))));
    }



    @Test
    public void fixedDelay2() {

        fromIntStream(IntStream.range(0, 1000))
                .fixedDelay(1l, TimeUnit.MICROSECONDS).peek(System.out::println)
                .forEach(a->{});
    }
    @Test
    public void onePerSecond() {

        long start = System.currentTimeMillis();
        iterate(4,0, it -> it + 1)
                .limit(3)
                .onePer(1, TimeUnit.SECONDS)
                .map(seconds -> "hello!")
                .peek(System.out::println)
                .toList();

        assertTrue(System.currentTimeMillis()-start>1000);

    }
    @Test
    public void xPerSecond() throws InterruptedException {
        Thread.sleep(500);
        long start = System.currentTimeMillis();
        iterate(4,1, it -> it + 1)
                .xPer(1,1, TimeUnit.SECONDS)
                .limit(3)
                .map(seconds -> "hello!")
                .peek(System.out::println)
                .toList();
        System.out.println("time = " +(System.currentTimeMillis()-start));
        assertTrue("failed time was " + (System.currentTimeMillis()-start),System.currentTimeMillis()-start>1000);

    }


    @Test
    public void batchBySize3(){
        System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
        assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
    }


    @Test
    public void batchBySizeSet(){
        System.out.println("List = " + of(1,1,1,1,1,1).grouped(3,()-> TreeSet.empty()).toList());
        assertThat(of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList().get(0).size(),is(1));
        assertThat(of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList().size(),is(2));
    }
    @Test
    public void batchBySizeSetEmpty(){

        assertThat(this.<Integer>of().grouped(3,()->TreeSet.empty()).toList().size(),is(0));
    }


    @Test
    public void combine(){
        assertThat(of(1,1,2,3)
                .combine((a, b)->a.equals(b),Semigroups.intSum)
                .toList(),equalTo(Arrays.asList(4,3)));

    }

    @Test
    public void emptyPermutations() {
        assertThat(of().permutations().map(s->s.toList()),equalTo(of()));
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
        assertThat(of().combinations().map(s->s.toList()),equalTo(of(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(of(1, 2, 3).combinations().map(s->s.toList()),equalTo(of(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
                Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }



    @Test
    public void emptyCombinations() {
        assertThat(of().combinations(2).toList(),equalTo(Arrays.asList()));
    }



    @Test
    public void combinations2() {
        assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()),
                equalTo(of(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
    }




    @Test @Ignore
    public void testOfType() {



        assertThat(of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),containsInAnyOrder(1, 2, 3));

        assertThat(of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList(),not(containsInAnyOrder("a", "b",null)));

        assertThat(of(1, "a", 2, "b", 3, null)

                .ofType(Serializable.class).toList(),containsInAnyOrder(1, "a", 2, "b", 3));

    }

    @Test
    public void testIntersperse() {

        assertThat(of(1,2,3).intersperse(0),equalTo(of(1,0,2,0,3)));

    }


    @Test
    public void reversedRange(){
        assertThat(range(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeWithReverse(){
        assertThat(ReactiveSeq.range(10, -10).reverse().count(),equalTo(20L));
    }
    @Test
    public void reversedRangeLong(){
        System.out.println(rangeLong(10, -10));
        assertThat(rangeLong(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeInt(){
        assertThat(range(10, -10).count(),equalTo(20L));
    }
    @Test
    public void reversedRangeLongWithReverse(){
        System.out.println(rangeLong(10, -10).reverse());
        assertThat(rangeLong(10, -10).reverse().count(),equalTo(20L));
    }
    @Test
    public void intStreamCompare0(){

        assertThat(IntStream.range(0,10).sum(),
                equalTo(ReactiveSeq.range(0,10).sumInt(i->i)));
    }
    @Test
    public void longStreamCompare0(){
        assertThat(LongStream.range(0,10).sum(),
                equalTo(rangeLong(0,10).sumLong(i->i)));
    }
    @Test
    public void intStreamCompareReversed(){


        assertThat(0,
                equalTo(range(-5,6).reverse().sumInt(i->i)));

    }
    @Test
    public void longStreamCompareReversed(){
        assertThat(0L,
                equalTo(rangeLong(-5,6).reverse().sumLong(i->i)));
    }
    @Test
    public void intStreamCompare(){
        assertThat(IntStream.range(-1,10).sum(),
                equalTo(range(-1,10).sumInt(i->i)));
    }
    @Test
    public void longStreamCompare(){
        assertThat(LongStream.range(-1l,10l).sum(),
                equalTo(rangeLong(-1l,10l).sumLong(i->i)));
    }
    @Test
    public void negative(){
        assertThat(range(-1000,150)
                .limit(100)
                .count(),equalTo(100l));
    }
    @Test
    public void negativeLong(){
        assertThat(rangeLong(-1000L,200)
                .limit(100)
                .count(),equalTo(100L));
    }
    @Test
    public void limitRange() throws InterruptedException{

        assertThat(range(0,150)
                .limit(100)
                .count(),equalTo(100L));
    }


    @Test
    public void rangeLong(){
        assertThat(rangeLong(0,5)
                .limit(2).toList(),equalTo(Arrays.asList(0l,1l)));
    }

    @Test
    public void rangeLongReversedSkip(){
        System.out.println(rangeLong(0,5).reverse()
                .skip(3));
        assertThat(rangeLong(0,5).reverse()
                .skip(3).toList(),equalTo(Arrays.asList(1l,0l)));
    }
    @Test
    public void rangeLongSkip(){
        assertThat(rangeLong(0,5)
                .skip(3).toList(),equalTo(Arrays.asList(3l,4l)));
    }
    @Test
    public void rangeInt(){
        System.out.println(range(0,150));
        assertThat(range(0,150)
                .limit(2).toList(),equalTo(Arrays.asList(0,1)));
    }
    @Test
    public void rangeIntReversed(){
        assertThat(range(0,150).reverse()
                .limit(2).toList(),equalTo(Arrays.asList(149, 148)));
    }
    @Test
    public void rangeIntReversedSkip2(){
        assertThat(range(0,5).reverse()
                .skip(3).toList(),equalTo(Arrays.asList(1,0)));
    }

    @Test
    public void rangeIntSkip2(){
        assertThat(range(0,5)
                .skip(3).toList(),equalTo(Arrays.asList(3,4)));
    }

    @Test
    public void take2Reversed(){
        range(0,10).reverse().limit(2).printOut();
        assertThat(range(0,10).reverse().limit(2).toList(),equalTo(Arrays.asList(9,8)));
    }
    @Test
    public void rangeIntReversedSkip(){

        assertThat(range(0,20).reverse()
                .limit(10).skip(8).toList(),equalTo(Arrays.asList(11, 10)));
    }

    @Test
    public void rangeIntSkip(){

        assertThat(range(0,20)
                .limit(10).skip(8).toList(),equalTo(Arrays.asList(8, 9)));
    }
    @Test
    public void limitArray() throws InterruptedException{

        List<Integer> list= new ArrayList<>();
        for(int i=0;i<1000;i++)
            list.add(i);
        assertThat(of(list.toArray())
                .limit(100)
                .count(),equalTo(100L));

    }
    @Test
    public void skipArray() throws InterruptedException{

        List<Integer> list= new ArrayList<>();
        for(int i=0;i<1000;i++)
            list.add(i);
        assertThat(of(list.toArray())
                .skip(100)
                .count(),equalTo(900L));

    }
    @Test
    public void skipRange() throws InterruptedException{

        assertThat(range(0,1000)
                .skip(100)
                .count(),equalTo(900L));
    }
    @Test
    public void skipRangeLong() throws InterruptedException{

        assertThat(rangeLong(0,1000)
                .skip(100)
                .count(),equalTo(900L));
    }
    @Test
    public void skipRangeReversed() throws InterruptedException{

        assertThat(range(0,1000)
                .skip(100).reverse()
                .count(),equalTo(900L));
    }







    @Test
    public void reduceWithMonoid(){

        assertThat(of("hello","2","world","4").foldMap(Reducers.toCountInt()),equalTo(4));
    }
    @Test
    public void reduceWithMonoid2(){

        assertThat(of("replaceWith","two","three","four").foldMap(this::toInt,Reducers.toTotalInt()),
                equalTo(10));
    }

    int toInt(String s){
        if("replaceWith".equals(s))
            return 1;
        if("two".equals(s))
            return 2;
        if("three".equals(s))
            return 3;
        if("four".equals(s))
            return 4;
        return -1;
    }
    @Test
    public void reduceWithMonoidJoin(){
        assertThat(of("hello","2","world","4").join(","),equalTo("hello,2,world,4"));
        assertThat(of("hello","2","world","4").foldLeft(Reducers.toString(",")),
                equalTo(",hello,2,world,4"));
    }



    @Test
    public void testMapToInt(){
        assertThat(of("1","2","3","4").stream().mapToInt(it -> Integer.valueOf(it)).max().getAsInt(),equalTo(4));

    }

    @Test
    public void mapToLong() {
        assertThat(of("1","2","3","4").stream().mapToLong(it -> Long.valueOf(it)).max().getAsLong(),equalTo(4l));
    }

    @Test
    public void mapToDouble() {
        assertThat(of("1","2","3","4").stream().mapToDouble(it -> Double.valueOf(it)).max().getAsDouble(),equalTo(4d));
    }





    @Test
    public void forEachOrderedx() {
        List<Integer> list = new ArrayList<>();
        of(1,5,3,4,2).forEach(it-> list.add(it));
        assertThat(list,hasItem(1));
        assertThat(list,hasItem(2));
        assertThat(list,hasItem(3));
        assertThat(list,hasItem(4));
        assertThat(list,hasItem(5));

    }

    @Test
    public void testToArray() {
        assertThat( Arrays.asList(1,2,3,4,5),hasItem(ReactiveSeq.of(1,5,3,4,2).toArray()[0]));
    }
    @Test
    public void testToArrayGenerator() {
        assertThat( Arrays.asList(1,2,3,4,5),hasItem(ReactiveSeq.of(1,5,3,4,2).toArray(it->new Integer[it])[0]));
    }



    @Test
    public void collectSBB(){

        List<Integer> list = ReactiveSeq.of(1,2,3,4,5).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertThat(list.size(),equalTo(5));
    }
  @Test
  public void removeValue(){
    assertThat(of(1,2,3).removeValue(0),equalTo(of(1,2,3)));
    assertThat(of(1,2,3).removeValue(4),equalTo(of(1,2,3)));
  }

}
