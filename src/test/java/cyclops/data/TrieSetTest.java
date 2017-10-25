package cyclops.data;

import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSetTest;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrieSetTest extends BaseImmutableSetTest{
    @Override
    protected <T> ImmutableSet<T> fromStream(Stream<T> s) {
        return TrieSet.fromStream(s);
    }

    @Override
    public <T> ImmutableSet<T> empty() {
        return TrieSet.empty();
    }

    @Override
    public <T> ImmutableSet<T> of(T... values) {
        return TrieSet.of(values);
    }

    @Override
    public ImmutableSet<Integer> range(int start, int end) {
        return TrieSet.range(start,end);
    }

    @Override
    public ImmutableSet<Long> rangeLong(long start, long end) {
        return TrieSet.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableSet<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return TrieSet.iterate(seed,fn,times);
    }

    @Override
    public <T> ImmutableSet<T> generate(int times, Supplier<T> fn) {
        return TrieSet.generate(fn,times);
    }

    @Override
    public <U, T> ImmutableSet<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return TrieSet.unfold(seed,unfolder);
    }
    @Test
    public void multiple(){
        assertThat(of(1, 2, 3,1,2,3).toSetX(),equalTo(SetX.of(1,2,3)));
    }
    @Test
    public void testOnEmpty2() throws X {
        System.out.println(of().onEmpty(1));
        assertEquals(asList(1), of().onEmpty(1).toListX());

    }

    @Test
    public void allCombinations3NoOrd() {
        of(1, 2, 3).combinations().map(s -> s.toList()).toListX().printOut();

        ListX<ListX<Integer>> x = of(1, 2, 3).combinations().map(s -> s.toListX()).toListX();
        System.out.println(x);
        assertTrue(x.containsValue(ListX.empty()));
        assertTrue(x.containsValue(ListX.of(1)));
        assertTrue(x.containsValue(ListX.of(2)));
        assertTrue(x.containsValue(ListX.of(3)));
        assertTrue(x.containsValue(ListX.of(1,2)));
        assertTrue(x.containsValue(ListX.of(1,3)));
        assertTrue(x.containsValue(ListX.of(2,3)));
        assertTrue(x.containsValue(ListX.of(1,2,3)));


    }
}
