package cyclops.collections.standard;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.oath.cyclops.types.foldable.Evaluation;
import cyclops.collections.CollectionXTestsWithNulls;
import cyclops.control.Option;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.QueueX;

public class QueueXTest extends CollectionXTestsWithNulls {

    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        return QueueX.of(values);
    }

    AtomicLong counter = new AtomicLong(0);
    @Before
    public void setup(){

        counter = new AtomicLong(0);
        super.setup();
    }
    @Test
    public void asyncTest() throws InterruptedException {
        Spouts.async(Stream.generate(()->"next"), Executors.newFixedThreadPool(1))
                .onePer(1, TimeUnit.MILLISECONDS)
                .take(1000)
                .to()
                .queueX(Evaluation.LAZY)
                .peek(i->counter.incrementAndGet())
                .materialize();

        long current = counter.get();
        Thread.sleep(400);
        assertTrue(current<counter.get());
    }
    @Test
    public void onEmptySwitch(){
        assertThat(QueueX.empty().onEmptySwitch(()->QueueX.of(1,2,3)).toList(),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void coflatMap(){
       assertThat(QueueX.of(1,2,3)
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
        return QueueX.empty();
    }

    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return QueueX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return QueueX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return QueueX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return QueueX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return QueueX.unfold(seed, unfolder);
    }

    @Test
    public void compareDifferentSizes(){
        assertThat(empty().size(),not(equalTo(of(1).size())));
        assertThat(of(1).size(),not(equalTo(empty().size())));
        assertThat(of(1).size(),not(equalTo(of(1,2,3).size())));
    }
}
