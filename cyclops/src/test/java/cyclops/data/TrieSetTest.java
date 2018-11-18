package cyclops.data;

import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSetTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
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
    @Test
    @Override
    public void insertAtIterable(){
        List<String> result = 	of(1,2,3).insertAt(1,of(100,200,300))
            .map(it ->it+"!!").collect(Collectors.toList());

        assertThat(result, hasItems("1!!","100!!","200!!","300!!","2!!","3!!"));
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
        assertThat(of(1, 2, 3,1,2,3).toSet(),equalTo(new java.util.HashSet<>(Arrays.asList(1,2,3))));
    }
    @Test
    public void testOnEmpty2() throws X {
        System.out.println(of().onEmpty(1));
        assertEquals(asList(1), of().onEmpty(1).toList());

    }

    @Test
    public void allCombinations3NoOrd() {


        Seq<Seq<Integer>> x = of(1, 2, 3).combinations().map(s -> s.seq()).seq();
        System.out.println(x);
        assertTrue(x.containsValue(Seq.empty()));
        assertTrue(x.containsValue(Seq.of(1)));
        assertTrue(x.containsValue(Seq.of(2)));
        assertTrue(x.containsValue(Seq.of(3)));
        assertTrue(x.containsValue(Seq.of(1,2)));
        assertTrue(x.containsValue(Seq.of(1,3)));
        assertTrue(x.containsValue(Seq.of(2,3)));
        assertTrue(x.containsValue(Seq.of(1,2,3)));


    }
  @Test
  public void lastIndexOf(){

    assertThat(empty().lastIndexOf(e->true),equalTo(Maybe.nothing()));
    assertThat(of(1).lastIndexOf(e->true),equalTo(Maybe.just(0l)));
    assertThat(of(1).lastIndexOf(e->false),equalTo(Maybe.nothing()));
    assertThat(of(1,2,3).lastIndexOf(e-> Objects.equals(2,e)),equalTo(Maybe.just(1l)));
    assertThat(of(1,2,3,2).lastIndexOf(e->Objects.equals(2,e)),
      equalTo(of(1,2,3,2).indexOf(e->Objects.equals(2,e))));
  }
    @Test
    public void lastIndexOfSlize(){
        assertThat(empty().lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        assertThat(of(1,2,3).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(0l)));
        assertThat(of(1).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        assertThat(of(0,1,2,3,4,5,6,1,2,3).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(1l)));
    }


}
