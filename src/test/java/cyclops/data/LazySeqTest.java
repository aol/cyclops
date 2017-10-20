package cyclops.data;


import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.companion.Reducers;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableListTest;
import cyclops.reactive.ReactiveSeq;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LazySeqTest extends BaseImmutableListTest {

    @Override
    protected <T> LazySeq<T> fromStream(Stream<T> s) {
        return LazySeq.fromStream(s);
    }

    @Override
    public <T> LazySeq<T> empty() {
        return LazySeq.empty();
    }

    @Override
    public <T> LazySeq<T> of(T... values) {
        return LazySeq.of(values);
    }
    @Test
    public void testScanRightSumMonoid() {
        assertThat(of("a", "ab", "abc").peek(System.out::println)
                .map(str -> str.length())
                .peek(System.out::println)
                .scanRight(Reducers.toTotalInt()).toList(), is(asList(6,5,3,0)));

    }
    @Test
    public void retainAllStream(){
        /**
        System.out.println(of(1,2,3,4,5));
        System.out.println(of(1,2,3,4,5).retainAllS(Stream.of(1,2,3)));

        System.out.println(ReactiveSeq.fromIterator(of(1,2,3,4,5).retainAllS(Stream.of(1,2,3)).iterator()).toListX());
         **/
      //  assertThat(Arrays.asList(1,2,3),hasItems(1,2,3));

        ImmutableList<Integer> l = of(1, 2, 3, 4, 5).retainAllS(Stream.of(1, 2, 3));
        for(Integer n : l)
            System.out.println("n is " +n);

        l.stream().forEach(System.out::println);

        System.out.println(l.stream().join(",","[","]"));
        l.stream().forEach(System.out::println);
        assertThat(l,hasItems(1,2,3));
    }


    @Test
    public void fromStreamTest(){
        ImmutableList<Integer> l = of(1,2,3,4,5).retainAllS(Stream.of(1, 2, 3));


        for(Integer n : l) {
           // System.out.println("n is " + n);
        }

         assertThat(fromStream(Stream.of(1,2,3)),equalTo(of(1,2,3)));
    }

    @Override
    public LazySeq<Integer> range(int start, int end) {
        return LazySeq.range(start,end);
    }

    @Override
    public LazySeq<Long> rangeLong(long start, long end) {
        return LazySeq.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableList<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return LazySeq.iterate(seed,fn,times);
    }

    @Override
    public <T> LazySeq<T> generate(int times, Supplier<T> fn) {
        return LazySeq.generate(fn,times);
    }

    @Override
    public <U, T> Seq<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return Seq.unfold(seed,unfolder);
    }
    @Test
    public void prependAllTest(){
        assertThat(LazySeq.of(1,2,3).prependAll(LazySeq.of(4,5,6)),equalTo(LazySeq.of(4,5,6,1,2,3)));
    }

    @Test
    public void scanRight(){
        LazySeq.of(1,2,3).scanRight(0,(a, b)->a+b).printOut();
    }



    @Test
    public void plusAll(){
        IterableX<Integer> vec = this.<Integer>empty().plusAll(Arrays.asList(1)).plusAll(Arrays.asList(2)).plusAll(Arrays.asList(5));

        MatcherAssert.assertThat(vec,equalTo(of(5,2,1)));
    }
    @Test
    public void plus(){
        IterableX<Integer> vec = this.<Integer>empty().plus(1).plus(2).plus(5);

        Assert.assertThat(vec,equalTo(Vector.of(5,2,1)));
    }

}
