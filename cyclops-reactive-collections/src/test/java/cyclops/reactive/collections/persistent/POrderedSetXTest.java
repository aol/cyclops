package cyclops.reactive.collections.persistent;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.types.foldable.Evaluation;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.collections.AbstractSetTest;
import cyclops.reactive.collections.immutable.OrderedSetX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.SetX;
import cyclops.control.Option;
import cyclops.companion.Comparators;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import com.oath.cyclops.data.collections.extensions.FluentCollectionX;

public class POrderedSetXTest extends AbstractSetTest {

	AtomicLong counter = new AtomicLong(0);
	@Before
	public void setup(){

		counter = new AtomicLong(0);
		super.setup();
	}
    @Test
    public void span(){

        assertThat(of(1,2,3,4,1,2,3,4).span(i->i<3),equalTo(Tuple.tuple(of(1,2),of(3,4,1,2,3,4))));
        assertThat(of(1,2,3).span(i->i<9),equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).span(i->i<0),equalTo(Tuple.tuple(of(),of(1,2,3))));
    }

    @Test
    public void splitBy(){

        assertThat(of(1,2,3,4,1,2,3,4).splitBy(i->i>3),equalTo(Tuple.tuple(of(1,2,3),of(4,1,2,3,4))));
        assertThat(of(1,2,3).splitBy(i->i<9),equalTo(Tuple.tuple(of(),of(1,2,3))));
        assertThat(of(1,2,3).splitBy(i->i<0),equalTo(Tuple.tuple(of(1,2,3),of())));
    }

    @Test
    public void splitAtTest(){
        assertThat(of(1,2,3).splitAt(4) ,equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).splitAt(3) ,equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).splitAt(2) ,equalTo(Tuple.tuple(of(1,2),of(3))));
        assertThat(of(1,2,3).splitAt(1) ,equalTo(Tuple.tuple(of(1),of(2,3))));
        assertThat(of(1,2,3).splitAt(0) ,equalTo(Tuple.tuple(of(),of(1,2,3))));
        assertThat(of(1,2,3).splitAt(-1) ,equalTo(Tuple.tuple(of(),of(1,2,3))));
    }

    @Test
    public void testPartition() {


        assertEquals(asList(1, 3, 5), of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)._1().toList());
        assertEquals(asList(2, 4, 6), of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)._2().toList());

        assertEquals(asList(2, 4, 6), of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 == 0)._1().toList());
        assertEquals(asList(1, 3, 5), of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 == 0)._2().toList());

        assertEquals(asList(1, 2, 3), of(1, 2, 3, 4, 5, 6).partition(i -> i <= 3)._1().toList());
        assertEquals(asList(4, 5, 6), of(1, 2, 3, 4, 5, 6).partition(i -> i <= 3)._2().toList());

        assertEquals(asList(1, 2, 3, 4, 5, 6), of(1, 2, 3, 4, 5, 6).partition(i -> true)._1().toList());
        assertEquals(asList(), of(1, 2, 3, 4, 5, 6).partition(i -> true)._2().toList());

        assertEquals(asList(), of(1, 2, 3, 4, 5, 6).partition(i -> false)._1().toList());
        assertEquals(asList(1, 2, 3, 4, 5, 6), of(1, 2, 3, 4, 5, 6).splitBy(i -> false)._1().toList());
    }
    @Test
    @Override
    public void minusOneLarge(){
        MatcherAssert.assertThat(range(0,10_000).removeValue(1).size(), CoreMatchers.equalTo(9999));
        MatcherAssert.assertThat(range(0,10_000).append(1).removeValue(1).size(), CoreMatchers.equalTo(9999));
    }
    @Test
    public void permuations3() {
        System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        CollectionX<List<Integer>> x = of(1, 2, 3).permutations().map(s -> s.toList()).toSetX();

        assertTrue(x.containsValue(ListX.of(1,2,3)));
        assertTrue(x.containsValue(ListX.of(3,2,1)));
        assertTrue(x.containsValue(ListX.of(2,1,3)));
        assertTrue(x.containsValue(ListX.of(2,3,1)));
        assertTrue(x.containsValue(ListX.of(3,1,2)));
        assertTrue(x.containsValue(ListX.of(1,3,2)));
    }
    @Test
    public void batchWhileCollection(){
        assertThat(of(1,2,3,4,5,6)
                .groupedWhile(i->i%3!=0,()-> Vector.empty())
                .toList().size(),equalTo(2));
        CollectionX<Vector<Integer>> x = of(1, 2, 3, 4, 5, 6)
                .groupedWhile(i -> i % 3 != 0, () -> Vector.empty());
        SetX<Vector<Integer>> s = x.toSetX();

        assertTrue(s.containsValue(Vector.of(1,2,3)));
        assertTrue(s.containsValue(Vector.of(4,5,6)));

    }
	@Override
	protected <T> CollectionX<T> fromStream(Stream<T> s) {
		return OrderedSetX.orderedSetX(ReactiveSeq.fromStream(s));
	}
	@Test
	public void asyncTest() throws InterruptedException {
		Spouts.async(Stream.generate(()->"next"), Executors.newFixedThreadPool(1))
				.onePer(1, TimeUnit.MILLISECONDS)
				.take(1000)
				.to(ReactiveConvertableSequence::converter)
				.orderedSetX(Evaluation.LAZY)
				.peek(i->counter.incrementAndGet())
				.materialize();

		long current = counter.get();
		Thread.sleep(400);
		assertTrue(current<counter.get());
	}
	@Override
	public <T> OrderedSetX<T> of(T... values) {
		return OrderedSetX.of(Comparators.naturalOrderIdentityComparator(),values);
	}
	@Test
    public void onEmptySwitch(){
            assertThat(OrderedSetX.<Integer>empty().onEmptySwitch(()-> OrderedSetX.of(1,2,3)),equalTo(OrderedSetX.of(1,2,3)));
    }
	@Test
    public void coflatMap(){
       assertThat(OrderedSetX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleOrElse(null),equalTo(6));

    }
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return OrderedSetX.empty(Comparators.naturalOrderIdentityComparator());
	}

    @Test
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a, b) -> a + b)
                              .size(),
                   equalTo(12));
    }

	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return OrderedSetX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return OrderedSetX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return OrderedSetX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return OrderedSetX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
	       return OrderedSetX.unfold(seed, unfolder);
	    }
}
