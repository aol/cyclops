package cyclops.streams.push;

import com.aol.cyclops2.streams.BaseSequentialTest;
import cyclops.collections.mutable.ListX;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class PushSequentialTest extends BaseSequentialTest {
    @Override
    protected <U> ReactiveSeq<U> of(U... array){

        return Spouts.of(array);
    }
    @Test
    public void duplicateReplay(){
        final Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t = of(1).duplicate();
        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
        assertThat(t._1().limit(1).toList(),equalTo(ListX.of(1)));
    }
    @Test
    public void iterate(){
        Spouts.iterate(1,i->i+1)
                .limit(10).forEach(System.out::println);
    }

}

