package cyclops.data;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.Option;
import cyclops.data.basetests.BaseImmutableListTest;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 02/09/2017.
 */
public class DifferenceListTest extends BaseImmutableListTest {

    int count;
    @Test
    public void memoize(){
        count =0;
        DifferenceList<Integer> list  = DifferenceList.of(LazySeq.range(1,10)).map(i->count++);
        System.out.println(list.size());
        assertThat(count,equalTo(9));
        System.out.println(list.size());
        assertThat(count,equalTo(18));
        list = list.memoize();
        System.out.println(list.size());
        assertThat(count,equalTo(27));
        System.out.println(list.size());
        assertThat(count,equalTo(27));

    }
    @Test
    public void append(){
        assertThat(DifferenceList.of(1,2,3).append(DifferenceList.of(4,5,6)).run(),equalTo(LazySeq.of(1,2,3,4,5,6)));
    }
    @Test
    public void map(){
        assertThat(DifferenceList.of(1,2,3).append(DifferenceList.of(4,5,6)).map(i->i*2).run(),equalTo(LazySeq.of(2,4,6,8,10,12)));
    }

    @Test
    public void flatMap(){
        assertThat(DifferenceList.of(1,2,3).append(DifferenceList.of(4,5,6)).flatMap(i-> DifferenceList.of(i*2)).run(),equalTo(LazySeq.of(2,4,6,8,10,12)));
    }

    @Override
    protected <T> ImmutableList<T> fromStream(Stream<T> s) {
        return DifferenceList.fromStream(s);
    }

    @Override
    public <T> ImmutableList<T> of(T... values) {
        return DifferenceList.of(values);
    }

    @Override
    public IterableX<Integer> range(int start, int end) {
        return DifferenceList.range(start,end);
    }

    @Override
    public IterableX<Long> rangeLong(long start, long end) {
        return DifferenceList.rangeLong(start, end);
    }

    @Override
    public <T> IterableX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return DifferenceList.iterate(seed,fn,times);
    }

    @Override
    public <T> IterableX<T> generate(int times, Supplier<T> fn) {
        return DifferenceList.generate(fn,times);
    }

    @Override
    public <U, T> IterableX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return DifferenceList.unfold(seed,unfolder);
    }

    @Override
    public <T> ImmutableList<T> empty() {
        return DifferenceList.empty();
    }
}
