package cyclops.streams.push.async;

import com.aol.cyclops2.streams.BaseSequentialTest;
import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class AsyncSequentialTest extends BaseSequentialTest {
    @Override
    protected <U> ReactiveSeq<U> of(U... array){

        return Spouts.async(s->{
            Thread t = new Thread(()-> {
                for (U next : array) {
                    s.onNext(next);
                }
                s.onComplete();
            });
            t.start();
        });
    }
    @Test
    public void duplicateReplay(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();
        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t.v1.limit(1).toList(),equalTo(ListX.of(1)));
    }
}
