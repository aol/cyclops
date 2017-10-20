package cyclops.collectionx.standard;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.types.foldable.Evaluation;
import com.aol.cyclops2.util.SimpleTimer;
import cyclops.collectionx.AbstractSetTest;
import cyclops.collectionx.immutable.VectorX;
import cyclops.collectionx.mutable.ListX;
import cyclops.collectionx.mutable.SetX;
import cyclops.control.Option;
import cyclops.function.FluentFunctions;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import cyclops.collectionx.mutable.SortedSetX;

public class SortedSetXTest extends AbstractSetTest {

    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        return SortedSetX.of(values);
    }

    public boolean include(int i){
        return true;
    }
    public String transform(int i){
        return "";
    }

    AtomicLong counter = new AtomicLong(0);
    @Before
    public void setup(){

        counter = new AtomicLong(0);
    }
    @Test
    public void combinations2NoOrder2() {

        //ListX.of(1, 2, 3).combinations(2).transform(t->t.toListX()).printOut();
        CollectionX<ListX<Integer>> st = of(1, 2, 3).combinations(2).map(s -> s.toListX());
        st.toListX().printOut();
       // assertThat(of(1, 2, 3).combinations(2).transform(s->s.toListX()).toListX().getValue(0).size(),
        //        equalTo(2));
    }
    @Test
    public void asyncTest() throws InterruptedException {
        Spouts.async(Stream.generate(()->"next"), Executors.newFixedThreadPool(1))
                .onePer(1, TimeUnit.MILLISECONDS)
                .take(1000)
                .to()
                .sortedSetX(Evaluation.LAZY)
                .peek(i->counter.incrementAndGet())
                .materialize();

        long current = counter.get();
        Thread.sleep(400);
        assertTrue(current<counter.get());
    }
    @Test
    public void tracking(){

        ReactiveSeq.fromStream(Stream.of(1,2))
                    .filter(this::include)
                    .elapsed()
                    .map(this::logAndUnwrap)
                    .map(FluentFunctions.of(this::transform)
                                       .around(a->{

                                        SimpleTimer timer = new SimpleTimer();
                                        String r = a.proceed();
                                        System.out.println(timer.getElapsedNanoseconds());
                                        return r;
                    }));


    }

    private Integer logAndUnwrap(Tuple2<Integer, Long> t) {
        return t._1();
    }



    @Test
    public void onEmptySwitch() {

        assertThat(SortedSetX.empty()
                             .onEmptySwitch(() -> SortedSetX.of(1, 2, 3)),
                   equalTo(SortedSetX.of(1, 2, 3)));
    }

    public void coflatMap(){
       assertThat(SortedSetX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleUnsafe(),equalTo(6));
        
    }
   

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#
     * zero()
     */
    @Override
    public <T> FluentCollectionX<T> empty() {
        return SortedSetX.empty();
    }

    @Test
    @Override
    public void forEach2() {

        System.out.println(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a, b) -> a + b));
        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a, b) -> a + b)
                              .size(),
                   equalTo(12));
    }

    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return SortedSetX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return SortedSetX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return SortedSetX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return SortedSetX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return SortedSetX.unfold(seed, unfolder);
    }

    @Test
    public void allCombinations3NoOrd() {
        ListX<SetX<Integer>> x = SortedSetX.of(1, 2, 3).combinations().map(s -> s.to().setX()).to().listX();
        System.out.println(x);
        assertTrue(x.containsValue(SetX.empty()));
        assertTrue(x.containsValue(SetX.of(1)));
        assertTrue(x.containsValue(SetX.of(2)));
        assertTrue(x.containsValue(SetX.of(3)));
        assertTrue(x.containsValue(SetX.of(1,2)));
        assertTrue(x.containsValue(SetX.of(1,3)));
        assertTrue(x.containsValue(SetX.of(2,3)));
        assertTrue(x.containsValue(SetX.of(1,2,3)));

    }
    @Test
    public void combinations2NoOrd() {
        SetX<SetX<Integer>> x = of(1, 2, 3).combinations(2).map(s -> s.toSetX()).toSetX();
        assertTrue(x.containsValue(SetX.of(1,2)));
        assertTrue(x.containsValue(SetX.of(1,3)));
        assertTrue(x.containsValue(SetX.of(2,3)));
    }
    @Test
    public void testOfTypeNoOrd() {



        SortedSetX<Number> set = SortedSetX.<Number>of(1, 10l, 2, 20l, 3);
        SortedSetX<Integer> setA = set.ofType(Integer.class);
        assertThat(setA.toListX(),containsInAnyOrder(1, 2, 3));

    }
    @Test @Ignore
    public void slidingNoOrd() {
        SetX<VectorX<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).toSetX();

        list.containsValue(VectorX.of(1,2));
        VectorX<Integer> vec = list.get(0).orElse(null);

        System.out.println(vec);
        System.out.println("same" +vec.equals(VectorX.of(1,2)));
        System.out.println(list.containsValue(VectorX.of(1,2)));
        assertTrue(list.containsValue(VectorX.of(1,2)));
    }

}
