package cyclops.reactive.collections;


import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.FluentCollectionX;


import cyclops.data.basetests.AbstractIterableXTest;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.TreeSet;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;

public abstract class AbstractCollectionXTest extends AbstractIterableXTest {
	public abstract <T> FluentCollectionX<T> empty();
	public abstract <T> FluentCollectionX<T> of(T... values);
	public abstract  CollectionX<Integer> range(int start, int end);
	public abstract  CollectionX<Long> rangeLong(long start, long end);
	public abstract <T> CollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn);
	public abstract <T> CollectionX<T> generate(int times,Supplier<T> fn);
	public abstract <U,T> CollectionX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder);


	int captured=-1;

	static Executor ex = Executors.newFixedThreadPool(1);
    boolean set = false;
    @Test
    public void testOnEmpty() throws AbstractIterableXTest.X {
        assertEquals(asList(1), of().onEmpty(1).to().listX());
        assertEquals(asList(1), of().onEmptyGet(() -> 1).to().listX());

        assertEquals(asList(2), of(2).onEmpty(1).to().listX());
        assertEquals(asList(2), of(2).onEmptyGet(() -> 1).to().listX());
        assertEquals(asList(2), of(2).onEmptyError(() -> new AbstractIterableXTest.X()).to().listX());


    }
    @Mock
    Function<Integer, String> serviceMock;

    Throwable error;




    private CompletableFuture<String> failedAsync(Throwable throwable) {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }

    @Test
    public void shouldSucceedAfterFewAsynchronousRetries() throws Exception {

        given(serviceMock.apply(anyInt())).willThrow(
            new RuntimeException(new SocketException("First")),
            new RuntimeException(new IOException("Second"))).willReturn(
            "42");

        long start = System.currentTimeMillis();
        String result = of( 1,  2, 3)
            .retry(serviceMock)
            .firstValue(null);

        assertThat((Long)System.currentTimeMillis()-start ,greaterThan(2000l));
        assertThat(result, is("42"));
    }


    @Test
    public void retryShouldNotThrowNPEIfRetryIsZero() {
        Function<Integer, Integer> fn = i -> 2 * i;

        int result = of(1)
            .retry(fn, 0, 1, TimeUnit.SECONDS)
            .firstValue(null);

        assertEquals(2, result);
    }

    @Test(expected = ArithmeticException.class)
    public void retryShouldExecuteFnEvenIfRetryIsZero() {
        Function<Integer, Integer> fn = i -> i / 0;

        of(1)
            .retry(fn, 0, 1, TimeUnit.MILLISECONDS)
            .firstValue(null);

        fail();
    }

    @Test
    public void retryShouldWaitOnlyAfterFailure() {
        final long[] timings = {System.currentTimeMillis(), Long.MAX_VALUE};
        Function<Integer, Integer> fn = i -> {
            timings[1] = System.currentTimeMillis();
            return 2 * i;
        };

        of(1)
            .retry(fn, 3, 10000, TimeUnit.MILLISECONDS)
            .firstValue(null);

        assertTrue(timings[1] - timings[0] < 5000);
    }


    @Test
    public void isLazy(){
        of(1,2,3).filterNot(i->{
            set = true;
            return i==1;
        });
        assertFalse(set);
       assertTrue(of(1,2,3).filterNot(i->{
            set = true;
            return i==1;
        }).isLazy());
    }

    @Test
    public void isEager(){
        of(1,2,3).eager().filterNot(i->{
            set = true;
            return i==1;
        });
        assertTrue(set);
        assertTrue( of(1,2,3).eager().filterNot(i->{
            set = true;
            return i==1;
        }).isEager());
    }
    @Test
    public void isLazyViaEager(){
        of(1,2,3).eager().lazy().filterNot(i->{
            set = true;
            return i==1;
        });
        assertFalse(set);
        assertTrue( of(1,2,3).eager().lazy().filterNot(i->{
            set = true;
            return i==1;
        }).isLazy());
    }

    @Test
    public void plusLoop(){
        assertThat(of(0,1,2).plusLoop(10,i->i+100).size(),equalTo(13));
    }
    @Test
    public void plusLoopOpt(){
        int[] i = {10};
        assertThat(of(0,1,2).plusLoop(()->i[0]!=20? Option.of(i[0]++) : Option.none()).size(),equalTo(13));
    }

	@Test
    public void plusOneOrder(){
        assertThat(of().plusInOrder(1),hasItem(1));
    }
	@Test
    public void plusAllOne(){
        assertThat(of().plusAll(of(1)),hasItem(1));
    }
    @Test
    public void plusAllTwo(){
        assertThat(of().plusAll(of(1)).plus(2),hasItems(1,2));
    }

	@Test
    public void minusOne(){
        assertThat(of().removeAt(1).size(),equalTo(0));
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
    public void minusAllOne(){
        assertThat(of().removeAll((Iterable)of(1)).size(),equalTo(0));
    }
    @Test
    public void minusAllOneNotEmpty(){
        System.out.println(of(1)
                .removeAll((Iterable)of(1)));
        assertThat(of(1).removeAll((Iterable)of(1)).size(),equalTo(0));
    }
    @Test
    public void minusAllOneTwoValues(){
        assertThat(of(1,2).removeAll((Iterable<Integer>)of(1)),hasItem(2));
        assertThat(of(1,2).removeAll((Iterable<Integer>)of(1)),not(hasItem(1)));
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
        assertThat(of(1,2,3,4,5).map(it -> it*100).foldLeft( (acc,next) -> acc+next).orElse(-1),is(1500));
    }
    @Test
    public void testMapReduceSeed(){
        assertThat(of(1,2,3,4,5).map(it -> it*100).foldLeft( 50,(acc,next) -> acc+next),is(1550));
    }


    @Test
    public void testMapReduceCombiner(){
        assertThat(of(1,2,3,4,5).map(it -> it*100).foldLeft( 0,
                (acc, next) -> acc+next,
                Integer::sum),is(1500));
    }
    @Test
    public void testFindFirst(){
        assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).headOption().orElse(-1)));
    }
    /**
    @Test
    public void testFindAny(){
        assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findAny().get()));
    }
     **/
    @Test
    public void testDistinct(){
        assertThat(of(1,1,1,2,1).distinct().collect(java.util.stream.Collectors.toList()).size(),is(2));
        assertThat(of(1,1,1,2,1).distinct().collect(java.util.stream.Collectors.toList()),hasItem(1));
        assertThat(of(1,1,1,2,1).distinct().collect(java.util.stream.Collectors.toList()),hasItem(2));
    }


    @Test
    public void testMax2(){
        assertThat(of(1,2,3,4,5).maximum((t1, t2) -> t1-t2).orElse(-1),is(5));
    }
    @Test
    public void testMin2(){
        assertThat(of(1,2,3,4,5).minimum((t1, t2) -> t1-t2).orElse(-10),is(1));
    }





    @Test
    public void sorted() {
        assertThat(of(1,5,3,4,2).sorted().collect(java.util.stream.Collectors.toList()),is(Arrays.asList(1,2,3,4,5)));
    }
    @Test
    public void sortedComparator() {
        assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(java.util.stream.Collectors.toList()).size(),is(5));
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
    public void testToArray() {
        assertThat( Arrays.asList(1,2,3,4,5), Matchers.hasItem(of(1,5,3,4,2).toArray()[0]));
    }


    @Test
    public void testCount(){
        assertThat(of(1,5,3,4,2).count(),is(5L));
    }


    @Test
    public void collect(){
        assertThat(of(1,2,3,4,5).collect(java.util.stream.Collectors.toList()).size(),is(5));
        assertThat(of(1,1,1,2).collect(java.util.stream.Collectors.toSet()).size(),is(2));
    }
    @Test
    public void testFilter(){
        assertThat(of(1,1,1,2).filter(it -> it==1).collect(java.util.stream.Collectors.toList()),hasItem(1));
    }
    @Test
    public void testFilterNot(){
        assertThat(of(1,1,1,2).filterNot(it -> it==1).collect(java.util.stream.Collectors.toList()),hasItem(2));
    }
    @Test
    public void testMap2(){
        assertThat(of(1).map(it->it+100).collect(java.util.stream.Collectors.toList()).get(0),is(101));
    }
    Object val;
    @Test
    public void testPeek2(){
        val = null;
        List l = of(1).map(it->it+100)
                        .peek(it -> val=it)
                        .collect(java.util.stream.Collectors.toList());
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
        assertThat(of(1,2,3,4,5).limit(2).collect(java.util.stream.Collectors.toList()).size(),is(2));
    }
	@Test
    public void testTake(){
        assertThat(of(1,2,3,4,5).take(2).collect(java.util.stream.Collectors.toList()).size(),is(2));
    }

    @Test
    public void testDrop() {
        assertThat(of(1, 2, 3, 4, 5).drop(2)
                                    .collect(java.util.stream.Collectors.toList())
                                    .size(),
                   is(3));
    }
    @Test
    public void testSkip(){
        assertThat(of(1,2,3,4,5).skip(2).collect(java.util.stream.Collectors.toList()).size(),is(3));
    }
    @Test
    public void testMax(){
        assertThat(of(1,2,3,4,5).maximum((t1, t2) -> t1-t2).orElse(-100),is(5));
    }
    @Test
    public void testMin(){
        assertThat(of(1,2,3,4,5).minimum((t1, t2) -> t1-t2).orElse(-3),is(1));
    }

	@Test
    public void testOnEmpty2() throws X {
        assertEquals(asList(1), of().onEmpty(1).toListX());
        assertEquals(asList(1), of().onEmptyGet(() -> 1).toListX());

        assertEquals(asList(2), of(2).onEmpty(1).toListX());
        assertEquals(asList(2), of(2).onEmptyGet(() -> 1).toListX());
        assertEquals(asList(2), of(2).onEmptyError(() -> new X()).toListX());


    }
    @Test
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a , b) -> a + b).toList().size(),
                equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 3, 4, 5, 6, 7, 8,
                        9, 10, 11, 12).size()));
    }

    @Test
    public void forEach2Filter() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), (a , b) -> a > 2 && b < 8,
                (a ,b) -> a + b).toList().size(), equalTo(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10).size()));
    }

    @Test
    public void take2Reversed(){
        range(0,10).reverse().limit(2).printOut();
        assertThat(range(0,10).materialize().reverse().limit(2).toListX(),equalTo(ListX.of(9,8)));
    }
    @Test
    public void intStreamCompareReversed(){


        assertThat(0,
                equalTo(range(-5,6).materialize().reverse().sumInt(i->i)));

    }
    @Test
    public void longStreamCompareReversed(){
        assertThat(0L,
                equalTo(rangeLong(-5,6).materialize().reverse().sumLong(i->i)));
    }
    @Test
    public void rangeLongReversedSkip(){
        System.out.println(rangeLong(0,5).materialize().reverse()
                .skip(3));
        assertThat(rangeLong(0,5).materialize().reverse()
                .skip(3).toListX(),equalTo(ListX.of(1l,0l)));
    }
    @Test
    public void rangeIntReversed(){
        assertThat(range(0,150).materialize().reverse()
                .limit(2).toListX(),equalTo(ListX.of(149, 148)));
    }
    @Test
    public void rangeIntReversedSkip2(){
        assertThat(range(0,5).materialize().reverse()
                .skip(3).toListX(),equalTo(ListX.of(1,0)));
    }
    @Test
    public void rangeIntReversedSkip(){

        assertThat(range(0,20).materialize().reverse()
                .limit(10).skip(8).toListX(),equalTo(ListX.of(11, 10)));
    }
    @Test
    public void batchBySizeSet(){
        System.out.println("List = " + of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList());
        assertThat(of(1,1,1,1,1,1).materialize().grouped(3,()->TreeSet.empty()).toList().get(0).size(),is(1));
        assertThat(of(1,1,1,1,1,1).materialize().grouped(3,()->TreeSet.empty()).toList().size(),is(1));
    }
}
