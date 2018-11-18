package cyclops.data;

import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSetTest;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class HashSetTest extends BaseImmutableSetTest{
    @Override
    protected <T> ImmutableSet<T> fromStream(Stream<T> s) {
        return HashSet.fromStream(s);
    }

    @Override
    public <T> ImmutableSet<T> empty() {
        return HashSet.empty();
    }

    @Override
    public <T> ImmutableSet<T> of(T... values) {
        return HashSet.of(values);
    }

    @Override
    public ImmutableSet<Integer> range(int start, int end) {
        return HashSet.range(start,end);
    }

    @Override
    public ImmutableSet<Long> rangeLong(long start, long end) {
        return HashSet.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableSet<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return HashSet.iterate(seed,fn,times);
    }

    @Override
    public <T> ImmutableSet<T> generate(int times, Supplier<T> fn) {
        return HashSet.generate(fn,times);
    }

    @Override
    public <U, T> ImmutableSet<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return HashSet.unfold(seed,unfolder);
    }

    @Test
    @Override
    public void insertAtIterable(){
        List<String> result = 	of(1,2,3).insertAt(1,of(100,200,300))
            .map(it ->it+"!!").collect(Collectors.toList());

        assertThat(result, hasItems("1!!","100!!","200!!","300!!","2!!","3!!"));
    }
    @Test
   public void removeValue(){
      assertThat(of(1,2,3).removeValue(0),equalTo(of(1,2,3)));
    }

  @Test
  public void removeAllTest(){
    assertThat(HashSet.of(1,2,3).removeAll(of(1,5,6,7,2)),equalTo(HashSet.of(3)));
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
