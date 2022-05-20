package cyclops.futurestream.react;


import cyclops.futurestream.FutureStream;
import cyclops.futurestream.LazyReact;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class FutureSequentialTest extends BaseSequentialTest {
    @Override
    protected <U> FutureStream<U> of(U... array){

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
        final Tuple2<FutureStream<Integer>, FutureStream<Integer>> t = of(1).duplicate();
        assertThat(t._1().limit(1).toList(),equalTo(List.of(1)));
        assertThat(t._2().limit(1).toList(),equalTo(List.of(1)));
    }
    @Test
    public void takeReplay() {
        final FutureStream<Integer> t = of(1).map(i -> i).flatMap(i -> Stream.of(i));
        assertThat(t.limit(1).toList(), equalTo(List.of(1)));

    }
    @Test
    public void splitLimit() {
        FutureStream<Integer> stream = of(1);
        final Tuple2<FutureStream<Integer>, FutureStream<Integer>> t = stream.duplicate();
        assertThat(stream.limit(1).toList(), equalTo(List.of(1)));
        assertThat(t._1().limit(1).toList(), equalTo(List.of(1)));

    }

}
