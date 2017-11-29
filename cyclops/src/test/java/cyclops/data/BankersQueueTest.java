package cyclops.data;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.collections.mutable.ListX;
import cyclops.data.basetests.BaseImmutableQueueTest;
import cyclops.data.tuple.Tuple;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
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
        System.out.println(q.tail());
        System.out.println(q.dequeue(-1));
        assertThat(q.dequeue(-1),equalTo(Tuple.tuple(1,q.tail())));
        BankersQueue<Integer> q2  = q.dequeue(-1)._2();

        assertThat(q2.dequeue(-1),equalTo(Tuple.tuple(10,q2.tail())));

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
        BankersQueue<Integer> q =  BankersQueue.cons(1)
                .enqueue(2)
                .enqueue(3);
                //BankersQueue.of(1,2,3);

        System.out.println("0 " + q.get(0));
        System.out.println("1 " + q.get(1));
        System.out.println("2 " + q.get(2));
        System.out.println("0 " + q.dequeue(-1));
        System.out.println("1 " + q.dequeue(-1)._2().dequeue(-1));
        System.out.println("2 " + q.dequeue(-1)._2().dequeue(-1)._2().dequeue(-1));
        for (Integer integer : q) {
            System.out.println(integer);
        }

        assertThat(q.get(0),equalTo(Option.some(1)));
        assertThat(q.get(1),equalTo(Option.some(2)));
        assertThat(q.get(2),equalTo(Option.some(3)));
        assertThat(q.get(3).isPresent(),equalTo(false));
        assertThat(q.get(-1).isPresent(),equalTo(false));
    }
    @Test
    public void get2(){
        BankersQueue<Integer> q =  BankersQueue.cons(1)
                .enqueue(2)
                .enqueue(3)
                .enqueue(4)
                .enqueue(5)
                .enqueue(6);
        //BankersQueue.of(1,2,3);

        System.out.println("0 " + q.get(0));
        System.out.println("1 " + q.get(1));
        System.out.println("2 " + q.get(2));
        System.out.println("3 " + q.get(3));
        System.out.println("4 " + q.get(4));
        System.out.println("5 " + q.get(5));
        System.out.println(q);
        System.out.println("0 " + q.dequeue(-1));
        System.out.println("1 " + q.dequeue(-1)._2().dequeue(-1));
        System.out.println("2 " + q.dequeue(-1)._2().dequeue(-1)._2().dequeue(-1));
        for (Integer integer : q) {
            System.out.println(integer);
        }

        assertThat(q.get(0),equalTo(Option.some(1)));
        assertThat(q.get(1),equalTo(Option.some(2)));
        assertThat(q.get(2),equalTo(Option.some(3)));
        assertThat(q.get(3).isPresent(),equalTo(true));
        assertThat(q.get(-1).isPresent(),equalTo(false));
    }

    @Override
    protected <T> ImmutableQueue<T> fromStream(Stream<T> s) {
        return BankersQueue.fromStream(s);
    }

    @Override
    public <T> ImmutableQueue<T> empty() {
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
    @Test
    public void retainAll(){
        System.out.println(of(1,2,3,4,5).retainAll((Iterable<Integer>)of(1,2,3)));
        assertThat(of(1,2,3,4,5).retainAll((Iterable<Integer>)of(1,2,3)),hasItems(1,2,3));
    }
    @Test
    public void streamable(){
        System.out.println("S"+of(1,2,3,4,5,6));
        Streamable<Integer> repeat = (of(1,2,3,4,5,6)
                .map(i->i*2)
        ).to()
                .streamable();

        assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
        assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
    }
}
