package cyclops.streams.future;

import com.aol.cyclops2.streams.BaseSequentialTest;
import cyclops.async.LazyReact;
import cyclops.collections.ListX;
import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class FutureSequentialTest extends BaseSequentialTest {
    @Override
    protected <U> ReactiveSeq<U> of(U... array){

        return LazyReact.sequentialBuilder().of(array);
    }
    @Test
    public void duplicateReplay(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();
        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t.v2.limit(1).toList(),equalTo(ListX.of(1)));
    }
    @Test
    public void limitReplay() {
        final ReactiveSeq<Integer> t = of(1).map(i -> i).flatMap(i -> Stream.of(i));
        assertThat(t.limit(1).toList(), equalTo(ListX.of(1)));

    }
    @Test
    public void splitLimit() {
        ReactiveSeq<Integer> stream = of(1);
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(), equalTo(ListX.of(1)));
        assertThat(t.v1.limit(1).toList(), equalTo(ListX.of(1)));

    }

}
