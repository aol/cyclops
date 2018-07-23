package cyclops.data;



import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.Option;
import cyclops.data.basetests.BaseImmutableListTest;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class NonEmptyListTest extends BaseImmutableListTest {
    @Override
    protected <T> Seq<T> fromStream(Stream<T> s) {
        return Seq.fromStream(s);
    }

    @Override
    public <T> Seq<T> empty() {
        return Seq.empty();
    }

    @Override
    public <T> ImmutableList<T> of(T... values) {


        if(values.length==0)
            return empty();
        if(values.length==1)
            return NonEmptyList.of(values[0]);

        return NonEmptyList.of(values[0], Arrays.copyOfRange(values,1,values.length));
    }
    @Test
    public void takeMinusOne(){
        System.out.println(of(1,2,3).take(-1));
    }
    @Test
    public void splitAtTest(){
        assertThat(of(1,2,3).splitAt(4) ,equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).splitAt(3) ,equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).splitAt(2) ,equalTo(Tuple.tuple(of(1,2),of(3))));
        assertThat(of(1,2,3).splitAt(1) ,equalTo(Tuple.tuple(of(1),of(2,3))));
        assertThat(of(1,2,3).splitAt(0) ,equalTo(Tuple.tuple(of(),of(1,2,3))));
        assertThat(of(1,2,3).splitAt(-1) ,equalTo(Tuple.tuple(Seq.empty(),of(1,2,3))));
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
    public void stream(){

        System.out.println("D"+of(1, 2, 3, 4, 5).drop(2).toList());
       assertThat(NonEmptyList.of(1,2,3).foldLeft(0,(a,b)->a+b),equalTo(6));
        NonEmptyList.of(1,2,3,4).takeRight(2).forEach(System.out::println);
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
