package cyclops.data;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.types.reactive.ReactiveSubscriber;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.companion.Reducers;
import cyclops.control.Either;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableListTest;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import javaslang.collection.Iterator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class SeqTest extends BaseImmutableListTest {
    @Override
    protected <T> Seq<T> fromStream(Stream<T> s) {
        return Seq.fromStream(s);
    }

    @Override
    public <T> Seq<T> empty() {
        return Seq.empty();
    }

    @Override
    public <T> Seq<T> of(T... values) {
        return Seq.of(values);
    }

    @Override
    public Seq<Integer> range(int start, int end) {
        return Seq.range(start,end);
    }

    @Override
    public Seq<Long> rangeLong(long start, long end) {
        return Seq.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableList<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return Seq.iterate(seed,fn,times);
    }

    @Override
    public <T> Seq<T> generate(int times, Supplier<T> fn) {
        return Seq.generate(fn,times);
    }

    @Override
    public <U, T> Seq<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return Seq.unfold(seed,unfolder);
    }
    @Test
    public void plusAll(){
        IterableX<Integer> vec = this.<Integer>empty().plusAll(Arrays.asList(1)).plusAll(Arrays.asList(2)).plusAll(Arrays.asList(5));

        assertThat(vec,equalTo(of(5,2,1)));
    }
    @Test
    public void plus(){
        IterableX<Integer> vec = this.<Integer>empty().plus(1).plus(2).plus(5);

        assertThat(vec,equalTo(Vector.of(5,2,1)));

    }


    @Test
    public void seqTest(){
        Seq.of(1,2,3).prepend(3);
        for(Integer next : Seq.of(1,2,3)){
            System.out.println(next);
        }

    }


    @Test
    public void setEither(){
        Seq<Integer> ints = Seq.of(1,2,3);
        assertTrue(ints.set(-1,10).isLeft());
        assertTrue(ints.set(4,10).isLeft());
        Assert.assertThat(ints.set(2,10),equalTo(Either.right(Seq.of(1,2,10))));
    }
    @Test
    public void deleteEither(){
        Seq<Integer> ints = Seq.of(1,2,3);
        assertTrue(ints.delete(-1).isLeft());
        assertTrue(ints.delete(4).isLeft());
        Assert.assertThat(ints.delete(2),equalTo(Either.right(Vector.of(1,2))));
    }

}
