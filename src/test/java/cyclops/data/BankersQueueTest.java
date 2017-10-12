package cyclops.data;

import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.collectionx.mutable.ListX;
import cyclops.data.basetests.BaseImmutableQueueTest;
import cyclops.data.tuple.Tuple;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 01/09/2017.
 */
public class BankersQueueTest extends BaseImmutableQueueTest {

    @Test
    public void enqueue(){
        BankersQueue<Integer> q = BankersQueue.cons(1);

        assertThat(q.dequeue(-1),equalTo(Tuple.tuple(1,q.tail())));

    }
    @Test
    public void enqueue2(){
        BankersQueue<Integer> q = BankersQueue.cons(1)
                                              .enqueue(10);


        System.out.println(q.equals(q));
        assertThat(q.dequeue(-1),equalTo(Tuple.tuple(10,q.tail())));
        BankersQueue<Integer> q2  = q.dequeue(-1)._2();

        assertThat(q2.dequeue(-1),equalTo(Tuple.tuple(1,q2.tail())));

    }

    @Test
    public void fromStream(){
        System.out.println(BankersQueue.of(1,2,3));
        System.out.println(fromStream(Stream.of(1,2,3)));
        fromStream(Stream.of(1,2,3)).iterator();
        assertThat(fromStream(Stream.of(1,2,3)),equalTo(BankersQueue.of(1,2,3)));
    }
    @Test
    public void fromStreamToList(){
        assertThat(fromStream(Stream.of(1,2,3)).toList(),equalTo(ListX.of(1,2,3)));
    }

    @Test
    public void get(){
        BankersQueue<Integer> q = BankersQueue.of(1,2,3);

        assertThat(q.get(0),equalTo(Option.some(1)));
        assertThat(q.get(1),equalTo(Option.some(2)));
        assertThat(q.get(2),equalTo(Option.some(3)));
        assertThat(q.get(3).isPresent(),equalTo(false));
        assertThat(q.get(-1).isPresent(),equalTo(false));
    }

    @Override
    protected <T> ImmutableQueue<T> fromStream(Stream<T> s) {
        return BankersQueue.fromStream(s);
    }

    @Override
    public <T> IterableX<T> empty() {
        return BankersQueue.empty();
    }

    @Override
    public <T> ImmutableQueue<T> of(T... values) {
        return BankersQueue.of(values);
    }

    @Override
    public IterableX<Integer> range(int start, int end) {
        return BankersQueue.fromStream(ReactiveSeq.range(start,end));
    }

    @Override
    public IterableX<Long> rangeLong(long start, long end) {
        return BankersQueue.fromStream(ReactiveSeq.rangeLong(start,end));
    }

    @Override
    public <T> IterableX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return BankersQueue.fromStream(ReactiveSeq.iterate(seed,fn).limit(times));
    }

    @Override
    public <T> IterableX<T> generate(int times, Supplier<T> fn) {
        return BankersQueue.fromStream(ReactiveSeq.generate(fn).limit(times));
    }

    @Override
    public <U, T> IterableX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return BankersQueue.fromStream(ReactiveSeq.unfold(seed,unfolder));
    }
}
