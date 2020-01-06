package cyclops.data.chain;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.Option;
import cyclops.data.Chain;
import cyclops.data.ImmutableList;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.basetests.BaseImmutableListTest;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class WrapTest extends BaseImmutableListTest {
    @Override
    protected <T> ImmutableList<T> fromStream(Stream<T> s) {
        return Chain.wrap(ReactiveSeq.fromStream(s));
    }

    @Override
    public <T> Chain<T> of(T... values) {
        if(values.length==0)
            return Chain.empty();
        return Chain.wrap(Seq.of(values));
    }

    @Override
    public IterableX<Integer> range(int start, int end) {
        return Chain.wrap(LazySeq.range(start, end));
    }

    @Override
    public IterableX<Long> rangeLong(long start, long end) {
        return Chain.wrap(ReactiveSeq.rangeLong(start, end));
    }

    @Override
    public <T> IterableX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return Chain.wrap(ReactiveSeq.iterate(seed,fn).take(times));
    }

    @Override
    public <T> IterableX<T> generate(int times, Supplier<T> fn) {
       return Chain.wrap(ReactiveSeq.generate(fn).take(times));
    }

    @Override
    public <U, T> IterableX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return Chain.wrap(ReactiveSeq.unfold(seed,unfolder));
    }

    @Override
    public <T> ImmutableList<T> empty() {
        return Chain.empty();
    }

    @Test
    public void prependAllTests(){
        assertThat(of(1,2,3,4,5,6,7).prependAll(10,11,12),equalTo(of(10,11,12,1,2,3,4,5,6,7)));
        assertThat(of(1,2,3,4,5,6,7).prependAll(Seq.of(10,11,12)),equalTo(of(10,11,12,1,2,3,4,5,6,7)));
    }

    @Test
    public void removeValue2(){
        Chain<Integer> vec = of(5,2,1);

     //   System.out.println(vec);
     //   System.out.println("SeqToStre"+vec.stream().seq());
      //  System.out.println("Seq"+vec.stream(). removeFirst(e-> Objects.equals(e,2)).seq());
        System.out.println("FS"+fromStream(vec.stream(). removeFirst(e-> Objects.equals(e,2))).seq());
       // assertThat(vec.removeValue(2), CoreMatchers.equalTo(of(5,1)));
    }

     @Test
    public void sorted() {

        ReactiveSeq<Integer> s = of(1, 5, 3, 4, 2).stream().sorted();
        System.out.println("S" + s.toList());
        ImmutableList<Integer> a = of(1, 5, 3, 4, 2).sorted();
        System.out.println("A" + a);
         System.out.println("A.class" + a.getClass());
         Chain<Integer> b = of(1, 2, 3, 4, 5);
         System.out.println("B" + b);
         System.out.println("B.class" + b.getClass());
        assertThat(of(1,5,3,4,2).sorted(),equalTo(of(1,2,3,4,5)));
    }

    @Override
    public void testCycleWhile() {

    }

    @Override
    public void testCycleUntil() {

    }

    @Override
    public void testCycleWhileNoOrd() {

    }

    @Override
    public void testCycleUntilNoOrd() {
    }
}
