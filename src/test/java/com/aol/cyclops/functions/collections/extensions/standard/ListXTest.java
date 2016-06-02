package com.aol.cyclops.functions.collections.extensions.standard;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Validator;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;

public class ListXTest extends CollectionXTestsWithNulls {

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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#
     * empty()
     */
    @Override
    public <T> FluentCollectionX<T> empty() {
        return ListX.empty();
    }

    @Test
    @Ignore // manual test for waiting kick in
    public void flatMapPublisherWithAsync20k() throws InterruptedException {
        for (int x = 0; x < 10_000; x++) {
            assertThat(ReactiveSeq.generate(() -> 1)
                    .flatMapPublisher(i -> Maybe.of(i), 500, QueueFactories.unboundedQueue()).limit(300_000).toListX(),
                    equalTo(Arrays.asList(1, 2, 3)));
        }

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

    @Test
    public void validate() {
        ListX<Integer> numbers = ListX.of(1, 2, 3, 4, 5, 6, 7);
        Validator<Integer, Integer, Integer> validator = Validator.of(i -> i % 2 == 0, 1, 1);
        Ior<ReactiveSeq<Integer>, ReactiveSeq<Integer>> ior = numbers.validate(validator);
        int even = ior.get().sum().get();
        int odd = ior.secondaryGet().sum().get();
        assertThat(even, equalTo(3));
        assertThat(odd, equalTo(4));

    }

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
