package cyclops.collections.standard;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import cyclops.collections.CollectionXTestsWithNulls;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Semigroups;
import cyclops.collections.immutable.*;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ListXTest extends CollectionXTestsWithNulls {
    @Test
    public void withTest(){

        assertEquals(of("x", "b", "c"), ListX.of("a", "b", "c").with(0, "x"));
        assertEquals(of("a", "x", "c"), ListX.of("a", "b", "c").with(1, "x"));
        assertEquals(of("a", "b", "x"), ListX.of("a", "b", "c").with(2, "x"));
    }

    int times =0;
    AtomicLong counter = new AtomicLong(0);
    @Before
    public void setup(){
        times = 0;
        counter = new AtomicLong(0);
    }
    @Test
    public void asyncTest() throws InterruptedException {
        Spouts.async(Stream.generate(()->"next"), Executors.newFixedThreadPool(1))
                .onePer(1, TimeUnit.MILLISECONDS)
                .take(1000)
                .to()
                .listX(Evaluation.LAZY)
                .peek(i->counter.incrementAndGet())
                .materialize();

        long current = counter.get();
        Thread.sleep(400);
        assertTrue(current<counter.get());
    }
    @Test
    public void cycleTests(){
        ListX.of(1,2,3)
                .cycle(2);

        ListX.of(5,6,7)
                .cycleWhile(i->i*times++<100)
        .printOut();
    }

    @Test
    public void lazy(){


        ListX<VectorX<String>> list =     ListX.of(1,2,3,5,6,7,8)
                                                .map(i->i*2)
                                                .filter(i->i<4)
                                                .sliding(2)
                                                .map(vec -> vec.map(i->"value is " + i));


          ListX.of(1,2,3,5,6,7,8)
                .map(i->i*2)
                .filter(i->i<4)
                .slidingT(2)
                .map(i->"value is " + i)
                .unwrap()
                  .to(Witness::reactiveSeq);

    }

    @Test
    public void coflatMapTest(){
        ListX<ListX<Integer>> list = ListX.of(1, 2, 3)
                .coflatMap(s -> s);

        ListX<Integer> stream2 = list.flatMap(s -> s).map(i -> i * 10);
        ListX<Integer> stream3 = list.flatMap(s -> s).map(i -> i * 100);

        assertThat(stream2,equalTo(ListX.of(10,20,30)));
        assertThat(stream3,equalTo(ListX.of(100,200,300)));

    }

    @Test
    public void cycle(){
        List<Integer> list = new ArrayList<>();
        ListX.of(1).cycle(2).forEach(e->list.add(e));
        assertThat(list,equalTo(ListX.of(1,1)));
        Iterator<Integer> it = ListX.of(1,1).cycle(2).iterator();
        List<Integer> list2 = new ArrayList<>();
        while(it.hasNext())
            list2.add(it.next());
        assertThat(list2,equalTo(ListX.of(1,1,1,1)));


    }
    @Test
    public void cycleStream(){
        List<Integer> list = new ArrayList<>();
        ListX.of(1).stream().cycle(2).forEach(e->list.add(e));
        assertThat(list,equalTo(ListX.of(1,1)));
        Iterator<Integer> it = ListX.of(1,1).stream().cycle(2).iterator();
        List<Integer> list2 = new ArrayList<>();
        while(it.hasNext())
            list2.add(it.next());
        assertThat(list2,equalTo(ListX.of(1,1,1,1)));


    }

    @Test
    public void onEmptySwitch(){
        assertThat(ListX.empty().onEmptySwitch(()->ListX.of(1,2,3)),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void coflatMap(){
       assertThat(ListX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleUnsafe(),equalTo(6));
        
    }
    @Test
    public void multipaths() {

        ListX<Integer> list = ListX.of(1, 2, 3);
        ListX<Integer> by10 = list.map(i -> i * 10);
        ListX<Integer> plus2 = list.map(i -> i + 2);
        ListX<Integer> by10Plus2 = by10.map(i -> i + 2);
        assertThat(by10, equalTo(Arrays.asList(10, 20, 30)));
        assertThat(plus2, equalTo(Arrays.asList(3, 4, 5)));
        assertThat(by10Plus2, equalTo(Arrays.asList(12, 22, 32)));
    }

    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        return ListX.of(values);
    }

    @Test
    public void toTest(){
        ListX<Integer> list = ListX.of(1,2,3)
                                   .to(l->l.stream())
                                   .map(i->i*2)
                                   .to(r->r.toSetX())
                                   .to(s->s.toListX());
        
        assertThat(list,equalTo(ListX.of(2,4,6)));
    }
    @Test
    public void zipSemigroup(){
        BinaryOperator<Zippable<Integer>> sumInts = Semigroups.combineZippables(Semigroups.intSum);
        assertThat(sumInts.apply(ListX.of(1,2,3), ListX.of(4,5,6)),equalTo(ListX.of(5,7,9)));
        
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
        return ListX.empty();
    }


    @Test
    public void when(){
        
        String res= of(1,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");
                    
        assertThat(res,equalTo("2world3"));
    }
    @Test
    public void whenGreaterThan2(){
        String res= of(5,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");
                
        assertThat(res,equalTo("2hello3"));
    }
    
    @Test
    public void when2(){
        
        Integer res =   of(1,2,3).visit((x,xs)->x,()->10);
        System.out.println(res);
    }
    @Test
    public void whenNilOrNot(){
        String res1=    of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
    }
    @Test
    public void whenNilOrNotJoinWithFirstElement(){
        
        
        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),()->"EMPTY");
        assertThat(res,equalTo("2world3"));
    }

    /**
     *
     * Eval e; //int cost = ReactiveSeq.of(1,2).when((head,tail)-> head.when(h->
     * (int)h>5, h-> 0 ) // .flatMap(h-> head.when());
     * 
     * ht.headMaybe().when(some-> Matchable.of(some).matches(
     * c->c.hasValues(1,2,3).then(i->"hello world"),
     * c->c.hasValues('b','b','c').then(i->"boo!") ),()->"hello");
     **/



    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return ListX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return ListX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return ListX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return ListX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ListX.unfold(seed, unfolder);
    }

}
