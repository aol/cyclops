package cyclops.data.chain;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.Option;
import cyclops.data.Chain;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import cyclops.data.basetests.BaseImmutableListTest;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SingletonTest extends BaseImmutableListTest {
    @Override
    protected <T> ImmutableList<T> fromStream(Stream<T> s) {
        Chain<T> res = null;
        for(T next : ReactiveSeq.fromStream(s)){
            if(res==null)
                res = Chain.singleton(next);
            else
                res = res.append(next);
        }
        if(res==null)
            return empty();
        return res;
    }

    @Override
    public <T> ImmutableList<T> of(T... values) {
        Chain<T> res = null;
        for(T next : values){
            if(res==null)
                res = Chain.singleton(next);
            else
                res = res.append(next);
        }
        if(res==null)
            return empty();
        return res;
    }

    @Override
    public IterableX<Integer> range(int start, int end) {
        Chain<Integer> res = null;
        for(Integer next : ReactiveSeq.range(start,end)){
            if(res==null)
                res = Chain.singleton(next);
            else
                res = res.append(next);
        }
        if(res==null)
            return empty();
        return res;
    }

    @Override
    public IterableX<Long> rangeLong(long start, long end) {
        Chain<Long> res =  null;
        for(Long next : ReactiveSeq.rangeLong(start,end)) {
            if (res == null)
                res = Chain.singleton(next);
            else
                res = res.append(next);
        }
        if(res==null)
            return empty();
        return res;
    }

    @Override
    public <T> IterableX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        Chain<T> res = null;
        for(T next : ReactiveSeq.<T>iterate(seed,fn).take(times)){
            if(res==null)
                res = Chain.singleton(next);
            else
                res = res.append(next);
        }
        if(res==null)
            return empty();
        return res;
    }

    @Override
    public <T> IterableX<T> generate(int times, Supplier<T> fn) {
        Chain<T> res = null;
        for(T next : ReactiveSeq.<T>generate(fn).take(times)){
            if(res==null)
                res = Chain.singleton(next);
            else
                res = res.append(next);
        }
        if(res==null)
            return empty();
        return res;
    }

    @Override
    public <U, T> IterableX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        Chain<T> res = null;
        for(T next : ReactiveSeq.unfold(seed,unfolder)){
            if(res==null)
                res = Chain.singleton(next);
            else
                res = res.append(next);
        }
        if(res==null)
            return empty();
        return res;
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
}
