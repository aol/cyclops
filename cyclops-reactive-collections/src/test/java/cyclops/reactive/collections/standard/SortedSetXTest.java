package cyclops.reactive.collections.standard;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import com.oath.cyclops.util.SimpleTimer;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.collections.AbstractSetTest;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.SetX;
import cyclops.control.Option;
import cyclops.function.FluentFunctions;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import cyclops.reactive.collections.mutable.SortedSetX;

public class SortedSetXTest extends AbstractSetTest {

    @Override
    public <T> SortedSetX<T> of(T... values) {
        return SortedSetX.of(values);
    }

    public boolean include(int i){
        return true;
    }
    public String transform(int i){
        return "";
    }

    AtomicLong counter = new AtomicLong(0);
    @Override
    protected <T> CollectionX<T> fromStream(Stream<T> s) {
        return SortedSetX.sortedSetX(ReactiveSeq.fromStream(s));
    }
    @Before
    public void setup(){

        counter = new AtomicLong(0);
        super.setup();
    }
    @Test @Ignore
    public void printNull() {

    }
    @Test
    @Override
    public void minusOneLarge(){
        MatcherAssert.assertThat(range(0,10_000).removeValue(1).size(), CoreMatchers.equalTo(9999));
        MatcherAssert.assertThat(range(0,10_000).append(1).removeValue(1).size(), CoreMatchers.equalTo(9999));
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
    public void emptyAllCombinations() {
        assertThat(of().combinations().map(s->s.toList()),equalTo(of(ListX.of())));
    }
    @Test
    public void emptyAllCombinationsNoOrder() {
        assertThat(of().combinations().map(s -> s.toList()), equalTo(of(ListX.of())));
    }
    @Test
    public void combinations2NoOrder2() {

        //ListX.of(1, 2, 3).combinations(2).map(t->t.toListX()).printOut();
        CollectionX<ListX<Integer>> st = of(1, 2, 3).combinations(2).map(s -> s.to(ReactiveConvertableSequence::converter)
            .listX()
);
        st.toListX().printOut();
       // assertThat(of(1, 2, 3).combinations(2).map(s->s.toListX()).toListX().getValue(0).size(),
        //        equalTo(2));
    }
    @Test
    public void asyncTest() throws InterruptedException {
        Spouts.async(Stream.generate(()->"next"), Executors.newFixedThreadPool(1))
                .onePer(1, TimeUnit.MILLISECONDS)
                .take(1000)
                .to(ReactiveConvertableSequence::converter)
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
                   .singleOrElse(null),equalTo(6));

    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#
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
        ListX<SetX<Integer>> x = SortedSetX.of(1, 2, 3).combinations().map(s -> s.to(ReactiveConvertableSequence::converter).setX()).to().listX();
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
        SetX<SetX<Integer>> x = of(1, 2, 3).combinations(2).map(s -> s.to(ReactiveConvertableSequence::converter)
                                    .setX()).toSetX();
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
        SetX<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).toSetX();

        list.containsValue(Seq.of(1,2));
        Seq<Integer> vec = list.elementAt(0).orElse(null);

        System.out.println(vec);
        System.out.println("same" +vec.equals(Seq.of(1,2)));
        System.out.println(list.containsValue(Seq.of(1,2)));
        assertTrue(list.containsValue(Seq.of(1,2)));
    }
    @Test @Ignore
    public void batchWhileCollection(){
        assertThat(of(1,2,3,4,5,6)
                .groupedWhile(i->i%3!=0,()-> Vector.empty())
                .toList().size(),equalTo(2));
        CollectionX<Vector<Integer>> x = of(1, 2, 3, 4, 5, 6)
                .groupedWhile(i -> i % 3 != 0, () -> Vector.empty());

        assertTrue(x.toSetX().containsValue(Vector.of(1,2,3)));
        assertTrue(x.toSetX().containsValue(Vector.of(4,5,6)));

    }

}
