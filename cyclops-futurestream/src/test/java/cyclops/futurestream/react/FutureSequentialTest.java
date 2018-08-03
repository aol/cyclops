package cyclops.futurestream.react;

import com.oath.cyclops.streams.BaseSequentialTest;
import cyclops.futurestream.LazyReact;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
import org.hamcrest.Matchers;
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
    public void fanOutTest() {
        System.out.println(of(1, 2, 3, 4)
            .fanOut(s1 -> s1.map(i -> i * 2),
                s2 -> s2.map(i -> i * 100))
            .toList());
    }
    @Test
    public void duplicateReplay(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();
        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t._2().limit(1).toList(),equalTo(ListX.of(1)));
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
        assertThat(t._1().limit(1).toList(), equalTo(ListX.of(1)));

    }

}
